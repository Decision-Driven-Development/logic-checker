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

/**
 * I am a class providing access to all the pages and static files packed inside the jar.
 *
 * @since 0.3.0
 */
public final class AllEndpoints implements Endpoints {
    /**
     * The template processor for the index page.
     */
    private final TemplateProcessor index;

    // @todo #47 Extract template processing into a class with lazy loading
    public AllEndpoints() {
        this.index = TemplateProcessor.buildProcessor(
            WebResource.readFileFromResources("templates/index.html")
        );
    }

    @Override
    public void register(final WebFramework web) {
        web.registerPartialPath(GET, "static", AllEndpoints::staticResource);
        web.registerPath(GET, "", this::getRequestDispatcher);
    }

    // @todo #47 Check the server state before dispatching the request
    @SuppressWarnings("PMD.UnusedFormalParameter")
    private Response getRequestDispatcher(final Request request) {
        final Response result;
        if (request.requestLine().getPathDetails().getIsolatedPath().isEmpty()) {
            result = Response.htmlOk(this.index.renderTemplate(Map.of()));
        } else {
            result = new Response(NOT_FOUND, "", PLAIN_TEXT);
        }
        return result;
    }

    private static Response staticResource(final Request request) {
        final Response result;
        if (request.requestLine().getPathDetails().getIsolatedPath().endsWith("main.css")) {
            final String body = WebResource.readFileFromResources("static/main.css");
            result = new Response(OK, body, CSS);
        } else {
            result = new Response(NOT_FOUND, "", PLAIN_TEXT);
        }
        return result;
    }
}
