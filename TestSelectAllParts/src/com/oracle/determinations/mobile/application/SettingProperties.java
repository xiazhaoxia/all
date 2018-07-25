package com.oracle.determinations.mobile.application;

import java.io.File;
import java.io.FileInputStream;

import java.io.FileOutputStream;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

import oracle.adfmf.framework.api.AdfmfJavaUtilities;

public class SettingProperties {
    private static ApplicationFilePathManager filePathmanager=new ApplicationFilePathManager();
    
    public SettingProperties() {

        super();
    }

    public static void ClearResetValue() {
        try {
            String path =filePathmanager.getBaseSettingFolderPath()+File.separator +"settings-mobile-flag.properties";
            File fSetting = new File(path);
            if (fSetting.exists()) {
                fSetting.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}


class SortedProperties extends Properties {
    /**    * Overrides, called by the store method.    */
    @SuppressWarnings("unchecked")
    public synchronized Enumeration keys() {
        Enumeration keysEnum = super.keys();
        Vector keyList = new Vector();
        while (keysEnum.hasMoreElements()) {
            keyList.add(keysEnum.nextElement());
        }
        Collections.sort(keyList);
        return keyList.elements();
    }
}
