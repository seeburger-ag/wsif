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
 * Creation date: (8/4/2001 2:26:18 PM)
 * @author <a href="mailto:gpfau@de.ibm.com">Gerhard Pfau</a>
 */
public class Address implements java.io.Serializable
{
  private java.lang.String fieldCity;

	private java.lang.String fieldStreet;
public Address()
{
  super();
}
public Address(String city, String street)
{
  super();
  fieldCity = city ;
  fieldStreet = street ;
}
/**
 * Creation date: (8/5/2001 1:53:58 PM)
 * @return java.lang.String
 */
public java.lang.String getCity() {
	return fieldCity;
}
/**
 * Creation date: (8/5/2001 1:54:34 PM)
 * @return java.lang.String
 */
public java.lang.String getStreet() {
	return fieldStreet;
}
/**
 * Creation date: (8/5/2001 1:53:58 PM)
 * @param newCity java.lang.String
 */
public void setCity(java.lang.String newCity) {
	fieldCity = newCity;
}
/**
 * Creation date: (8/5/2001 1:54:34 PM)
 * @param newStreet java.lang.String
 */
public void setStreet(java.lang.String newStreet) {
	fieldStreet = newStreet;
}
}
