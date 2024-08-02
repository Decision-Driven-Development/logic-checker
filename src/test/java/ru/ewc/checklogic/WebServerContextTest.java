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

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * I am a test class for the {@link WebServerContext} class.
 *
 * @since 0.3.2
 */
final class WebServerContextTest {
    @Test
    void emptyServerContextHasDefaultRequestLocatorName() {
        final WebServerContext context = new WebServerContext();
        MatcherAssert.assertThat(
            "New server context should have 'request' as default incoming request locator's name",
            context.getParameterValue("request"),
            Matchers.is("request")
        );
    }

    @Test
    void emptyServerContextHasDefaultCommandAvailabilityOutcome() {
        final WebServerContext context = new WebServerContext();
        MatcherAssert.assertThat(
            "New server context should have 'available' as default command availability outcome",
            context.getParameterValue("command"),
            Matchers.is("available")
        );
    }

    @Test
    void shouldUpdateRequestLocatorName() {
        final WebServerContext context = new WebServerContext();
        context.setParameterValue("request", "incoming");
        MatcherAssert.assertThat(
            "Context server should update its incoming request locator's name",
            context.getParameterValue("request"),
            Matchers.is("incoming")
        );
    }

    @Test
    void shouldUpdateCommandAvailabilityOutcome() {
        final WebServerContext context = new WebServerContext();
        context.setParameterValue("command", "enabled");
        MatcherAssert.assertThat(
            "Context server should update its command availability outcome",
            context.getParameterValue("command"),
            Matchers.is("enabled")
        );
    }
}
