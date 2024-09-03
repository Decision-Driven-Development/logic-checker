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

import java.net.URI;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ru.ewc.decisions.api.ComputationContext;
import ru.ewc.decisions.api.DecisionTables;
import ru.ewc.decisions.api.DecitaException;
import ru.ewc.decisions.input.CombinedCsvFileReader;
import ru.ewc.state.State;

/**
 * I am a unique instance of a decision table computation.
 *
 * @since 0.1.0
 */
@SuppressWarnings("PMD.ProhibitPublicStaticMethods")
public final class ServerInstance {
    /**
     * The root path for the external business logic resources.
     */
    private final String root;

    /**
     * The stored state of the system, persisted between requests.
     */
    private State state;

    /**
     * The URI of the tables folder, used to recreate the computation context for each request.
     */
    private final URI tables;

    /**
     * The context of the computation.
     */
    private ComputationContext context;

    /**
     * The web server context.
     */
    private final ServerConfiguration server;

    /**
     * The factory for the states.
     */
    private final StateFactory states;

    ServerInstance(
        final StateFactory initial,
        final URI tables,
        final ServerConfiguration server) {
        this.states = initial;
        this.server = server;
        this.root = this.states.getRoot();
        this.state = this.states.initialState();
        this.tables = tables;
        this.context = new ComputationContext(this.state, this.getAllTables());
    }

    public static ServerInstance testable() {
        return ServerContextFactory.testable().initialState();
    }

    public void perform(final String command, final Map<String, String> args) {
        args.forEach(
            (key, value) -> {
                if (!value.isEmpty()) {
                    final String[] split = key.split("::");
                    this.context.setValueFor(split[0], split[1], value);
                }
            });
        try {
            this.context.perform(command);
            this.context = new ComputationContext(this.state, this.getAllTables());
        } catch (final DecitaException exception) {
            throw new IllegalStateException(
                "Command file for '%s' not found".formatted(command),
                exception
            );
        }
    }

    public Map<String, String> stateFor(final String table, final Map<String, String> entities) {
        final Map<String, String> actual = HashMap.newHashMap(entities.size());
        for (final String fragment : entities.keySet()) {
            actual.put(fragment, this.context.valueFor(table, fragment));
        }
        return actual;
    }

    public Map<String, Map<String, Object>> storedState() {
        return this.context.storedState();
    }

    public String valueFor(final String locator, final String fragment) {
        String value;
        try {
            value = this.context.valueFor(locator, fragment);
        } catch (final DecitaException exception) {
            value = "";
        }
        return value;
    }

    public Map<String, List<String>> commandData() {
        return this.context.commandData();
    }

    public boolean isAvailable(final String command, final String field) {
        return "true".equalsIgnoreCase(this.context.decisionFor(command).get(field));
    }

    public void update(final List<String> values) {
        this.state.locators().put(
            this.server.requestLocatorName(),
            InMemoryStorage.from(this.server.requestLocatorName(), values)
        );
        this.context = new ComputationContext(this.state, this.getAllTables());
    }

    public String requestLocatorName() {
        return this.server.requestLocatorName();
    }

    public boolean isEmpty() {
        return this.state.locators().isEmpty();
    }

    public String getRoot() {
        return this.root;
    }

    public void initialize() {
        this.states.initialize();
        this.state = this.states.initialState();
        this.context = new ComputationContext(this.state, this.getAllTables());
    }

    public boolean hasTestsFolder() {
        return Paths.get(this.root, "tests").toFile().exists();
    }

    public void createTestFolder() {
        Paths.get(this.root, "tests").toFile().mkdirs();
        this.context = new ComputationContext(this.state, this.getAllTables());
    }

    private DecisionTables getAllTables() {
        return DecisionTables.using(new CombinedCsvFileReader(this.tables, ".csv", ";"));
    }
}