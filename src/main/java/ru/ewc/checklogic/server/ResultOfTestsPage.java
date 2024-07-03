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
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import org.assertj.core.api.SoftAssertions;
import org.yaml.snakeyaml.Yaml;
import ru.ewc.checklogic.FileUtils;
import ru.ewc.checklogic.InMemoryStorage;
import ru.ewc.checklogic.ServerContext;
import ru.ewc.checklogic.TestData;
import ru.ewc.checklogic.TestResult;
import ru.ewc.decisions.api.Locator;
import ru.ewc.state.State;

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

    @SuppressWarnings("unchecked")
    public static State stateFromFile(
        final InputStream stream,
        final String root
    ) throws IOException {
        final Map<String, Object> config = new Yaml().load(FileUtils.applicationConfig(root));
        final List<String> names = (List<String>) config.get("locators");
        final Map<String, Locator> locators = HashMap.newHashMap(names.size());
        names.forEach(name -> locators.put(name, new InMemoryStorage(new HashMap<>())));
        final Map<String, Map<String, Object>> raw =
            (Map<String, Map<String, Object>>) new Yaml().loadAll(stream).iterator().next();
        raw.keySet().forEach(name -> locators.put(name, new InMemoryStorage(raw.get(name))));
        return new State(locators);
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
        final ServerContext target = new ServerContext(
            stateFromFile(Files.newInputStream(new File(test.file()).toPath()), this.root),
            Path.of(this.root, "tables").toUri(),
            Path.of(this.root, "commands").toUri()
        );
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
