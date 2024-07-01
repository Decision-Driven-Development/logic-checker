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

import com.renomad.minum.templating.TemplateProcessor;
import com.renomad.minum.web.Request;
import com.renomad.minum.web.Response;
import com.renomad.minum.web.WebFramework;
import java.util.Map;
import ru.ewc.checklogic.ServerContext;
import ru.ewc.decisions.api.DecitaException;

/**
 * I am a configuration object for all the command-related endpoints.
 *
 * @since 0.3.0
 */
public final class CommandPage implements Endpoints {
    /**
     * The computation to be used for the command processing.
     */
    private final ServerContext computation;

    /**
     * The template processor for the Command page.
     */
    private final TemplateProcessor description;

    /**
     * The template processor for the error page.
     */
    private final TemplateProcessor error;

    /**
     * Ctor.
     *
     * @param computation The computation to be used for the command processing.
     */
    public CommandPage(final ServerContext computation) {
        this.computation = computation;
        this.description = TemplateProcessor.buildProcessor(
            WebResource.readFileFromResources("templates/command-info.html")
        );
        this.error = TemplateProcessor.buildProcessor(
            WebResource.readFileFromResources("templates/command-error.html")
        );
    }

    @Override
    public void register(final WebFramework web) {
        web.registerPath(POST, "command", this::executeCommand);
        web.registerPath(GET, "command", this::commandInfo);
    }

    // @todo #25 Pass the filled parameters to the command
    public Response executeCommand(final Request request) {
        final String command = request.body().asString("command");
        Response response;
        try {
            this.computation.perform(command);
            response = Response.htmlOk("OK", Map.of("HX-Redirect", "/"));
        } catch (final DecitaException exception) {
            response = Response.htmlOk(
                this.error.renderTemplate(
                    Map.of(
                        "error", exception.getMessage()
                    )
                )
            );
        }
        return response;
    }

    // @todo #25 Extract all the required parameters from the command's description
    // @todo #25 Implement the check for the command's decision table
    public Response commandInfo(final Request request) {
        final String command = request.requestLine().queryString().getOrDefault("command", "");
        return Response.htmlOk(this.description.renderTemplate(Map.of("command_name", command)));
    }
}
