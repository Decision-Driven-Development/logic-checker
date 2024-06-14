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
import ru.ewc.decisions.api.ComputationContext;

/**
 * I am a unique instance of a decision table computation.
 *
 * @since 0.1.0
 */
@SuppressWarnings("PMD.ProhibitPublicStaticMethods")
public final class Computation {
    /**
     * The context of the computation.
     */
    private final ComputationContext context;

    public Computation(final ComputationContext context) {
        this.context = context;
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

    public void perform(final String command) {
        this.context.perform(command);
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

    public List<String> commandNames() {
        return this.context.commandNames();
    }
}
