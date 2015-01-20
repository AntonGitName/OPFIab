/*
 * Copyright 2012-2014 One Platform Foundation
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

package org.onepf.opfiab.model.event.response;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.onepf.opfiab.model.billing.ConsumableDetails;
import org.onepf.opfiab.model.event.request.ConsumeRequest;

public class ConsumeResponse extends Response {

    @Nullable
    private final ConsumableDetails consumableDetails;

    public ConsumeResponse(@NonNull final ConsumeRequest request,
                           @NonNull final Status status,
                           @Nullable final ConsumableDetails consumableDetails) {
        super(request, status);
        this.consumableDetails = consumableDetails;
    }

    @Nullable
    public ConsumableDetails getConsumableDetails() {
        return consumableDetails;
    }

    @NonNull
    @Override
    public ConsumeRequest getRequest() {
        return (ConsumeRequest) super.getRequest();
    }
}
