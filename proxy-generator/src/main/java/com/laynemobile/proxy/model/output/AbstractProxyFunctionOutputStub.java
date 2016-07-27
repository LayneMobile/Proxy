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

package com.laynemobile.proxy.model.output;

import com.laynemobile.proxy.Util;
import com.laynemobile.proxy.elements.TypeParameterElementAlias;
import com.laynemobile.proxy.model.ProxyElement;
import com.laynemobile.proxy.model.ProxyFunctionElement;
import com.laynemobile.proxy.types.AliasTypes;
import com.laynemobile.proxy.types.TypeMirrorAlias;
import com.laynemobile.proxy.types.TypeVariableAlias;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import java.io.IOException;
import java.util.List;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;

import sourcerer.processor.Env;

public class AbstractProxyFunctionOutputStub extends AbstractTypeElementOutputStub {
    private static final String ABSTRACT_PREFIX = "Abstract";

    private final ProxyElement parent;
    private final ProxyFunctionElement function;
    private final ExecutableElement element;
    private final String basePackageName;
    private final String baseClassName;
    private final TypeMirror superClass;

    private AbstractProxyFunctionOutputStub(ProxyElement parent, ProxyFunctionElement function, String baseClassName) {
        this(parent, function, parent.packageName(), baseClassName);
    }

    private AbstractProxyFunctionOutputStub(ProxyElement parent, ProxyFunctionElement function, String basePackageName,
            String baseClassName) {
        super(basePackageName + ".generated", ABSTRACT_PREFIX + baseClassName);
        this.parent = parent;
        this.function = function;
        this.element = function.element();
        this.basePackageName = basePackageName;
        this.baseClassName = baseClassName;
        this.superClass = function.abstractProxyFunctionType();
    }

    private AbstractProxyFunctionOutputStub(AbstractProxyFunctionOutputStub source, TypeMirror superClass) {
        super(source.packageName(), source.className());
        this.parent = source.parent;
        this.function = source.function;
        this.element = source.element;
        this.basePackageName = source.basePackageName;
        this.baseClassName = source.baseClassName;
        this.superClass = superClass;
    }

    public static AbstractProxyFunctionOutputStub create(ProxyFunctionElement function) {
        ProxyElement parent = function.parent();
        ExecutableElement element = function.element();
        String parammys = "";
        for (TypeMirror paramType : function.boxedParamTypes()) {
            if (parammys.isEmpty()) {
                parammys += "__";
            } else {
                parammys += "_";
            }
            if (paramType.getKind() == TypeKind.DECLARED) {
                parammys += ((DeclaredType) paramType).asElement().getSimpleName();
            } else if (paramType.getKind() == TypeKind.TYPEVAR) {
                parammys += ((TypeVariable) paramType).asElement().getSimpleName();
            } else {
                throw new IllegalStateException("unknown param type: " + paramType);
            }
        }

        String baseClassName = parent.element().getSimpleName() + "_" + element.getSimpleName() + parammys;
        return new AbstractProxyFunctionOutputStub(parent, function, baseClassName);
    }

    public AbstractProxyFunctionOutputStub withSuperClass(TypeMirror superClass) {
        return new AbstractProxyFunctionOutputStub(this, superClass);
    }

    @Override protected TypeSpec build(TypeSpec.Builder classBuilder) {
        classBuilder = classBuilder.superclass(TypeName.get(superClass))
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
        // TODO: add annotation!
//                .addAnnotation(AnnotationSpec.builder(ProxyFunctionImplementation.class)
//                        .addMember("value", "$T.class", subclass)
//                        .build())
        ;

        List<TypeVariableAlias> typeVariables = Util.buildList(parent.element().getTypeParameters(),
                new Util.Transformer<TypeVariableAlias, TypeParameterElementAlias>() {
                    @Override
                    public TypeVariableAlias transform(TypeParameterElementAlias typeParameterElementAlias) {
                        TypeMirrorAlias type = typeParameterElementAlias.asType();
                        if (type.getKind() == TypeKind.TYPEVAR) {
                            return AliasTypes.get((TypeVariable) type.actual());
                        }
                        return null;
                    }
                });

        for (TypeVariableAlias typeVariable : typeVariables) {
            classBuilder.addTypeVariable(TypeVariableName.get(typeVariable.actual()));
        }

        String name = function.name();

        // Constructor
        classBuilder.addMethod(MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PROTECTED)
                .addParameter(TypeName.get(function.functionType()), name)
                .addStatement("super($L)", name)
                .build());

        // handler method
        ClassName NamedMethodHandler = ClassName.get(com.laynemobile.proxy.NamedMethodHandler.class);
        ClassName NamedMethodHandler_Builder = NamedMethodHandler.nestedClass("Builder");
        ClassName FunctionHandlers = ClassName.get(com.laynemobile.proxy.functions.FunctionHandlers.class);
        classBuilder.addMethod(MethodSpec.methodBuilder("handler")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(NamedMethodHandler)
                .addCode(CodeBlock.builder()
                        .add("$[")
                        .add("return new $T()\n", NamedMethodHandler_Builder)
                        .add(".setName($S)\n", element.getSimpleName())
                        .add(".setMethodHandler($T.from(function()))\n", FunctionHandlers)
                        .add(".build()")
                        .add(";\n$]")
                        .build())
                .build());
        return classBuilder.build();
    }

    @Override public TypeElementOutput writeTo(Env env) throws IOException {
        TypeElementOutput output = super.writeTo(env);
        return new FunctionParentOutput(this, output.typeSpec());
    }

    private static class FunctionParentOutput extends AbstractTypeElementOutput {
        private final AbstractProxyFunctionOutputStub source;

        private FunctionParentOutput(AbstractProxyFunctionOutputStub source, TypeSpec typeSpec) {
            super(source, typeSpec);
            this.source = source;
        }

        @Override public boolean hasOutput() {
            return true;
        }

        @Override public TypeElementOutputStub outputStub(Env env) {
            return new FunctionSubclassOutputStub(this, env);
        }
    }

    private static final class FunctionSubclassOutputStub extends AbstractTypeElementOutputStub {
        private final FunctionParentOutput parentOutput;
        private final TypeMirror superClass;

        private FunctionSubclassOutputStub(FunctionParentOutput parentOutput, Env env) {
            super(parentOutput.source.basePackageName + ".templates", parentOutput.source.baseClassName);
            this.parentOutput = parentOutput;
            this.superClass = parentOutput.element(env).asType();
        }

        @Override protected TypeSpec build(TypeSpec.Builder classBuilder) {
            classBuilder = classBuilder.superclass(TypeName.get(superClass))
                    .addModifiers(Modifier.PUBLIC);

            AbstractProxyFunctionOutputStub stub = parentOutput.source;

            List<TypeVariableAlias> typeVariables = Util.buildList(stub.parent.element().getTypeParameters(),
                    new Util.Transformer<TypeVariableAlias, TypeParameterElementAlias>() {
                        @Override
                        public TypeVariableAlias transform(TypeParameterElementAlias typeParameterElementAlias) {
                            TypeMirrorAlias type = typeParameterElementAlias.asType();
                            if (type.getKind() == TypeKind.TYPEVAR) {
                                return AliasTypes.get((TypeVariable) type.actual());
                            }
                            return null;
                        }
                    });

            for (TypeVariableAlias typeVariable : typeVariables) {
                classBuilder.addTypeVariable(TypeVariableName.get(typeVariable.actual()));
            }

            ProxyFunctionElement function = stub.function;
            String name = function.name();

            // Constructor
            classBuilder.addMethod(MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PROTECTED)
                    .addParameter(TypeName.get(function.functionType()), name)
                    .addStatement("super($L)", name)
                    .build());

            return classBuilder.build();
        }
    }
}
