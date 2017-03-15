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

import com.google.common.truth.FailureStrategy;
import com.google.testing.compile.JavaFileObjects;
import com.google.testing.compile.JavaSourcesSubjectFactory;

import com.github.jonathanxd.buildergenerator.apt.AnnotationProcessor;
import com.github.jonathanxd.iutils.collection.CollectionUtils;

import org.junit.Test;

import javax.tools.JavaFileObject;

public class SimpleTest {

    public static final JavaFileObject INTERFACE = JavaFileObjects.forResource("Person.java");

    public static final JavaFileObject IMPL = JavaFileObjects.forResource("PersonImpl.java");

    @Test
    public void test() {
        JavaSourcesSubjectFactory.javaSources()
                .getSubject(new Fail(),
                        CollectionUtils.listOf(INTERFACE, IMPL))
                .processedWith(new AnnotationProcessor())
                .compilesWithoutError();

    }


    public static class Fail extends FailureStrategy {
    }

}
