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
import lombok.Getter;
import ru.ewc.checklogic.ServerContextFactory;
import ru.ewc.decisions.api.ComputationContext;
import ru.ewc.decisions.api.DecitaException;
import ru.ewc.decisions.api.OutputTracker;
import ru.ewc.decisions.api.RuleFragment;
import ru.ewc.decisions.api.RuleFragments;
import ru.ewc.decisions.commands.Assignment;
import ru.ewc.decisions.conditions.Condition;
import ru.ewc.decisions.core.Coordinate;

/**
 * I am a single file containing one or more tests. I am responsible for performing all the tests
 * and logging the results.
 *
 * @since 0.4.1
 */
public final class CheckFile {
    /**
     * The file containing the tests.
     */
    @Getter
    private final String file;

    /**
     * The collection of single tests inside the file.
     */
    private final List<RuleFragments> tests;

    /**
     * The name of the request locator.
     */
    private final String request;

    /**
     * The link to the suite that contains this file.
     */
    private CheckSuite suite;

    public CheckFile(final String file, final List<RuleFragments> tests, final String request) {
        this.file = file;
        this.tests = tests;
        this.request = request;
    }

    public List<TestResult> performChecks(final String root, final CheckSuite files) {
        this.suite = files;
        return this.tests.stream()
            .filter(rule -> rule.getFragments().stream().anyMatch(f -> f.nonEmptyOfType("CND")))
            .map(
                rule -> {
                    final long start = System.currentTimeMillis();
                    final ComputationContext context = ServerContextFactory.create(root).context();
                    final long elapsed = System.currentTimeMillis() - start;
                    return this.getTestResult(rule, context, elapsed);
                }
            ).toList();
    }

    public void performInSameContext(final ComputationContext ctx, final CheckSuite files) {
        this.suite = files;
        this.getTestResult(this.tests.getFirst(), ctx, 0);
    }

    private TestResult getTestResult(
        final RuleFragments rule,
        final ComputationContext ctx,
        final long time
    ) {
        logCheckpoint(ctx, "%s - started".formatted(rule.header()));
        final long start = System.currentTimeMillis();
        final OutputTracker<String> tracker = ctx.startTracking();
        final List<CheckFailure> failures = new ArrayList<>(1);
        for (final RuleFragment fragment : rule.getFragments()) {
            if (fragment.nonEmptyOfType("CND")) {
                final Condition check = Condition.from(fragment);
                try {
                    if (!check.evaluate(ctx)) {
                        failures.add(new CheckFailure(check.asString(), check.result()));
                    }
                } catch (final DecitaException | IllegalArgumentException exception) {
                    failures.add(
                        new CheckFailure(
                            check.asString(),
                            "Exception: %s".formatted(exception.getMessage())
                        )
                    );
                }
            } else {
                try {
                    this.perform(fragment, ctx);
                } catch (final DecitaException | IllegalArgumentException exception) {
                    failures.add(new CheckFailure("", exception.getMessage()));
                }
            }
        }
        logCheckpoint(ctx, "%s - %s".formatted(rule.header(), CheckFile.desc(failures)));
        final long elapsed = System.currentTimeMillis() - start;
        return new TestResult(
            rule.header().replace("::", " - "),
            failures.isEmpty(),
            resultAsUnorderedList(failures),
            tracker.events(),
            time,
            elapsed
        );
    }

    private void perform(final RuleFragment fragment, final ComputationContext ctx) {
        switch (fragment.type()) {
            case "ASG" -> new Assignment(fragment.left(), fragment.right()).performIn(ctx);
            case "EXE" -> {
                if ("command".equals(fragment.left())) {
                    final Coordinate coordinate = Coordinate.from(fragment.right());
                    coordinate.resolveIn(ctx);
                    ctx.perform(coordinate.valueIn(ctx));
                    ctx.resetComputationState(this.request);
                }
                if ("include".equals(fragment.left())) {
                    this.suite.findAndPerform(fragment.right(), ctx);
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
        return "<li>Expected: <kbd>%s</kbd>, but got: <kbd>%s</kbd></li>".formatted(
            CheckFile.clearConstants(failure.expectation()),
            CheckFile.clearConstants(failure.actual().split("=")[0])
        );
    }

    private static String clearConstants(final String expectation) {
        return expectation.replaceAll("constant::", "");
    }
}
