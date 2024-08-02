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
import java.nio.file.Path;

/**
 * I am a factory for creating server contexts.
 *
 * @since 0.3.2
 */
public class ServerContextFactory {
    /**
     * The root path for the external business logic resources.
     */
    private final String root;

    public ServerContextFactory(final String root) {
        this.root = root;
    }

    /**
     * Creates a new server context from the application configuration.
     *
     * @return A new server context initialized with the basic set of empty Locators.
     */
    public FullServerContext initialState() {
        final FullServerContext result = new FullServerContext(
            new StateFactory(this.root),
            Path.of(this.root, "tables").toUri(),
            Path.of(this.root, "commands").toUri(),
            new WebServerContext()
        );
        result.cache("command", "available");
        result.cache("request", "request");
        return result;
    }

    /**
     * Creates a new server context from a state file. Used in state-based tests, where each test
     * gets its own initial state, described in test "Arrange" section.
     *
     * @param file The stream of the test file's contents.
     * @return A new server context initialized with state described in test file.
     */
    public FullServerContext fromStateFile(final InputStream file) {
        return new FullServerContext(
            new StateFactory(this.root).with(file),
            Path.of(this.root, "tables").toUri(),
            Path.of(this.root, "commands").toUri(),
            new WebServerContext()
        );
    }
}
