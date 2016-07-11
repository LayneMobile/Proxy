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

package com.laynemobile.api.templates;

import com.laynemobile.api.NetworkChecker;
import com.laynemobile.api.generated.AbstractNetworkSource_networkChecker;
import com.laynemobile.proxy.functions.Func0;

// subclass template created for user for constructor extensions
public class NetworkSource_networkCheckerFunction extends AbstractNetworkSource_networkChecker {
    public NetworkSource_networkCheckerFunction(Func0<NetworkChecker> networkCheckerFunc0) {
        super(networkCheckerFunc0);
    }

    public NetworkSource_networkCheckerFunction(final NetworkChecker networkChecker) {
        super(new Func0<NetworkChecker>() {
            @Override public NetworkChecker call() {
                return networkChecker;
            }
        });
    }

    public NetworkSource_networkCheckerFunction() {
        super(new Func0<NetworkChecker>() {
            @Override public NetworkChecker call() {
                // TODO: plugin for default implementation
                return NetworkChecker.ALWAYS_AVAILABLE;
            }
        });
    }
}
