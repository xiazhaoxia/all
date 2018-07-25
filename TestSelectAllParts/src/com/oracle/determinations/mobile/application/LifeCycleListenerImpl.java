package com.oracle.determinations.mobile.application;

import java.lang.reflect.Method;

import java.util.logging.Level;

import oracle.adfmf.application.LifeCycleListener;
import oracle.adfmf.util.Utility;
import oracle.adfmf.util.logging.Trace;


public class LifeCycleListenerImpl implements LifeCycleListener {
    private String oldUrl;

    public LifeCycleListenerImpl() {
    }

    public void start() {
        
 
        Trace.log(Utility.ApplicationLogger, Level.SEVERE, LifeCycleListenerImpl.class, "start",
                  "!!!!!!!!!!Application Start!!!!!!!!!!");
        
       
        SettingProperties.ClearResetValue();
            
        FileUtil.deliverCert();
    }

    public void stop() {
        Trace.log(Utility.ApplicationLogger, Level.SEVERE, LifeCycleListenerImpl.class, "Stop",
                  "!!!!!!!!!!Application Stop!!!!!!!!!!");
    }

    public void activate() {
        Trace.log(Utility.ApplicationLogger, Level.SEVERE, LifeCycleListenerImpl.class, "activate",
                  "!!!!!!!!!!Application Activate!!!!!!!!!!");
        try {
            System.out.println("---===try to start https web server===---");
            Class clsHttpServer = Class.forName("com.oracle.determinations.mobile.application.HttpServer");
            Method startHttpServer = clsHttpServer.getMethod("startServer", new Class[]{ int.class });
            startHttpServer.invoke(null, 12344);          
            System.out.println("---===https web server started===---");
            
            System.out.println("---===try to start http cert server===---");
            Class certServer = Class.forName("com.oracle.determinations.mobile.application.CertificateServer");
            Method startcertServer = certServer.getMethod("startServer", new Class[]{ int.class });
            startcertServer.invoke(null, 12355);          
            System.out.println("---===http cert server started===---");
            
        } catch (Exception e) {
            e.printStackTrace();
        } 
    }


    public void deactivate() {
        Trace.log(Utility.ApplicationLogger, Level.SEVERE, LifeCycleListenerImpl.class, "deactivate",
                  "!!!!!!!!!!Application Deactivate!!!!!!!!!!");

    }
}
