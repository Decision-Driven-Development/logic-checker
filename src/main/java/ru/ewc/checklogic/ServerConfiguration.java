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

import java.util.HashMap;
import java.util.Map;

/**
 * I am a context for the web server.
 *
 * @since 0.3.2
 */
public final class ServerConfiguration {
    /**
     * The parameters of the context.
     */
    private final Map<String, String> parameters = new HashMap<>(
        Map.of(
            "request", "request",
            "command", "available"
        )
    );

    /**
     * Returns the value of the specified parameter.
     *
     * @param parameter The name of the parameter.
     * @return The value of the parameter.
     */
    public String getParameterValue(final String parameter) {
        return this.parameters.getOrDefault(parameter, "");
    }

    /**
     * Sets the value of the specified parameter.
     *
     * @param parameter The name of the parameter.
     * @param value The value of the parameter.
     */
    public void setParameterValue(final String parameter, final String value) {
        this.parameters.put(parameter, value);
    }

    public String commandAvailabilityField() {
        return this.getParameterValue("command");
    }

    public String requestLocatorName() {
        return this.getParameterValue("request");
    }
}
