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

package org.apache.wsif.providers.soap.apacheaxis;

import java.awt.Image;
import java.io.IOException;
import java.io.InputStream;

import javax.activation.DataHandler;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMultipart;
import javax.swing.ImageIcon;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;

import org.apache.axis.attachments.AttachmentPart;
import org.apache.axis.client.Call;
import org.apache.axis.encoding.ser.JAFDataHandlerDeserializerFactory;
import org.apache.axis.encoding.ser.JAFDataHandlerSerializerFactory;
import org.apache.wsif.WSIFException;
import org.apache.wsif.WSIFMessage;
import org.apache.wsif.attachments.WSIFAttachmentPart;
import org.apache.wsif.logging.Trc;

/**
 * The sole purpose of this class is to collect all the code that uses
 * attachement classes outside of the WSIFOperation_ApacheAxis class 
 * so that there is not a runtime requirement on activation.jar and
 * mail.jar unless attachements are actually being used.
 *  
 * @author Ant Elder <ant.elder@uk.ibm.com>
 */
public class MIMEHelper {

    public static void registerAttachmentType(Call call, QName type) {
        call.registerTypeMapping(
            DataHandler.class,
            type,
            JAFDataHandlerSerializerFactory.class,
            JAFDataHandlerDeserializerFactory.class);
    }

    public static boolean addAttachementIfMIMEPart(Call call, Object o) {
		Trc.entry(null, call, o);
		boolean ok;
		
		if (o instanceof DataHandler) {
            call.addAttachmentPart(new AttachmentPart((DataHandler)o));
            ok = true;
		} else {
			ok = false;
		}
                    
		Trc.exit(new Boolean(ok));
		return ok;
    }

    public static AttachmentPart getAttachementPart(Object o)
        throws WSIFException {
        Trc.entry(null, o);
        AttachmentPart ap = null;
        if (o instanceof WSIFAttachmentPart) {
            ap = WSIFAXISUtils.wsifToAxisAttachmentPart((WSIFAttachmentPart) o);
        } else if (o instanceof DataHandler) {
            ap = new AttachmentPart((DataHandler) o);
        } else if (o instanceof AttachmentPart) {
            ap = (AttachmentPart) o;
        } else {
            throw new WSIFException("Object is not a DataHandler: " + o);
        }
        Trc.exit(ap);
        return ap;
    }

	public static void setMIMEMessagePart(
		WSIFMessage msg,
		String name,
		Object value,
		Class type)
		throws WSIFException {
		Trc.entry(null, msg, name, value, type);

		Class valueType = value == null ? null : value.getClass();
		try {
			if (valueType != null
				&& DataHandler.class.equals(type)
				&& AttachmentPart.class.isAssignableFrom(valueType)) {
				AttachmentPart ap = (AttachmentPart) value;
				DataHandler dh = ap.getDataHandler();
				msg.setObjectPart(name, dh);
			} else if (
			//            Attachments that are Strings, Images, etc are unsupported at present.
			//            Attachments can only be DataHandlers.
			valueType
				!= null
					&& (String.class.equals(type)
						|| Image.class.equals(type)
						|| StreamSource.class.equals(type)
						|| DOMSource.class.equals(type)
						|| SAXSource.class.equals(type)
						|| MimeMultipart.class.equals(type))
					&& AttachmentPart.class.isAssignableFrom(valueType)) {

				AttachmentPart ap = (AttachmentPart) value;
				InputStream is = ap.getDataHandler().getInputStream();

				if (String.class.equals(type)) {
					byte[] bBuff = new byte[is.available()];
					is.read(bBuff);
					msg.setObjectPart(name, new String(bBuff));
				} else if (Image.class.equals(type)) {
					byte[] bBuff = new byte[is.available()];
					is.read(bBuff);
					msg.setObjectPart(name, new ImageIcon(bBuff).getImage());
				} else if (StreamSource.class.equals(type))
					// Warning: this next line of code has never been tested.
					msg.setObjectPart(name, new StreamSource(is));
				else if (DOMSource.class.equals(type))
					throw new WSIFException("DOMSource is not supported");
				else if (SAXSource.class.equals(type))
					throw new WSIFException("SAXSource is not supported");
				else if (MimeMultipart.class.equals(type))
					// Warning: this next line of code has never been tested.
					msg.setObjectPart(
						name,
						new MimeMultipart(ap.getDataHandler().getDataSource()));
			} else if (
				valueType != null
					&& type != null // will be null for async responses
					&& !type.isPrimitive()
					&& !(type.isAssignableFrom(valueType))) {
				throw new WSIFException(
					"return value "
						+ value
						+ " has unexpected type "
						+ valueType
						+ " instead of "
						+ type);
			} else
				msg.setObjectPart(name, value);
		} catch (SOAPException se) {
			Trc.exception(se);
			throw new WSIFException(
				"WSIFOperation_ApacheAxis.setMessagePart messageName="
					+ (msg.getName() == null ? "null" : msg.getName())
					+ " partName="
					+ name
					+ "  caught "
					+ se);
		} catch (IOException ioe) {
			Trc.exception(ioe);
			throw new WSIFException(
				"WSIFOperation_ApacheAxis.setMessagePart messageName="
					+ (msg.getName() == null ? "null" : msg.getName())
					+ " partName="
					+ name
					+ "  caught "
					+ ioe);
		} catch (MessagingException me) {
			Trc.exception(me);
			throw new WSIFException(
				"WSIFOperation_ApacheAxis.setMessagePart messageName="
					+ (msg.getName() == null ? "null" : msg.getName())
					+ " partName="
					+ name
					+ "  caught "
					+ me);
		}

		Trc.exit();
	}

}
