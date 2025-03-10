/*
 * Copyright (C) 2021 StatiXOS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.syberia.android.systemui;

import android.content.Context;

import com.android.internal.logging.UiEventLogger;
import com.android.systemui.Dumpable;
import com.android.systemui.R;
import com.android.systemui.VendorServices;
import com.android.systemui.dagger.SysUISingleton;
import com.android.systemui.flags.FeatureFlags;
import com.android.systemui.statusbar.phone.StatusBar;

import com.google.android.systemui.columbus.ColumbusContext;
import com.google.android.systemui.columbus.ColumbusServiceWrapper;
import com.google.android.systemui.elmyra.ElmyraContext;
import com.google.android.systemui.elmyra.ElmyraService;
import com.google.android.systemui.elmyra.ServiceConfigurationGoogle;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.inject.Inject;

import dagger.Lazy;

@SysUISingleton
public class SyberiaServices extends VendorServices {

    private final UiEventLogger mUiEventLogger;
    private final Lazy<ColumbusServiceWrapper> mColumbusServiceLazy;
    private final Lazy<ServiceConfigurationGoogle> mServiceConfigurationGoogle;
    private final ArrayList<Object> mServices = new ArrayList<>();

    @Inject
    public SyberiaServices(Context context, UiEventLogger uiEventLogger, Lazy<ServiceConfigurationGoogle> serviceConfigurationGoogle, Lazy<ColumbusServiceWrapper> columbusService) {
        super(context);
        mUiEventLogger = uiEventLogger;
        mServiceConfigurationGoogle = serviceConfigurationGoogle;
        mColumbusServiceLazy = columbusService;
    }

    @Override
    public void start() {
        if (mContext.getPackageManager().hasSystemFeature("android.hardware.context_hub")
                && new ElmyraContext(mContext).isAvailable()) {
            addService(new ElmyraService(mContext, mServiceConfigurationGoogle.get(), mUiEventLogger));
        }
        if (new ColumbusContext(mContext).isAvailable()) {
            addService(mColumbusServiceLazy.get());
        }
    }

    @Override
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        for (int i = 0; i < mServices.size(); i++) {
            if (mServices.get(i) instanceof Dumpable) {
                ((Dumpable) mServices.get(i)).dump(fileDescriptor, printWriter, strArr);
            }
        }
    }

    private void addService(Object obj) {
        if (obj != null) {
            mServices.add(obj);
        }
    }

}
