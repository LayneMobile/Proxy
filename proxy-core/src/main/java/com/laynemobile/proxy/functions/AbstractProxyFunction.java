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

import com.laynemobile.proxy.MethodResult;
import com.laynemobile.proxy.TypeToken;
import com.laynemobile.proxy.internal.ProxyLog;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public abstract class AbstractProxyFunction<F extends Function, R> extends BaseProxyFunction<F, R> {
    private static final String TAG = AbstractProxyFunction.class.getSimpleName();

    private final FuncN<R> funcN;

    protected AbstractProxyFunction(AbstractProxyFunction<F, R> proxyFunction) {
        super(proxyFunction);
        this.funcN = proxyFunction.funcN;
    }

    protected AbstractProxyFunction(FunctionDef<R> functionDef, F function) {
        super(functionDef, function);
        this.funcN = toFuncN(function);
    }

    protected AbstractProxyFunction(String name, F function, TypeToken<R> returnType, TypeToken<?>[] paramTypes) {
        super(name, function, returnType, paramTypes);
        this.funcN = toFuncN(function);
    }

    protected abstract FuncN<R> toFuncN(F function);

    @Override
    public final boolean handle(Object proxy, Method method, Object[] args, MethodResult result) throws Throwable {
        List<TypeToken<?>> paramTypes = paramTypes();
        int length = paramTypes.size();
        Class<?>[] parameterTypes = method.getParameterTypes();
        ProxyLog.d(TAG, "method parameterTypes: %s", Arrays.toString(parameterTypes));
        if (length != parameterTypes.length) {
            return false;
        }

        for (int i = 0; i < length; i++) {
            TypeToken<?> type = paramTypes.get(i);
            Class<?> clazz = parameterTypes[i];
            if (!clazz.isAssignableFrom(type.getRawType())) {
                ProxyLog.w(TAG, "param type '%s' not assignable from handler type '%s'", clazz, type.getRawType());
                return false;
            }
        }

        result.set(funcN.call(args));
        return true;
    }

    public final FuncN<R> funcN() {
        return funcN;
    }
}
