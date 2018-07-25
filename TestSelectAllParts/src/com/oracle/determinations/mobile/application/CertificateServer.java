package com.oracle.determinations.mobile.application;


import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import java.io.OutputStream;

import java.net.*;

import java.security.KeyStore;

import java.text.SimpleDateFormat;

import java.util.Calendar;

import javax.net.ssl.*;


public class CertificateServer implements Runnable {

    private int port;

    public CertificateServer(int port) {
        this.port = port;
    }

    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(port, 0, InetAddress.getLoopbackAddress());
            System.out.println("====http certificate server====Started at port:" + port);
            for (;;) {
                Socket s = serverSocket.accept();
                new Thread(new CertificateHandler(s)).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    
    public static class CertificateHandler implements Runnable {

        protected Socket s;
        protected InputStream in;
        protected OutputStream out;
        protected static MimeTypeMap mimeTypeMap = new MimeTypeMap();

        public CertificateHandler(Socket s) {
            this.s = s;
        }

        public void run() {
            try {
                out = s.getOutputStream();
                ClassLoader cl = Thread.currentThread().getContextClassLoader();
                URL url = cl.getResource("cert/myCA.cer");
                String absolutePath = url.getFile();
                File f = new File(absolutePath);

                String ext = "cer";
                String mimeType = mimeTypeMap.get(ext);
                long flen = f.length();
                int statusCode = f.exists() ? 200 : 404;
                println("HTTP/1.1 " + statusCode + " OK");
                
                if (f.exists()) {
                    println("Content-Type: " + mimeType);
                    println("Connection: close");
                    println("Access-Control-Allow-Origin: *");
                    println("Content-Length: " + flen);
                    println("");

                    byte[] buff = new byte[512];
                    InputStream in = new FileInputStream(f);
                    for (;;) {
                        int len = in.read(buff);
                        if (len == -1)
                            break;
                        out.write(buff, 0, len);
                    }
                    in.close();
                } else {
                    String str = "file not found!";
                    println("Content-Type: " + mimeType);
                    println("Content-Length: " + (str.length() + 2));
                    println("Connection: close");
                    println("");
                    println(str);
                }

                out.flush();

                CertificateHandler.log("====MAF_certificate_server====" + System.currentTimeMillis() + ":finish:status:" + statusCode + ":" +
                         absolutePath);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void println(String s) throws Exception {
            out.write((s + "\r\n").getBytes("UTF-8"));
        }

        static protected void log(String msg) {
            String logdate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
            System.out.println(logdate + " " + msg);
        }
    }

    public static void startServer(int port) {
        CertificateServer server = new CertificateServer(port);
        new Thread(server).start();
    }
}
