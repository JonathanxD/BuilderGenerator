/*
 *      BuilderGenerator - Builder implementation generator <https://github.com/JonathanxD/BuilderGenerator>
 *
 *         The MIT License (MIT)
 *
 *      Copyright (c) 2017 JonathanxD
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
package com.github.jonathanxd.buildergenerator.util;

import java.util.Arrays;

public final class CharArrayUtil {

    private CharArrayUtil() {
        throw new IllegalStateException();
    }

    /**
     * Returns true if the {@code chars} array contains the {@code text} sequence starting in
     * position {@code start}. All characters from {@code start} to {@code start+text.length()} must
     * be equal to characters of {@code text}.
     *
     * Throws {@link IllegalArgumentException} if the searching range is invalid.
     *
     * @param chars Char array.
     * @param start Start range in char array.
     * @param text  Text to search.
     * @throws IllegalArgumentException if the searching range is invalid.
     */
    public static boolean safeContains(char[] chars, int start, String text) throws IllegalArgumentException {

        if (chars.length - start < text.length())
            throw new IllegalArgumentException("Cannot search for text '" + text + "' in the provided char array '" + Arrays.toString(chars) + "' because the searching range '" + start + "-" + (start + text.length()) + "' is bigger than char array length: '" + chars.length + "'!");

        return CharArrayUtil.contains(chars, start, text);
    }

    /**
     * Returns true if the {@code chars} array contains the {@code text} sequence starting in
     * position {@code start}. All characters from {@code start} to {@code start+text.length()} must
     * be equal to characters of {@code text}.
     *
     * @param chars Char array.
     * @param start Start range in char array.
     * @param text  Text to search.
     */
    public static boolean contains(char[] chars, int start, String text) {

        if (chars.length - start < text.length())
            return false;

        int pos = 0;

        char[] textChars = text.toCharArray();

        for (int i = start; i < start + text.length(); i++) {
            if (pos < text.length() && chars[i] != textChars[pos++])
                return false;
        }

        return pos == text.length();
    }


}
