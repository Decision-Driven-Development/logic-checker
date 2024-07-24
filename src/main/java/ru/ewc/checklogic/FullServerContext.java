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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.SneakyThrows;
import ru.ewc.decisions.api.ComputationContext;
import ru.ewc.decisions.api.DecitaException;
import ru.ewc.state.State;

/**
 * I am a unique instance of a decision table computation.
 *
 * @since 0.1.0
 */
@SuppressWarnings("PMD.ProhibitPublicStaticMethods")
public final class FullServerContext implements ServerContext {
    /**
     * The stored state of the system, persisted between requests.
     */
    private final State state;

    /**
     * The URI of the tables folder, used to recreate the computation context for each request.
     */
    private final URI tables;

    /**
     * The URI of the commands folder, used to recreate the computation context for each request.
     */
    private final URI commands;

    /**
     * The context of the computation.
     */
    private ComputationContext context;

    /**
     * The cached request parameters.
     */
    private final Map<String, String> parameters;

    FullServerContext(final State initial, final URI tables, final URI commands) {
        this.state = initial;
        this.tables = tables;
        this.commands = commands;
        this.context = new ComputationContext(initial, tables, commands);
        this.parameters = new HashMap<>(2);
    }

    @Override
    public void perform(final String command) {
        this.perform(command, Map.of());
    }

    @Override
    public void perform(final String command, final Map<String, String> args) {
        args.forEach(
            (key, value) -> {
                if (!value.isEmpty()) {
                    final String[] split = key.split("::");
                    this.context.setValueFor(split[0], split[1], value);
                }
            });
        this.context.perform(command);
        this.context = new ComputationContext(this.state, this.tables, this.commands);
    }

    @Override
    public Map<String, String> stateFor(final String table, final Map<String, String> entities) {
        final Map<String, String> actual = HashMap.newHashMap(entities.size());
        for (final String fragment : entities.keySet()) {
            actual.put(fragment, this.context.valueFor(table, fragment));
        }
        return actual;
    }

    @Override
    public Map<String, Map<String, Object>> storedState() {
        return this.context.storedState();
    }

    @Override
    public String valueFor(final String locator, final String fragment) {
        String value;
        try {
            value = this.context.valueFor(locator, fragment);
        } catch (final DecitaException exception) {
            value = "";
        }
        return value;
    }

    @Override
    public Map<String, List<String>> commandData() {
        return this.context.commandData();
    }

    @Override
    public boolean isAvailable(final String command, final String field) {
        return "true".equalsIgnoreCase(this.context.decisionFor(command).get(field));
    }

    @Override
    public void update(final List<String> values) {
        this.state.locators().put(this.cached("request"), InMemoryStorage.from(values));
        this.context = new ComputationContext(this.state, this.tables, this.commands);
    }

    @Override
    public String cached(final String parameter) {
        return this.parameters.getOrDefault(parameter, "");
    }

    @Override
    public void cache(final String parameter, final String value) {
        this.parameters.put(parameter, value);
    }

    @SneakyThrows
    static ServerContext contextForFolder(final String root) {
        final ServerContext result = new ServerContextFactory(root).instance(
            LogicChecker.stateFromAppConfig(FileUtils.applicationConfig(root))
        );
        result.cache("available", "available");
        result.cache("request", "request");
        return result;
    }
}
