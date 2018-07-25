package com.oracle.determinations.mobile.application;

import java.sql.ResultSet;

import java.sql.ResultSetMetaData;

import java.util.Map;

import oracle.adfmf.json.JSONArray;
import oracle.adfmf.json.JSONException;
import oracle.adfmf.json.JSONObject;
import java.util.*;
import java.math.*;


public class Convertor {

    public Convertor() {
        super();
    }
    /**
     * Convert a result set into a JSON Array
     * @param resultSet
     * @return a JSONArray
     * @throws Exception
     */
    public static JSONArray convertToJSON(ResultSet resultSet)
            throws Exception {
        JSONArray jsonArray = new JSONArray();
        
        ResultSetMetaData rsmd = resultSet.getMetaData();
        int total_rows = rsmd.getColumnCount();
        String [] columnNames = new String[total_rows];
        for(int i=0;i<total_rows;i++) {
            columnNames[i] = rsmd.getColumnName(i+1);
        }
        
        while (resultSet.next()) {
            JSONObject obj = new JSONObject();
            for (int i = 0; i < total_rows; i++) {
                Object value = resultSet.getObject(i + 1);
                String str = (value == null) ? "" : value.toString(); 
                obj.put(columnNames[i], str);
            }
            jsonArray.put(obj);
        }
        return jsonArray;
        
    }

    private  static Map<String,CSSPropertySet> map = new HashMap<String,CSSPropertySet>();
    private  static  int reqId=0;

    public static CSSPropertySet GetPartResult(String key){
        System.out.print("-----------key--------:"+key);        
        return map.get(key);
    }

    public static int GetReqId(){
        return reqId++;
    }

    /**
     * Convert a result set into a PropertySet
     * @param resultSet
     * @return a CSSPropertySet
     * @throws Exception
     */
    public static CSSPropertySet convertToPS(ResultSet resultSet)
            throws Exception {
        CSSPropertySet result = new CSSPropertySet();
        ArrayList<CSSPropertySet> resultList = new ArrayList<CSSPropertySet>();
        resultList.add(result);


        
        ResultSetMetaData rsmd = resultSet.getMetaData();
        int total_rows = rsmd.getColumnCount();
        String [] columnNames = new String[total_rows];
        for(int i=0;i<total_rows;i++) {
            columnNames[i] = rsmd.getColumnName(i+1);
        }
        
        String totalString="";
        String tempString="";
        double total=0;
        double maxLen=10*1024*1024;
        boolean isPart=false;
        int col=0;
        int _reqid=0;
        JSONArray first=new JSONArray();
        JSONArray last=new JSONArray();


        while (resultSet.next()) {
            System.out.println(resultList.size());
            result=resultList.get(resultList.size()-1);
            CSSPropertySet obj = new CSSPropertySet();

            obj.setType("Record");
            //id|bc|content from 1
            for (int i = 0; i < total_rows; i++) {
            	
                Object value = resultSet.getObject(i + 1);
                String str = (value == null) ? "" : value.toString(); 
                
                if(Convertor.isJsonArray(str)){
                	
                    double colLen=str.length();
                    if(total+colLen>maxLen){                     	
                    	
                        JSONArray jsonArray=new JSONArray(str);
                      
                        // the count of Array
                        int count=jsonArray.length();
//                        int ratio = (int) Math.round(colLen / (maxLen - total));                 
//                        int partCount=Math.round(count/ratio);
//                    
                        JSONObject jb=null;
                        int jblen=0;
                        for(int j=0;j<count;j++){
                        	jb=jsonArray.getJSONObject(j);
                        	jblen=jb.toString().length();
                        	if(total+jblen <= maxLen) {                        		
                        		first.put(jsonArray.getJSONObject(j));      
                        		total+=jblen;
                        	}else {
                        		last.put(jsonArray.getJSONObject(j));
                        	}
                        }
                        str=first.toString();                         
                        isPart=true;
                        _reqid=Convertor.GetReqId();
                        obj.addProperty("reqid", _reqid+"");                      
                  
                    }   
                    else{
                        total+=colLen;                       
                    }               
                  
                }
                obj.addProperty(columnNames[i], str);
            }            
            result.addSubset(obj);
            
            if(isPart==true){
                CSSPropertySet objPart = new CSSPropertySet();
                for (int j = 0; j < total_rows; j++) {
                    if(col==j){
                        objPart.addProperty(columnNames[j], last.toString());
                    }else{
                        objPart.addProperty(columnNames[j], obj.getProperty(columnNames[j]));
                    }
                }
                
                CSSPropertySet resultPart=new CSSPropertySet();
                resultPart.addSubset(objPart);
                resultList.add(resultPart);
                isPart=false;
                total=last.toString().length();
                map.put(_reqid+"",resultList.get(resultList.size()-1));
            }      

        }

        return resultList.get(0);
        
    }

    /**
     * Convert a result set into a XML List
     * @param resultSet
     * @return a XML String with list elements
     * @throws Exception if something happens
     */
    public static String convertToXML(ResultSet resultSet)
            throws Exception {
        StringBuffer xmlArray = new StringBuffer("");
        while (resultSet.next()) {
            int total_rows = resultSet.getMetaData().getColumnCount();
            xmlArray.append("");
        }
        xmlArray.append("");
        return xmlArray.toString();
    }
    
    public static boolean isJson(String json){
        try{
            if(json==null){
                return false;
            }
            JSONObject object=new JSONObject(json);
            return true;
        } catch (JSONException e) {
            return false;
        }
    }
    
    public static boolean isJsonArray(String json){
        try{
            if(json==null){
                return false;
            }
            JSONArray jsonArray=new JSONArray(json);
            return true;
        } catch (JSONException e) {
            return false;
        }
    }
}
