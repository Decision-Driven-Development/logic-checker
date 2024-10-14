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

package ru.ewc.checklogic.testing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import ru.ewc.decisions.api.ComputationContext;
import ru.ewc.decisions.input.ContentsReader;

/**
 * I represent a collection of test-cases to performChecks. My main responsibility is to run all
 * the tests and collect the results.
 *
 * @since 0.4.1
 */
@SuppressWarnings("PMD.ProhibitPublicStaticMethods")
public final class CheckSuite {
    /**
     * Collection of test files, each containing multiple tests and outcomes.
     */
    private final Collection<CheckFile> tests;

    /**
     * Collection of test results.
     */
    private final List<TestResult> results;

    /**
     * The root directory of the business logic source files.
     */
    private final String root;

    /**
     * Milliseconds elapsed to read all the test files.
     */
    private final long reading;

    private CheckSuite(final Collection<CheckFile> tests, final String root, final long reading) {
        this.tests = tests;
        this.results = new ArrayList<>(tests.size());
        this.root = root;
        this.reading = reading;
    }

    public static CheckSuite using(
        final ContentsReader reader,
        final String root,
        final String request
    ) {
        final long start = System.currentTimeMillis();
        final List<CheckFile> files = reader.readAll().stream()
            .map(sl -> new CheckFile(sl.fileName(), sl.specifiedRulesFragments(), request))
            .toList();
        final long elapsed = System.currentTimeMillis() - start;
        return new CheckSuite(files, root, elapsed);
    }

    public String statsAsHtmlDiv(final double elapsed) {
        return """
            %d test(s) performed in %.3f second(s) (read time: %.3f), %d passed, %d failed</br>
            %d milliseconds spent creating context, %d milliseconds spent in the tests
            """.formatted(
            this.results.size(),
            elapsed,
            this.reading / 1000.0,
            this.results.stream().filter(TestResult::successful).count(),
            this.results.stream().filter(result -> !result.successful()).count(),
            this.results.stream().reduce(0L, (acc, result) -> acc + result.context(), Long::sum),
            this.results.stream().reduce(0L, (acc, result) -> acc + result.elapsed(), Long::sum)
        );
    }

    public CheckSuite perform() {
        this.results.addAll(this.tests.stream()
            .map(test -> test.performChecks(this.root, this))
            .flatMap(List::stream)
            .toList()
        );
        return this;
    }

    public void findAndPerform(final String file, final ComputationContext ctx) {
        this.tests.stream().filter(test -> file.equals(test.getFile()))
            .findFirst()
            .ifPresent(test -> test.performInSameContext(ctx, this));
    }

    public List<String> checkNames() {
        return this.tests.stream().map(CheckFile::getFile).toList();
    }

    public String resultAsHtmlRows() {
        return this.results.stream()
            .sorted(Comparator.naturalOrder())
            .map(TestResult::asHtmlTableRow)
            .collect(Collectors.joining());
    }
}
