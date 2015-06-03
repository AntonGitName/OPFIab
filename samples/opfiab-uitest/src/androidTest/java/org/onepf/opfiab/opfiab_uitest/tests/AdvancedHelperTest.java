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

package org.onepf.opfiab.opfiab_uitest.tests;

import android.app.Instrumentation;
import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.onepf.opfiab.OPFIab;
import org.onepf.opfiab.api.AdvancedIabHelper;
import org.onepf.opfiab.api.IabHelper;
import org.onepf.opfiab.billing.BillingProvider;
import org.onepf.opfiab.listener.OnConsumeListener;
import org.onepf.opfiab.listener.OnInventoryListener;
import org.onepf.opfiab.listener.OnPurchaseListener;
import org.onepf.opfiab.listener.OnSetupListener;
import org.onepf.opfiab.listener.OnSkuDetailsListener;
import org.onepf.opfiab.model.BillingProviderInfo;
import org.onepf.opfiab.model.Configuration;
import org.onepf.opfiab.model.event.billing.PurchaseResponse;
import org.onepf.opfiab.opfiab_uitest.EmptyActivity;
import org.onepf.opfiab.opfiab_uitest.manager.BillingManagerAdapter;
import org.onepf.opfiab.opfiab_uitest.manager.TestManager;
import org.onepf.opfiab.opfiab_uitest.util.MockBillingProviderBuilder;
import org.onepf.opfiab.opfiab_uitest.util.validators.ConsumeResponseValidator;
import org.onepf.opfiab.opfiab_uitest.util.validators.EventValidator;
import org.onepf.opfiab.opfiab_uitest.util.validators.InventoryResponseValidator;
import org.onepf.opfiab.opfiab_uitest.util.validators.PurchaseRequestValidator;
import org.onepf.opfiab.opfiab_uitest.util.validators.PurchaseResponseValidator;
import org.onepf.opfiab.opfiab_uitest.util.validators.SetupResponseValidator;
import org.onepf.opfiab.opfiab_uitest.util.validators.SetupStartedEventValidator;
import org.onepf.opfiab.opfiab_uitest.util.validators.SkuDetailsResponseValidator;

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

    private static final String TEST_PROVIDER_NAME = "TEST_PROVIDER_NAME";
    private static final String TEST_PROVIDER_PACKAGE = "org.onepf.opfiab.uitest";

    @Rule
    public ActivityTestRule<EmptyActivity> testRule = new ActivityTestRule<>(EmptyActivity.class);

    private EmptyActivity activity;
    private Instrumentation instrumentation;

    @Before
    public void setUp() throws Exception {
        activity = testRule.getActivity();
        setupDexmaker();
        instrumentation = InstrumentationRegistry.getInstrumentation();
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

    @Test
    public void testSetup() throws InterruptedException {
        final BillingProvider billingProvider = prepareMockProvider(TEST_PROVIDER_NAME);

        final TestManager testManager = new TestManager.Builder()
                .expectEvent(new SetupStartedEventValidator())
                .expectEvent(new SetupResponseValidator(TEST_PROVIDER_NAME))
                .build();

        final Configuration configuration = new Configuration.Builder()
                .addBillingProvider(billingProvider)
                .setBillingListener(new BillingManagerAdapter(testManager))
                .build();

        instrumentation.runOnMainSync(new Runnable() {
            @Override
            public void run() {
                OPFIab.init(activity.getApplication(), configuration);
                OPFIab.setup();
            }
        });
        assertTrue(testManager.await(MAX_WAIT_TIME));
    }

    private BillingProvider prepareMockProvider(String TEST_PROVIDER_NAME) {
        return new MockBillingProviderBuilder()
                .setIsAuthorised(true)
                .setIsAvailable(true)
                .setInfo(new BillingProviderInfo(TEST_PROVIDER_NAME, null))
                .build();
    }

    @Test
    public void testMultiRequest() throws InterruptedException {
        final BillingProvider billingProvider = prepareMockProvider(TEST_PROVIDER_NAME);

        final TestManager testManager = new TestManager.Builder()
                .expectEvent(new SetupStartedEventValidator())
                .expectEvent(new SetupResponseValidator(TEST_PROVIDER_NAME))
                .expectEvent(new PurchaseRequestValidator(SKU_CONSUMABLE))
                .expectEvent(new PurchaseRequestValidator(SKU_NONCONSUMABLE))
                .expectEvent(new PurchaseRequestValidator(SKU_SUBSCRIPTION))
                .expectEvent(new PurchaseResponseValidator(TEST_PROVIDER_NAME, true))
                .expectEvent(new PurchaseResponseValidator(TEST_PROVIDER_NAME, true))
                .expectEvent(new PurchaseResponseValidator(TEST_PROVIDER_NAME, true))
                .setStrategy(TestManager.Strategy.UNORDERED_EVENTS)
                .build();

        final BillingManagerAdapter testAdapter = new BillingManagerAdapter();
        final Configuration configuration = new Configuration.Builder()
                .addBillingProvider(billingProvider)
                .setBillingListener(testAdapter)
                .build();

        testAdapter.addTestManager(testManager);

        instrumentation.runOnMainSync(new Runnable() {
            @Override
            public void run() {

                OPFIab.init(activity.getApplication(), configuration);
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
    public void testFailPurchase() throws Exception {
        final BillingProvider billingProvider = new MockBillingProviderBuilder()
                .setIsAuthorised(true)
                .setWillPostSuccess(false)
                .setInfo(new BillingProviderInfo(TEST_PROVIDER_NAME, TEST_PROVIDER_PACKAGE))
                .setIsAvailable(true)
                .build();

        final TestManager testManager = new TestManager.Builder()
                .expectEvent(new SetupStartedEventValidator())
                .expectEvent(new SetupResponseValidator(TEST_PROVIDER_NAME))
                .expectEvent(new PurchaseRequestValidator(SKU_CONSUMABLE))
                .expectEvent(new PurchaseResponseValidator(TEST_PROVIDER_NAME, false))
                .setStrategy(TestManager.Strategy.UNORDERED_EVENTS)
                .build();

        final Configuration configuration = new Configuration.Builder()
                .addBillingProvider(billingProvider)
                .setBillingListener(new BillingManagerAdapter(testManager))
                .build();

        instrumentation.runOnMainSync(new Runnable() {
            @Override
            public void run() {
                OPFIab.init(activity.getApplication(), configuration);
                OPFIab.getAdvancedHelper().purchase(SKU_CONSUMABLE);
            }
        });

        assertTrue(testManager.await(MAX_WAIT_TIME));
    }

    @Test
    public void testSuccessfulPurchase() throws Exception {
        final BillingProvider billingProvider = new MockBillingProviderBuilder()
                .setIsAuthorised(true)
                .setWillPostSuccess(true)
                .setInfo(new BillingProviderInfo(TEST_PROVIDER_NAME, null))
                .setIsAvailable(true)
                .build();

        final TestManager testManager = new TestManager.Builder()
                .expectEvent(new SetupStartedEventValidator())
                .expectEvent(new SetupResponseValidator(TEST_PROVIDER_NAME))
                .expectEvent(new PurchaseRequestValidator(SKU_CONSUMABLE))
                .expectEvent(new PurchaseResponseValidator(TEST_PROVIDER_NAME, true))
                .setStrategy(TestManager.Strategy.UNORDERED_EVENTS)
                .build();

        final Configuration configuration = new Configuration.Builder()
                .addBillingProvider(billingProvider)
                .setBillingListener(new BillingManagerAdapter(testManager))
                .build();

        instrumentation.runOnMainSync(new Runnable() {
            @Override
            public void run() {
                OPFIab.init(activity.getApplication(), configuration);
                OPFIab.getAdvancedHelper().purchase(SKU_CONSUMABLE);
            }
        });

        assertTrue(testManager.await(MAX_WAIT_TIME));
    }

    @Test
    public void testMultipleHelpersMultipleRequests() throws Exception {
        final int n = 5;
        final BillingProvider billingProvider = prepareMockProvider(TEST_PROVIDER_NAME);
        final List<AdvancedIabHelper> helpers = new ArrayList<>(n);

        final TestManager testManager = new TestManager.Builder()
                .expectEvent(new SetupStartedEventValidator())
                .expectEvent(new SetupResponseValidator(TEST_PROVIDER_NAME))
                .expectEvent(new PurchaseRequestValidator(SKU_CONSUMABLE))
                .expectEvent(new PurchaseRequestValidator(SKU_NONCONSUMABLE))
                .expectEvent(new PurchaseRequestValidator(SKU_SUBSCRIPTION))
                .expectEvent(new PurchaseResponseValidator(TEST_PROVIDER_NAME, true))
                .expectEvent(new PurchaseResponseValidator(TEST_PROVIDER_NAME, true))
                .expectEvent(new PurchaseResponseValidator(TEST_PROVIDER_NAME, true))
                .setStrategy(TestManager.Strategy.UNORDERED_EVENTS)
                .build();

        final BillingManagerAdapter testAdapter = new BillingManagerAdapter();
        final Configuration configuration = new Configuration.Builder()
                .addBillingProvider(billingProvider)
                .setBillingListener(testAdapter)
                .build();

        testAdapter.addTestManager(testManager);

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

    @Test
    public void testRegister() throws Exception {
        final BillingProvider billingProvider = new MockBillingProviderBuilder()
                .setIsAuthorised(true)
                .setWillPostSuccess(true)
                .setInfo(new BillingProviderInfo(TEST_PROVIDER_NAME, TEST_PROVIDER_PACKAGE))
                .setIsAvailable(true)
                .build();

        final TestManager testSetupManager = new TestManager.Builder()
                .expectEvent(new SetupStartedEventValidator())
                .expectEvent(new SetupResponseValidator(TEST_PROVIDER_NAME))
                .setTag("Setup")
                .build();
        final OnSetupListener setupListenerAdapter = new BillingManagerAdapter(testSetupManager);

        final TestManager testPurchaseManager = new TestManager.Builder()
                .expectEvent(new PurchaseResponseValidator(TEST_PROVIDER_NAME, true))
                .setTag("Purchase")
                .build();
        final OnPurchaseListener purchaseListenerAdapter = new BillingManagerAdapter(
                testPurchaseManager);

        final TestManager testInventoryManager = new TestManager.Builder()
                .expectEvent(new InventoryResponseValidator(TEST_PROVIDER_NAME, true, null))
                .setTag("Inventory")
                .build();
        final OnInventoryListener inventoryListenerAdapter = new BillingManagerAdapter(
                testInventoryManager);

        final TestManager testSkuDetailsManager = new TestManager.Builder()
                .expectEvent(new SkuDetailsResponseValidator(TEST_PROVIDER_NAME, true))
                .setTag("SkuDetails")
                .build();
        final OnSkuDetailsListener skuDetailsListenerAdapter = new BillingManagerAdapter(
                testSkuDetailsManager);

        final TestManager testConsumeManager = new TestManager.Builder()
                .expectEvent(new ConsumeResponseValidator(TEST_PROVIDER_NAME, true))
                .setTag("Consume")
                .build();
        final OnConsumeListener consumeListenerAdapter = new BillingManagerAdapter(
                testConsumeManager);

        final TestManager testGlobalListenerManager = new TestManager.Builder()
                .expectEvent(new SetupStartedEventValidator())
                .expectEvent(new SetupResponseValidator(TEST_PROVIDER_NAME))
                .expectEvent(new PurchaseRequestValidator(SKU_CONSUMABLE))
                .expectEvent(new PurchaseResponseValidator(TEST_PROVIDER_NAME, true))
                .expectEvent(new InventoryResponseValidator(TEST_PROVIDER_NAME, true, null))
                .expectEvent(new SkuDetailsResponseValidator(TEST_PROVIDER_NAME, true))
                .expectEvent(new ConsumeResponseValidator(TEST_PROVIDER_NAME, true))
                .setStrategy(TestManager.Strategy.UNORDERED_EVENTS)
                .setTag("Global")
                .build();

        final Configuration configuration = new Configuration.Builder()
                .addBillingProvider(billingProvider)
                .setBillingListener(new BillingManagerAdapter(testGlobalListenerManager))
                .build();

        instrumentation.runOnMainSync(new Runnable() {
            @Override
            public void run() {
                OPFIab.init(activity.getApplication(), configuration);
                final AdvancedIabHelper helper = OPFIab.getAdvancedHelper();

                helper.addSetupListener(setupListenerAdapter);
                helper.addPurchaseListener(purchaseListenerAdapter);
                helper.addInventoryListener(inventoryListenerAdapter);
                helper.addSkuDetailsListener(skuDetailsListenerAdapter);
                helper.addConsumeListener(consumeListenerAdapter);
                helper.addPurchaseListener(new OnPurchaseListener() {
                    @Override
                    public void onPurchase(@NonNull final PurchaseResponse purchaseResponse) {
                        if (purchaseResponse.getPurchase() != null) {
                            helper.consume(purchaseResponse.getPurchase());
                        }
                    }
                });
                helper.register();

                OPFIab.setup();
                helper.purchase(SKU_CONSUMABLE);
                helper.purchase(SKU_SUBSCRIPTION);
                helper.skuDetails(SKU_CONSUMABLE, SKU_NONCONSUMABLE, SKU_SUBSCRIPTION);
                helper.inventory(true);
            }
        });

        final TestManager[] managers = {testSetupManager, testInventoryManager, testPurchaseManager,
                testSkuDetailsManager, testConsumeManager, testGlobalListenerManager};


        for (TestManager manager : managers) {
            Assert.assertTrue(manager.await(MAX_WAIT_TIME * 4));
        }
    }

    @Test
    public void testUnregister() throws Exception {
        final BillingProvider billingProvider = new MockBillingProviderBuilder()
                .setIsAuthorised(true)
                .setWillPostSuccess(true)
                .setInfo(new BillingProviderInfo(TEST_PROVIDER_NAME, TEST_PROVIDER_PACKAGE))
                .setIsAvailable(true)
                .build();

        final EventValidator[] eventValidators = {
                new SetupStartedEventValidator(),
                new SetupResponseValidator(TEST_PROVIDER_NAME),
                new PurchaseResponseValidator(TEST_PROVIDER_NAME, true),
                new InventoryResponseValidator(TEST_PROVIDER_NAME, true, null),
                new SkuDetailsResponseValidator(TEST_PROVIDER_NAME, true),
        };

        final TestManager testBeforeUnregistrationManager = new TestManager.Builder()
                .expectEvents(eventValidators)
                .expectEvent(new ConsumeResponseValidator(TEST_PROVIDER_NAME, true))
                .setStrategy(TestManager.Strategy.UNORDERED_EVENTS)
                .setTag("BeforeUnreg")
                .build();
        final BillingManagerAdapter testAdapter = new BillingManagerAdapter(
                testBeforeUnregistrationManager);

        final TestManager subscribeSensitiveManager = new TestManager.Builder()
                .expectEvents(eventValidators)
                .expectEvent(new ConsumeResponseValidator(TEST_PROVIDER_NAME, true))
                .setStrategy(TestManager.Strategy.UNORDERED_EVENTS)
                .setFailOnReceive(true)
                .setSkipWrongEvents(false)
                .setTag("RegisterSensitive")
                .build();
        final BillingManagerAdapter subscribeSensitiveAdapter = new BillingManagerAdapter(
                subscribeSensitiveManager);

        final AdvancedIabHelper[] helpers = new AdvancedIabHelper[1];

        final Configuration configuration = new Configuration.Builder()
                .addBillingProvider(billingProvider)
                .setBillingListener(testAdapter)
                .build();

        instrumentation.runOnMainSync(new Runnable() {
            @Override
            public void run() {
                OPFIab.init(activity.getApplication(), configuration);
                helpers[0] = OPFIab.getAdvancedHelper();
                final AdvancedIabHelper helper = helpers[0];

                helper.addSetupListener(subscribeSensitiveAdapter);
                helper.addPurchaseListener(subscribeSensitiveAdapter);
                helper.addInventoryListener(subscribeSensitiveAdapter);
                helper.addSkuDetailsListener(subscribeSensitiveAdapter);
                helper.addConsumeListener(subscribeSensitiveAdapter);

                helper.addPurchaseListener(new OnPurchaseListener() {
                    @Override
                    public void onPurchase(@NonNull final PurchaseResponse purchaseResponse) {
                        if (purchaseResponse.getPurchase() != null) {
                            helper.consume(purchaseResponse.getPurchase());
                        }
                    }
                });
                helper.register();

                OPFIab.setup();
                helper.purchase(SKU_CONSUMABLE);
                helper.skuDetails(SKU_CONSUMABLE, SKU_NONCONSUMABLE, SKU_SUBSCRIPTION);
                helper.inventory(true);
            }
        });

        Assert.assertTrue(testBeforeUnregistrationManager.await(MAX_WAIT_TIME * 4));

        final TestManager testAfterUnRegistrationManager = new TestManager.Builder()
                .expectEvents(eventValidators)
                .setStrategy(TestManager.Strategy.UNORDERED_EVENTS)
                .setTag("AfterUnreg")
                .build();

        testAdapter.addTestManager(testAfterUnRegistrationManager);

        instrumentation.runOnMainSync(new Runnable() {
            @Override
            public void run() {
                final AdvancedIabHelper helper = helpers[0];
                helper.unregister();

                OPFIab.setup();
                helper.purchase(SKU_CONSUMABLE);
                helper.skuDetails(SKU_CONSUMABLE, SKU_NONCONSUMABLE, SKU_SUBSCRIPTION);
                helper.inventory(true);
            }
        });

        Assert.assertTrue(testAfterUnRegistrationManager.await(MAX_WAIT_TIME * 4));
        Assert.assertTrue(subscribeSensitiveManager.await(MAX_WAIT_TIME));
    }
}