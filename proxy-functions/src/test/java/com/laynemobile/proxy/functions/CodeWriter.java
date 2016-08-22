/*
 * Copyright 2016 Layne Mobile, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.laynemobile.proxy.functions;

import org.junit.Test;

import java.io.File;

public class CodeWriter {
    private static final String OUTPUT_DIR = "build/code";
    private static String PACKAGE_NAME = "com.laynemobile.proxy.functions.transforms";

    @Test public void write() throws Exception {
        File dir = new File(OUTPUT_DIR);
        int iterations = 9;
        for (int i = 1; i < iterations + 1; i++) {
            new ActionTransformTemplate(PACKAGE_NAME, i)
                    .fill()
                    .writeToDir(dir);
            new FuncTransformTemplate(PACKAGE_NAME, i)
                    .fill()
                    .writeToDir(dir);
            new ActionDefTemplate(i)
                    .fill()
                    .writeToDir(dir);
            new FuncDefTemplate(i)
                    .fill()
                    .writeToDir(dir);
            new ProxyFuncDefTemplate(i)
                    .fill()
                    .writeToDir(dir);
            new ProxyActionDefTemplate(i)
                    .fill()
                    .writeToDir(dir);
            new ProxyFuncTransformTemplate(PACKAGE_NAME, i)
                    .fill()
                    .writeToDir(dir);
            new ProxyActionTransformTemplate(PACKAGE_NAME, i)
                    .fill()
                    .writeToDir(dir);
        }
    }
}
