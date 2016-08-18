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

package com.laynemobile.api.functions;

import com.laynemobile.api.NetworkChecker;
import com.laynemobile.api.Params;
import com.laynemobile.proxy.TypeToken;
import com.laynemobile.proxy.annotations.Generated;
import com.laynemobile.proxy.functions.Func0;
import com.laynemobile.proxy.functions.ProxyFunc0;
import com.laynemobile.proxy.functions.transforms.Func0Transform;

@Generated
public class NetworkSource_networkChecker<T, P extends Params> extends ProxyFunc0<NetworkChecker> {
    public NetworkSource_networkChecker(Transform function) {
        super(new Def(), function);
    }

    public static class Def extends ProxyFunc0.Def<NetworkChecker> {
        public Def() {
            super("networkChecker", TypeToken.get(NetworkChecker.class));
        }
    }

    public static class Transform extends Func0Transform<NetworkChecker> {
        public Transform(Func0<? extends NetworkChecker> function) {
            super(function);
        }

        public Transform(NetworkChecker value) {
            super(value);
        }
    }
}
