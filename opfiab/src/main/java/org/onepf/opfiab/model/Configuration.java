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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.onepf.opfiab.billing.BillingProvider;
import org.onepf.opfiab.listener.BillingListener;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

@SuppressWarnings("PMD.MissingStaticMethodInNonInstantiatableClass")
public final class Configuration {

    private static final long DEFAULT_REQUEST_DELAY = 500L;


    @NonNull
    private final Set<BillingProvider> providers;
    @Nullable
    private final BillingListener billingListener;
    private final long subsequentRequestDelay;
    private final boolean skipUnauthorised;
    private final boolean autoRecover;

    Configuration(@NonNull final Set<BillingProvider> providers,
                          @Nullable final BillingListener billingListener,
                          final long subsequentRequestDelay, final boolean skipUnauthorised,
                          final boolean autoRecover) {
        this.subsequentRequestDelay = subsequentRequestDelay;
        this.autoRecover = autoRecover;
        this.providers = Collections.unmodifiableSet(providers);
        this.billingListener = billingListener;
        this.skipUnauthorised = skipUnauthorised;
    }

    @SuppressWarnings("TypeMayBeWeakened")
    @NonNull
    public Set<BillingProvider> getProviders() {
        return providers;
    }

    @Nullable
    public BillingListener getBillingListener() {
        return billingListener;
    }

    /**
     * Minimal time gap between requests with the same type.
     *
     * @return time gap in milliseconds.
     */
    public long getSubsequentRequestDelay() {
        return subsequentRequestDelay;
    }

    public boolean skipUnauthorised() {
        return skipUnauthorised;
    }

    public boolean autoRecover() {
        return autoRecover;
    }

    public static class Builder {

        @NonNull
        private final Set<BillingProvider> providers = new LinkedHashSet<>();
        @Nullable
        private BillingListener billingListener;
        private long subsequentRequestDelay = DEFAULT_REQUEST_DELAY;
        private boolean skipUnauthorised;
        private boolean autoRecover;

        public Builder addBillingProvider(@NonNull final BillingProvider provider) {
            providers.add(provider);
            return this;
        }

        public Builder setBillingListener(@Nullable final BillingListener billingListener) {
            this.billingListener = billingListener;
            return this;
        }

        /**
         * Set time gap between attempts to execute enqueued requests.<br>
         * Default valued is 300ms.
         *
         * @param subsequentRequestDelay time gap in milliseconds.
         */
        public Builder setSubsequentRequestDelay(final long subsequentRequestDelay) {
            this.subsequentRequestDelay = subsequentRequestDelay;
            return this;
        }

        public Builder setSkipUnauthorised(final boolean skipUnauthorised) {
            this.skipUnauthorised = skipUnauthorised;
            return this;
        }

        public Builder setAutoRecover(final boolean autoRecover) {
            this.autoRecover = autoRecover;
            return this;
        }

        public Configuration build() {
            return new Configuration(providers, billingListener, subsequentRequestDelay,
                                     skipUnauthorised, autoRecover);
        }
    }
}
