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

import com.squareup.javapoet.JavaFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;

import sourcerer.processor.Env;

public final class Proxies extends Env {
    private final TreeSet<ProxyElement> proxyElements = new TreeSet<>();

    Proxies(Env env) {
        super(env);
    }

    boolean add(Element element) {
        ProxyElement proxyElement = ProxyElement.cache().parse(element, this);
        if (proxyElement == null) {
            return false;
        }
        synchronized (proxyElements) {
            if (!proxyElements.contains(proxyElement)) {
                proxyElements.add(proxyElement);
            }
        }
        return true;
    }

    List<ProxyElement> proxyElements() {
        synchronized (proxyElements) {
            return new ArrayList<>(proxyElements);
        }
    }

    public void writeTo(Filer filer) throws IOException {
        for (ProxyElement proxyElement : proxyElements()) {
            for (ProxyFunctionElement functionElement : proxyElement.functions()) {
                JavaFile abstractProxyFunctionClass
                        = functionElement.newAbstractProxyFunctionTypeJavaFile();
                log("writing AbstractProxyFunctionClass -> \n" + abstractProxyFunctionClass.toString());
                abstractProxyFunctionClass.writeTo(filer);
            }
        }
    }

    // TODO: tree structure for the proxy elements, write in pre-order traversal
//
//    private interface Node<T> {
//        T data();
//
//        Node<T> parent();
//
//        List<? extends Node<T>> children();
//    }
//
//    private static class ProxyNode implements Node<ProxyElement> {
//        private final ProxyElement element;
//        private final boolean root;
//        private ProxyNode parent;
//        private List<ProxyNode> children;
//
//        public ProxyNode(ProxyElement element) {
//            this.element = element;
//        }
//
//        @Override public ProxyElement data() {
//            return element;
//        }
//
//        @Override public ProxyNode parent() {
//            return parent;
//        }
//
//        @Override public List<ProxyNode> children() {
//            return children;
//        }
//    }
}
