/*
 *      BuilderGenerator - Builder implementation generator <https://github.com/JonathanxD/BuilderGenerator>
 *
 *         The MIT License (MIT)
 *
 *      Copyright (c) 2018 JonathanxD
 *      Copyright (c) contributors
 *
 *
 *      Permission is hereby granted, free of charge, to any person obtaining a copy
 *      of this software and associated documentation files (the "Software"), to deal
 *      in the Software without restriction, including without limitation the rights
 *      to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *      copies of the Software, and to permit persons to whom the Software is
 *      furnished to do so, subject to the following conditions:
 *
 *      The above copyright notice and this permission notice shall be included in
 *      all copies or substantial portions of the Software.
 *
 *      THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *      IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *      FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *      AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *      LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *      OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *      THE SOFTWARE.
 */
package com.github.jonathanxd.buildergenerator.apt;

import java.util.Map;

public final class Options {

    private static final String PATH = "jonathanxd.buildergenerator";

    /**
     * Disables the strict verification of builder setter methods ({@code with} methods).
     *
     * Annotation processor will continue verifying parameter number in setter methods.
     */
    private static boolean DISABLE_STRICT_SETTER_CHECK;

    /**
     * Throw exceptions instead of only logging them.
     */
    private static boolean THROW_EXCEPTIONS;


    private Options() {
    }

    /**
     * @see #DISABLE_STRICT_SETTER_CHECK
     */
    public static boolean isDisableStrictSetterCheck() {
        return Options.DISABLE_STRICT_SETTER_CHECK;
    }

    public static boolean isThrowExceptions() {
        return Options.THROW_EXCEPTIONS;
    }

    public static void load(Map<String, String> options) {
        Options.DISABLE_STRICT_SETTER_CHECK =
                Boolean.valueOf(options.getOrDefault(PATH + ".disableStrictSetterCheck", "false"));

        Options.THROW_EXCEPTIONS =
                Boolean.valueOf(options.getOrDefault(PATH + ".throwExceptions", "false"));
    }

}
