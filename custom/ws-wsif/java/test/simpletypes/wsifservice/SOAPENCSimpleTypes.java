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

package simpletypes.wsifservice;

import java.math.BigDecimal;

/**
 * @author owenb
 */
public class SOAPENCSimpleTypes {
	public String getString(String s) {
		return "getString OK";
	}

	public Boolean getBoolean(boolean b) {
		return new Boolean(true);
	}

	public Float getFloat(float f) {
		return new Float(1234.0);
	}
	
	public Double getDouble(double d) {
		return new Double(1234.0);
	}

	public BigDecimal getDecimal(BigDecimal bd) {
		return new BigDecimal(1234.0);
	}
			
	public Integer getInteger(int i) {
		return new Integer(1234);
	}
	
	public Short getShort(short s) {
		short sh = 1234;
		return new Short(sh);
	}			

	public Byte getByte(byte b) {
		int i = 11;
		byte bt = (byte) i;		
		return new Byte(bt);
	}

	public byte[] getBase64(byte[] b) {
		int i = 11;
		byte bt = (byte) i;
		return new byte[] {bt};
	}
}

