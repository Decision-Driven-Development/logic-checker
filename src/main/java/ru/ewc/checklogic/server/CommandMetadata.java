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
import java.util.Map;
import java.util.stream.Collectors;
import ru.ewc.checklogic.FullServerContext;

/**
 * I am a class encapsulating commands metadata. My main responsibility is to present that metadata
 * in a human-readable form.
 *
 * @since 0.3.0
 */
public class CommandMetadata {
    /**
     * The names of the commands.
     */
    private final List<String> names;

    /**
     * The map of commands metadata.
     */
    private final Map<String, List<String>> metadata;

    /**
     * Ctor.
     *
     * @param metadata The map of commands metadata.
     */
    public CommandMetadata(final Map<String, List<String>> metadata) {
        this.names = metadata.keySet().stream().toList();
        this.metadata = metadata;
    }

    /**
     * Converts the command names to an HTML list.
     *
     * @param computation The server context to get the existing commands from.
     * @param outcome The decision table's outcome field that shows command's availability.
     * @return The command names as an HTML list to be used in a page template.
     */
    public String namesAsHtmlList(final FullServerContext computation, final String outcome) {
        return this.names.stream().map(
            command -> """
                <button class="btn %2$s"
                hx-get="/command" hx-target="body" hx-swap="beforeend"
                hx-vals='{"command":"%1$s"}'>%1$s</button>
                """.formatted(command, buttonCssClass(computation, command, outcome))
        ).collect(Collectors.joining());
    }

    /**
     * Presents the command arguments as an HTML form to accept the user input.
     *
     * @param command Command name to get the arguments for.
     * @param context The server context to get the existing arguments values from.
     * @return The command arguments as an HTML form to be used in a page template.
     */
    public String commandArgsAsHtmlForm(final String command, final FullServerContext context) {
        return this.metadata.get(command).stream()
            .distinct()
            .filter(arg -> context.requestLocatorName().equals(arg.split("::")[0]))
            .map(
                arg -> new StringBuilder()
                    .append("<div class='mb-3'>")
                    .append("<label for='%1$s' class='form-label'>%1$s:</label>")
                    .append("<input type='text' id='%1$s' name='%1$s' value='%2$s' ")
                    .append("class='form-control'>")
                    .append("</div>").toString()
                    .formatted(arg, extractValue(context, arg))
            )
            .collect(Collectors.joining());
    }

    private static String buttonCssClass(
        final FullServerContext computation,
        final String command,
        final String availability
    ) {
        final String result;
        if (computation.isAvailable(command, availability)) {
            result = "btn-success";
        } else {
            result = "btn-secondary";
        }
        return result;
    }

    private static String extractValue(final FullServerContext context, final String arg) {
        return context.valueFor(arg.split("::")[0], arg.split("::")[1]);
    }
}
