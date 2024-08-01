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
public final class WebPages {
    /**
     * The template processors to be used for rendering the pages.
     */
    private final TemplateProcessors processors;

    /**
     * The root path for the external business logic resources.
     */
    private final String root;

    public WebPages(final TemplateProcessors processors, final String root) {
        this.processors = processors;
        this.root = root;
    }

    public Response indexPage() {
        return Response.htmlOk(this.templateNamed("templates/index.html", Map.of()));
    }

    public Response uninitializedPage() {
        return Response.htmlOk(this.templateNamed("templates/uninitialized.html", Map.of()));
    }

    public Response noTestsFolder() {
        return Response.htmlOk(this.templateNamed("templates/noTestsFolder.html", Map.of()));
    }

    public Response testPage() {
        final String results = FileUtils.readFileNames(this.root)
            .map(this::performTest)
            .map(TestResult::asHtmlTableRow)
            .collect(Collectors.joining());
        return Response.htmlOk(
            this.templateNamed("templates/test.html", Map.of("tests", "%s".formatted(results)))
        );
    }

    public Response statePage(final FullServerContext context) {
        final StoredState stored = new StoredState(context.storedState());
        return Response.htmlOk(
            this.templateNamed(
                "templates/state.html",
                Map.of(
                    "state", stored.asHtmlList(),
                    "available", context.cached("available"),
                    "request", context.cached("request")
                )
            )
        );
    }

    private String templateNamed(final String template, final Map<String, String> values) {
        return this.processors.forTemplate(template).renderTemplate(values);
    }

    // @todo #47 Move performTest method to dedicated test runner
    @SneakyThrows
    private TestResult performTest(final TestData test) {
        final SoftAssertions softly = new SoftAssertions();
        TestResult result;
        final FullServerContext target;
        try {
            target = new ServerContextFactory(this.root)
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
        } catch (IllegalStateException exception) {
            result = new TestResult(test.toString(), false, exception.getMessage());
        }

        return result;
    }

}
