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

package org.onepf.opfiab;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;

import org.onepf.opfiab.billing.BillingProvider;
import org.onepf.opfiab.listener.BillingListener;
import org.onepf.opfiab.misc.ActivityMonitor;
import org.onepf.opfiab.model.Configuration;
import org.onepf.opfiab.model.event.SetupRequest;
import org.onepf.opfutils.OPFChecks;
import org.onepf.opfutils.OPFLog;
import org.onepf.opfutils.exception.InitException;

import java.util.Collection;
import java.util.concurrent.Executors;

import de.greenrobot.event.EventBus;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public final class OPFIab {

    private static volatile Configuration configuration;
    private static volatile EventBus eventBus;
    private static volatile Context context;

    private static BillingBase billingBase;
    private static BillingEventDispatcher eventDispatcher;
    private static BillingRequestScheduler requestScheduler;


    private OPFIab() {
        throw new UnsupportedOperationException();
    }

    private static void checkInit() {
        OPFChecks.checkThread(true);
        if (billingBase == null) {
            throw new InitException(false);
        }
    }

    private static EventBus newBus() {
        return EventBus.builder()
                // Must use only one background thread
                .executorService(Executors.newSingleThreadExecutor())
                .throwSubscriberException(true)
                .eventInheritance(true)
                .logNoSubscriberMessages(OPFLog.isEnabled())
                .logSubscriberExceptions(OPFLog.isEnabled())
                .build();
    }

    static void register(@NonNull final Object subscriber) {
        if (!eventBus.isRegistered(subscriber)) {
            eventBus.register(subscriber);
        }
    }

    static void register(@NonNull final Object subscriber, final int priority) {
        if (!eventBus.isRegistered(subscriber)) {
            eventBus.register(subscriber, priority);
        }
    }

    static void unregister(@NonNull final Object subscriber) {
        if (eventBus.isRegistered(subscriber)) {
            eventBus.unregister(subscriber);
        }
    }

    static void cancelEventDelivery(@NonNull final Object event) {
        eventBus.cancelEventDelivery(event);
    }

    @NonNull
    static BillingBase getBase() {
        checkInit();
        return billingBase;
    }

    @NonNull
    static BillingEventDispatcher getBillingEventDispatcher() {
        checkInit();
        return eventDispatcher;
    }

    @NonNull
    static BillingRequestScheduler getRequestScheduler() {
        checkInit();
        return requestScheduler;
    }

    /**
     * Post an event to deliver to all subscribers. Intend to be used by {@link org.onepf.opfiab.billing.BillingProvider} implementations.
     *
     * @param event Event object to deliver.
     */
    public static void post(final Object event) {
        if (eventBus.hasSubscriberForEvent(event.getClass())) {
            eventBus.post(event);
        }
    }

    @NonNull
    public static SimpleIabHelper getSimpleHelper() {
        return new SimpleIabHelper();
    }

    @NonNull
    public static AdvancedIabHelper getAdvancedHelper() {
        return new AdvancedIabHelper();
    }

    @NonNull
    public static ActivityIabHelper getActivityHelper(
            @NonNull final FragmentActivity fragmentActivity) {
        return new ActivityIabHelper(fragmentActivity);
    }

    @NonNull
    public static ActivityIabHelper getActivityHelper(@NonNull final Activity activity) {
        return new ActivityIabHelper(activity);
    }

    @NonNull
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static FragmentIabHelper getFragmentHelper(
            @NonNull final android.app.Fragment fragment) {
        return new FragmentIabHelper(fragment);
    }

    @NonNull
    public static FragmentIabHelper getFragmentHelper(
            @NonNull final android.support.v4.app.Fragment fragment) {
        return new FragmentIabHelper(fragment);
    }

    @NonNull
    public static Context getContext() {
        if (context == null) {
            throw new IllegalStateException();
        }
        return context;
    }

    @NonNull
    public static Configuration getConfiguration() {
        if (configuration == null) {
            throw new IllegalStateException();
        }
        return configuration;
    }

    @SuppressFBWarnings({"LI_LAZY_INIT_UPDATE_STATIC"})
    public static void init(@NonNull final Application application,
                            @NonNull final Configuration configuration) {
        OPFChecks.checkThread(true);
        if (billingBase != null) {
            throw new InitException(true);
        }

        // Check if manifest satisfies all billing providers.
        final Collection<BillingProvider> providers = configuration.getProviders();
        for (final BillingProvider provider : providers) {
            provider.checkManifest();
        }

        OPFIab.configuration = configuration;
        OPFIab.context = application.getApplicationContext();
        OPFIab.eventBus = newBus();
        OPFIab.billingBase = new BillingBase();
        final BillingListener billingListener = configuration.getBillingListener();
        OPFIab.eventDispatcher = new BillingEventDispatcher(billingListener);
        OPFIab.requestScheduler = new BillingRequestScheduler();

        int priority = Integer.MAX_VALUE;
        register(billingBase, priority);
        register(new SetupManager(), --priority);
        register(eventDispatcher, --priority);
        register(requestScheduler, --priority);

        final ActivityMonitor monitor = ActivityMonitor.getInstance();
        // Make sure instance registered only once.
        application.unregisterActivityLifecycleCallbacks(monitor);
        application.registerActivityLifecycleCallbacks(monitor);
    }

    public static void setup() {
        checkInit();
        post(new SetupRequest(configuration));
    }
}
