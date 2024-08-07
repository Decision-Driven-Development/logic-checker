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
import ru.ewc.state.State;

/**
 * I am a factory for creating state objects. My subclasses are responsible for creating the initial
 * state and loading the state from a file. They can do that for real or mock the result for testing
 * purposes.
 *
 * @since 0.3.2
 */
public abstract class StateFactory {
    /**
     * The root path for the external business logic resources.
     */
    private final String root;

    public StateFactory(final String root) {
        this.root = root;
    }

    /**
     * Returns the path to the root folder of the external business logic resources.
     *
     * @return Path to the root folder as a string.
     */
    public String getRoot() {
        return this.root;
    }

    public abstract State initialState();

    public abstract StateFactory with(InputStream file);

    public abstract void initialize();
}
