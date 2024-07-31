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
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Collectors;
import ru.ewc.checklogic.FullServerContext;

/**
 * I am the configuration and logic for the context web page.
 *
 * @since 0.3.1
 */
public final class ContextPage implements Endpoints {
    /**
     * The server context.
     */
    private final FullServerContext context;

    public ContextPage(final FullServerContext context) {
        this.context = context;
    }

    @Override
    public void register(final WebFramework web) {
        web.registerPath(POST, "context", this::contextPage);
    }

    private Response contextPage(final Request request) {
        this.context.cache("available", request.body().asString("availOutcome"));
        this.context.cache("request", request.body().asString("reqLocator"));
        this.updateContext(request.body().asString("reqValues"));
        final CommandMetadata commands = new CommandMetadata(this.context.commandData());
        return Response.htmlOk(commands.namesAsHtmlList(this.context, this.availabilityField()));
    }

    private String availabilityField() {
        return this.context.cached("available");
    }

    private void updateContext(final String values) {
        final String decoded = URLDecoder.decode(values, StandardCharsets.UTF_8);
        this.context.update(Arrays.stream(decoded.split("\n")).collect(Collectors.toList()));
    }
}
