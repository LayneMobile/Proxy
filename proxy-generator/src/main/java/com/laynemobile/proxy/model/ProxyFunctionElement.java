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
import com.google.common.collect.ImmutableSet;
import com.laynemobile.proxy.Util;
import com.laynemobile.proxy.annotations.GenerateProxyFunction;
import com.laynemobile.proxy.cache.EnvCache;
import com.laynemobile.proxy.cache.MultiAliasCache;
import com.laynemobile.proxy.elements.AliasElements;
import com.laynemobile.proxy.elements.ExecutableElementAlias;
import com.laynemobile.proxy.elements.TypeElementAlias;
import com.laynemobile.proxy.elements.TypeParameterElementAlias;
import com.laynemobile.proxy.types.AliasTypes;
import com.laynemobile.proxy.types.DeclaredTypeAlias;
import com.laynemobile.proxy.types.TypeMirrorAlias;
import com.laynemobile.proxy.types.TypeVariableAlias;
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
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.processing.Filer;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import sourcerer.processor.Env;

public class ProxyFunctionElement extends AbstractValueAlias<MethodElement> implements TypeElementGenerator {
    private static MultiAliasCache<TypeElementAlias, MethodElement, ProxyFunctionElement> CACHE
            = MultiAliasCache.create(new Creator());
    private static final String ABSTRACT_PREFIX = "Abstract";

    private final String name;
    private final ImmutableSet<ProxyFunctionElement> overrides;
    private final TypeElementAlias functionElement;
    private final DeclaredTypeAlias functionType;
    private final TypeElementAlias abstractProxyFunctionElement;
    private final DeclaredTypeAlias abstractProxyFunctionType;
    private final ImmutableList<TypeMirror> boxedParamTypes;
    private final AtomicReference<FunctionParentOutputStub> output = new AtomicReference<>();

    private ProxyFunctionElement(MethodElement source, Set<? extends ProxyFunctionElement> overrides, Env env) {
        super(source);
        ExecutableElement element = source.element();
        GenerateProxyFunction function = element.getAnnotation(GenerateProxyFunction.class);
        String name = function == null ? "" : function.value();
        if (name.isEmpty()) {
            name = element.getSimpleName().toString();
        }
        env.log("name: %s", name);

        List<TypeMirrorAlias> params = source.paramTypes();
        int length = params.size();
        String num;
        if (length > 9) {
            length = 0;
            num = "N";
        } else {
            num = Integer.toString(length);
        }

        TypeMirrorAlias returnType = source.returnType();
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
            paramTypes[length] = boxedType(returnType, env);
            functionClass = "Func";
        }

        env.log("params length: %d", length);
        env.log("params: %s", params);
        ImmutableList.Builder<TypeMirror> boxedParamTypes = ImmutableList.builder();
        for (int i = 0; i < length; i++) {
            TypeMirror boxedType = boxedType(params.get(i), env);
            paramTypes[i] = boxedType;
            boxedParamTypes.add(boxedType);
        }
        env.log("paramTypes: %s", Arrays.toString(paramTypes));

        Elements elementUtils = env.elements();
        Types typeUtils = env.types();
        String packageName = "com.laynemobile.proxy.functions.";
        TypeElementAlias functionElement = AliasElements.get(
                elementUtils.getTypeElement(packageName + functionClass + num));
        env.log("function element: %s", functionElement);
        DeclaredTypeAlias functionType = AliasTypes.get(
                typeUtils.getDeclaredType(functionElement.actual(), paramTypes));
        env.log("function type: %s", functionType);

        TypeElement abstractProxyFunctionElement
                = elementUtils.getTypeElement(packageName + "AbstractProxyFunction");
        DeclaredTypeAlias abstractProxyFunctionType
                = AliasTypes.get(typeUtils.getDeclaredType(abstractProxyFunctionElement, functionType.actual()));
        env.log("AbstractProxyFunction type: %s", abstractProxyFunctionType);
        env.log("AbstractProxyFunction type typeArguments: %s", abstractProxyFunctionType.getTypeArguments());

        this.name = name;
        this.overrides = ImmutableSet.copyOf(overrides);
        this.functionElement = AliasElements.get(functionElement);
        this.functionType = functionType;
        this.abstractProxyFunctionElement = AliasElements.get(abstractProxyFunctionElement);
        this.abstractProxyFunctionType = abstractProxyFunctionType;
        this.boxedParamTypes = boxedParamTypes.build();
    }

    public static ImmutableList<ProxyFunctionElement> parse(TypeElementAlias typeElement, Env env) {
        EnvCache<MethodElement, ProxyFunctionElement> cache = CACHE.getOrCreate(typeElement, env);
        ImmutableList.Builder<ProxyFunctionElement> builder = ImmutableList.builder();
        for (MethodElement element : MethodElement.parse(typeElement, env)) {
            builder.add(cache.getOrCreate(element, env));
        }
        return builder.build();
    }

    private static TypeMirror boxedType(TypeMirrorAlias typeMirror, Env env) {
        Types typeUtils = env.types();
        if (typeMirror.getKind().isPrimitive()) {
            return typeUtils.boxedClass((PrimitiveType) typeMirror.actual())
                    .asType();
        }
        return typeMirror.actual();
    }

    ProxyElement parent() {
        TypeElementAlias typeElement = typeElement();
        ProxyElement parent = ProxyElement.cache().get(typeElement);
        if (parent == null) {
            throw new IllegalStateException(typeElement + " parent must be in cache");
        }
        return parent;
    }

    public MethodElement alias() {
        return value();
    }

    public TypeElementAlias typeElement() {
        return value().typeElement();
    }

    public ExecutableElementAlias element() {
        return value().element();
    }

    public ImmutableSet<ProxyFunctionElement> overrides() {
        return overrides;
    }

    @Override public FunctionParentOutputStub output() {
        FunctionParentOutputStub o;
        AtomicReference<FunctionParentOutputStub> ref = output;
        if ((o = ref.get()) == null) {
            ref.compareAndSet(null, newOutput());
            return ref.get();
        }
        return o;
    }

    public void writeTo(Filer filer, Env env) throws IOException {
        TypeElementAlias typeElement = typeElement();
        ProxyElement parent = ProxyElement.cache().get(typeElement);
        if (parent == null) {
            throw new IllegalStateException(typeElement + " parent must be in cache");
        }
        GeneratedTypeElementStub output = output();
//        TypeSpec abstractType = output.newTypeSpec();
//        String generated = parent.packageName() + ".generated";
        JavaFile abstractTypeFile = output.newJavaFile()
                .build();
        abstractTypeFile.writeTo(filer);
//
//        String templates = parent.packageName() + ".templates.temp";
//        String className = abstractType.name.substring(ABSTRACT_PREFIX.length());
//
//        TypeSpec.Builder classBuider = TypeSpec.classBuilder(className)
//                .superclass()
    }

    @Override public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("element", element())
                .toString();
    }

    @Override public String toDebugString() {
        return MoreObjects.toStringHelper(this)
                .add("name", name)
                .add("\nelement", element())
                .add("\noverrides", overrides)
                .add("\nfunctionElement", functionElement)
                .add("\nabstractProxyFunctionElement", abstractProxyFunctionElement)
                .add("\nfunctionType", functionType)
                .add("\nabstractProxyFunctionType", abstractProxyFunctionType)
                .toString();
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProxyFunctionElement)) return false;
        if (!super.equals(o)) return false;
        ProxyFunctionElement that = (ProxyFunctionElement) o;
        return Objects.equal(name, that.name) &&
                Objects.equal(functionType, that.functionType);
    }

    @Override public int hashCode() {
        return Objects.hashCode(super.hashCode(), name, functionType);
    }

    private static final class Creator implements MultiAliasCache.ValueCreator<TypeElementAlias, MethodElement, ProxyFunctionElement> {
        @Override public ProxyFunctionElement create(TypeElementAlias typeElement, MethodElement element, Env env) {
            ImmutableSet.Builder<ProxyFunctionElement> overrides = ImmutableSet.builder();
            ProxyFunctionElement _overrides = overrides(typeElement.getSuperclass(), element, env);
            if (_overrides != null) {
                overrides.add(_overrides);
            }
            for (TypeMirrorAlias typeAlias : typeElement.getInterfaces()) {
                if ((_overrides = overrides((DeclaredTypeAlias) typeAlias, element, env)) != null) {
                    overrides.add(_overrides);
                }
            }
            ProxyFunctionElement proxyFunctionElement = new ProxyFunctionElement(element, overrides.build(), env);
            env.log("created proxy function element: %s\n\n", proxyFunctionElement.toDebugString());
            return proxyFunctionElement;
        }

        private ProxyFunctionElement overrides(TypeMirrorAlias typeAlias, MethodElement element, Env env) {
            if (typeAlias != null && typeAlias.getKind() == TypeKind.DECLARED) {
                return overrides((DeclaredTypeAlias) typeAlias, element, env);
            }
            return null;
        }

        private ProxyFunctionElement overrides(DeclaredTypeAlias typeAlias, MethodElement element, Env env) {
            if (typeAlias != null) {
                TypeElementAlias tea = (TypeElementAlias) typeAlias.asElement();
                for (MethodElement methodElement : MethodElement.parse(tea, env)) {
                    if (element.overrides(methodElement, env)) {
                        return CACHE.getOrCreate(tea, methodElement, env);
                    }
                }
            }
            return null;
        }
    }

    private FunctionParentOutputStub newOutput() {
        ProxyElement parent = parent();
        ExecutableElement element = ProxyFunctionElement.this.element();
        String parammys = "";
        for (TypeMirror paramType : boxedParamTypes) {
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
        return new FunctionParentOutputStub(parent, this, baseClassName);
    }

    static final class FunctionParentOutputStub extends AbstractGeneratedTypeElementStub {
        private final ProxyElement parent;
        private final ProxyFunctionElement function;
        private final ExecutableElement element;
        private final String basePackageName;
        private final String baseClassName;
        private final TypeMirror superClass;

        private FunctionParentOutputStub(ProxyElement parent, ProxyFunctionElement function, String baseClassName) {
            this(parent, function, parent.packageName(), baseClassName);
        }

        private FunctionParentOutputStub(ProxyElement parent, ProxyFunctionElement function, String basePackageName,
                String baseClassName) {
            super(basePackageName + ".generated", ABSTRACT_PREFIX + baseClassName);
            this.parent = parent;
            this.function = function;
            this.element = function.element();
            this.basePackageName = basePackageName;
            this.baseClassName = baseClassName;
            this.superClass = function.abstractProxyFunctionType;
        }

        private FunctionParentOutputStub(FunctionParentOutputStub source, TypeMirror superClass) {
            super(source.packageName(), source.className());
            this.parent = source.parent;
            this.function = source.function;
            this.element = source.element;
            this.basePackageName = source.basePackageName;
            this.baseClassName = source.baseClassName;
            this.superClass = superClass;
        }

        FunctionParentOutputStub withSuperClass(TypeMirror superClass) {
            return new FunctionParentOutputStub(this, superClass);
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

            String name = function.name;

            // Constructor
            classBuilder.addMethod(MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PROTECTED)
                    .addParameter(TypeName.get(function.functionType), name)
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

        @Override public GeneratedTypeElement generatedOutput(Env env) {
            return new FunctionParentOutput(this, env);
        }
    }

    private static class FunctionParentOutput extends AbstractGeneratedTypeElement {
        private final FunctionParentOutputStub stub;

        private FunctionParentOutput(FunctionParentOutputStub stub, Env env) {
            super(stub, env);
            this.stub = stub;
        }

        @Override public boolean hasOutput() {
            return true;
        }

        @Override public GeneratedTypeElementStub output(Env env) {
            return new FunctionSubclassOutputStub(this, env);
        }
    }

    private static final class FunctionSubclassOutputStub extends AbstractGeneratedTypeElementStub {
        private final FunctionParentOutput parentOutput;
        private final TypeMirror superClass;

        private FunctionSubclassOutputStub(FunctionParentOutput parentOutput, Env env) {
            super(parentOutput.stub.basePackageName + ".templates", parentOutput.stub.baseClassName);
            this.parentOutput = parentOutput;
            this.superClass = parentOutput.value().asType().actual();
        }

        @Override protected TypeSpec build(TypeSpec.Builder classBuilder) {
            classBuilder = classBuilder.superclass(TypeName.get(superClass))
                    .addModifiers(Modifier.PUBLIC);

            FunctionParentOutputStub stub = parentOutput.stub;

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
            String name = function.name;

            // Constructor
            classBuilder.addMethod(MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PROTECTED)
                    .addParameter(TypeName.get(function.functionType), name)
                    .addStatement("super($L)", name)
                    .build());

            return classBuilder.build();
        }

        @Override public GeneratedTypeElement generatedOutput(Env env) {
            // TODO:
            return super.generatedOutput(env);
        }
    }
}
