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

import androidx.preference.TwoStatePreference;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.android.car.settings.common.PreferenceController;
import com.android.car.settings.enterprise.DeviceAdminAddHeaderPreferenceController.ActivationListener;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

public final class DeviceAdminAddHeaderPreferenceControllerTest extends
        BasePreferenceControllerTestCase {

    private DeviceAdminAddHeaderPreferenceController mController;

    @Mock
    private TwoStatePreference mPreference;

    @Mock
    private ActivationListener mActivationListener;

    @Before
    public void setController() {
        mController = new DeviceAdminAddHeaderPreferenceController(mSpiedContext,
                mPreferenceKey, mFragmentController, mUxRestrictions);
        mController.setDeviceAdmin(mDefaultDeviceAdminInfo);
    }

    @Test
    public void testGetAvailabilityStatus_admin() throws Exception {
        assertAvailability(mController.getAvailabilityStatus(),
                PreferenceController.AVAILABLE);
    }

    @Test
    public void testGetAvailabilityStatus_noAdmin() throws Exception {
        DeviceAdminAddHeaderPreferenceController controller =
                new DeviceAdminAddHeaderPreferenceController(mSpiedContext, mPreferenceKey,
                        mFragmentController, mUxRestrictions);

        assertAvailability(controller.getAvailabilityStatus(),
                PreferenceController.CONDITIONALLY_UNAVAILABLE);
    }

    @Test
    public void testUpdateState_activeAdminWithNoProperties() throws Exception {
        mockActiveAdmin(mDefaultAdmin);

        mController.updateState(mPreference);

        verifyPreferenceSetChecked(mPreference, true);
        verifyPreferenceEnabled(mPreference);
        verifyPreferenceTitleSet(mPreference, mDefaultDeviceAdminInfo.loadLabel(mRealPm));
        verifyPreferenceSummaryNeverSet(mPreference);
        verifyPreferenceIconSet(mPreference);
    }

    @Test
    public void testUpdateState_activeAdminWithAllProperties() throws Exception {
        mController.setDeviceAdmin(mFancyDeviceAdminInfo);
        mockActiveAdmin(mFancyAdmin);

        mController.updateState(mPreference);

        verifyPreferenceSetChecked(mPreference, true);
        verifyPreferenceEnabled(mPreference);
        verifyPreferenceTitleSet(mPreference, mFancyDeviceAdminInfo.loadLabel(mRealPm));
        verifyPreferenceSummarySet(mPreference, mFancyDeviceAdminInfo.loadDescription(mRealPm));
        verifyPreferenceIconSet(mPreference);
    }

    @Test
    public void testUpdateStatus_inactiveAdmin() throws Exception {
        mockInactiveAdmin(mDefaultAdmin);

        mController.updateState(mPreference);

        verifyPreferenceSetChecked(mPreference, false);
        verifyPreferenceEnabled(mPreference);
        verifyPreferenceTitleSet(mPreference, mDefaultDeviceAdminInfo.loadLabel(mRealPm));
    }

    @Test
    public void testUpdateStatus_deviceOwner() throws Exception {
        mockDeviceOwner();

        mController.updateState(mPreference);

        verifyPreferenceSetChecked(mPreference, true);
        verifyPreferenceDisabled(mPreference);
        verifyPreferenceTitleSet(mPreference, mDefaultDeviceAdminInfo.loadLabel(mRealPm));
    }

    @Test
    public void testUpdateStatus_profileOwner() throws Exception {
        mockProfileOwner();

        mController.updateState(mPreference);

        verifyPreferenceSetChecked(mPreference, true);
        verifyPreferenceDisabled(mPreference);
        verifyPreferenceTitleSet(mPreference, mDefaultDeviceAdminInfo.loadLabel(mRealPm));
    }

    @Test
    public void testHandlePreferenceChanged_activateAdmin_withoutListener() {
        mockInactiveAdmin(mDefaultAdmin);

        mController.handlePreferenceChanged(mPreference, true);

        verifyAdminNeverDeactivated();
        verifyAdminActivated();
        verifyActivationListenerNotCalled();
    }

    @Test
    public void testHandlePreferenceChanged_activateAdmin_withListener() {
        mockInactiveAdmin(mDefaultAdmin);
        mController.setActivationListener(mActivationListener);

        mController.handlePreferenceChanged(mPreference, true);

        verifyAdminNeverDeactivated();
        verifyAdminActivated();
        verifyActivationListenerCalled(true);
    }

    @Test
    public void testHandlePreferenceChanged_deactivateAdmin_withoutListener() {
        mockActiveAdmin(mDefaultAdmin);

        mController.handlePreferenceChanged(mPreference, false);

        verifyAdminNeverActivated();
        verifyAdminDeactivated();
        verifyActivationListenerNotCalled();
    }

    @Test
    public void testHandlePreferenceChanged_deactivateAdmin_withListener() {
        mockActiveAdmin(mDefaultAdmin);
        mController.setActivationListener(mActivationListener);

        mController.handlePreferenceChanged(mPreference, false);

        verifyAdminNeverActivated();
        verifyAdminDeactivated();
        verifyActivationListenerCalled(false);
    }

    private void verifyPreferenceSetChecked(TwoStatePreference preference, boolean isChecked) {
        verify(preference).setChecked(isChecked);
    }

    private void verifyActivationListenerNotCalled() {
        verify(mActivationListener, never()).onChanged(anyBoolean());
    }

    private void verifyActivationListenerCalled(boolean active) {
        verify(mActivationListener).onChanged(active);
    }
}