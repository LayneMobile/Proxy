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

import java.io.File;
import java.io.IOException;

public class FileTemplate extends CodeTemplate {
    final String packageName;
    final String fileName;

    public FileTemplate(String packageName, String fileName, String template) {
        super(template);
        this.packageName = packageName;
        this.fileName = fileName;
        reset();
    }

    @Override public FileTemplate reset() {
        super.reset()
                .fill("PACKAGE_NAME", packageName)
                .fill("FILE_NAME", fileName);
        return this;
    }

    public final void writeToDir(File dir) throws IOException {
        dir.mkdirs();
        writeTo(new File(dir, fileName + ".java"));
    }
}
