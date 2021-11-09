/*
 * Copyright (C) 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.car.settings.enterprise;

import android.annotation.Nullable;
import android.car.drivingstate.CarUxRestrictions;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import androidx.preference.TwoStatePreference;

import com.android.car.settings.common.FragmentController;

/**
 * Controller for the header preference the device admin details screen.
 */
public final class DeviceAdminAddHeaderPreferenceController
        extends BaseDeviceAdminAddPreferenceController<TwoStatePreference> {

    private @Nullable ActivationListener mActivationListener;

    public DeviceAdminAddHeaderPreferenceController(Context context, String preferenceKey,
            FragmentController fragmentController, CarUxRestrictions uxRestrictions) {
        super(context, preferenceKey, fragmentController, uxRestrictions);
    }

    DeviceAdminAddHeaderPreferenceController setActivationListener(ActivationListener listener) {
        mActivationListener = listener;
        return this;
    }

    @Override
    protected Class<TwoStatePreference> getPreferenceType() {
        return TwoStatePreference.class;
    }

    @Override
    protected void updateState(TwoStatePreference preference) {
        CharSequence name = mDeviceAdminInfo.loadLabel(mPm);
        Drawable icon = mDeviceAdminInfo.loadIcon(mPm);
        CharSequence description = getDescription(mDeviceAdminInfo);

        mLogger.d("updateState: name=" + name  + ", description=" + description);
        preference.setTitle(name);
        preference.setIcon(icon);
        if (!TextUtils.isEmpty(description)) {
            preference.setSummary(description);
        }

        ComponentName componentName = mDeviceAdminInfo.getComponent();
        preference.setChecked(isActive(componentName));
        if (isProfileOrDeviceOwner(componentName)) {
            // TODO(b/170332519): once work profiles are supported, they could be removed
            mLogger.d("updateState(): " + ComponentName.flattenToShortString(componentName)
                    + " is PO or DO");
            preference.setEnabled(false);
        } else {
            preference.setEnabled(true);
        }
    }

    @Override
    protected boolean handlePreferenceChanged(TwoStatePreference preference, Object newValue) {
        boolean activated = (boolean) newValue;
        ComponentName admin = mDeviceAdminInfo.getComponent();
        if (activated) {
            mLogger.i("Activating " + ComponentName.flattenToShortString(admin));
            // TODO(b/192372143): support refreshing
            mDpm.setActiveAdmin(admin, /* refreshing= */ false);
        } else {
            mLogger.i("Deactivating " + ComponentName.flattenToShortString(admin));
            mDpm.removeActiveAdmin(admin);
        }
        if (mActivationListener != null) {
            mActivationListener.onChanged(activated);
        }
        return true;
    }

    private boolean isActive(ComponentName componentName) {
        return mDpm.isAdminActive(componentName);
    }

    interface ActivationListener {
        void onChanged(boolean active);
    }
}
