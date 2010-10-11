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
 * Creation date: (8/6/2001 12:28:25 PM)
 * @author <a href="mailto:gpfau@de.ibm.com">Gerhard Pfau</a>
 */
public class SubmitOrderResult implements java.io.Serializable
{
	protected AirMilesContainer fieldAirMilesContainer;
	protected long fieldOrderConfirmationNumber;
public SubmitOrderResult() 
{
	super();
}
public SubmitOrderResult(AirMilesContainer airMilesContainer, long orderConfirmationNumber)
{
	super();
	fieldAirMilesContainer = airMilesContainer ;
	fieldOrderConfirmationNumber = orderConfirmationNumber ;
}
public AirMilesContainer getAirMilesContainer()
{
  return fieldAirMilesContainer;
}
public long getOrderConfirmationNumber()
{
  return fieldOrderConfirmationNumber;
}
public void setAirMilesContainer(AirMilesContainer newAirMilesContainer)
{
  fieldAirMilesContainer = newAirMilesContainer;
}
public void setOrderConfirmationNumber(long newOrderConfirmationNumber)
{
  fieldOrderConfirmationNumber = newOrderConfirmationNumber;
}
public String toString()
{
	return super.toString() + 
		"\norderConfirmationNumber = " + fieldOrderConfirmationNumber + 
		"\nairMilesContainer       =\n" + fieldAirMilesContainer ;
}
}
