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

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import org.yaml.snakeyaml.Yaml;

/**
 * I am a utility class that provides methods for file operations.
 *
 * @since 0.3.0
 */
@SuppressWarnings("PMD.ProhibitPublicStaticMethods")
public final class FileUtils {
    /**
     * Primary hidden constructor.
     */
    private FileUtils() {
        // Utility class
    }

    @SneakyThrows
    public static Stream<TestData> readFileNames(final String root) {
        return Files.walk(Path.of(root, "states"))
            .filter(Files::isRegularFile)
            .map(
                path -> path.toFile().getAbsolutePath()
            ).map(
                path -> {
                    final InputStream stream;
                    try {
                        stream = Files.newInputStream(Paths.get(path));
                    } catch (final IOException exception) {
                        throw new IllegalStateException(exception);
                    }
                    return FileUtils.createTestData(path, stream);
                });
    }

    @SuppressWarnings("unchecked")
    private static TestData createTestData(
        final String path,
        final InputStream stream
    ) {
        final Iterator<Object> iterator = new Yaml().loadAll(stream).iterator();
        iterator.next();
        final String command;
        if (iterator.hasNext()) {
            command = FileUtils.extractCommand(iterator.next());
        } else {
            command = "";
        }
        Map<String, Map<String, String>> expectations = new HashMap<>();
        if (iterator.hasNext()) {
            expectations = (Map<String, Map<String, String>>) iterator.next();
        }
        return new TestData(path, command, expectations);
    }

    @SuppressWarnings("unchecked")
    private static String extractCommand(final Object next) {
        final Map<String, String> map = (Map<String, String>) next;
        final String command;
        if (map == null) {
            command = "";
        } else {
            command = map.getOrDefault("command", "");
        }
        return command;
    }
}
