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
package ru.ewc.checklogic.server;

import java.util.List;
import java.util.stream.Collectors;

/**
 * I am a class encapsulating the command names. My main responsibility is to present those names
 * in a human-readable form.
 *
 * @since 0.3.0
 */
public class CommandNames {
    /**
     * The names of the commands.
     */
    private final List<String> names;

    /**
     * Ctor.
     *
     * @param names The names of the commands.
     */
    public CommandNames(final List<String> names) {
        this.names = names;
    }

    /**
     * Converts the command names to an HTML list.
     *
     * @return The command names as an HTML list to be used in a page template.
     */
    public String asHtmlList() {
        return "<ul>%s</ul>".formatted(
            this.names.stream()
                .map("""
                    <button hx-get="/command" hx-target="body" hx-swap="beforeend"
                    hx-vals='{"command":"%1$s"}'>%1$s</button>
                    """::formatted
                ).collect(Collectors.joining())
        );
    }
}
