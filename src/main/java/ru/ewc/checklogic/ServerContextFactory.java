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
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import org.yaml.snakeyaml.Yaml;
import ru.ewc.decisions.api.Locator;
import ru.ewc.state.State;

/**
 * I am a factory for creating server contexts.
 *
 * @since 0.3.2
 */
public class ServerContextFactory {
    /**
     * The root path for the external business logic resources.
     */
    private final String root;

    public ServerContextFactory(final String root) {
        this.root = root;
    }

    /**
     * Creates a new server context from the application configuration.
     *
     * @return A new server context initialized with the basic set of empty Locators.
     */
    // @todo #33 Decide on what type of ServerContext to create
    @SneakyThrows
    public ServerContext initialState() {
        final FullServerContext result = new FullServerContext(
            this.loadInitialState(),
            Path.of(this.root, "tables").toUri(),
            Path.of(this.root, "commands").toUri()
        );
        result.cache("available", "available");
        result.cache("request", "request");
        return result;
    }

    /**
     * Creates a new server context from a state file. Used in state-based tests, where each test
     * gets its own initial state, described in test "Arrange" section.
     *
     * @param file The stream of the test file's contents.
     * @return A new server context initialized with state described in test file.
     */
    @SneakyThrows
    public ServerContext fromStateFile(final InputStream file) {
        final State state = this.loadInitialState();
        state.locators().putAll(locatorsFromFile(file));
        return new FullServerContext(
            state,
            Path.of(this.root, "tables").toUri(),
            Path.of(this.root, "commands").toUri()
        );
    }

    private static Map<String, Locator> locatorsFromFile(final InputStream file) {
        final Map<String, Map<String, Object>> raw =
            (Map<String, Map<String, Object>>) new Yaml().loadAll(file).iterator().next();
        final Map<String, Locator> locators = new HashMap<>(raw.size());
        raw.forEach((name, data) -> locators.put(name, new InMemoryStorage(data)));
        return locators;
    }

    private State loadInitialState() throws IOException {
        State state;
        try (InputStream file = Files.newInputStream(Path.of(this.root, "application.yaml"))) {
            state = ServerContextFactory.stateFromAppConfig(file);
        } catch (final NoSuchFileException exception) {
            state = new NullState(Map.of());
        }
        return state;
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
}
