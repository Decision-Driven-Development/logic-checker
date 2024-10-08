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

import com.renomad.minum.web.Request;
import com.renomad.minum.web.Response;
import com.renomad.minum.web.WebFramework;
import java.util.Map;
import ru.ewc.checklogic.ServerConfiguration;
import ru.ewc.checklogic.ServerInstance;

/**
 * I am a class providing access to all the pages and static files packed inside the jar.
 *
 * @since 0.3.0
 */
public final class AllEndpoints implements Endpoints {
    /**
     * The context to be used for the server.
     */
    private final ServerInstance context;

    /**
     * The template renderer, creating pages to be served.
     */
    private final WebPages pages;

    public AllEndpoints(
        final ServerInstance context,
        final ServerConfiguration config,
        final ResourceTemplateRender render
    ) {
        this.context = context;
        this.pages = new WebPages(render, context.getRoot(), config);
    }

    @Override
    public void register(final WebFramework web) {
        web.registerPartialPath(GET, "static", AllEndpoints::staticResource);
        web.registerPath(GET, "", this::httpGetRouter);
        web.registerPath(GET, "test", this::httpGetRouter);
    }

    private Response httpGetRouter(final Request request) {
        final Response result;
        if (this.context.isEmpty()) {
            result = this.pages.uninitializedPage();
        } else {
            result = this.getAddressFor(request);
        }
        return result;
    }

    private Response getAddressFor(final Request request) {
        final Response result;
        final String address = request.requestLine().getPathDetails().getIsolatedPath();
        if (address.isEmpty()) {
            result = this.renderHtmlFor("templates/index.html");
        } else if ("test".equals(address)) {
            result = this.pages.testPage();
        } else {
            result = new Response(NOT_FOUND, "", PLAIN_TEXT);
        }
        return result;
    }

    private Response renderHtmlFor(final String template) {
        return Response.htmlOk(this.pages.renderInLayout(template, Map.of()));
    }

    private static Response staticResource(final Request request) {
        final Response result;
        if (request.requestLine().getPathDetails().getIsolatedPath().endsWith("main.css")) {
            final String body = WebResource.contentOf("static/main.css");
            result = new Response(OK, body, CSS);
        } else if (request.requestLine().getPathDetails().getIsolatedPath().endsWith("logo.png")) {
            final byte[] body = WebResource.bytesOf("static/logo.png");
            result = new Response(OK, body, PNG);
        } else {
            result = new Response(NOT_FOUND, "", PLAIN_TEXT);
        }
        return result;
    }
}
