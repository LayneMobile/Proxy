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

public class ActionDefTemplate extends AbstractFunctionDefTemplate {
    private static final String FILE_NAME_TEMPLATE = "Action${LENGTH}Def";

    public ActionDefTemplate(int length) {
        super(fileName(length), TEMPLATE, length);
    }

    private static String fileName(int length) {
        return new CodeTemplate(FILE_NAME_TEMPLATE)
                .fill("LENGTH", length)
                .output();
    }

    private static final String TEMPLATE = "" +
            "/*\n" +
            " * Copyright 2016 Layne Mobile, LLC\n" +
            " *\n" +
            " * Licensed under the Apache License, Version 2.0 (the \"License\");\n" +
            " * you may not use this file except in compliance with the License.\n" +
            " * You may obtain a copy of the License at\n" +
            " *\n" +
            " *     http://www.apache.org/licenses/LICENSE-2.0\n" +
            " *\n" +
            " * Unless required by applicable law or agreed to in writing, software\n" +
            " * distributed under the License is distributed on an \"AS IS\" BASIS,\n" +
            " * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
            " * See the License for the specific language governing permissions and\n" +
            " * limitations under the License.\n" +
            " */\n" +
            "\n" +
            "package ${PACKAGE_NAME};\n" +
            "\n" +
            "import com.laynemobile.proxy.TypeToken;\n" +
            "import com.laynemobile.proxy.functions.transforms.Action${LENGTH}Transform;\n" +
            "\n" +
            "public class ${FILE_NAME}<${TYPE_ARGS}> extends ActionDef<Action${LENGTH}Transform<${TYPE_ARGS}>> {\n" +
            "    public Action${LENGTH}Def(String name, ${TYPE_TOKEN_PARAMETERS}) {\n" +
            "        super(name, new TypeToken<?>[]{${FUNCTION_ARGS}});\n" +
            "    }\n" +
            "\n" +
            "    @Override public Action<${TYPE_ARGS}> asFunction(Action${LENGTH}Transform<${TYPE_ARGS}> transform) {\n" +
            "        return new Action<>(this, transform);\n" +
            "    }\n" +
            "\n" +
            "    public static class Action<${TYPE_ARGS}> extends ProxyAction<Action${LENGTH}Transform<${TYPE_ARGS}>> {\n" +
            "        protected Action(Action${LENGTH}Def<${TYPE_ARGS}> actionDef, Action${LENGTH}Transform<${TYPE_ARGS}> action) {\n" +
            "            super(actionDef, action);\n" +
            "        }\n" +
            "    }\n" +
            "}\n";
}
