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

package org.onepf.sample.trivialdrive.ui.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.onepf.opfiab.api.IabHelper;
import org.onepf.sample.trivialdrive.R;
import org.onepf.sample.trivialdrive.TrivialBilling;
import org.onepf.sample.trivialdrive.TrivialData;

public class TrivialView extends RelativeLayout
        implements View.OnClickListener, SharedPreferences.OnSharedPreferenceChangeListener {

    private View btnDrive;
    private View btnBuyGas;
    private View btnBuyPremium;
    private View btnBuySubscription;

    private ImageView ivCar;
    private ImageView ivGas;

    private IabHelper iabHelper;
    private boolean hasSubscription;

    public TrivialView(Context context) {
        super(context);
        init();
    }

    public TrivialView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TrivialView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public TrivialView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    protected void init() {
        final Context context = getContext();
        inflate(context, R.layout.view_trivial, this);
        if (isInEditMode()) {
            return;
        }

        btnBuyGas = findViewById(R.id.btn_buy_gas);
        btnBuyPremium = findViewById(R.id.btn_buy_premium);
        btnBuySubscription = findViewById(R.id.btn_buy_subscription);
        btnDrive = findViewById(R.id.btn_drive);

        ivGas = (ImageView) findViewById(R.id.img_gas);
        ivCar = (ImageView) findViewById(R.id.img_car);

        btnDrive.setOnClickListener(this);
        btnBuyGas.setOnClickListener(this);
        btnBuyPremium.setOnClickListener(this);
        btnBuySubscription.setOnClickListener(this);

        setEnabled(false);
        setHasPremium(false);
        setHasSubscription(false);
        updateGas();

        TrivialData.registerOnSharedPreferenceChangeListener(this);
    }

    private boolean canBuyGas() {
        return !hasSubscription && TrivialData.canAddGas();
    }

    private void drive() {
        if (!hasSubscription && !TrivialData.canSpendGas()
                && btnBuyGas.isEnabled()) {
            btnBuyGas.callOnClick();
        } else {
            TrivialData.spendGas();
            Toast.makeText(getContext(), R.string.msg_drive_success, Toast.LENGTH_SHORT).show();
        }
    }

    private void updateGas() {
        if (hasSubscription) {
            ivGas.setImageResource(R.drawable.img_gas_inf);
        } else {
            ivGas.setImageResource(R.drawable.img_gas_level);
            ivGas.getDrawable().setLevel(TrivialData.getGas());
        }
        btnBuyGas.setEnabled(canBuyGas());
    }

    public void setHasPremium(final boolean hasPremium) {
        ivCar.setImageResource(hasPremium ? R.drawable.img_car_premium : R.drawable.img_car);
    }

    public void setHasSubscription(final boolean hasSubscription) {
        this.hasSubscription = hasSubscription;
        updateGas();
    }

    public void setIabHelper(IabHelper iabHelper) {
        this.iabHelper = iabHelper;
    }

    @Override
    public void onClick(final View v) {
        if (v == btnDrive) {
            drive();
        } else if (v == btnBuyGas) {
            iabHelper.purchase(TrivialBilling.SKU_GAS);
        } else if (v == btnBuyPremium) {
            iabHelper.purchase(TrivialBilling.SKU_PREMIUM);
        } else if (v == btnBuySubscription) {
            iabHelper.purchase(TrivialBilling.SKU_SUBSCRIPTION);
        }
    }

    @Override
    public void setEnabled(final boolean enabled) {
        super.setEnabled(enabled);
        btnBuyGas.setEnabled(enabled && canBuyGas());
        btnBuyPremium.setEnabled(enabled);
        btnBuySubscription.setEnabled(enabled);
    }

    @Override
    protected void onDetachedFromWindow() {
        TrivialData.unregisterOnSharedPreferenceChangeListener(this);
        super.onDetachedFromWindow();
    }

    @Override
    public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences,
                                          final String key) {
        updateGas();
    }
}
