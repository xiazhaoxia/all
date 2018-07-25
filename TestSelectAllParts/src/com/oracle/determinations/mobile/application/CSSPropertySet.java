
//////////////////////////////////////////////////////////////////////////////
//
// Copyright (C) 1998, Siebel Systems, Inc., All rights reserved.
//
//  $Revision: 1 $
//      $Date: 10/04/2015 2:23a $
//    $Author: Hastings $ of last update
//
// CREATOR:    Qizhi Xia
//
//////////////////////////////////////////////////////////////////////////////
package com.oracle.determinations.mobile.application;

import java.util.NoSuchElementException;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.ArrayList;
import java.io.*;

/**
 * In addition to storing name/value pairs, this class
 * provides storage for subsets of type <code>Hashtable</code>
 * and also adds a type attribute to the property set.
 */
public final class CSSPropertySet implements Serializable
{
   private String SEPARATOR = "*";
   private Hashtable    m_propertyMap = new Hashtable ();
   private ArrayList    m_subsetList   = new ArrayList ();
   private String          m_type;
   private String          m_value;

   public CSSPropertySet ()
   {
   }

   public CSSPropertySet (String type)
   {
      m_type = type;
   }

   public void addProperty (String key,
                            String value)
   {
      // In C++ m_propertyMap is a CMapStringToString,
      // so NULL value is equivalent to an empty CString ()
      m_propertyMap.put (key, (value == null ? "" : value));
   }

   public void addSubset (CSSPropertySet subset)
   {
      m_subsetList.add ( subset);
   }

   public Enumeration enumPropertyKeys ()
   {
      return (m_propertyMap.keys ());
   }

   public String getProperty (String key)
   {
      try
      {
         return ((String) m_propertyMap.get (key));
      }
      catch (NoSuchElementException e)
      {
         return (null);
      }
   }

   public boolean getProperty (String       key,
                               CSSStringRef value)
   {
      String   val;

      try
      {
         val = (String) m_propertyMap.get (key);
      }
      catch (NoSuchElementException e)
      {
         val = null;
      }

      value.setValue (val);

      return (val != null);
   }

   public String getType ()
   {
      return (m_type);
   }

   public String getValue ()
   {
      return (m_value);
   }
   
   public boolean removeProperty (String key)
   {
      return (m_propertyMap.remove (key) != null);
   }

   public void reset ()
   {
      m_propertyMap.clear ();
      m_subsetList.clear ();
   }

   public void setType (String type)
   {
      m_type = type;
   }

   public void setValue (String val)
   {
      m_value = val;
   }
   
   private void writeInt(StringBuffer  output, int n)
   {
	   output.append(n);
	   output.append (SEPARATOR);
   }

   private void writeStr(StringBuffer  output, String str)
   {
	   if(str == null)
		   str = "";
	   output.append(str.length());
	   output.append (SEPARATOR);
	   if(str.length() != 0) {
		   output.append(str);
	   }
   }
   
   //
   // CSSPropertySet::ToString
   //
   // Parameters:
   //
   //    none
   //
   // Returns:
   //
   // String
   //
   // Description:
   // 
   // Encodes a CSSPropertySet as a string.  The coding is as following :
   // [Length of Type]#[Type][Size of propertyMap]#[KeyLength]#[Key]
   // [ValueLength]#[Value][KeyLength]#[Key][ValueLength]#[Value]....
   // [NumberOfSubsets]#[SubsetTypeLength]#[SubsetType][encoding for subset]
   // #[SubsetTypeLength]#[SubsetType][encoding for subset]...

   public String toString ()
   {
      Enumeration     pos;
      String          key;
      String          value;
      CSSPropertySet  subSet;
      StringBuffer    output = new StringBuffer ();

      // [Size of propertyMap]#
      this.writeInt(output, m_propertyMap.size ());
      // [NumberOfSubsets]#
      this.writeInt(output, m_subsetList.size ());

      // [Length of Type]#[Type]
      this.writeStr(output, m_type);
      // [Length of Value]#[Value]
      this.writeInt(output, 3);
      this.writeStr(output, m_value);
      
      pos = m_propertyMap.keys ();
      
      while (pos.hasMoreElements())
      {
         key = (String) pos.nextElement ();
         value = (String) m_propertyMap.get (key);

         // #[KeyLength]#[Key]
         this.writeStr(output, key);

         // [ValueLength]#[Value]
         this.writeStr(output, value);
      }

      for (int i=0; i<m_subsetList.size(); i++)
      {
         subSet = (CSSPropertySet) m_subsetList.get (i);

         // [encoding for subSet]
         output.append (subSet.toString ());
      }
      
      return (output.toString ());
   }
   
   public String encodeAsString() {
	   String output = "@0*0*";//header
	   return output + this.toString();
   }
}


/**
* This wraps a String object so that it can be manipulated by reference.
*/
final class CSSStringRef
{
	private String    m_val = null;
	
	
	public CSSStringRef ()
	{
	}
	
	
	public CSSStringRef (String val)
	{
	setValue (val);
	}
	
	
	public String getValue ()
	{
	return (m_val);
	}
	
	
	public void setValue (String val)
	{
	m_val = val;
	}
	
	
	public String toString ()
	{
	return (m_val);
	}
}