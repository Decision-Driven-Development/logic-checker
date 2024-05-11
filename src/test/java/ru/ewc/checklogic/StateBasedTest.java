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

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.SneakyThrows;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.yaml.snakeyaml.Yaml;
import ru.ewc.decisions.api.Locators;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end tests based on yaml descriptions.
 *
 * @since 0.1
 */
@SuppressWarnings("PMD.ProhibitPublicStaticMethods")
class StateBasedTest {
    /**
     * The soft assertions to gather all the failures.
     */
    private SoftAssertions softly;

    @BeforeEach
    void setUp() {
        this.softly = new SoftAssertions();
    }

    @SneakyThrows
    static Stream<TestData> readFileNames() {
        return Files.walk(Paths.get(Computation.uriFrom(getFinalPathTo("states"))))
            .filter(Files::isRegularFile)
            .map(path -> path.toFile().getAbsolutePath())
            .map(
                path -> {
                    final InputStream stream;
                    try {
                        stream = Files.newInputStream(Paths.get(path));
                    } catch (final IOException exception) {
                        throw new IllegalStateException(exception);
                    }
                    return createTestData(path, stream);
                });
    }

    @SneakyThrows
    @ParameterizedTest(name = "{0}")
    @MethodSource("readFileNames")
    void testPerformingFileBasedTest(final TestData test) {
        final Computation target = new Computation(
            Computation.uriFrom(getFinalPathTo("tables")),
            Computation.uriFrom(getFinalPathTo("commands")),
            Computation.uriFrom(test.file)
        );
        for (int i = 0; i < test.commands.size(); i++) {
            Transition command = test.commands.get(i);
            assertThat(target.decideFor(command.name, command.request))
                .describedAs(String.format("Command '%s'[%d] should be available", command.name, i + 1))
                .containsEntry("available", "true");
            target.perform(command);
        }
        for (final String table : test.expectations.keySet()) {
            this.softly
                .assertThat(target.decideFor(table))
                .describedAs(String.format("Table '%s'", table))
                .isEqualTo(test.expectations.get(table));
        }
        this.softly.assertAll();
    }

    @SuppressWarnings("unchecked")
    private static TestData createTestData(final String path, final InputStream stream) {
        final Iterator<Object> iterator = new Yaml().loadAll(stream).iterator();
        iterator.next();
        List<Transition> commands = new ArrayList<>(1);
        if (iterator.hasNext()) {
            commands = extractCommands(iterator.next());
        }
        Map<String, Map<String, String>> expectations = new HashMap<>();
        if (iterator.hasNext()) {
            expectations = (Map<String, Map<String, String>>) iterator.next();
        }
        return new TestData(path, commands, expectations);
    }

    @SuppressWarnings("unchecked")
    private static List<Transition> extractCommands(final Object next) {
        final Map<String, List<Object>> commands = (Map<String, List<Object>>) next;
        return commands.getOrDefault("commands", List.of()).stream()
            .map(entry -> {
                Map<String, Object> map = (Map<String, Object>) entry;
                return new Transition((String) map.get("name"), requestLocator((Map<String, Object>) map.get("request")));
            })
            .toList();
    }

    private static Locators requestLocator(Map<String, Object> entry) {
        return new Locators(Map.of("request", new InMemoryStorage(entry)));
    }

    /**
     * Gets the path to a folder with specified resources.
     *
     * @param resource Resource name to get its folder.
     * @return Path to a resource folder as a String.
     */
    private static String getFinalPathTo(final String resource) {
        final String states;
        if (System.getProperties().containsKey(resource)) {
            states = System.getProperty(resource);
        } else {
            states = String.format(
                "%s%s%s",
                System.getProperty("user.dir"),
                "/src/test/resources/",
                resource
            );
        }
        return states;
    }

    /**
     * I am the helper class containing the data for parameterized state tests.
     *
     * @since 0.2.3
     */
    @Getter
    @SuppressWarnings("PMD.TestClassWithoutTestCases")
    public static class TestData {
        /**
         * Path to the file containing state and expectations.
         */
        private final String file;

        /**
         * The collection of commands to execute before the decision.
         */
        private final List<Transition> commands;

        /**
         * The collection of expected decision table results.
         */
        private final Map<String, Map<String, String>> expectations;

        TestData(final String file,
                 final List<Transition> commands,
                 final Map<String, Map<String, String>> expectations) {
            this.file = file;
            this.commands = commands;
            this.expectations = expectations;
        }

        @Override
        public String toString() {
            final String test = this.file
                .substring(Math.max(0, this.file.lastIndexOf('\\')))
                .substring(Math.max(0, this.file.lastIndexOf('/')));
            return String.format("file='%s'", test);
        }
    }
}
