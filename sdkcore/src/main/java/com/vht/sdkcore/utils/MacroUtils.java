package com.vht.sdkcore.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;

public class MacroUtils {
    public static String getValue(Context context, String key) {
        try {
            ApplicationInfo applicationInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), 128);
            Object value = applicationInfo.metaData.get(key);
            return value + "";
        } catch (Exception var4) {
            var4.printStackTrace();
            return "";
        }
    }
}
