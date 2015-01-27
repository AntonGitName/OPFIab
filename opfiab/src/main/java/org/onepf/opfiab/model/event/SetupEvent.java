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

package org.onepf.opfiab.model.event;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.onepf.opfiab.BillingProvider;
import org.onepf.opfiab.OPFIab;

import java.util.Arrays;
import java.util.Collection;

import static org.onepf.opfiab.model.event.SetupEvent.Status.PROVIDER_CHANGED;
import static org.onepf.opfiab.model.event.SetupEvent.Status.SUCCESS;
import static org.onepf.opfiab.model.event.SetupEvent.Status.UNAUTHORISED;

public class SetupEvent {

    public static enum Status {

        SUCCESS,
        PROVIDER_CHANGED,
        UNAUTHORISED,
        FAILED,
    }

    private static final Collection<Status> SUCCESSFUL = OPFIab.getConfiguration().skipUnauthorised()
            ? Arrays.asList(SUCCESS, PROVIDER_CHANGED)
            : Arrays.asList(SUCCESS, PROVIDER_CHANGED, UNAUTHORISED);


    @NonNull
    private final Status status;

    @Nullable
    private final BillingProvider billingProvider;


    public SetupEvent(@NonNull final Status status,
                      @Nullable final BillingProvider billingProvider) {
        this.status = status;
        this.billingProvider = billingProvider;
    }

    @Nullable
    public BillingProvider getBillingProvider() {
        return billingProvider;
    }

    @NonNull
    public Status getStatus() {
        return status;
    }

    public boolean isSuccessful() {
        return SUCCESSFUL.contains(getStatus());
    }
}
