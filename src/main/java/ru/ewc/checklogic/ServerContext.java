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

import java.util.List;
import java.util.Map;

/**
 * I am the context for the server. I provide the server with the necessary information to perform
 * the commands, to store the state of the system and to make decisions based on the state.
 *
 * @since 0.3.2
 */
public interface ServerContext {
    void perform(String command);

    void perform(String command, Map<String, String> args);

    Map<String, String> stateFor(String table, Map<String, String> entities);

    Map<String, Map<String, Object>> storedState();

    String valueFor(String locator, String fragment);

    Map<String, List<String>> commandData();

    boolean isAvailable(String command, String field);

    void update(List<String> values);

    String cached(String parameter);

    void cache(String parameter, String value);

    void putLocators(Map<String, Map<String, Object>> raw);
}
