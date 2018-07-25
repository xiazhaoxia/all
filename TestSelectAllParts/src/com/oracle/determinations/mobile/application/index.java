package com.oracle.determinations.mobile.application;

public class index {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.print("hello world");
		String dbName="OracleCRM811xLocalStore_siebelserviceformobileenu.BFRIEDEL";
		
		SqliteDBBean sqlconnector=new SqliteDBBean();
		sqlconnector.getConnection(dbName);
		CSSPropertySet ps= sqlconnector.executeQuery(dbName,"select * from ApplicationData");
		System.out.print(ps.toString());
		
		CSSPropertySet ps1= Convertor.GetPartResult("0");
		System.out.print(ps1.toString());
	}

}
