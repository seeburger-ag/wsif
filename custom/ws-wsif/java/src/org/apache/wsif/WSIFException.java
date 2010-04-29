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

package org.apache.wsif;

import java.rmi.RemoteException;
import org.apache.wsif.logging.Trc;

/**
 * A WSIFException is used to indicate something going wrong
 * within the Web service invocation framework and not something
 * that is related to the service invocation itself.
 *
 * @author Paul Fremantle
 * @author Alekander Slominski
 * @author Matthew J. Duftler
 * @author Sanjiva Weerawarana
 * @author Nirmal Mukhi
 */
public class WSIFException extends RemoteException {
	private static final long serialVersionUID = 1L;

    public WSIFException(String msg) {
        super(msg);
        Trc.entry(this,msg);
        Trc.exit();
    }

    public WSIFException(String msg, Throwable targetException) {
        this(msg);
        Trc.entry(this,msg,targetException);
        this.detail = targetException;
        Trc.exit();
    }

    public void setTargetException(Throwable targetException) {
    	Trc.entry(this,targetException);
        this.detail = targetException;
        Trc.exit();
    }

    public Throwable getTargetException() {
    	Trc.entry(this);
    	Trc.exit(detail);
        return detail;
    }

    //  public String getMessage () {
    //    String msg = super.getMessage ();
    //    if (detail != null) {
    //      msg += "; target message was '" + detail.getMessage () + "'";
    //    }
    //    return msg;
    //  }
    //
    //
    //  public String toString () {
    //    return "[WSIFException: msg=" + getMessage () +
    //      ((detail != null) ? ("; targetException=" + detail)
    //       : "") + "]";
    //  }
}
