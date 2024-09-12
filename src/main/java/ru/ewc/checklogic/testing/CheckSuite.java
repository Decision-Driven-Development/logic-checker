/*
 * MIT License
 *
 * Copyright (c) 2024 Eugene Terekhov
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
 * I represent a collection of test-cases to perform. My main responsibility is to run all the tests
 * and collect the results.
 *
 * @since 0.8.0
 */
@SuppressWarnings("PMD.ProhibitPublicStaticMethods")
public final class CheckSuite {
    /**
     * Collection of test files, each containing multiple tests and outcomes.
     */
    private final Collection<CheckInstance> tests;

    private CheckSuite(final Collection<CheckInstance> tests) {
        this.tests = tests;
    }

    public static CheckSuite using(final ContentsReader reader) {
        return new CheckSuite(reader.readAll().stream()
            .map(sl -> new CheckInstance(new CheckRuleFragments(sl.specifiedRulesFragments())))
            .toList());
    }

    public List<TestResult> perform(final ComputationContext context) {
        return this.tests.stream()
            .map(test -> test.testResult(context))
            .flatMap(List::stream)
            .toList();
    }

}
