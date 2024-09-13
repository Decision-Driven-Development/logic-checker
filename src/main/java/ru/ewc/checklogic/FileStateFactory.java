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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.SneakyThrows;
import org.yaml.snakeyaml.Yaml;
import ru.ewc.checklogic.testing.FunctionsLocator;
import ru.ewc.decisions.api.InMemoryLocator;
import ru.ewc.decisions.api.Locator;
import ru.ewc.state.State;

/**
 * I am a factory for creating the state of the system.
 *
 * @since 0.3.2
 */
public final class FileStateFactory implements StateFactory {
    /**
     * An instance of server configuration.
     */
    private final ServerConfiguration config;

    /**
     * The collection of all the state and functions locators.
     */
    private final List<Locator> locators;

    public FileStateFactory(final ServerConfiguration config) {
        this.config = config;
        this.locators = new ArrayList<>(1);
    }

    @Override
    public State initialState() {
        this.locators.clear();
        this.loadLocatorsFromApplicationConfig();
        this.loadInMemoryRequestLocator();
        this.loadFunctionsLocator();
        return new State(this.locators);
    }

    private void loadFunctionsLocator() {
        this.locators.add(
            new FunctionsLocator(
                this.config.functionsLocatorName(),
                Paths.get(this.config.getRoot(), "functions")
            )
        );
    }

    private void loadInMemoryRequestLocator() {
        this.locators.add(
            new InMemoryLocator(this.config.requestLocatorName(), new HashMap<>())
        );
    }

    @SneakyThrows
    private InputStream applicationConfigFile() {
        final File fileconf = this.config.applicationConfig().toFile();
        if (!fileconf.exists() && fileconf.createNewFile()) {
            try (OutputStream out = Files.newOutputStream(fileconf.toPath())) {
                out.write("locators:\n  - request\n".getBytes(StandardCharsets.UTF_8));
            }
        }
        return Files.newInputStream(this.config.applicationConfig());
    }

    @SuppressWarnings("unchecked")
    private void loadLocatorsFromApplicationConfig() {
        try (InputStream file = this.applicationConfigFile()) {
            final Map<String, Object> yaml = new Yaml().load(file);
            this.locators.addAll(
                ((List<String>) yaml.get("locators")).stream()
                    .map(name -> new InMemoryLocator(name, new HashMap<>()))
                    .toList()
            );
        } catch (final IOException exception) {
            this.locators.addAll(List.of());
        }
    }
}
