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
import java.io.File;
import java.nio.file.Files;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import org.assertj.core.api.SoftAssertions;
import ru.ewc.checklogic.FileUtils;
import ru.ewc.checklogic.FullServerContext;
import ru.ewc.checklogic.ServerContextFactory;
import ru.ewc.checklogic.TestData;
import ru.ewc.checklogic.TestResult;

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

    private WebPages(final TemplateRender processors, final String root) {
        this.processors = processors;
        this.root = root;
    }

    public static WebPages create(final TemplateRender processors, final String root) {
        return new WebPages(processors, root);
    }

    public static WebPages testable() {
        return new WebPages(new MockTemplateRender(), "root folder");
    }

    public Response indexPage() {
        return Response.htmlOk(this.renderInLayout("templates/index.html", Map.of()));
    }

    public Response uninitializedPage() {
        return Response.htmlOk(this.renderInLayout("templates/uninitialized.html", Map.of()));
    }

    public Response noTestsFolder() {
        return Response.htmlOk(this.renderInLayout("templates/noTestsFolder.html", Map.of()));
    }

    public Response testPage() {
        final long start = System.currentTimeMillis();
        final List<TestResult> results = FileUtils.readFileNames(this.root)
            .map(this::performTest)
            .toList();
        final String rows = results.stream()
            .sorted(Comparator.comparing(TestResult::result))
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

    public Response statePage(final FullServerContext context) {
        final StoredState stored = new StoredState(context.storedState());
        return Response.htmlOk(
            this.renderInLayout(
                "templates/state.html",
                Map.of("state", stored.asHtmlList())
            )
        );
    }

    public String configPage() {
        return this.renderInLayout("templates/config.html", Map.of());
    }

    private String renderInLayout(final String template, final Map<String, String> values) {
        return this.processors.renderInLayout(template, values);
    }

    // @todo #47 Move performTest method to dedicated test runner
    @SneakyThrows
    private TestResult performTest(final TestData test) {
        final SoftAssertions softly = new SoftAssertions();
        TestResult result;
        final FullServerContext target;
        try {
            target = ServerContextFactory.create(this.root)
                .fromStateFile(Files.newInputStream(new File(test.file()).toPath()));
            try {
                if (!test.command().isEmpty()) {
                    target.perform(test.command());
                }
                for (final String locator : test.expectations().keySet()) {
                    softly
                        .assertThat(target.stateFor(locator, test.expectations().get(locator)))
                        .describedAs(String.format("State for entity '%s'", locator))
                        .containsExactlyInAnyOrderEntriesOf(test.expectations().get(locator));
                }
                softly.assertAll();
                result = new TestResult(test.toString(), true, "");
            } catch (final Throwable error) {
                result = new TestResult(test.toString(), false, error.getMessage());
            }
        } catch (final IllegalStateException exception) {
            result = new TestResult(test.toString(), false, exception.getMessage());
        }
        return result;
    }
}
