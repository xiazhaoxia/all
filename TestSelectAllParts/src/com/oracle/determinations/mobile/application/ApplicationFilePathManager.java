package com.oracle.determinations.mobile.application;

import java.io.File;

import oracle.adf.model.datacontrols.device.DeviceManager;
import oracle.adf.model.datacontrols.device.DeviceManagerFactory;

import oracle.adfmf.framework.api.AdfmfJavaUtilities;

public class ApplicationFilePathManager {
    
    private static DeviceManager deviceManager = DeviceManagerFactory.getDeviceManager();;
    
    protected String internalFolder="";
    protected String externalFolder="";
    public ApplicationFilePathManager() {
        initBaseFolder();
    }
    public void initBaseFolder(){
        File appDirPath = new File(AdfmfJavaUtilities.getDirectoryPathRoot(AdfmfJavaUtilities.ApplicationDirectory));        
        //Library Directory
        File rootPath = appDirPath.getParentFile();
      
        String deviceDir = AdfmfJavaUtilities.getDirectoryPathRoot(AdfmfJavaUtilities.DeviceOnlyDirectory);       
        
        File fCachePath = new File(deviceDir); 
        File fLibrary = fCachePath.getParentFile(); 
        
        String osVersion=deviceManager.getOs();
        if(osVersion.equalsIgnoreCase("ios"))
        {
            internalFolder=fLibrary.getAbsolutePath();
            externalFolder=appDirPath.getAbsolutePath();
        }
        else if(osVersion.equalsIgnoreCase("android"))
        {
            internalFolder= rootPath.getAbsolutePath();
            externalFolder=fLibrary.getAbsolutePath();
        }
    }
    
    public String getBaseSettingFolderPath(){
        //return externalFolder;
        //eula
        return AdfmfJavaUtilities.getDirectoryPathRoot(1);
    }
    
    public String getSettingPropertiesFolderPath(){
        //return externalFolder+File.separator + "settings";
        //bug 22231143
        return internalFolder+File.separator + "settings";
    }
    
    public String getSettingPropertiesFilePath(){
        String path = getSettingPropertiesFolderPath();
        return path+File.separator +"appSettings.properties";
    }
    
    public String getAttachmentDownloadPath(){
        return externalFolder+File.separator + "siebelFile" ;
    }   
    public String getAttachmentTempPath(){
        //return externalFolder+File.separator + "tempFile";
        //bug 22231143
        return externalFolder+File.separator + "tempFile";
    }
    public String getLocalStoragePath(){
        return internalFolder+File.separator + "database"+File.separator ;
    }
    public  String getResPath()
    {  
        return internalFolder+File.separator+"res";
    }

    public void setExternalFolder(String externalFolder) {
        this.externalFolder = externalFolder;
    }

    public String getExternalFolder() {
        return externalFolder;
    }
}


