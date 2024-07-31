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
import java.util.HashMap;
import java.util.Map;

/**
 * I am a collection of template processors that render the pages to be served based on template
 * files packed into 'resources' folder inside jar-file.
 *
 * @since 0.3.2
 */
public final class ResourceTemplateProcessors implements TemplateProcessors {
    /**
     * The map of template processors for the templates. Used to lazy load the processors, because
     * they are expensive to create.
     */
    private final Map<String, TemplateProcessor> processors;

    public ResourceTemplateProcessors() {
        this.processors = new HashMap<>();
    }

    @Override
    public TemplateProcessor forTemplate(final String template) {
        this.processors.putIfAbsent(
            template,
            TemplateProcessor.buildProcessor(WebResource.readFileFromResources(template))
        );
        return this.processors.get(template);
    }
}
