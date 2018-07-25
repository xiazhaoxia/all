package com.oracle.determinations.mobile.application;

import java.io.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.net.Socket;

import java.nio.ByteBuffer;

import java.security.MessageDigest;

import java.sql.SQLException;

import java.text.SimpleDateFormat;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

import oracle.adfmf.json.*;

import sun.misc.*;

import javax.net.ssl.SSLSocket;

public class SqliteRequestProcessor {
    protected static Map<String, Object> objectPool = new HashMap<String, Object>();

    private static ConnectionCommitThread commitThread = new ConnectionCommitThread();
    
    private static Map<String, StringBuilder> dataCacheMap = new HashMap<>();
    
    public static final Object obejctLock = new Object(); 
    
    public SqliteRequestProcessor() {
        super();
    }
    
    static {
        new Thread(commitThread).start();
    }
    
    public String invokeSqliteMethodForLargeData(String methodParams) {
        synchronized(obejctLock) {
            int startSeperator = methodParams.indexOf("|");
            String tag = methodParams.substring(0, startSeperator);
            
            if(methodParams.endsWith("siebel_end")) {
                int lastSeparator = methodParams.lastIndexOf("|");
                dataCacheMap.get(tag).append(methodParams.substring(startSeperator+1, lastSeparator));
                String data = dataCacheMap.get(tag).toString();
                this.log("invokeSqliteMethodForLargeData:request_length:" + data.length());
                String result = this.invokeSqliteMethod(data);
                dataCacheMap.get(tag).setLength(0);
                return result;
            } else {
                this.log("invokeSqliteMethodForLargeData:composing:request_length:" + methodParams.length());
                if(!dataCacheMap.containsKey(tag)) {
                    dataCacheMap.put(tag, new StringBuilder());
                }
                dataCacheMap.get(tag).append(methodParams.substring(startSeperator+1));
                return "";
            }
        }
        
    }

    public String invokeSqliteMethod(String methodParams) {
        String result = "";
        synchronized(commitThread.syncObject) {
            result = buildResponse(methodParams);
            commitThread.requestCount++;
            commitThread.requestCountLastSecond++;
        }
        return result;
    }

    public String buildResponse(String sData) {
        String resultString = "";
        try {
            long response_start = System.currentTimeMillis();
            int paramSizeIndex = sData.indexOf("{");
            int paramSize = -1;
            if (paramSizeIndex > 0) {
                paramSize = Integer.parseInt(sData.substring(0, paramSizeIndex));
            }
            if (paramSize > 0) {        
                String paramString = sData.substring(paramSizeIndex, paramSizeIndex + paramSize);
                JSONObject paramJson = new JSONObject(paramString);
                String className = paramJson.getString("className");
                String methodName = paramJson.getString("methodName");
                long start = paramJson.getLong("start");
                long now = System.currentTimeMillis();
                this.log("RequestProcessor:response:" + now + " paramSize:" + paramSize + " took:" + (now - start) +
                         "ms paramString:" + paramString);
                int reqId = paramJson.getInt("reqId");
                int paramCount = paramJson.getInt("methodParamCount");
                int[] paramSizes = new int[paramCount];
                if (paramCount > 0) {
                    JSONArray paramSizeArray = paramJson.getJSONArray("methodParamLengths");
                    for (int i = 0; i < paramCount; i++) {
                        paramSizes[i] = paramSizeArray.getInt(i);
                    }
                }
                String paramData = sData.substring(paramSizeIndex + paramSize);

                String[] params = new String[paramCount];
                int paramIndex = 0;
                for (int i = 0; i < paramCount; i++) {
                    params[i] = paramData.substring(paramIndex, paramIndex + paramSizes[i]);
                    paramIndex += paramSizes[i];
                }

                CSSPropertySet psResult = this.invokeMethod(reqId, className, methodName, params);
                resultString = psResult.encodeAsString();
                this.log("RequestProcessor:Result.encodeAsString:done:length:" + resultString.length());
                long response_end = System.currentTimeMillis();
                this.log("RequestProcessor:method_time:" + methodName + " took:" + (response_end - response_start) +
                         "ms paramString:" + paramString);

            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return resultString;
        }
    }

    private CSSPropertySet invokeMethod(Integer reqId, String className, String methodName,
                                        String[] params) throws Exception {
        int paramCount = params.length;
        CSSPropertySet psResult = new CSSPropertySet();
        Boolean success = false;
        String errorMessage = "";
        Integer errorCode = 0;
        this.log("RequestProcessor:invokeMethod:begin:" + methodName);

        try {
            CSSPropertySet psQueryResult = null;
            if (methodName.equals("executeQuery")) {
                SqliteDBBean dbean = new SqliteDBBean();

                if (paramCount == 2) {
                    psQueryResult = dbean.executeQuery(params[0], params[1]);
                    success = true;
                } else if (paramCount == 3) {
                    psQueryResult = dbean.executeQuery(params[0], params[1], params[2]);
                    success = true;
                }
                psQueryResult.setType("data");
                psResult.addSubset(psQueryResult);
            } else {
                String resultString = "";
                Class theClass = null;
                Object theObject = null;

                theObject = objectPool.get(className);
                if (theObject == null) {
                    theClass = Class.forName(className);
                    theObject = theClass.newInstance();
                    objectPool.put(className, theObject);
                } else {
                    theClass = theObject.getClass();
                }

                Class[] paramTypes = new Class[paramCount];
                for (int i = 0; i < paramCount; i++) {
                    paramTypes[i] = String.class;
                }
                Method theMethod = theClass.getMethod(methodName, paramTypes);
                Object resultObject = null;

                resultObject = theMethod.invoke(theObject, params);
                success = true;

                if (resultObject != null) {
                    resultString = resultObject.toString();
                }
                psResult.addProperty("data", resultString);
            }
        } catch (InvocationTargetException ex) {
            ex.printStackTrace();
            Throwable targetException = ex.getTargetException();
            if (targetException != null) {
                errorMessage = targetException.getMessage();
                if (targetException instanceof SQLException) {
                    SQLException sqlex = (SQLException) targetException;
                    errorCode = sqlex.getErrorCode();

                    if (errorCode == 0) {
                        errorCode = 1;
                        if (errorMessage.equalsIgnoreCase("SQLite.Exception: error in step"))
                            errorMessage = "constraint failed";
                    }
                }
            } else {
                errorMessage = ex.getMessage();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            errorMessage = ex.getMessage();
        }
        this.log("RequestProcessor:invokeMethod:done:" + methodName);

        psResult.setType("SqlResult");
        psResult.addProperty("success", success.toString());
        psResult.addProperty("reqId", reqId.toString());
        if (!success) {
            psResult.addProperty("errorMessage", errorMessage == null ? "" : errorMessage);
            psResult.addProperty("errorCode", errorCode.toString());
        }
        return psResult;
    }


    protected void log(String msg) {
        String logdate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
        System.out.println(logdate + " " + msg);
    }
}
