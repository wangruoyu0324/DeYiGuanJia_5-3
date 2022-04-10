/*******************************************************************************
 * Copyright (c) 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.chuxinbuer.deyiguanjia.database;

import android.content.Context;

import org.json.JSONObject;

public class UpdateManager {

    private static AppConfigPB appConfig;
    private static UpdateManager curUpdateManager;


    public static UpdateManager newUpdateManager() {
        if (curUpdateManager == null) {
            curUpdateManager = new UpdateManager();
        }
        appConfig = AppConfigManager.getInitedAppConfig();
        return curUpdateManager;
    }


    public void updateLocalDataAfter(JSONObject data, final Context context) {//在更新本地络数据后的操作

    }

    public void updateLocalDataBefore(String data) {//在更新本地络数据前的操作-
        // 此处做自动更新的标准是:这些数据的变更，可能不是用户软件内行为引起的。

    }
}
