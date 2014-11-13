/*
 * Copyright 2012-2014 One Platform Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onepf.opfiab.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import org.onepf.opfiab.api.ManagedOPFIabHelper;

public class OPFIabSupportFragment extends Fragment implements OPFIabHelperHolder {

    @NonNull
    public static OPFIabFragment newInstance() {
        return new OPFIabFragment();
    }

    @NonNull
    private final OPFIabFragmentImpl implementation = new OPFIabFragmentImpl();

    @Override
    public void setOPFIabHelper(final @NonNull ManagedOPFIabHelper managedOPFIabHelper) {
        implementation.setOPFIabHelper(managedOPFIabHelper);
    }

    public OPFIabSupportFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        implementation.onCreate();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onResume() {
        super.onResume();
        implementation.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        implementation.onPause();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
