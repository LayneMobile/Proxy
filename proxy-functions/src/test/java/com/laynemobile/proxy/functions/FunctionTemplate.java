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

public class FunctionTemplate extends AbstractFunctionTemplate {
    private static final String FILE_NAME_TEMPLATE = "Func${LENGTH}Transform";

    public FunctionTemplate(String packageName, int length) {
        super(packageName, fileName(length), FUNCTION_TEMPLATE, length);
    }

    private static String fileName(int length) {
        return new CodeTemplate(FILE_NAME_TEMPLATE)
                .fill("LENGTH", length)
                .output();
    }

    private static final String ACTION_TEMPLATE = "" +
            "\n" +
            "package ${PACKAGE_NAME};\n" +
            "\n" +
            "import com.laynemobile.proxy.functions.Action0;\n" +
            "import com.laynemobile.proxy.functions.Action${LENGTH};\n" +
            "import com.laynemobile.proxy.functions.Actions;\n" +
            "\n" +
            "public class ${FILE_NAME}<${TYPE_ARGS}>\n" +
            "        extends ActionTransform<Action${LENGTH}<${WILDCARD_TYPE_ARGS}>>\n" +
            "        implements Action${LENGTH}<${TYPE_ARGS}> {\n" +
            "\n" +
            "    public Action${LENGTH}Transform() {\n" +
            "        super(Actions.empty());\n" +
            "    }\n" +
            "\n" +
            "    public Action${LENGTH}Transform(Action${LENGTH}<${WILDCARD_TYPE_ARGS}> action) {\n" +
            "        super(action);\n" +
            "    }\n" +
            "\n" +
            "    public Action${LENGTH}Transform(Action${LENGTH}Transform<${WILDCARD_TYPE_ARGS}> action) {\n" +
            "        super(action.function);\n" +
            "    }\n" +
            "\n" +
            "    public Action${LENGTH}Transform(final Action0 action) {\n" +
            "        super(new Action${LENGTH}<${TYPE_ARGS}>() {\n" +
            "            @Override public void call(${FUNCTION_PARAMETERS}) {\n" +
            "                action.call();\n" +
            "            }\n" +
            "        });\n" +
            "    }\n" +
            "\n" +
            "    @SuppressWarnings(\"unchecked\")\n" +
            "    @Override protected final void invoke(Object... args) {\n" +
            "        if (args.length != ${LENGTH}) {\n" +
            "            throw new RuntimeException(\"Action${LENGTH} expecting ${LENGTH} arguments.\");\n" +
            "        }\n" +
            "        function.call(${CAST_FUNCTION_ARGS});\n" +
            "    }\n" +
            "\n" +
            "    @Override public final void call(${FUNCTION_PARAMETERS}) {\n" +
            "        function.call(${FUNCTION_ARGS});\n" +
            "    }\n" +
            "}";

    private static final String FUNCTION_TEMPLATE = "" +
            "\n" +
            "package ${PACKAGE_NAME};\n" +
            "\n" +
            "import com.laynemobile.proxy.functions.Func0;\n" +
            "import com.laynemobile.proxy.functions.Func${LENGTH};\n" +
            "\n" +
            "public class Func${LENGTH}Transform<${TYPE_ARGS}, R>\n" +
            "        extends FunctionTransform<Func${LENGTH}<${WILDCARD_TYPE_ARGS}, ? extends R>, R>\n" +
            "        implements Func${LENGTH}<${TYPE_ARGS}, R> {\n" +
            "\n" +
            "    public Func${LENGTH}Transform(Func${LENGTH}<${WILDCARD_TYPE_ARGS}, ? extends R> function) {\n" +
            "        super(function);\n" +
            "    }\n" +
            "\n" +
            "    public Func${LENGTH}Transform(final Func0<? extends R> function) {\n" +
            "        super(new Func${LENGTH}<${TYPE_ARGS}, R>() {\n" +
            "            @Override public R call(${FUNCTION_PARAMETERS}) {\n" +
            "                return function.call();\n" +
            "            }\n" +
            "        });\n" +
            "    }\n" +
            "\n" +
            "    public Func${LENGTH}Transform(final R value) {\n" +
            "        super(new Func${LENGTH}<${TYPE_ARGS}, R>() {\n" +
            "            @Override public R call(${FUNCTION_PARAMETERS}) {\n" +
            "                return value;\n" +
            "            }\n" +
            "        });\n" +
            "    }\n" +
            "\n" +
            "    @Override public final R call(${FUNCTION_PARAMETERS}) {\n" +
            "        return function.call(${FUNCTION_ARGS});\n" +
            "    }\n" +
            "\n" +
            "    @SuppressWarnings(\"unchecked\")\n" +
            "    @Override public final R call(Object... args) {\n" +
            "        return function.call(${CAST_FUNCTION_ARGS});\n" +
            "    }\n" +
            "}";
}