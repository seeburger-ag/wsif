/*
 * Copyright 2002-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation and was
 * originally based on software copyright (c) 2001, 2002, International
 * Business Machines, Inc., http://www.apache.org.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package shop;

/**
 * Creation date: (8/4/2001 2:33:16 PM)
 * @author <a href="mailto:gpfau@de.ibm.com">Gerhard Pfau</a>
 */
public class Item implements Cloneable, java.io.Serializable
{
	private java.lang.String fieldNumber;
	private java.lang.String fieldName;
	private int fieldQuantity;
	private long fieldPrice;
/**
 * Item constructor comment.
 */
public Item() {
	super();
}
/**
 * Item constructor comment.
 */
public Item(String itemNumber, String itemName, int itemQuantity) 
{
	super();
	fieldNumber = itemNumber ;
	fieldName = itemName ;
	fieldQuantity = itemQuantity ;
}
	public Object clone() 
	{
		Object obj = null ;
		try
		{
			obj = super.clone() ;
		}
		catch(CloneNotSupportedException ex)
		{
			// Should never happen
		}
	  return obj ;
	}
/**
 * Creation date: (8/4/2001 2:47:20 PM)
 * @return java.lang.String
 */
public java.lang.String getName() {
	return fieldName;
}
/**
 * Creation date: (8/4/2001 2:46:56 PM)
 * @return java.lang.String
 */
public java.lang.String getNumber() {
	return fieldNumber;
}
/**
 * Creation date: (8/4/2001 3:08:47 PM)
 * @return long
 */
public long getPrice() {
	return fieldPrice;
}
/**
 * Creation date: (8/4/2001 2:47:35 PM)
 * @return int
 */
public int getQuantity() {
	return fieldQuantity;
}
/**
 * Creation date: (8/4/2001 2:47:20 PM)
 * @param newName java.lang.String
 */
public void setName(java.lang.String newName) {
	fieldName = newName;
}
/**
 * Creation date: (8/4/2001 2:46:56 PM)
 * @param newNumber java.lang.String
 */
public void setNumber(java.lang.String newNumber) {
	fieldNumber = newNumber;
}
/**
 * Creation date: (8/4/2001 3:08:47 PM)
 * @param newPrice long
 */
public void setPrice(long newPrice) {
	fieldPrice = newPrice;
}
/**
 * Creation date: (8/4/2001 2:47:35 PM)
 * @param newQuantity int
 */
public void setQuantity(int newQuantity) {
	fieldQuantity = newQuantity;
}
public String toString()
{
	return super.toString() + 
		"\nname     = " + fieldName + 
		"\nnumber   = " + fieldNumber + 
		"\nprice    = " + fieldPrice + 
		"\nquantity = " + fieldQuantity ;

}
}
