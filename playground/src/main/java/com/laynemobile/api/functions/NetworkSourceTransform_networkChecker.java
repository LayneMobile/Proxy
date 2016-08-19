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
import com.laynemobile.proxy.functions.Func0;

public class NetworkSourceTransform_networkChecker extends NetworkSource_networkChecker.Transform {
    public NetworkSourceTransform_networkChecker(Func0<? extends NetworkChecker> function) {
        super(function);
    }

    public NetworkSourceTransform_networkChecker(NetworkChecker networkChecker) {
        super(networkChecker);
    }

    public NetworkSourceTransform_networkChecker() {
        // TODO: plugin for default implementation
        super(NetworkChecker.ALWAYS_AVAILABLE);
    }
}