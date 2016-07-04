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

import com.google.common.collect.ImmutableSet;
import com.laynemobile.proxy.annotations.Generate;
import com.laynemobile.proxy.annotations.Generate.ProxyBuilder;
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
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import sourcerer.processor.Template;

public class ProxyTemplate extends Template {
    private final Proxies proxies;

    public ProxyTemplate(ProxyTemplate template) {
        super(template);
        this.proxies = new Proxies(this);
    }

    public ProxyTemplate(ProcessingEnvironment processingEnv) {
        super(processingEnv);
        this.proxies = new Proxies(this);
    }

    @Override public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        final Proxies proxies = this.proxies;
        final Types typeUtils = types();
        final Elements elementUtils = elements();
        boolean processed = false;

        for (Element element : roundEnv.getElementsAnnotatedWith(ProxyBuilder.class)) {
            if (!proxies.add(element)) {
                return true; // Exit processing
            }
            processed = true;
        }

        if (!processed) {
            return false;
        }

        for (ProxyElement proxyElement : proxies.proxyElements()) {
            log(proxyElement, "processing element: %s", proxyElement);

            TypeElement typeElement = proxyElement.element();
            for (Element enclosed : typeElement.getEnclosedElements()) {
                if (enclosed.getKind() != ElementKind.METHOD) {
                    continue;
                }
                ExecutableElement methodElement = (ExecutableElement) enclosed;
                log(methodElement, "processing method element: %s", methodElement);
                Generate.ProxyFunction function = enclosed.getAnnotation(Generate.ProxyFunction.class);
                String name = function == null ? "" : function.value();
                if (name.isEmpty()) {
                    name = methodElement.getSimpleName().toString();
                }
                log("name: %s", name);

                List<? extends VariableElement> params = methodElement.getParameters();
                int length = params.size();
                String num;
                if (length > 9) {
                    length = 0;
                    num = "N";
                } else {
                    num = Integer.toString(length);
                }

                TypeMirror returnType = methodElement.getReturnType();
                String functionClass;
                TypeMirror[] paramTypes;
                log("returnType: %s", returnType);
                if (returnType.getKind() == TypeKind.VOID) {
                    log("void");
                    paramTypes = new TypeMirror[length];
                    functionClass = "Action";
                } else {
                    log("not void");
                    paramTypes = new TypeMirror[length + 1];
                    paramTypes[length] = returnType;
                    functionClass = "Func";
                }

                log("params length: %d", length);
                log("params: %s", params);

                for (int i = 0; i < length; i++) {
                    VariableElement param = params.get(i);
                    log("param: %s", param);
                    ElementKind paramKind = param.getKind();
                    log("param kind: %s", paramKind);
                    TypeMirror paramType = param.asType();
                    log("param type: %s", paramType);
                    paramTypes[i] = paramType;
                }
                log("paramTypes: %s", Arrays.toString(paramTypes));

                String packageName = "com.laynemobile.proxy.functions.";
                TypeElement functionElement = elementUtils.getTypeElement(packageName + functionClass + num);
                log("function element: %s", functionElement);
                DeclaredType functionType = typeUtils.getDeclaredType(functionElement, paramTypes);
                log("function type: %s", functionType);

                TypeElement abstractProxyFunctionElement
                        = elementUtils.getTypeElement(packageName + "AbstractProxyFunction");
                DeclaredType abstractProxyFunctionType
                        = typeUtils.getDeclaredType(abstractProxyFunctionElement, functionType);
                log("AbstractProxyFunction type: %s", abstractProxyFunctionType);
                log("AbstractProxyFunction type typeArguments: %s", abstractProxyFunctionType.getTypeArguments());

                List<? extends TypeParameterElement> typeParams = methodElement.getTypeParameters();
                log("typeParams length: %d", typeParams.size());
                log("typeParams: %s", typeParams);

                String className = "Abstract" + typeElement.getSimpleName() + "_" + methodElement.getSimpleName() + "Function";
                TypeSpec.Builder classBuilder = TypeSpec.classBuilder(className)
                        .superclass(TypeName.get(abstractProxyFunctionType))
                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);
                for (TypeVariable typeVariable : proxyElement.typeVariables()) {
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
                                .add(".setName($S)\n", methodElement.getSimpleName())
                                .add(".setMethodHandler($T.from(function()))\n", FunctionHandlers)
                                .add(".build()")
                                .add(";\n$]")
                                .build())
                        .build());

                packageName = proxyElement.packageName() + ".generated";

                // Write java file
                try {
                    JavaFile javaFile = JavaFile.builder(packageName, classBuilder.build())
                            .build();
                    log("writing abstract class type thing");
                    log(javaFile.toString());
                    javaFile.writeTo(filer());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return processed;
    }

    @Override public Set<String> supportedAnnotationTypes() {
        return ImmutableSet.of(ProxyBuilder.class.getCanonicalName());
    }

    public final void log(ProxyElement proxyElement, String msg, Object... args) {
        super.log(proxyElement.element(), msg, args);
    }
}
