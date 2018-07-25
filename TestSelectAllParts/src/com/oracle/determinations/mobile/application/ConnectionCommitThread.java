package com.oracle.determinations.mobile.application;


public class ConnectionCommitThread implements Runnable {

    public Object syncObject = new Object();
    public int requestCount = 0;
    public int requestCountLastSecond = 0;
    public long lastCommitTimeStamp = System.currentTimeMillis();
    public static final long MaxCommitDuration = 60000;

    public ConnectionCommitThread() {
        super();
    }
    
    public void run() {
        for(;;) {
            synchronized(syncObject) {
                long now = System.currentTimeMillis();
                if((now-lastCommitTimeStamp)>=MaxCommitDuration) {
                    SqliteDBBean.commitConnections();
                    requestCountLastSecond = 0;
                    requestCount = 0;
                    lastCommitTimeStamp = now;
                } else {
                    if(requestCountLastSecond==0) {
                        if(requestCount>0) {
                            SqliteDBBean.commitConnections();
                            requestCount = 0;
                            lastCommitTimeStamp = now;
                        }
                    } else {
                        requestCountLastSecond = 0;
                    }
                }

            }
            try {Thread.sleep(500);} catch (Exception e) {e.printStackTrace();}
        }
    }    
}