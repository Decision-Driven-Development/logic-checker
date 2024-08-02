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

package ru.ewc.checklogic;

import com.renomad.minum.web.FullSystem;
import com.renomad.minum.web.WebFramework;
import ru.ewc.checklogic.server.AllEndpoints;
import ru.ewc.checklogic.server.CommandPage;
import ru.ewc.checklogic.server.ContextPage;
import ru.ewc.checklogic.server.Endpoints;

/**
 * I am an entry point for the logic checker web-based application. My main responsibility is to
 * create an instance of the web framework, register the endpoints that will be served and start
 * listening to incoming HTTP requests.
 *
 * @since 0.1
 */
@SuppressWarnings("PMD.ProhibitPublicStaticMethods")
public final class LogicChecker {
    private LogicChecker() {
        // Utility class
    }

    public static void main(final String[] args) {
        if (args.length != 1) {
            throw new IllegalArgumentException("Please provide the path to the resources");
        }
        final String root = args[0];
        final FullServerContext context = new ServerContextFactory(root).initialState();
        final FullSystem minum = FullSystem.initialize();
        final WebFramework web = minum.getWebFramework();
        registerEndpoints(web, new CommandPage(context));
        registerEndpoints(web, new ContextPage(context));
        registerEndpoints(web, new AllEndpoints(context));
        minum.block();
    }

    private static void registerEndpoints(final WebFramework web, final Endpoints endpoints) {
        endpoints.register(web);
    }
}
