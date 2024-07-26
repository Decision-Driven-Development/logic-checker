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

import com.renomad.minum.templating.TemplateProcessor;
import com.renomad.minum.web.Request;
import com.renomad.minum.web.Response;
import com.renomad.minum.web.WebFramework;
import java.io.File;
import java.nio.file.Files;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import org.assertj.core.api.SoftAssertions;
import ru.ewc.checklogic.FileUtils;
import ru.ewc.checklogic.ServerContext;
import ru.ewc.checklogic.ServerContextFactory;
import ru.ewc.checklogic.TestData;
import ru.ewc.checklogic.TestResult;

/**
 * I am the configuration and logic for the test results web page.
 *
 * @since 0.3.0
 */
public final class ResultOfTestsPage implements Endpoints {
    /**
     * The root directory of the application.
     */
    private final String root;

    /**
     * The template processor for the TestResults page.
     */
    private final TemplateProcessor template;

    public ResultOfTestsPage(final String root) {
        this.root = root;
        this.template = TemplateProcessor.buildProcessor(
            WebResource.readFileFromResources("templates/test.html")
        );
    }

    @Override
    public void register(final WebFramework web) {
        web.registerPath(GET, "test", this::testPage);
    }

    private Response testPage(final Request request) {
        Objects.requireNonNull(request);
        final String results = FileUtils.readFileNames(this.root)
            .map(this::performTest)
            .map(TestResult::asHtmlTableRow)
            .collect(Collectors.joining());
        return Response.htmlOk(
            this.template.renderTemplate(Map.of("tests", "%s".formatted(results)))
        );
    }

    @SneakyThrows
    private TestResult performTest(final TestData test) {
        final SoftAssertions softly = new SoftAssertions();
        final ServerContext target = new ServerContextFactory(this.root)
            .fromStateFile(Files.newInputStream(new File(test.file()).toPath()));
        TestResult result;
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
        } catch (final AssertionError error) {
            result = new TestResult(test.toString(), false, error.getMessage());
        }
        return result;
    }
}
