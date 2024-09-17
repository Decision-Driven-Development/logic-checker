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
import com.renomad.minum.web.RequestLine;
import com.renomad.minum.web.Response;
import com.renomad.minum.web.WebFramework;
import java.net.URI;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Collectors;
import ru.ewc.checklogic.ServerConfiguration;
import ru.ewc.checklogic.ServerInstance;
import ru.ewc.checklogic.testing.CheckSuite;
import ru.ewc.decisions.input.CombinedCsvFileReader;

/**
 * I am a class providing access to the state page.
 *
 * @since 0.4.1
 */
public final class StatePage implements Endpoints {
    /**
     * The context that stores the current state of the system.
     */
    private final ServerInstance context;

    /**
     * The server configuration.
     */
    private final ServerConfiguration config;

    /**
     * The template renderer, creating pages to be served.
     */
    private final ResourceTemplateRender processors;

    public StatePage(
        final ServerInstance context,
        final ServerConfiguration config,
        final ResourceTemplateRender processors
    ) {
        this.context = context;
        this.config = config;
        this.processors = processors;
    }

    @Override
    public void register(final WebFramework web) {
        web.registerPath(GET, "state", this::statePage);
        web.registerPath(POST, "state", this::createState);
        web.registerPath(DELETE, "state", this::resetState);
    }

    private Response statePage(final Request request) {
        assert request.requestLine().getMethod().equals(RequestLine.Method.GET);
        final StoredState stored = new StoredState(this.context.storedState());
        return Response.htmlOk(
            this.processors.renderInLayout(
                "templates/state.html",
                Map.of(
                    "state", stored.asHtmlList(),
                    "includes", this.listOfIncludes()
                )
            )
        );
    }

    private Response createState(final Request request) {
        assert request.requestLine().getMethod().equals(RequestLine.Method.POST);
        final String include = request.body().asString("include");
        this.context.createState(include, this.testSuite());
        return Response.htmlOk("OK", Map.of("HX-Redirect", "/state"));
    }

    private Response resetState(final Request request) {
        assert request.requestLine().getMethod().equals(RequestLine.Method.DELETE);
        this.context.initialize();
        return Response.htmlOk("OK", Map.of("HX-Redirect", "/state"));
    }

    private CheckSuite testSuite() {
        return CheckSuite.using(
            new CombinedCsvFileReader(this.testsFolder(), ".csv", ";"),
            this.context.getRoot(),
            this.config.requestLocatorName()
        );
    }

    private URI testsFolder() {
        return Path.of(this.context.getRoot(), "tests").toUri();
    }

    private String listOfIncludes() {
        return this.testSuite().checkNames().stream()
            .sorted()
            .map(name -> "<option value=\"%s\">%s</option>".formatted(name, name))
            .collect(Collectors.joining());
    }
}
