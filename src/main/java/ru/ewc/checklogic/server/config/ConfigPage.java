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
package ru.ewc.checklogic.server.config;

import com.renomad.minum.templating.TemplateProcessor;
import com.renomad.minum.web.Request;
import com.renomad.minum.web.Response;
import com.renomad.minum.web.WebFramework;
import java.util.Map;
import java.util.Set;
import ru.ewc.checklogic.ServerConfiguration;
import ru.ewc.checklogic.server.Endpoints;
import ru.ewc.checklogic.server.WebResource;

/**
 * I am a page that shows the current configuration of the web server. I also allow the user to
 * change the configuration.
 *
 * @since 0.3.2
 */
public final class ConfigPage implements Endpoints {
    /**
     * The name of the configuration parameter that holds the name of the command availability
     * outcome.
     */
    private static final String COMMAND = "command";

    /**
     * The name of the configuration parameter that holds the name of the request locator.
     */
    private static final String REQUEST = "request";

    /**
     * The name of the configuration parameter that holds the delimiter for the table.
     */
    private static final String DELIMITER = "delimiter";

    /**
     * The configuration of the web server.
     */
    private final ServerConfiguration config;

    /**
     * The template processor that renders the configuration page.
     */
    private final TemplateProcessor processor;

    public ConfigPage(final ServerConfiguration config) {
        this.config = config;
        this.processor = TemplateProcessor.buildProcessor(
            WebResource.contentOf("templates/config.html")
        );
    }

    @Override
    public void register(final WebFramework web) {
        web.registerPath(GET, "config", this::render);
        web.registerPath(POST, "config", this::update);
    }

    void updateParametersFrom(final Request request) {
        final Set<String> keys = request.body().getKeys();
        if (keys.contains("availOutcome") && request.body().asBytes("availOutcome").length > 0) {
            this.config.setParameterValue(ConfigPage.COMMAND, fieldFrom(request, "availOutcome"));
        }
        if (keys.contains("reqLocator") && request.body().asBytes("reqLocator").length > 0) {
            this.config.setParameterValue(ConfigPage.REQUEST, fieldFrom(request, "reqLocator"));
        }
        if (keys.contains("delimiter") && request.body().asBytes("delimiter").length > 0) {
            this.config.setParameterValue(ConfigPage.DELIMITER, fieldFrom(request, "delimiter"));
        }
    }

    Response render(final Request request) {
        return Response.htmlOk(this.processor.renderTemplate(this.parameters()));
    }

    Map<String, String> parameters() {
        return Map.of(
            "commandAvailabilityOutcome", this.config.getParameterValue(ConfigPage.COMMAND),
            "requestLocatorName", this.config.getParameterValue(ConfigPage.REQUEST),
            "tableDelimiter", this.config.getParameterValue(ConfigPage.DELIMITER)
        );
    }

    private Response update(final Request request) {
        this.updateParametersFrom(request);
        return Response.htmlOk(this.processor.renderTemplate(this.parameters()));
    }

    private static String fieldFrom(final Request request, final String field) {
        return request.body().asString(field);
    }
}
