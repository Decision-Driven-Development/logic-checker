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
import java.util.Map;
import java.util.stream.Collectors;
import ru.ewc.decisions.api.ComputationContext;
import ru.ewc.decisions.api.OutputTracker;
import ru.ewc.decisions.api.RuleFragment;
import ru.ewc.decisions.api.RuleFragments;
import ru.ewc.decisions.commands.Assignment;
import ru.ewc.decisions.conditions.Condition;

public class CheckRuleFragments {
    private final List<RuleFragments> rules;

    public CheckRuleFragments(List<RuleFragments> rules) {
        this.rules = rules;
    }

    public Map<String, List<CheckFailure>> perform(ComputationContext context) {
        return this.rules.stream()
            .collect(Collectors.toMap(
                RuleFragments::header,
                rule -> CheckRuleFragments.performAndLog(context, rule)
            ));
    }

    private static List<CheckFailure> performAndLog(final ComputationContext ctx, final RuleFragments rule) {
        logCheckpoint(ctx, "%s - started".formatted(rule.header()));
        List<CheckFailure> failures = new ArrayList<>(1);
        for (RuleFragment fragment : rule.getFragments()) {
            if (fragment.nonEmptyOfType("CND")) {
                final Condition check = Condition.from(fragment);
                if (!check.evaluate(ctx)) {
                    failures.add(new CheckFailure(check.asString(), check.result()));
                }
            } else {
                CheckRuleFragments.perform(fragment, ctx);
            }
        }
        logCheckpoint(ctx, "%s - %s".formatted(rule.header(), CheckRuleFragments.desc(failures)));
        return failures;
    }

    private static void perform(RuleFragment fragment, ComputationContext ctx) {
        switch (fragment.type()) {
            case "ASG" -> new Assignment(fragment.left(), fragment.right()).performIn(ctx);
            case "OUT" -> {
                if ("execute".equals(fragment.right())) {
                    ctx.perform(fragment.left());
                }
            }
            case "HDR" -> {
                // do nothing
            }
            default ->
                throw new IllegalArgumentException("Unknown fragment type: %s".formatted(fragment.type()));
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
}
