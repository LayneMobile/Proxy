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

package com.laynemobile.proxy.model;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.laynemobile.proxy.annotations.GenerateProxyFunction;
import com.laynemobile.proxy.annotations.Generated;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import sourcerer.processor.Env;

public class FunctionElement {
    private final TypeElement typeElement;
    private final ExecutableElement element;
    private final String name;
    private final TypeMirror returnType;
    private final ImmutableList<? extends VariableElement> params;
    private final ImmutableList<TypeMirror> paramTypes;
    private final TypeElement functionElement;
    private final DeclaredType functionType;
    private final TypeElement abstractProxyFunctionElement;
    private final DeclaredType abstractProxyFunctionType;

    private FunctionElement(TypeElement typeElement, ExecutableElement element, String name, TypeMirror returnType,
            List<? extends VariableElement> params, List<TypeMirror> paramTypes, TypeElement functionElement,
            DeclaredType functionType, TypeElement abstractProxyFunctionElement,
            DeclaredType abstractProxyFunctionType) {
        this.typeElement = typeElement;
        this.element = element;
        this.name = name;
        this.returnType = returnType;
        this.params = ImmutableList.copyOf(params);
        this.paramTypes = ImmutableList.copyOf(paramTypes);
        this.functionElement = functionElement;
        this.functionType = functionType;
        this.abstractProxyFunctionElement = abstractProxyFunctionElement;
        this.abstractProxyFunctionType = abstractProxyFunctionType;
    }

    public static FunctionElement parse(TypeElement typeElement, Element element, Env env) {
        if (element.getKind() != ElementKind.METHOD) {
            return null;
        }
        ExecutableElement methodElement = (ExecutableElement) element;
        env.log(methodElement, "processing method element: %s", methodElement);
        return parse(typeElement, methodElement, env);
    }

    private static FunctionElement parse(TypeElement typeElement, ExecutableElement element, Env env) {
        GenerateProxyFunction function = element.getAnnotation(GenerateProxyFunction.class);
        String name = function == null ? "" : function.value();
        if (name.isEmpty()) {
            name = element.getSimpleName().toString();
        }
        env.log("name: %s", name);

        List<? extends VariableElement> params = element.getParameters();
        int length = params.size();
        String num;
        if (length > 9) {
            length = 0;
            num = "N";
        } else {
            num = Integer.toString(length);
        }

        TypeMirror returnType = element.getReturnType();
        String functionClass;
        TypeMirror[] paramTypes;
        env.log("returnType: %s", returnType);
        if (returnType.getKind() == TypeKind.VOID) {
            env.log("void");
            paramTypes = new TypeMirror[length];
            functionClass = "Action";
        } else {
            env.log("not void");
            paramTypes = new TypeMirror[length + 1];
            paramTypes[length] = returnType;
            functionClass = "Func";
        }

        env.log("params length: %d", length);
        env.log("params: %s", params);

        for (int i = 0; i < length; i++) {
            VariableElement param = params.get(i);
            env.log("param: %s", param);
            ElementKind paramKind = param.getKind();
            env.log("param kind: %s", paramKind);
            TypeMirror paramType = param.asType();
            env.log("param type: %s", paramType);
            paramTypes[i] = paramType;
        }
        env.log("paramTypes: %s", Arrays.toString(paramTypes));

        Elements elementUtils = env.elements();
        Types typeUtils = env.types();
        String packageName = "com.laynemobile.proxy.functions.";
        TypeElement functionElement = elementUtils.getTypeElement(packageName + functionClass + num);
        env.log("function element: %s", functionElement);
        DeclaredType functionType = typeUtils.getDeclaredType(functionElement, paramTypes);
        env.log("function type: %s", functionType);

        TypeElement abstractProxyFunctionElement
                = elementUtils.getTypeElement(packageName + "AbstractProxyFunction");
        DeclaredType abstractProxyFunctionType
                = typeUtils.getDeclaredType(abstractProxyFunctionElement, functionType);
        env.log("AbstractProxyFunction type: %s", abstractProxyFunctionType);
        env.log("AbstractProxyFunction type typeArguments: %s", abstractProxyFunctionType.getTypeArguments());

        return new FunctionElement(typeElement, element, name, returnType, params, Arrays.asList(paramTypes),
                functionElement, functionType, abstractProxyFunctionElement, abstractProxyFunctionType);
    }

    private static final String ABSTRACT_PREFIX = "Abstract";

    public void writeTo(Filer filer, Env env) throws IOException {
        ProxyElement parent = ProxyElement.cached(typeElement);
        if (parent == null) {
            throw new IllegalStateException(typeElement + " parent must be in cache");
        }
        TypeSpec abstractType = newAbstractProxyFunctionTypeSpec();
        String generated = parent.packageName() + ".generated";
        JavaFile abstractTypeFile = JavaFile.builder(generated, abstractType)
                .build();
        abstractTypeFile.writeTo(filer);
//
//        String templates = parent.packageName() + ".templates.temp";
//        String className = abstractType.name.substring(ABSTRACT_PREFIX.length());
//
//        TypeSpec.Builder classBuider = TypeSpec.classBuilder(className)
//                .superclass()
    }

    public TypeSpec newAbstractProxyFunctionTypeSpec() {
        ProxyElement parent = ProxyElement.cached(typeElement);
        if (parent == null) {
            throw new IllegalStateException(typeElement + " parent must be in cache");
        }
        String subclassName = typeElement.getSimpleName() + "_" + element.getSimpleName() + "Function";
        String subclassPackage = parent.packageName() + ".templates";
        ClassName subclass = ClassName.get(subclassPackage, subclassName);

        String className = ABSTRACT_PREFIX + subclassName;
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(className)
                .superclass(TypeName.get(abstractProxyFunctionType))
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .addAnnotation(Generated.class)
                // TODO: add annotation!
//                .addAnnotation(AnnotationSpec.builder(ProxyFunctionImplementation.class)
//                        .addMember("value", "$T.class", subclass)
//                        .build())
                ;
        for (TypeVariable typeVariable : parent.typeVariables()) {
            classBuilder.addTypeVariable(TypeVariableName.get(typeVariable));
        }

        // Constructor
        classBuilder.addMethod(MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PROTECTED)
                .addParameter(TypeName.get(functionType), name)
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

    public JavaFile newAbstractProxyFunctionTypeJavaFile() {
        ProxyElement parent = ProxyElement.cached(typeElement);
        if (parent == null) {
            throw new IllegalStateException(typeElement + " parent must be in cache");
        }
        TypeSpec typeSpec = newAbstractProxyFunctionTypeSpec();
        String packageName = parent.packageName() + ".generated";
        return JavaFile.builder(packageName, typeSpec)
                .build();
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FunctionElement)) return false;
        FunctionElement that = (FunctionElement) o;
        return Objects.equal(element, that.element);
    }

    @Override public int hashCode() {
        return Objects.hashCode(element);
    }

    @Override public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("\nelement", element)
                .add("\nname", name)
                .add("\nreturnType", returnType)
                .add("\nparams", params)
                .add("\nparamTypes", paramTypes)
                .add("\nfunctionType", functionType)
                .add("\nabstractProxyFunctionType", abstractProxyFunctionType)
                .toString();
    }
}
