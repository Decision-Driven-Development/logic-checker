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
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.yaml.snakeyaml.Yaml;
import ru.ewc.commands.CommandsFacade;
import ru.ewc.decisions.api.ComputationContext;
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
public final class Computation {
    /**
     * Facade for making all the decisions.
     */
    private final DecitaFacade decisions;

    /**
     * Facade for executing all the commands.
     */
    private final CommandsFacade commands;

    /**
     * The current state of the system.
     */
    private final Locators state;

    /**
     * Ctor.
     *
     * @param tables Path to the folder with decision tables.
     * @param commands Path to the folder with commands.
     * @param stream Path to yaml describing the current system's state.
     */
    public Computation(final URI tables, final URI commands, final InputStream stream) {
        this.decisions = new DecitaFacade(tables, ".csv", ";");
        this.commands = new CommandsFacade(commands, this.decisions);
        this.state = stateFrom(stream);
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
     * Computes the decision for a specified table.
     *
     * @param table Name of the tables to make a decision against.
     * @param locators The locators to use for the decision.
     * @return The collection of outcomes from the specified table.
     * @throws DecitaException If the table could not be found or computed.
     */
    public Map<String, String> decideFor(final String table, final Locators locators)
        throws DecitaException {
        return this.decisions.decisionFor(table, this.state.mergedWith(locators));
    }

    public Map<String, String> decideFor(final String table) throws DecitaException {
        return this.decisions.decisionFor(table, this.state);
    }

    public void perform(final Transition command) {
        this.commands.perform(command.name(), this.state.mergedWith(command.request()));
    }

    public boolean hasStateFor(String table) {
        return this.state.hasLocator(table);
    }

    public Map<String, String> stateFor(String table, Map<String, String> state) {
        final Locator locator = this.state.locatorFor(table);
        final ComputationContext context = new ComputationContext(this.state);
        Map<String, String> actual = new HashMap<>(state.size());
        for (String s : state.keySet()) {
            actual.put(s, locator.fragmentBy(s, context));
        }
        return actual;
    }

    /**
     * Loads the state from the specified {@code InputStream}.
     *
     * @param stream InputStream containing state info.
     * @return Collection of {@link Locator} objects, containing desired state.
     */
    private static Locators stateFrom(final InputStream stream) {
        return new Locators(
            stateFromFile(stream)
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entryToLocator()))
        );
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Map<String, Object>> stateFromFile(final InputStream stream) {
        return (Map<String, Map<String, Object>>) new Yaml().loadAll(stream).iterator().next();
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
