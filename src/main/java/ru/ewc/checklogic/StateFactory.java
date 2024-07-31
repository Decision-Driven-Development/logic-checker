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

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.SneakyThrows;
import org.yaml.snakeyaml.Yaml;
import ru.ewc.decisions.api.Locator;
import ru.ewc.state.State;

/**
 * I am a factory for creating the state of the system.
 *
 * @since 0.3.2
 */
public final class StateFactory {
    /**
     * The root path for the external business logic resources.
     */
    @Getter
    private final String root;

    /**
     * Optional source of locators to be added to the initial (clean) state.
     */
    private InputStream src;

    public StateFactory(final String root) {
        this.root = root;
    }

    @SneakyThrows
    public State initialState() {
        State state;
        try (InputStream file = Files.newInputStream(Path.of(this.root, "application.yaml"))) {
            state = StateFactory.stateFromAppConfig(file);
            state.locators().putAll(this.locatorsFromFile());
        } catch (final NoSuchFileException exception) {
            state = new NullState(Map.of());
        }
        return state;
    }

    public StateFactory with(final InputStream file) {
        this.src = file;
        return this;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Locator> locatorsFromFile() {
        final Map<String, Locator> locators = new HashMap<>();
        if (this.src != null) {
            final Map<String, Map<String, Object>> raw =
                (Map<String, Map<String, Object>>) new Yaml().loadAll(this.src).iterator().next();
            raw.forEach((name, data) -> locators.put(name, new InMemoryStorage(data)));
        }
        return locators;
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
