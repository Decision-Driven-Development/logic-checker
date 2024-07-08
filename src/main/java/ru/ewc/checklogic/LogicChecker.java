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

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import org.yaml.snakeyaml.Yaml;
import ru.ewc.checklogic.server.WebServer;
import ru.ewc.state.State;

/**
 * End-to-end tests based on yaml descriptions.
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
        new WebServer(ServerContext.contextForFolder(root), root).start();
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    static State stateFromAppConfig(final InputStream file) {
        final Map<String, Object> config = new Yaml().load(file);
        final Stream<String> names = ((List<String>) config.get("locators")).stream();
        return new State(
            names.collect(
                Collectors.toMap(
                    name -> name,
                    name -> new InMemoryStorage(new HashMap<>())
                )
            )
        );
    }
}
