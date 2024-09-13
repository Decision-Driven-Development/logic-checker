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
package ru.ewc.checklogic.testing;

import groovy.lang.Binding;
import groovy.util.GroovyScriptEngine;
import java.net.URL;
import java.nio.file.Path;
import java.util.HashMap;
import lombok.SneakyThrows;
import ru.ewc.decisions.api.ComputationContext;
import ru.ewc.decisions.api.DecitaException;
import ru.ewc.decisions.api.InMemoryLocator;
import ru.ewc.decisions.api.Locator;

/**
 * I am a special locator that can load functions from Groovy scripts. My main responsibility is to
 * run the Groovy script and return the result of the script execution.
 *
 * @since 0.4.1
 */
public final class FunctionsLocator implements Locator {
    /**
     * The locator's name.
     */
    private final String name;

    /**
     * The path to the Groovy scripts.
     */
    private final Path path;

    /**
     * The in-memory locator used to store overridden values.
     */
    private final InMemoryLocator locator;

    /**
     * Groovy script engine used to run the Groovy scripts.
     */
    private final GroovyScriptEngine engine;

    @SneakyThrows
    public FunctionsLocator(final String name, final Path path) {
        this.name = name;
        this.path = path;
        this.locator = new InMemoryLocator("locator", new HashMap<>());
        this.engine = new GroovyScriptEngine(new URL[]{path.toUri().toURL()});
    }

    @SneakyThrows
    @Override
    public String fragmentBy(
        final String fragment,
        final ComputationContext context
    ) throws DecitaException {
        final String result;
        if (this.locator.state().containsKey(fragment)) {
            result = this.locator.fragmentBy(fragment, context);
        } else if (this.hasScript(fragment)) {
            final Binding binding = new Binding();
            binding.setVariable("context", context);
            result = this.engine.run("%s.groovy".formatted(fragment), binding).toString();
        } else {
            result = "undefined";
        }
        return result;
    }

    @Override
    public void setFragmentValue(final String fragment, final String value) {
        this.locator.setFragmentValue(fragment, value);
    }

    @Override
    public String locatorName() {
        return this.name;
    }

    public boolean functionSpecified(final String arg) {
        return this.hasValue(arg) || this.hasScript(arg);
    }

    private boolean hasScript(final String arg) {
        return this.path.resolve("%s.groovy".formatted(arg)).toFile().exists();
    }

    private boolean hasValue(final String arg) {
        return !"undefined".equals(this.locator.state().getOrDefault(arg, "undefined"));
    }
}
