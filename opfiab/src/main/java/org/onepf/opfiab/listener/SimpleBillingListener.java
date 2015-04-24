/*
 * Copyright 2012-2015 One Platform Foundation
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

package org.onepf.opfiab.listener;

import android.support.annotation.NonNull;

import org.onepf.opfiab.model.event.SetupResponse;
import org.onepf.opfiab.model.event.SetupStartedEvent;
import org.onepf.opfiab.model.event.billing.BillingRequest;
import org.onepf.opfiab.model.event.billing.BillingResponse;
import org.onepf.opfiab.model.event.billing.ConsumeResponse;
import org.onepf.opfiab.model.event.billing.InventoryResponse;
import org.onepf.opfiab.model.event.billing.PurchaseResponse;
import org.onepf.opfiab.model.event.billing.SkuDetailsResponse;
import org.onepf.opfutils.OPFLog;

/**
 * Stub implementation of {@link BillingListener} interface.
 */
@SuppressWarnings("PMD.UncommentedEmptyMethod")
public class SimpleBillingListener implements BillingListener {

    @Override
    public void onSetupStarted(@NonNull final SetupStartedEvent setupStartedEvent) {
        OPFLog.logMethod(setupStartedEvent);
    }

    @Override
    public void onSetupResponse(@NonNull final SetupResponse setupResponse) {
        OPFLog.logMethod(setupResponse);
    }

    @Override
    public void onRequest(@NonNull final BillingRequest billingRequest) {
        OPFLog.logMethod(billingRequest);
    }

    @Override
    public void onResponse(@NonNull final BillingResponse billingResponse) {
        OPFLog.logMethod(billingResponse);
    }

    @Override
    public void onConsume(@NonNull final ConsumeResponse consumeResponse) {
        OPFLog.logMethod(consumeResponse);
    }

    @Override
    public void onPurchase(@NonNull final PurchaseResponse purchaseResponse) {
        OPFLog.logMethod(purchaseResponse);
    }

    @Override
    public void onInventory(@NonNull final InventoryResponse inventoryResponse) {
        OPFLog.logMethod(inventoryResponse);
    }

    @Override
    public void onSkuDetails(@NonNull final SkuDetailsResponse skuDetailsResponse) {
        OPFLog.logMethod(skuDetailsResponse);
    }
}
