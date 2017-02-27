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
package com.github.jonathanxd.buildergenerator.test;

import com.github.jonathanxd.buildergenerator.util.CharArrayUtil;
import com.github.jonathanxd.codeapi.type.GenericType;
import com.github.jonathanxd.iutils.string.ToStringHelper;

import org.junit.Test;

import java.util.Arrays;
import java.util.stream.Collectors;

public class GenericTest {

    @Test
    public void containsTest() {
        //          0123456789
        String t = "toasted potato";

        char[] chars = t.toCharArray();

        boolean eq = CharArrayUtil.contains(chars, 4, "ted potato");

        System.out.println(eq);
    }

    private String genericToString(GenericType genericType) {
        return ToStringHelper.helper(genericType.getClass().getCanonicalName(), ", ", "{", "}")
                .add("name", genericType.getName())
                .add("bounds", Arrays.stream(genericType.getBounds()).map(this::boundToString).collect(Collectors.toList()))
                .add("isType", genericType.isType())
                .add("codeType", genericType.getCodeType())
                .toString();
    }

    private String boundsToString(GenericType.Bound[] bounds) {
        return Arrays.stream(bounds).map(this::boundToString).collect(Collectors.joining(", ", "[", "]"));
    }

    private String boundToString(GenericType.Bound bound) {
        return ToStringHelper.helper(bound.getClass().getCanonicalName(), ", ", "{", "}")
                .add("sign", bound.getSign())
                .add("type", bound.getType().toString())
                .toString();

    }

}
