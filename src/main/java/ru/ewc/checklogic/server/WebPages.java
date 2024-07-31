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

import com.renomad.minum.web.Response;
import java.util.Map;

/**
 * I am a collection of template processors that render the pages to be served.
 *
 * @since 0.3.2
 */
public final class WebPages {
    /**
     * The template processors to be used for rendering the pages.
     */
    private final TemplateProcessors processors;

    public WebPages(final TemplateProcessors processors) {
        this.processors = processors;
    }

    public Response indexPage() {
        return Response.htmlOk(this.templateNamed("templates/index.html", Map.of()));
    }

    public Response uninitializedPage() {
        return Response.htmlOk(this.templateNamed("templates/uninitialized.html", Map.of()));
    }

    private String templateNamed(final String template, final Map<String, String> values) {
        return this.processors.forTemplate(template).renderTemplate(values);
    }
}
