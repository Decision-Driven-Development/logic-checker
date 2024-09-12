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
import java.util.List;
import java.util.stream.Collectors;
import ru.ewc.decisions.api.ComputationContext;
import ru.ewc.decisions.api.OutputTracker;
import ru.ewc.decisions.api.RuleFragment;
import ru.ewc.decisions.api.RuleFragments;
import ru.ewc.decisions.commands.Assignment;
import ru.ewc.decisions.conditions.Condition;

/**
 * I am a single file containing one or more tests. I am responsible for performing all the tests
 * and logging the results.
 *
 * @since 0.4.1
 */
public final class CheckFile {
    /**
     * The collection of single tests inside the file.
     */
    private final List<RuleFragments> tests;

    public CheckFile(final List<RuleFragments> tests) {
        this.tests = tests;
    }

    public List<TestResult> performChecks(final ComputationContext context, final String locator) {
        return this.tests.stream()
            .map(rule -> CheckFile.performAndLog(context, rule, locator))
            .toList();
    }

    private static TestResult performAndLog(
        final ComputationContext ctx,
        final RuleFragments rule,
        final String locator
    ) {
        logCheckpoint(ctx, "%s - started".formatted(rule.header()));
        final OutputTracker<String> tracker = ctx.startTracking();
        final List<CheckFailure> failures = new ArrayList<>(1);
        for (final RuleFragment fragment : rule.getFragments()) {
            if (fragment.nonEmptyOfType("CND")) {
                final Condition check = Condition.from(fragment);
                if (!check.evaluate(ctx)) {
                    failures.add(new CheckFailure(check.asString(), check.result()));
                }
            } else {
                CheckFile.perform(fragment, ctx, locator);
            }
        }
        logCheckpoint(ctx, "%s - %s".formatted(rule.header(), CheckFile.desc(failures)));
        return new TestResult(
            rule.header(),
            failures.isEmpty(),
            resultAsUnorderedList(failures),
            tracker.events()
        );
    }

    private static void perform(
        final RuleFragment fragment,
        final ComputationContext ctx,
        final String locator
    ) {
        switch (fragment.type()) {
            case "ASG" -> new Assignment(fragment.left(), fragment.right()).performIn(ctx);
            case "OUT" -> {
                if ("execute".equals(fragment.left())) {
                    ctx.perform(fragment.right());
                    ctx.resetComputationState(locator);
                }
            }
            default -> {
            }
        }
    }

    private static String desc(final List<CheckFailure> messages) {
        final String result;
        if (messages.isEmpty()) {
            result = "PASSED";
        } else {
            result = "FAILED";
        }
        return result;
    }

    private static void logCheckpoint(final ComputationContext context, final String formatted) {
        context.logComputation(OutputTracker.EventType.CH, formatted);
    }

    private static String resultAsUnorderedList(final List<CheckFailure> failures) {
        return "<ul>%s</ul>".formatted(String.join("", checkFailureAsHtml(failures)));
    }

    private static String checkFailureAsHtml(final List<CheckFailure> failures) {
        return failures.stream()
            .map(CheckFile::formattedDescriptionFor)
            .collect(Collectors.joining());
    }

    private static String formattedDescriptionFor(final CheckFailure failure) {
        return "<li>Expected: <kbd>%s</kbd>, but got: <kbd>%s</kbd></li>"
            .formatted(failure.expectation(), failure.actual());
    }
}
