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

package org.onepf.opfiab.model;

import android.app.Application;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.onepf.opfiab.api.AdvancedIabHelper;
import org.onepf.opfiab.billing.BillingProvider;
import org.onepf.opfiab.listener.BillingListener;
import org.onepf.opfiab.listener.DefaultBillingListener;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Model class representing library configuration.
 *
 * @see org.onepf.opfiab.OPFIab#init(Application, Configuration)
 */
@SuppressWarnings("PMD.MissingStaticMethodInNonInstantiatableClass")
public final class Configuration {

    private static final long DEFAULT_REQUEST_DELAY = 50L;


    @NonNull
    private final Set<BillingProvider> providers;
    @Nullable
    private final BillingListener billingListener;
    private final long subsequentRequestDelay;
    private final boolean skipUnauthorised;
    private final boolean autoRecover;

    Configuration(@NonNull final Set<BillingProvider> providers,
                  @Nullable final BillingListener billingListener,
                  final long subsequentRequestDelay,
                  final boolean skipUnauthorised,
                  final boolean autoRecover) {
        this.subsequentRequestDelay = subsequentRequestDelay;
        this.autoRecover = autoRecover;
        this.providers = Collections.unmodifiableSet(providers);
        this.billingListener = billingListener;
        this.skipUnauthorised = skipUnauthorised;
    }

    /**
     * Get supported billing provider.
     *
     * @return Collection of BillingProvider objects.
     */
    @SuppressWarnings("TypeMayBeWeakened")
    @NonNull
    public Set<BillingProvider> getProviders() {
        return providers;
    }

    /**
     * Get persistent listener used to handle all billing events.
     *
     * @return BillingListener object. Can be null.
     */
    @Nullable
    public BillingListener getBillingListener() {
        return billingListener;
    }

    /**
     * Get minimal time gap between requests with the same type.
     *
     * @return Time gap in milliseconds.
     */
    public long getSubsequentRequestDelay() {
        return subsequentRequestDelay;
    }

    /**
     * Indicates whether unauthorized {@link BillingProvider}s should be skipped during setup
     * process.
     *
     * @return True if unauthorized BillingProviders should be skipped. False otherwise.
     * @see BillingProvider#isAuthorised()
     */
    public boolean skipUnauthorised() {
        return skipUnauthorised;
    }

    /**
     * Indicates whether library should attempt to pick another suitable {@link BillingProvider} if
     * current one become unavailable.
     *
     * @return True if library will attempt to pick another BillingProvider. False otherwise.
     */
    public boolean autoRecover() {
        return autoRecover;
    }

    /**
     * Builder class for {@link Configuration} object.
     */
    public static class Builder {

        @NonNull
        private final Set<BillingProvider> providers = new LinkedHashSet<>();
        @Nullable
        private BillingListener billingListener;
        private long subsequentRequestDelay = DEFAULT_REQUEST_DELAY;
        private boolean skipUnauthorised;
        private boolean autoRecover;

        /**
         * Add supported billing provider.
         * <br>
         * During setup process billing providers will be considered in order they were added.
         *
         * @param provider BillingProvider object to add.
         * @return this object.
         */
        public Builder addBillingProvider(@NonNull final BillingProvider provider) {
            providers.add(provider);
            return this;
        }

        /**
         * Set global listener to handle all billing events.
         * <br>
         * This listener will be stored in static reference.
         *
         * @param billingListener BillingListener object to use.
         * @return this object.
         * @see DefaultBillingListener
         */
        public Builder setBillingListener(@Nullable final BillingListener billingListener) {
            this.billingListener = billingListener;
            return this;
        }

        /**
         * Set time gap between attempts to execute enqueued requests.
         * <br>
         * Default valued is 50ms.
         *
         * @param subsequentRequestDelay Time gap in milliseconds.
         * @see #getSubsequentRequestDelay()
         * @see AdvancedIabHelper
         */
        public Builder setSubsequentRequestDelay(final long subsequentRequestDelay) {
            this.subsequentRequestDelay = subsequentRequestDelay;
            return this;
        }

        /**
         * Set flag indicating whether unauthorized {@link BillingProvider}s should be skipped
         * during setup.
         *
         * @param skipUnauthorised True to skip unauthorized BillingProviders.
         * @return this object.
         * @see BillingProvider#isAuthorised()
         */
        public Builder setSkipUnauthorised(final boolean skipUnauthorised) {
            this.skipUnauthorised = skipUnauthorised;
            return this;
        }

        /**
         * Set flag indicating whether library should attempt to substitute current
         * {@link BillingProvider} is it becomes unavailable.
         *
         * @param autoRecover True to attempt substitution of unavailable provider.
         * @return this object.
         * @see BillingProvider#isAvailable()
         */
        public Builder setAutoRecover(final boolean autoRecover) {
            this.autoRecover = autoRecover;
            return this;
        }

        /**
         * Construct new configuration object.
         *
         * @return Newly constructed Configuration instance.
         */
        public Configuration build() {
            return new Configuration(providers, billingListener, subsequentRequestDelay,
                                     skipUnauthorised, autoRecover);
        }
    }
}
