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
import java.util.List;
import java.util.Map;
import lombok.Getter;
import ru.ewc.decisions.api.ComputationContext;
import ru.ewc.decisions.api.DecisionTables;
import ru.ewc.decisions.api.DecitaException;
import ru.ewc.decisions.api.InMemoryLocator;
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
    @Getter
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
        this.root = this.server.getRoot();
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

    public ComputationContext computation() {
        return this.context;
    }

    public void update(final List<String> values) {
        final InMemoryLocator request = InMemoryLocator.empty(this.server.requestLocatorName());
        values.forEach(
            value -> {
                final String[] split = value.split(":");
                request.setFragmentValue(split[0].trim(), split[1].trim());
            });
        this.state.locators().put(this.server.requestLocatorName(), request);
        this.context = new ComputationContext(this.state, this.getAllTables());
    }

    public boolean isEmpty() {
        return this.state.locators().isEmpty();
    }

    public void initialize() {
        this.state = this.states.initialState();
        this.context = new ComputationContext(this.state, this.getAllTables());
    }

    public boolean isNotSpecified(final String arg) {
        final String[] args = arg.split("::");
        final boolean function = this.server.functionsLocatorName().equals(args[0]);
        final boolean request = this.server.requestLocatorName().equals(args[0]);
        return function && !this.states.functionSpecified(args[1]) || request;
    }

    private DecisionTables getAllTables() {
        return DecisionTables.using(this.server.csvReader(this.tables));
    }
}
