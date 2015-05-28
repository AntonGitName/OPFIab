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

package org.onepf.opfiab.opfiab_uitest;

import android.app.Instrumentation;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.onepf.opfiab.OPFIab;
import org.onepf.opfiab.api.AdvancedIabHelper;
import org.onepf.opfiab.api.IabHelper;
import org.onepf.opfiab.billing.BillingProvider;
import org.onepf.opfiab.model.BillingProviderInfo;
import org.onepf.opfiab.model.Configuration;
import org.onepf.opfiab.opfiab_uitest.util.MockBillingProviderBuilder;
import org.onepf.opfiab.opfiab_uitest.validators.PurchaseRequestValidator;
import org.onepf.opfiab.opfiab_uitest.validators.PurchaseResponseValidator;
import org.onepf.opfiab.opfiab_uitest.validators.SetupResponseValidator;
import org.onepf.opfiab.opfiab_uitest.validators.SetupStartedEventValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static junit.framework.Assert.assertTrue;

/**
 * @author antonpp
 * @since 28.05.15
 */
public class AdvancedHelperTest {

    private static final long MAX_WAIT_TIME = 2000L;

    private static final int NUM_TESTS = 10;

    private static final String SKU_CONSUMABLE = "org.onepf.opfiab.consumable";
    private static final String SKU_NONCONSUMABLE = "org.onepf.opfiab.nonconsumable";
    private static final String SKU_SUBSCRIPTION = "org.onepf.opfiab.subscription";

    @Rule
    public ActivityTestRule<EmptyActivity> testRule = new ActivityTestRule<>(EmptyActivity.class);

    private AdvancedIabHelper iabHelper;
    private EmptyActivity activity;
    private Instrumentation instrumentation;

    @Before
    public void setUp() throws Exception {
        activity = testRule.getActivity();
        setupDexmaker();
        instrumentation = InstrumentationRegistry.getInstrumentation();
    }

    private BillingProvider prepareMockProvider(String name) {
        return new MockBillingProviderBuilder()
                .setIsAuthorised(true)
                .setIsAvailable(true)
                .setInfo(new BillingProviderInfo(name, null))
                .build();
    }

    @Test
    public void testSetup() throws InterruptedException {
        final String name = "Absolutely random name";
        final BillingProvider billingProvider = prepareMockProvider(name);

        final TestManager testManager = new TestManager.Builder()
                .expectEvent(new SetupStartedEventValidator())
                .expectEvent(new SetupResponseValidator(name))
                .build();

        final Configuration configuration = new Configuration.Builder()
                .addBillingProvider(billingProvider)
                .setBillingListener(testManager)
                .build();

        instrumentation.runOnMainSync(new Runnable() {
            @Override
            public void run() {

                OPFIab.init(activity.getApplication(), configuration);

                testManager.startTest();

                iabHelper = OPFIab.getAdvancedHelper();

                OPFIab.setup();
            }
        });
        assertTrue(testManager.await(MAX_WAIT_TIME));
    }

    @Test
    public void testMultiRequest() throws InterruptedException {
        final String name = "Absolutely random name";
        final BillingProvider billingProvider = prepareMockProvider(name);

        final TestManager testManager = new TestManager.Builder()
                .expectEvent(new SetupStartedEventValidator())
                .expectEvent(new SetupResponseValidator(name))
                .expectEvent(new PurchaseRequestValidator(SKU_CONSUMABLE))
                .expectEvent(new PurchaseRequestValidator(SKU_NONCONSUMABLE))
                .expectEvent(new PurchaseRequestValidator(SKU_SUBSCRIPTION))
                .expectEvent(new PurchaseResponseValidator(name))
                .expectEvent(new PurchaseResponseValidator(name))
                .expectEvent(new PurchaseResponseValidator(name))
                .setStrategy(TestManager.Strategy.UNORDERED_EVENTS)
                .build();

        final Configuration configuration = new Configuration.Builder()
                .addBillingProvider(billingProvider)
                .setBillingListener(testManager)
                .build();

        instrumentation.runOnMainSync(new Runnable() {
            @Override
            public void run() {

                OPFIab.init(activity.getApplication(), configuration);
                iabHelper = OPFIab.getAdvancedHelper();
                OPFIab.setup();

                final IabHelper iabHelper = OPFIab.getAdvancedHelper();

                for (int i = 0; i < NUM_TESTS; ++i) {
                    iabHelper.purchase(SKU_CONSUMABLE);
                    iabHelper.purchase(SKU_NONCONSUMABLE);
                    iabHelper.purchase(SKU_SUBSCRIPTION);
                }
            }
        });

        assertTrue(testManager.await(MAX_WAIT_TIME * NUM_TESTS));
    }

    @Test
    public void testMultipleHelpersMultipleRequests() throws Exception {
        final int n = 5;
        final String name = "Absolutely random name";
        final BillingProvider billingProvider = prepareMockProvider(name);
        final List<AdvancedIabHelper> helpers = new ArrayList<>(n);

        final TestManager testManager = new TestManager.Builder()
                .expectEvent(new SetupStartedEventValidator())
                .expectEvent(new SetupResponseValidator(name))
                .expectEvent(new PurchaseRequestValidator(SKU_CONSUMABLE))
                .expectEvent(new PurchaseRequestValidator(SKU_NONCONSUMABLE))
                .expectEvent(new PurchaseRequestValidator(SKU_SUBSCRIPTION))
                .expectEvent(new PurchaseResponseValidator(name))
                .expectEvent(new PurchaseResponseValidator(name))
                .expectEvent(new PurchaseResponseValidator(name))
                .setStrategy(TestManager.Strategy.UNORDERED_EVENTS)
                .build();

        final Configuration configuration = new Configuration.Builder()
                .addBillingProvider(billingProvider)
                .setBillingListener(testManager)
                .build();

        final Random rnd = new Random();
        final String[] skus = new String[]{SKU_CONSUMABLE, SKU_NONCONSUMABLE, SKU_SUBSCRIPTION};

        instrumentation.runOnMainSync(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < NUM_TESTS; ++i) {
                    OPFIab.init(activity.getApplication(), configuration);

                    for (int j = 0; j < n; ++j) {
                        helpers.add(OPFIab.getAdvancedHelper());
                    }

                    OPFIab.setup();

                    for (int j = 0; j < NUM_TESTS; ++j) {
                        helpers.get(rnd.nextInt(n)).purchase(skus[j % skus.length]);
                    }

                }
            }
        });

        assertTrue(testManager.await(MAX_WAIT_TIME * NUM_TESTS));
    }

    /**
     * Workaround for Mockito and JB-MR2 incompatibility to avoid
     * java.lang.IllegalArgumentException: dexcache == null
     *
     * @see <a href="https://code.google.com/p/dexmaker/issues/detail?id=2">
     * https://code.google.com/p/dexmaker/issues/detail?id=2</a>
     */
    private void setupDexmaker() {
        // Explicitly set the Dexmaker cache, so tests that use mockito work
        final String dexCache = activity.getCacheDir().getPath();
        System.setProperty("dexmaker.dexcache", dexCache);
    }
}