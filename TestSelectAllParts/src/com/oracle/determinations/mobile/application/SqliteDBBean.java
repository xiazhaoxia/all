package com.oracle.determinations.mobile.application;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;

import java.io.RandomAccessFile;

import java.security.MessageDigest;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import oracle.adfmf.framework.api.AdfmfJavaUtilities;
import oracle.adfmf.util.Utility;
import oracle.adfmf.json.JSONObject;

import java.sql.PreparedStatement;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.Calendar;
import java.text.SimpleDateFormat;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import oracle.adfmf.json.JSONArray;
import oracle.adfmf.json.JSONException;
import sun.misc.*;
import oracle.adf.model.datacontrols.device.DeviceManagerFactory;
import java.util.Base64;;


public class SqliteDBBean {
    //private static ApplicationFilePathManager filePathmanager=new ApplicationFilePathManager();
    
    static String documentDir;
    static String dbPath;
    
    //constant variable
    static final String MSG_PARAMETERISNULL = "Parameter is null";
    static final String MSG_SUCCESS = "Success";
    static final String MSG_Error = "Error";
    static final String MSG_FILENOTFOUND = "File not found";
    
    protected static Map<String,Connection> dbConnectionMap = new HashMap<String,Connection>();
    
    private String __key__ = "e015c8bb-82bd-4ab7-8be2-394c97e9b835";
    private String __key0__ = "ecc75bd9-b464-4825-8eb1-22adf4277440";
    private String configFolderPath=documentDir+File.separator + "settings";
    private String __seed__ = null;
    private String __dbpassword__ = null;

    public SqliteDBBean() {
        super();
        //documentDir=filePathmanager.getBaseSettingFolderPath();
        //dbPath=filePathmanager.getLocalStoragePath();
        dbPath="d:\\database\\";
    }
    
    public static void commitConnections() {
        log("---===start commit all the transactions===---");
        long start = System.currentTimeMillis();
        Collection<Connection> connections = dbConnectionMap.values();
        for(Connection conn:connections) {
            try {
                conn.commit();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        long end = System.currentTimeMillis();
        log("---===commit transactions done! took "+(end-start)+"ms===---");
    }
    
    public Connection getConnection(String dbName) {
        Connection conn = null;
        if(dbConnectionMap.containsKey(dbName)) {
            conn = dbConnectionMap.get(dbName);
        } else {
            conn = createConnection(dbName);
            System.out.println("---===dbconnection for ["+dbName+"] was not found, a new connection was created");
        }
        return conn;
    }
    
    public Connection createConnection(String dbName) {
        File defaultDir = new File(dbPath);
        if (!defaultDir.exists()) {
            defaultDir.mkdir();
        }
        if(dbName==null || dbName.length()==0){
            throw new RuntimeException("Database doesn't exist.Please create database frist");
        }
        String db = dbPath + dbName;
        
        Connection conn = null;
        
        if(dbConnectionMap.containsKey(dbName)) {
            conn = dbConnectionMap.get(dbName);
        } else {
            try {
                conn = new SQLite.JDBCDataSource("jdbc:sqlite:" + db).getConnection(null,null);
                conn.setAutoCommit(false);
                dbConnectionMap.put(dbName, conn);
            } catch (SQLException e) {
                Utility.ApplicationLogger.logp(Level.WARNING, "SqliteDBBean", "createDatabase", e.getMessage());
                throw new RuntimeException("Database doesn't exist.Please  create database first.");
            }
        }
        return conn;
    }
    
    public String createDatabase(String dbName) {
        if(dbName==null){
            throw new RuntimeException(MSG_PARAMETERISNULL);
        }
        createConnection(dbName);
        return getReturnMessage("0",MSG_SUCCESS,dbName);
    }
    
    public void closeQuietly(Closeable object) {
        try {
            if(object!=null) object.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void closeQuietly(AutoCloseable object) {
        try {
            if(object!=null) object.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //delete database by name
    public String deleteDatabase(String databaseName) {
        
        Connection conn = getConnection(databaseName);
        closeQuietly(conn);
        
        File fdb = null;
        File fdbwithpath = null;
        if ((databaseName == null || databaseName.length() == 0)) {
            throw new RuntimeException(MSG_PARAMETERISNULL);
        } else {
            fdb = new File(databaseName);
            fdbwithpath = new File(dbPath + databaseName);
            if (fdb.exists()) {
                fdb.delete();
            }
            if (fdbwithpath.exists()) {
                fdbwithpath.delete();
            }
        }
        
        return getReturnMessage("0",MSG_SUCCESS);
    }


    //delete muti database by name array
    public String deleteDatabase(String[] databaseNames) {
        for (int i = 0; i < databaseNames.length; i++) {
            deleteDatabase(databaseNames[i]);
        }
        return getReturnMessage("0",MSG_SUCCESS);
    }
    
    //check if database already existed
    public String databaseExist(String databaseName) {
        File fdb = new File(databaseName);
        if (!fdb.exists()) {
            fdb = new File(dbPath + databaseName);
        }
        return fdb.exists() == true ?"1" :"0";
    }

    public boolean fileExist(String filePath) {
        File f = new File(filePath);
        return f.exists();
    }

    public String createTable(String dbName,String sql) throws Exception {
        return executeSql(dbName,sql);        
    }

    public String existTable(String dbName,String tableName) {
        if (tableName == null || tableName.length() == 0) {
            throw new RuntimeException("Parameter is null");
        }
        
        Connection conn = null;
        ResultSet rs = null;
        Statement stmt = null;
        String returnValue = "false";
        
        try {
            String sql = "select * from sqlite_master where type='table' and name='" + tableName + "'";
            
            conn = getConnection(dbName);
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);        
            returnValue = rs.next()?"1" : "0";
        } catch (Exception e) {
            
            System.out.println("----error info for existTable-----");
            System.out.println("dbName:"+dbName);
            System.out.println("tableName:"+tableName);
            
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            closeQuietly(rs);
            closeQuietly(stmt);
        }
        return returnValue;
    }

    //
    public String dropTable(String dbName,String tableName) throws Exception{
        String sql = "drop table if exists " + tableName;
        return executeSql(dbName,sql);
    }
    
    
    /*------------------------schema change----------------------------------*/
    public String renameTable(String dbName,String oldTableName,String newTableName) throws Exception{
        if(oldTableName==null){
            return getReturnMessage("1",MSG_PARAMETERISNULL);    
        }
        String sql="Alter table "+oldTableName+" rename to "+newTableName;
        return executeSql(dbName,sql);
    }

    public String addColumn(String dbName,String tableName,String col,String colType){
        String sql="ALTER TABLE ? ADD COLUMN ? ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = getConnection(dbName);
            pstmt = conn.prepareStatement(sql);            
            pstmt.setString(1,tableName);  
            pstmt.setString(2,col);
            pstmt.setString(3,colType);
            pstmt.executeUpdate();                       
            return getReturnMessage("0",MSG_SUCCESS);
        } catch (SQLException e) {
            System.out.println("----error info for addColumn-----");
            System.out.println("dbName:"+dbName);
            System.out.println("tableName:"+tableName);
            System.out.println("col:"+col);
            System.out.println("colType:"+colType);
            
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            closeQuietly(pstmt);
        }
    }

    /**
     * @param tableName
     * @param columns:[{col:col1,coltype:"string"},{col:col2,coltype:"int"}]
     * @return
     */
    public String addColumns(String dbName,String tableName,String columns){
        String sql="ALTER TABLE ? ADD COLUMN ? ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            if(Convertor.isJsonArray(columns)){
                JSONArray colArray = new JSONArray(columns);
                conn = getConnection(dbName);
                pstmt = conn.prepareStatement(sql);
                for(int i=0;i<colArray.length();i++){
                    pstmt.setString(1, tableName);  
                    pstmt.setString(2,colArray.getJSONObject(i).getString("col"));
                    pstmt.setString(3,colArray.getJSONObject(i).getString("coltype"));
                    pstmt.executeUpdate();
                }                
                return getReturnMessage("0",MSG_SUCCESS);
            } else {
                throw new RuntimeException("Wrong column parameters");    
            }
        } catch (Exception e) {
            System.out.println("----error info for addColumns-----");
            System.out.println("dbName:"+dbName);
            System.out.println("tableName:"+tableName);
            System.out.println("columns:"+columns);
            
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            closeQuietly(pstmt);
        }

    }


    /**
     * @param tableName
     * @param indexName
     * @param column:support json string like {"columns":[col1,col2]}
     * @return
     */
    public String createIndex(String dbName,String tableName,String indexName,String column){
        String sql="CREATE INDEX ? ON ? ";
        JSONArray cols=new JSONArray();
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            if (Convertor.isJson(column)) {
                JSONObject jb;
                jb = new JSONObject(column);
                cols = jb.getJSONArray("cols");
                for (int i = 0; i < cols.length(); i++) {
                    if (i == 0) {
                        sql += "( ";
                    }
                    sql += "?";
                    if (i == cols.length() - 1) {
                        sql += " )";
                    } else {
                        sql += ",";
                    }
                }
            } else {
                cols.put(0, column);
            }
            conn = getConnection(dbName);
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, indexName);
            pstmt.setString(2, tableName);

            for (int i = 0; i < cols.length(); i++) {
                pstmt.setString(i + 3, cols.get(i).toString());
            }
            pstmt.executeUpdate();
            return getReturnMessage("0", MSG_SUCCESS);
        } catch (Exception e) {
            System.out.println("----error info for createIndex-----");
            System.out.println("dbName:"+dbName);
            System.out.println("tableName:"+tableName);
            System.out.println("indexName:"+indexName);
            System.out.println("column:"+column);
            
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            closeQuietly(pstmt);
        }
    
    }
    
    public String createUniqueIndex(String dbName, String tableName, String indexName, String column) {
        String sql = "CREATE UNIQUE INDEX ? on ? (?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = getConnection(dbName);
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, indexName);
            pstmt.setString(2, tableName);
            pstmt.setString(3, column);
            pstmt.executeUpdate();
            return getReturnMessage("0", MSG_SUCCESS);
        } catch (SQLException e) {
            System.out.println("----error info for createUniqueIndex-----");
            System.out.println("dbName:"+dbName);
            System.out.println("tableName:"+tableName);
            System.out.println("indexName:"+indexName);
            System.out.println("column:"+column);
            
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            closeQuietly(pstmt);
        }
    }

    public String dropIndex(String dbName,String tableName,String indexName){
       String sql="DROP INDEX ? on ?;";
       Connection conn  = null;           
       PreparedStatement pstmt = null; 
       try {
           conn  = getConnection(dbName);           
           pstmt = conn.prepareStatement(sql);   
           pstmt.setString(1, tableName);
           pstmt.setString(2, indexName);
           pstmt.executeUpdate();
           return getReturnMessage("0",MSG_SUCCESS);
       } catch (SQLException e) {
           System.out.println("----error info for dropIndex-----");
           System.out.println("dbName:"+dbName);
           System.out.println("tableName:"+tableName);
           System.out.println("indexName:"+indexName);
           
           e.printStackTrace();
           throw new RuntimeException(e);
       } finally {
           closeQuietly(pstmt);
       }
    }
              
    public String getAllIndex(String dbName,String tableName){
        String sql=" SELECT * FROM sqlite_master WHERE type = 'index'";
        if(tableName.length()>0){
            sql+=" and tbl_name='"+tableName+"'";    
        }
        CSSPropertySet ja = executeQuery(dbName,sql);
        return ja.encodeAsString();
    }

    public CSSPropertySet executeQuery(String reqid) {
        return Convertor.GetPartResult(reqid);
    }

    //get the results from sql
    public CSSPropertySet executeQuery(String dbName,String sql) {
        this.log("SqliteDBBean:executeQuery:" + sql);
        CSSPropertySet propSet = new CSSPropertySet();
        String message="";
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        long start = System.currentTimeMillis();
        try {
            conn = getConnection(dbName);
            stmt = conn.createStatement();
            
            rs = stmt.executeQuery(sql);            
            propSet = Convertor.convertToPS(rs);  
            this.log("SqliteDBBean:convertToPS:done");
        } catch (Exception e) {
            System.out.println("----error info for executeQuery-----");
            System.out.println("dbName:"+dbName);
            System.out.println("sql:"+sql);
            
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            closeQuietly(rs);
            closeQuietly(stmt);
        }
        long end = System.currentTimeMillis();
        
        log("---===SqliteDBBean:executeQuery:time{"+(end-start)+"} response size:" + message.length() + " sql:"+sql);
        
        return propSet;
    }
    
    //get the results from sql
    public CSSPropertySet executeQuery(String dbName,String sql,String values) {
        this.log("SqliteDBBean:executeQuery:"+sql);
        String message="";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        long start = System.currentTimeMillis();
        try {
            JSONArray array = new JSONArray(values);            
            conn=getConnection(dbName); 
            pstmt = conn.prepareStatement(sql);             
            
            for(int i=0;i<array.length();i++){
                JSONObject row=array.getJSONObject(i);
                JSONArray column=row.getJSONArray("v");
                
                for(int j=0;j<column.length();j++){
                    pstmt.setString(j+1, column.get(j).toString());
                }
            }
            rs=pstmt.executeQuery();
            CSSPropertySet propSet = new CSSPropertySet();
            propSet = Convertor.convertToPS(rs);
            this.log("SqliteDBBean:convertToPS:done");
            long end = System.currentTimeMillis();
            this.log("---===SqliteDBBean:executeQuery:time{"+(end-start)+"} values_size:"+values.length()+" sql:"+sql);
            return propSet;
        } catch (Exception e) {
            System.out.println("----error info for executeQuery-----");
            System.out.println("dbName:"+dbName);
            System.out.println("sql:"+sql);
            System.out.println("values:"+values);
            
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            closeQuietly(rs);
            closeQuietly(pstmt);
        }
    }
    
    protected String getReturnMessage(String code,String message,String info){
        JSONObject re=new JSONObject();
        try {
            re.put("code", code);
            re.put("message", message);
            re.put("info",info);
        } catch (JSONException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return re.toString();
    }
    
    protected String getReturnMessage(String code,String message){        
        return getReturnMessage(code,message,"");
    }
    
    
    static protected void log(String msg) {
        String logdate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
        System.out.println(logdate + " " + msg);
    }
    
    //excute like insert or
    public String executeSql(String dbName,String sql) throws Exception{
        this.log("++sql:"+sql);        
        JSONArray ja = new JSONArray();
        String message="";
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try{
            conn = getConnection(dbName);
            stmt = conn.createStatement();
            boolean result =stmt.execute(sql);  
            rs = stmt.getResultSet();
            if(rs!=null){
                if(rs.getMetaData().getColumnCount()==0) {
                    message = ""+stmt.getUpdateCount();
                } else {
                    ja = Convertor.convertToJSON(rs); 
                    this.log("SqliteDBBean:convertToJSON:done");
                    if(ja.length()>0){
                        message= ja.toString();
                    }
                    this.log("SqliteDBBean:JSONtoString:done");
                }
            }
            else{
                int updateCount = stmt.getUpdateCount();
                if (updateCount == -1) {
                
                }else{
                    message=updateCount+"";    
                }
            }
            return message;
        } catch (Exception e) {
            System.out.println("----error info for executeSql-----");
            System.out.println("dbName:"+dbName);
            System.out.println("sql:"+sql);
            
            e.printStackTrace();
            String errorMessage = e.getMessage(); 
            throw e;
            /*
            if(sql!=null && sql.toLowerCase().startsWith("insert") && 
               errorMessage!=null && errorMessage.indexOf("error in step")>0) {
                System.out.println("----return empty string for insert error----");
                return errorMessage;
            } else {
                throw e;
            }
            */
        } finally {
            closeQuietly(rs);
            closeQuietly(stmt);
        }
    }
    //insert record|update record|delete record can use this one
    public String executeSql(String dbName,String sql,String values) throws Exception {
        this.log("++sql:"+sql);
        //System.out.println("++values"+values);
        long time1 = System.currentTimeMillis();
        long between = 0;
        JSONArray ja = new JSONArray();
        String message="";
        boolean bReturn=false;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {            
            JSONArray array = new JSONArray(values);            
            conn = getConnection(dbName);
            int arraySize = array.length();
            
            for(int i=0;i<arraySize;i++){
                try {
                    pstmt = conn.prepareStatement(sql);
                    JSONObject row=array.getJSONObject(i);
                    JSONArray column=row.getJSONArray("v");
                    
                    for(int j=0;j<column.length();j++){
                        pstmt.setString(j+1, column.get(j).toString());
                    }
                    if(DeviceManagerFactory.getDeviceManager().getOs().equalsIgnoreCase("android") && sql.toLowerCase().startsWith("update")){
                        pstmt.execute();
                        conn.commit();
                    } else {
                        pstmt.execute();
                    }
                } finally {
                    if(i<arraySize-1) {
                        closeQuietly(pstmt);
                    }
                }
            }
            rs = pstmt.getResultSet();           
            if(rs!=null){
                if(rs.getMetaData().getColumnCount()==0) {
                    message = ""+pstmt.getUpdateCount();
                } else {
                    this.log("mytest:convertToJSON:done");
                    ja = Convertor.convertToJSON(rs);
                    if(ja.length()>0){
                        message= ja.toString();
                    }
                    this.log("mytest:JSON to String:done");
                }
            }
            else{
                int updateCount = pstmt.getUpdateCount();
                if (updateCount == -1) {
                
                }else{
                    message=updateCount+"";    
                }
            }
            return message;
        } catch (Exception e) {
            System.out.println("----error info for executeSql-----");
            System.out.println("dbName:"+dbName);
            System.out.println("sql:"+sql);
            System.out.println("values:"+values);
            e.printStackTrace();
            
            String errorMessage = e.getMessage();    
            
            throw e;
            /*
            if(sql!=null && sql.toLowerCase().startsWith("insert") && 
               errorMessage!=null && errorMessage.indexOf("error in step")>0) {
                System.out.println("----return empty string for insert error----");
                return errorMessage;
            } else {
                throw new RuntimeException(e);
            }*/
            
        } finally {
            closeQuietly(rs);
            closeQuietly(pstmt);
        }
    }
 
    public void insertRecordSet(String dbName,String sql,String values) {
        Connection conn = null;
        PreparedStatement stmt = null;
        long start = System.currentTimeMillis();
        long recordCount = 0;
        try {
            conn = getConnection(dbName);
            stmt = conn.prepareStatement(sql);
            int allIndex = 0;
            for(;;) {
                int atIndex = values.indexOf("@", allIndex);
                if(atIndex==-1) break;
                String sRowLength = values.substring(allIndex,atIndex);
                int rowLength = Integer.parseInt(sRowLength);
                //String rowText = values.substring(atIndex+1, atIndex+1+rowLength);
                
                int hashIndex = values.indexOf("#",atIndex+1);
                if(hashIndex==-1) continue;
                //String rowHeader = rowText.substring(0,hashIndex);
                String rowHeader = values.substring(atIndex+1,hashIndex);
                String [] columnSize = rowHeader.split("\\|");
                int colCount = columnSize.length;
                int rowStart = hashIndex+1;
                for(int i=0;i<colCount;i++) {
                    int colSize = Integer.parseInt(columnSize[i]);
                    //String colData = rowText.substring(rowStart,rowStart+colSize);
                    String colData = values.substring(rowStart,rowStart+colSize);
                    stmt.setString(i+1, colData);
                    rowStart+=colSize;
                }
                stmt.addBatch();
                allIndex=atIndex+1+rowLength;
                recordCount++;
            }
            stmt.executeBatch();
        } catch (Exception e) {
            System.out.println("----error info for insertRecordSet-----");
            System.out.println("dbName:"+dbName);
            System.out.println("sql:"+sql);
            System.out.println("values:"+values);
            
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            closeQuietly(stmt);
        }
        long end = System.currentTimeMillis();
        System.out.println("---===time_test[insertRecordSet]{"+(end-start)+"} values_size:"+values.length()+" sql:"+sql);
        
    }
      
    public String getJSONString(JSONArray ja){
        try {
            return ja.get(0).toString();
        } catch (JSONException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    
        
    
    
    /////----------------------------------------
    
    public String getKey() {
        return __key__+getSeed();
    }
    
    public String getDbPassword() {
        try {
            if(__dbpassword__!=null) return __dbpassword__;
            MessageDigest md = MessageDigest.getInstance("md5");
            byte[] md5bytes = md.digest(getKey().getBytes("UTF-8"));
            BASE64Encoder encoder = new BASE64Encoder();
            String encodedString = encoder.encode(md5bytes);
            __dbpassword__ = encodedString;
            return encodedString;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return getKey();
    }
    
    
    
    private byte [] encryptAsBytes(String key, String data) {
        if (data == null)
            return new byte[0];
        if (data.length() == 0)
            return new byte[0];
        try {
            MessageDigest md = MessageDigest.getInstance("md5");
            byte[] md5bytes = md.digest(key.getBytes("UTF-8"));
            SecretKey skey = SecretKeyFactory.getInstance("DES").generateSecret(new DESKeySpec(md5bytes));

            Cipher cipher = Cipher.getInstance("DES");
            cipher.init(Cipher.ENCRYPT_MODE, skey);
            byte[] encodedBytes = cipher.doFinal(data.getBytes());
            return encodedBytes;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data.getBytes();
    }
    
    private String decryptBytes(String key, byte [] encodedBytes) {
        try {
            MessageDigest md = MessageDigest.getInstance("md5");
            byte[] md5bytes = md.digest(key.getBytes("UTF-8"));
            SecretKey skey = SecretKeyFactory.getInstance("DES").generateSecret(new DESKeySpec(md5bytes));

            Cipher cipher = Cipher.getInstance("DES");
            cipher.init(Cipher.DECRYPT_MODE, skey);

            byte[] dataBytes = cipher.doFinal(encodedBytes);
            String data = new String(dataBytes, "UTF-8");

            return data;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new String(encodedBytes);
    }
    
    private String getSeed() {
        try {
            if(__seed__!=null) return __seed__;
            String resPath = getResPath();
            
            File fResPath = new File(resPath);
            if(!fResPath.exists()) fResPath.mkdirs();
            
            String seedFilePath = resPath+File.separator+"key.png";
            File fSeedFile = new File(seedFilePath);
            
            if(fSeedFile.exists()) {
                RandomAccessFile fSeed = new RandomAccessFile(seedFilePath,"r");
                fSeed.seek(fffSize);
                int seed1Size = fSeed.read();
                int seed2Size = fSeed.read();
                byte[] bseed1 = new byte[seed1Size];
                byte[] bseed2 = new byte[seed2Size];
                fSeed.read(bseed1);
                fSeed.read(bseed2);
                String seed2 = decryptBytes(__key0__, bseed2);
                __seed__ = seed2;
                return __seed__;
            } else {
                String seed1 = UUID.randomUUID().toString();
                String seed2 = UUID.randomUUID().toString();
                FileOutputStream fout = new FileOutputStream(seedFilePath);
                fout.write(base64_decode(fff));
                byte [] bseed1 = encryptAsBytes(__key0__, seed1);
                byte [] bseed2 = encryptAsBytes(__key0__, seed2);
                fout.write(bseed1.length);
                fout.write(bseed2.length);
                fout.write(bseed1);
                fout.write(bseed2);
                __seed__ = seed2;
                return __seed__;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
    
    private byte[] base64_decode(String str) {
        try {
            BASE64Decoder decoder = new BASE64Decoder();
            byte[] encodedBytes = decoder.decodeBuffer(str);
            return encodedBytes;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    static String fff = "";
    static int fffSize = 1690;
    static {
        fff+="iVBORw0KGgoAAAANSUhEUgAAAB0AAAAdCAYAAAEhlFeZAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJ";
        fff+="bWFnZVJlYWR5ccllPAAABjxJREFUeNpi/P//P8MNBfn/zP/+MfxlYlrFcF1e7v8vZub//xgY/r8Q";
        fff+="EvzPBJJhPXGCgfHtWwbOHz8ZmIDKVv98+YLhs6kpw6/qKgaAAGIEatkMVOUDlGDQePCQkQGkD6Qf";
        fff+="BICSH5hA+mAAZB7Ln65Ohu87dzF8FhZi+MvLOxMggEBmbAHKePN9/coAU/2dg53hEzc3yF2TGW7J";
        fff+="yvz/yMUFNhNs9qdPYDZIDCj3AeQssI7fLCxg3f/5+MBskBhQ7h8j1KuTgHIxIEeAAEgTEMwGOrsc";
        fff+="IAATZH/CMAgF8VMSxZAU7AwdNLNkgeyQv7NCJwg0UEv9iLR9zzahguj9fBze8R8EiUST1a8PvIVI";
        fff+="9KjZ89WEgPbpUedcrB+NUVyeVNsGe3ewzqH1HudxLJq51CnBxAiyLRvLUjRzedQyz99TiANVUSl4";
        fff+="rdFS0/vyxoC5THWN9dQh9D3yMGDtOqhpAvM965Uu9i/KjaJcPgJwSnY3CAJBEJ7j+BMSg8ZXbcgC";
        fff+="7MCSNBRgQg+0YgXG+OYfeuLMneK7JBsIu3O7+8199/QEol5uOCTO0VkHJXolrcWD8YwtXiaMKW/F";
        fff+="oZMov4et9E7Jw0rMruyCaLeDWS5xXMz9/Lcs1TT7WGRHFJSEWBKm+AzPagWcTjCTCcBdxucLD335";
        fff+="iS5ZNvOWqIvoxwys17xNFthuYZrGi9G2AR3zyWcq6fztcNqJtj0Zpq5hNptf9+kUqCr/qbzqVC9d";
        fff+="rEtwzdJUYJyNkBdF2JlJE8j64o6iW1kMO/P/4WtF/w/ttwCUVLFKA0EQnawXE/0HWz/AQuukDCkE";
        fff+="v0ED/oBFGq1SXEAQ/Q4hRUrJXwiCvaRJJca7y8X33u6G5cTChcll7+bt7Lx5MxF8gf097Ijurg46";
        fff+="IhiRa8djJJt32BWAL6zxMzbnIiOQRjJcANbhyuKDJPr1QOCWUfbLyjooT6cocUBlacdUexk02LZv";
        fff+="SK1oZ4qupmsjt26QO4GMmkbMVivbQv4fZ6e6OqNnDidTCB5Y6H/MT6TxfAiCGdJHpYT5yYQIBPC5";
        fff+="A7FdqKZkRR+JQSNo480Nhx44GFgrz72SkhX9BIwtLMBsZrZYmM3nSK625kp9HZONpi7p9fwhy+Uv";
        fff+="YOrrPN3e4qmKFrS6WyAo+kmz/CG9HAdr6LHJqkoCH34vwtgUML5kwgSwbs060pkCWGMQ0ZcY3a+E";
        fff+="gPly0+9bBWbraW7laGTVeGyfB11rTSb2hasf3t7JVylBck94Xv9TqzexO06weYQdgzv3R3dgXNgr";
        fff+="7BLd8fYjQKtV0xpFEERfz8yKuy7GYwhE0GtAA/sH8gfUS3I218jeco0HjYKBoJccgn8h/gCFRa96";
        fff+="CkT0EogfpxwChgTW4Mz0WK+6enZ2I3owDcNMN9P9uqpevaq4sSMLjPadsbgJ+Z2vFD3ELh7INw+1";
        fff+="v3i4a7wT3TkZzZe0TEBLmsk8ftWItDiZ+Ux2lSp2yjI+leU5qqAQTuHVGq9WhTCXGu7U5rUOwHSl";
        fff+="x6x83qy89Gs28dQiaVajqmplgouADlFDi4kn6qShvyDodd481dwpVBZapsj8jqDqWu/PO83A3e4u";
        fff+="3Px8vcZyl5s45yKlLs3UA3LKjQwGGIEoYK0ir8Ejn/821IHHx6MFqefUscS8E/JZecpMm1K2jbt1";
        fff+="ZK1WRKlRbmcHWFkZgdCqppBwHB6OXWTkqeJciLKwMZBEwRukcUtLcNvbAWhxUXo4OVi6EUj3Vg8W";
        fff+="yoleI1SnSEY703BclTQalT+N6WlUEpOoWpiZCeus0v8xTDaToABGeVKdc7e5KWktqi86rB7Z2DCW";
        fff+="/PjnwWMpZOcRh3iZUp0mk97VeL3Njo60FmFhQclRg/HNOUfUdLrZ1lj0AnNTrZ7NtCGelt2LSJnK";
        fff+="rIl5SsA6ZQzcUuY78/RAPm7yR5p+9UEf2bUptTjtduG/fUUyO4tSWlk3HKJcX0drawt+MIAXFmer";
        fff+="q/BiYflxD+7WbQwfP1LQ9toaUqkCl+SiJ6/foPj8Kd5xn6DSt2EQmwnfaettfSJlUQqRl42n0pl1";
        fff+="7y/j14f3aEt7N3z3FuncHNJ7d5FLPvqzn8ilEb7c6yF78hTu9ARl5woqkVHG9ezLQQRkg9mPgp9a";
        fff+="H9VvCuUFCj7p/lDE/hknvwFg0u70omOKlAAAAABJRU5ErkJggg==";
    }
     
       
        private String getResPath() {
            //return filePathmanager.getResPath();
        	return "";
        }

}
