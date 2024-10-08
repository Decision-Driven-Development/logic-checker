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

import java.util.Collection;
import java.util.List;
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
     * The root directory of the business logic source files.
     */
    private final String root;

    private CheckSuite(final Collection<CheckFile> tests, final String root) {
        this.tests = tests;
        this.root = root;
    }

    public static CheckSuite using(
        final ContentsReader reader,
        final String root,
        final String request
    ) {
        return new CheckSuite(
            reader.readAll().stream()
                .map(sl -> new CheckFile(sl.fileName(), sl.specifiedRulesFragments(), request))
                .toList(),
            root
        );
    }

    public List<TestResult> perform() {
        return this.tests.stream()
            .map(test -> test.performChecks(this.root, this))
            .flatMap(List::stream)
            .toList();
    }

    public void findAndPerform(final String file, final ComputationContext ctx) {
        this.tests.stream().filter(test -> file.equals(test.getFile()))
            .findFirst()
            .ifPresent(test -> test.performInSameContext(ctx, this));
    }

    public List<String> checkNames() {
        return this.tests.stream().map(CheckFile::getFile).toList();
    }
}
