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

package org.apache.wsif.attachments;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.activation.DataHandler;

import org.apache.wsif.WSIFException;
import org.apache.wsif.logging.Trc;

/**
 * WSIFAttachmentPart is a MIME attachment that is described in a binding 
 * independent way. 
 * 
 * @author Mark Whitlock
 */
public class WSIFAttachmentPart implements Cloneable /*,Serializable */ {
    private static final long serialVersionUID = 1L;
    private transient DataHandler dh;
    private Map properties;

    public WSIFAttachmentPart() {
        Trc.entry(this);
        this.dh = null;
        this.properties = new HashMap();
        Trc.exit();
    }

    public WSIFAttachmentPart(DataHandler dh) {
        Trc.entry(this, dh);
        this.dh = dh;
        this.properties = new HashMap();
        Trc.exit();
    }

    public WSIFAttachmentPart(Object obj) {
        Trc.entry(this, obj);
        this.dh = (DataHandler)obj;
        this.properties = new HashMap();
        Trc.exit();
    }

    public WSIFAttachmentPart(DataHandler dh, Map properties)
        throws WSIFException {
        Trc.entry(this, dh, properties);
        validateProperties(properties);
        this.dh = dh;
        this.properties = properties;
        Trc.exit();
    }

    public void setProperty(String name, String value) throws WSIFException {
        Trc.entry(this, name, value);
        properties.put(name, value);
        Trc.exit();
    }

    public void setProperties(Map properties) throws WSIFException {
        Trc.entry(this, properties);
        validateProperties(properties);
        properties.putAll(properties);
        Trc.exit();
    }

    public String getProperty(String name) {
        Trc.entry(this, name);
        String value = (String) properties.get(name);
        Trc.exit(value);
        return value;
    }

    public boolean containsProperty(String name) {
        Trc.entry(this, name);
        boolean ok = properties.containsKey(name);
        Trc.exit(ok);
        return ok;
    }

    public Iterator getPropertyIterator() {
        Trc.entry(this);
        Iterator it = properties.keySet().iterator();
        Trc.exit(it);
        return it;
    }

    public void clearProperties() {
        Trc.entry(this);
        properties = new HashMap();
        Trc.exit();
    }

    public DataHandler getDataHandler() {
        Trc.entry(this);
        Trc.exit(dh);
        return dh;
    }

    public void setDataHandler(DataHandler dh) {
        Trc.entry(this, dh);
        this.dh = dh;
        Trc.exit();
    }

    private void validateProperties(Map properties) throws WSIFException {
        Trc.entry(this, properties);
        Iterator it = properties.keySet().iterator();
        while (it.hasNext()) {
            String next = (String) it.next();
            if (!(properties.get(next) instanceof String))
                throw new WSIFException(
                    "Property " + next + " was not a String.");
        }
        Trc.exit();
    }

    public Object clone() throws CloneNotSupportedException {
        Trc.entry(this);
        DataHandler dh1 = new DataHandler(dh.getDataSource());
        HashMap properties1 = new HashMap(properties);
        WSIFAttachmentPart wap = null;
        try {
            wap = new WSIFAttachmentPart(dh1, properties1);
        } catch (WSIFException we) {
            Trc.exception(we);
            throw new CloneNotSupportedException(we.toString());
        }
        Trc.exit(wap);
        return wap;
    }
    
    //    /**
    //     * Override default deserialization
    //     */
    //    private void writeObject(ObjectOutputStream oos) throws IOException {
    //        Trc.entry(this, oos);
    //        oos.defaultWriteObject();
    //        writeOutDh(oos);
    //        oos.flush();
    //        Trc.exit();
    //    }
    //
    //    /**
    //     * Override default deserialization
    //     */
    //    private void readObject(ObjectInputStream ois)
    //        throws ClassNotFoundException, IOException {
    //        Trc.entry(this, ois);
    //        ois.defaultReadObject();
    //        dh = convertDh(ois.readObject());
    //        Trc.exit();
    //    }
    //
    //    private DataHandler convertDh(Object o) {
    //    	return null;
    //    }
    //
    //    private void writeOutDh(ObjectOutputStream oos) {
    //    }
}
