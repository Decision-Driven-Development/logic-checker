/*
 * MIT License
 *
 * Copyright (c) 2024 Decision-Driven Development
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package ru.ewc.checklogic.server;

import com.renomad.minum.web.Response;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import ru.ewc.checklogic.ServerConfiguration;
import ru.ewc.checklogic.testing.CheckSuite;
import ru.ewc.checklogic.testing.TestResult;
import ru.ewc.decisions.input.CombinedCsvFileReader;

/**
 * I am a collection of template processors that render the pages to be served.
 *
 * @since 0.3.2
 */
@SuppressWarnings("PMD.ProhibitPublicStaticMethods")
public final class WebPages {
    /**
     * The template processors to be used for rendering the pages.
     */
    private final TemplateRender processors;

    /**
     * The root path for the external business logic resources.
     */
    private final String root;

    /**
     * Server configuration that holds the request locator name.
     */
    private final ServerConfiguration config;

    public WebPages(
        final TemplateRender processors,
        final String root,
        final ServerConfiguration config
    ) {
        this.processors = processors;
        this.root = root;
        this.config = config;
    }

    public static WebPages testable() {
        return new WebPages(
            new MockTemplateRender(),
            "root folder",
            new ServerConfiguration("root folder")
        );
    }

    public Response uninitializedPage() {
        return Response.htmlOk(this.renderInLayout("templates/uninitialized.html", Map.of()));
    }

    public Response testPage() {
        final CheckSuite suite = CheckSuite.using(
            new CombinedCsvFileReader(Path.of(this.root, "tests").toUri(), ".csv", ";"),
            this.root,
            this.config.requestLocatorName()
        );
        final long start = System.currentTimeMillis();
        final List<TestResult> results = suite.perform();
        final String rows = results.stream()
            .sorted(Comparator.naturalOrder())
            .map(TestResult::asHtmlTableRow)
            .collect(Collectors.joining());
        final double elapsed = (System.currentTimeMillis() - start) / 1000.0;
        return Response.htmlOk(
            this.renderInLayout(
                "templates/test.html",
                Map.of(
                    "tests", "%s".formatted(rows),
                    "stats", "%d test(s) performed in %.3f second(s), %d passed, %d failed".formatted(
                        results.size(),
                        elapsed,
                        results.stream().filter(TestResult::successful).count(),
                        results.stream().filter(result -> !result.successful()).count()
                    )
                )
            )
        );
    }

    public String renderInLayout(final String template, final Map<String, String> values) {
        return this.processors.renderInLayout(template, values);
    }
}
