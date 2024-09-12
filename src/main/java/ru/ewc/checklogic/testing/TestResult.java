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

import java.util.List;

public record TestResult(
    String file,
    boolean successful,
    String error,
    List<String> log
) implements Comparable<TestResult> {

    public String result() {
        final String result;
        if (this.successful) {
            result = "PASS";
        } else {
            result = "FAIL";
        }
        return result;
    }

    public String asHtmlTableRow() {
        return String.format(
            "<tr class=\"%s\"><td>%s</td><td>%s</td><td>%s</td></tr>",
            this.classBasedOnSuccess(),
            this.file,
            this.result(),
            this.errorMessages()
        );
    }

    private String errorMessages() {
        final String result;
        if (this.successful) {
            result = "";
        } else {
            result = this.error.replace("\n\n", "<br><br>").replace("\n\t", " ");
        }
        return result;
    }

    private String classBasedOnSuccess() {
        final String result;
        if (this.successful) {
            result = "table-success";
        } else {
            result = "table-danger";
        }
        return result;
    }

    @Override
    public int compareTo(final TestResult other) {
        int result = Boolean.compare(this.successful, other.successful);
        if (result == 0) {
            result = this.file.compareTo(other.file);
        }
        return result;
    }
}
