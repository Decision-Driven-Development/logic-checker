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
import ru.ewc.checklogic.FullServerContext;

/**
 * I am a class providing access to all the pages and static files packed inside the jar.
 *
 * @since 0.3.0
 */
public final class AllEndpoints implements Endpoints {
    /**
     * The context to be used for the server.
     */
    private final FullServerContext context;

    /**
     * The template renderer, creating pages to be served.
     */
    private final WebPages pages;

    public AllEndpoints(final FullServerContext context) {
        this.context = context;
        this.pages = new WebPages(new ResourceTemplateProcessors(), context.getRoot());
    }

    @Override
    public void register(final WebFramework web) {
        web.registerPartialPath(GET, "static", AllEndpoints::staticResource);
        web.registerPath(GET, "", this::httpGetRouter);
        web.registerPath(GET, "test", this::httpGetRouter);
        web.registerPath(GET, "state", this::httpGetRouter);
        web.registerPath(POST, "state", this::httpPostRouter);
        web.registerPath(POST, "test", this::httpPostRouter);
    }

    private Response httpPostRouter(final Request request) {
        final Response result;
        final String address = request.requestLine().getPathDetails().getIsolatedPath();
        if (this.context.isEmpty()) {
            if ("state".equals(address)) {
                this.context.initialize();
                result = Response.htmlOk("OK", Map.of("HX-Redirect", "/"));
            } else {
                result = this.pages.uninitializedPage();
            }
        } else if (!this.context.hasTestsFolder() && "test".equals(address)) {
            this.context.createTestFolder();
            result = Response.htmlOk("OK", Map.of("HX-Redirect", "/test"));
        } else {
            result = new Response(NOT_FOUND, "", PLAIN_TEXT);
        }
        return result;
    }

    private Response httpGetRouter(final Request request) {
        final Response result;
        if (this.context.isEmpty()) {
            result = this.pages.uninitializedPage();
        } else {
            final String address = request.requestLine().getPathDetails().getIsolatedPath();
            if (address.isEmpty()) {
                result = this.pages.indexPage();
            } else if ("test".equals(address)) {
                if (!this.context.hasTestsFolder()) {
                    result = this.pages.noTestsFolder();
                } else {
                    result = this.pages.testPage();
                }
            } else if ("state".equals(address)) {
                result = this.pages.statePage(this.context);
            } else {
                result = new Response(NOT_FOUND, "", PLAIN_TEXT);
            }
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
