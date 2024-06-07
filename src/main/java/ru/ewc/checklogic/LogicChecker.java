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

package ru.ewc.checklogic;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import org.assertj.core.api.SoftAssertions;
import org.yaml.snakeyaml.Yaml;
import ru.ewc.checklogic.server.WebServer;
import ru.ewc.decisions.api.ComputationContext;
import ru.ewc.decisions.api.Locator;
import ru.ewc.state.State;

/**
 * End-to-end tests based on yaml descriptions.
 *
 * @since 0.1
 */
@SuppressWarnings("PMD.ProhibitPublicStaticMethods")
public final class LogicChecker {
    /**
     * The logger.
     */
    private static final Logger LOGGER = Logger.getLogger(LogicChecker.class.getName());

    private LogicChecker() {
        // Utility class
    }

    public static void main(final String[] args) throws IOException {
        if (args.length == 0) {
            throw new IllegalArgumentException("Please provide the path to the resources");
        }
        final String root = args[0];
        if (args.length > 1 && "server".equals(args[1])) {
            final ComputationContext context = new ComputationContext(
                stateFromAppConfig(FileUtils.applicationConfig(root)),
                Path.of(root, "tables").toUri(),
                Path.of(root, "commands").toUri()
            );
            new WebServer(new Computation(context)).start();
        } else {
            System.setProperty("sources", root);
            final SoftAssertions softly = new SoftAssertions();
            FileUtils.readFileNames().forEach(test -> performTest(test, softly, root));
        }
    }

    @SneakyThrows
    static void performTest(
        final TestData test,
        final SoftAssertions softly,
        final String root
    ) {
        final Computation target = new Computation(
            new ComputationContext(
                stateFromFile(Files.newInputStream(new File(test.file()).toPath()), root),
                Path.of(FileUtils.getFinalPathTo("tables")).toUri(),
                Path.of(FileUtils.getFinalPathTo("commands")).toUri()
            )
        );
        try {
            if (!test.command.isEmpty()) {
                target.perform(test.command);
            }
            for (final String locator : test.expectations.keySet()) {
                softly
                    .assertThat(target.stateFor(locator, test.expectations.get(locator)))
                    .describedAs(String.format("State for entity '%s'", locator))
                    .containsExactlyInAnyOrderEntriesOf(test.expectations.get(locator));
            }
            softly.assertAll();
            LOGGER.info("Running test for %s... done".formatted(test.toString()));
        } catch (final AssertionError error) {
            LOGGER.severe("Running test for %s... failed".formatted(test.toString()));
            LOGGER.severe(error.getMessage());
        }
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    private static State stateFromAppConfig(final InputStream file) {
        final Map<String, Object> config = new Yaml().load(file);
        final Stream<String> names = ((List<String>) config.get("locators")).stream();
        return new State(
            names.collect(
                Collectors.toMap(
                    name -> name,
                    name -> new InMemoryStorage(new HashMap<>())
                )
            )
        );
    }

    @SuppressWarnings("unchecked")
    private static State stateFromFile(
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

    /**
     * I am the helper class containing the data for parameterized state tests.
     *
     * @param file The path to the file containing state and expectations.
     * @param command The name of the command to execute before the decision.
     * @param expectations The collection of expected decision table results.
     * @since 0.2.3
     */
    public record TestData(
        String file,
        String command,
        Map<String, Map<String, String>> expectations) {

        @Override
        public String toString() {
            final String test = this.file
                .substring(Math.max(0, this.file.lastIndexOf('\\')))
                .substring(Math.max(0, this.file.lastIndexOf('/')));
            return String.format("file='%s'", test);
        }
    }
}
