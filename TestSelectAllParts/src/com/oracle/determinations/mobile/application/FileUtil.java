package com.oracle.determinations.mobile.application;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.io.OutputStream;

import java.net.URL;

import java.sql.Statement;


public class FileUtil {
    public FileUtil() {
        super();
    }
    /**
    * ????????????buffer?
    * @param buffer buffer
    * @param filePath ????
    * @throws IOException ??
    * @author cn.outofmemory
    * @date 2013-1-7
    */
   public static void readToBuffer(StringBuffer buffer, String filePath){
    try{
        /*InputStream is = new FileInputStream(filePath);
        String line; // ???????????
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        line = reader.readLine(); // ?????
        while (line != null) { // ?? line ???????
           buffer.append(line); // ????????? buffer ?
           line = reader.readLine(); // ?????
        }
        reader.close();
        is.close();  */ 
        FileReader fr = new FileReader(new File(filePath)); 
        BufferedReader reader = new BufferedReader(fr); 
        String nextLine;
        while ((nextLine = reader.readLine()) != null) {            
            buffer.append(nextLine);
        }       
    
    }catch(FileNotFoundException e){
        e.printStackTrace();
        throw new RuntimeException(e);
    }catch (Exception e) {
        e.printStackTrace();
        throw new RuntimeException(e);
    } 
  
   }

   /**
    * ????????
    * @param filePath ??????
    * @return ????
    * @throws IOException ??
    * @author cn.outofmemory
    * @date 2013-1-7
    */
   public static String readFile(String filePath) throws IOException {
       StringBuffer sb = new StringBuffer();
       readToBuffer(sb, filePath);
       return sb.toString();
   }
   
   public static void deliverCert(){
        InputStream inputStream = null;
        OutputStream outputStream = null;
       
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        URL url = cl.getResource("cert/myCA.cer");
        String absolutePath = url.getFile();
        File f = new File(absolutePath);       
        if (f.exists()) {
            System.out.println("suxia write certificate source:"+f.toString());

            try {
                inputStream = new FileInputStream(f);
                ApplicationFilePathManager afm=new ApplicationFilePathManager();        
               
                
                File targetFile=new File(afm.getExternalFolder()+"/files/SiebelMobileInternal.cer");
                System.out.println("suxia write certificate to:"+targetFile.toString());
                
                targetFile.getParentFile().mkdirs(); // Will create parent directories if not exists
                //targetFile.createNewFile();          
                outputStream = new FileOutputStream(targetFile,true);   
                int read = 0;
                byte[] bytes = new byte[1024];
                while ((read = inputStream.read(bytes)) != -1) {
                        outputStream.write(bytes, 0, read);
                }
    
                    System.out.println("Deliver certificate successfully!");
            
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (inputStream != null) {
                        try {
                                inputStream.close();
                        } catch (IOException e) {
                                e.printStackTrace();
                        }
                    }
                    if (outputStream != null) {
                        try {
                                // outputStream.flush();
                                outputStream.close();
                        } catch (IOException e) {
                                e.printStackTrace();
                        }

                    }
                }


        }
        

    }

}
