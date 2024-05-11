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

import java.util.Map;
import ru.ewc.decisions.api.ComputationContext;
import ru.ewc.decisions.api.Locator;

/**
 * I am a simple in-memory key-value storage. My main responsibility is to store and return the
 * specified data for testing purposes.
 *
 * @since 0.2
 */
public final class InMemoryStorage implements Locator {
    /**
     * Simple key-value storage.
     */
    private final Map<String, Object> storage;

    /**
     * Ctor.
     *
     * @param storage The pre-filled key-value storage to start with.
     */
    public InMemoryStorage(final Map<String, Object> storage) {
        this.storage = storage;
    }

    @Override
    public String fragmentBy(final String fragment, final ComputationContext context) {
        return this.storage.getOrDefault(fragment, "undefined").toString();
    }

    @Override
    public void setFragmentValue(final String fragment, final String value) {
        this.storage.put(fragment, value);
    }
}
