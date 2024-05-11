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
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import org.yaml.snakeyaml.Yaml;
import ru.ewc.decisions.api.DecitaException;
import ru.ewc.decisions.api.DecitaFacade;
import ru.ewc.decisions.api.Locator;
import ru.ewc.decisions.api.Locators;

/**
 * I am a unique instance of a decision table computation.
 *
 * @since 0.1.0
 */
@SuppressWarnings("PMD.ProhibitPublicStaticMethods")
public class Computation {
    /**
     * Facade for making all the decisions
     */
    private final DecitaFacade facade;

    /**
     * A URI pointing to a state yaml file.
     */
    private final URI state;

    /**
     * Default Ctor.
     */
    public Computation() {
        this(new DecitaFacade(() -> new Locators(Map.of())), null);
    }

    /**
     * Ctor.
     *
     * @param facade Reader that can read tables data from the file system.
     * @param state Path to yaml describing the current system's state.
     */
    private Computation(final DecitaFacade facade, final URI state) {
        this.facade = facade;
        this.state = state;
    }

    /**
     * Converts a string representation of the file system path to a correct URI.
     *
     * @param path File system path as a String.
     * @return URI that corresponds to a given path.
     */
    public static URI uriFrom(final String path) {
        final StringBuilder result = new StringBuilder("file:/");
        if (path.charAt(0) == '/') {
            result.append(path.replace('\\', '/').substring(1));
        } else {
            result.append(path.replace('\\', '/'));
        }
        return URI.create(result.toString());
    }

    /**
     * Creates a copy of this instance with a new path to state yaml.
     *
     * @param path Path to a file that holds the current state's description.
     * @return A new instance of {@link Computation}.
     */
    public Computation statePath(final String path) {
        return new Computation(
            this.facade,
            uriFrom(path)
        );
    }

    /**
     * Creates a copy of this instance with a new path to tables folder.
     *
     * @param path Path to a folder containing all the decision tables.
     * @return A new instance of {@link Computation}.
     */
    public Computation tablePath(final String path) {
        return new Computation(
            new DecitaFacade(uriFrom(path), ".csv", ";"),
            this.state
        );
    }

    /**
     * Converts yaml data read from input stream to a correct {@link InMemoryStorage} object.
     *
     * @return The collection of {@link InMemoryStorage} objects.
     */
    @SneakyThrows
    public Locators currentState() {
        try (InputStream stream = Files.newInputStream(new File(this.state).toPath())) {
            return stateFrom(stream);
        }
    }

    /**
     * Computes the decision for a specified table.
     *
     * @param table Name of the tables to make a decision against.
     * @return The collection of outcomes from the specified table.
     * @throws DecitaException If the table could not be found or computed.
     */
    public Map<String, String> decideFor(final String table) throws DecitaException {
        return this.facade.decisionFor(table, this.currentState());
    }

    /**
     * Loads the state from the specified {@code InputStream}.
     *
     * @param stream InputStream containing state info.
     * @return Collection of {@link Locator} objects, containing desired state.
     */
    @SuppressWarnings("unchecked")
    private static Locators stateFrom(final InputStream stream) {
        return new Locators(
            ((Map<String, Map<String, Object>>) new Yaml().loadAll(stream).iterator().next())
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entryToLocator()))
        );
    }

    /**
     * Converts a {@link Map.Entry} to a {@link Locator} object.
     *
     * @return A function that converts a {@link Map.Entry} to a {@link Locator} object.
     */
    private static Function<Map.Entry<String, Map<String, Object>>, Locator> entryToLocator() {
        return e -> new InMemoryStorage(e.getValue());
    }
}
