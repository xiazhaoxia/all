package com.oracle.determinations.mobile.application;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Sample
{
  public static void main(String[] args) throws ClassNotFoundException
  {
    // load the sqlite-JDBC driver using the current class loader
    Class.forName("org.sqlite.JDBC");
    
    Connection connection = null;
    try
    {

      // create a database connection
      connection = DriverManager.getConnection("jdbc:sqlite:d:/database/OracleCRM811xLocalStore_siebelserviceformobileenu.BFRIEDEL");
      Statement statement = connection.createStatement();
      statement.setQueryTimeout(30);  // set timeout to 30 sec.   
   
      ResultSet rs = statement.executeQuery("select * from ApplicationData");
      CSSPropertySet ps=Convertor.convertToPS(rs);
      
      System.out.print("rs0 :-----"+ps.toString().length());
		
		CSSPropertySet ps1= Convertor.GetPartResult("0");
		System.out.print("rs1:-------"+ps1.toString().length());
		

		CSSPropertySet ps2= Convertor.GetPartResult("1");
		System.out.print("rs1:-------"+ps2.toString().length());
    
    }
    catch(SQLException e)
    {
      // if the error message is "out of memory", 
      // it probably means no database file is found
      System.err.println(e.getMessage());
    } catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    finally
    {
      try
      {
        if(connection != null)
          connection.close();
      }
      catch(SQLException e)
      {
        // connection close failed.
        System.err.println(e);
      }
    }
  }
}