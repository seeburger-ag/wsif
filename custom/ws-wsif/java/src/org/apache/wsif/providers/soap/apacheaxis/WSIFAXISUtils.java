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

import java.util.Iterator;

import javax.xml.soap.MimeHeader;
import javax.xml.soap.SOAPException;

import org.apache.axis.attachments.AttachmentPart;
import org.apache.wsif.WSIFException;
import org.apache.wsif.attachments.WSIFAttachmentPart;
import org.apache.wsif.logging.Trc;

/**
 * Utility class to convert between WSIFAttachmentParts and Axis AttachmentParts.
 * 
 * @author Mark Whitlock
 */
public class WSIFAXISUtils {

    public static AttachmentPart wsifToAxisAttachmentPart(WSIFAttachmentPart wsifAp)
        throws WSIFException {
        Trc.entry(null, wsifAp);

        AttachmentPart axisAp = new AttachmentPart(wsifAp.getDataHandler());

        Iterator it = wsifAp.getPropertyIterator();
        while (it.hasNext()) {
            String name = (String) it.next();
            if ("Content-Id".equals(name)) {
                axisAp.setContentId(wsifAp.getProperty(name));
            } else if ("Content-Location".equals(name)) {
                axisAp.setContentLocation(wsifAp.getProperty(name));
            } else if ("Content-Type".equals(name)) {
                axisAp.setContentType(wsifAp.getProperty(name));
            } else {
                axisAp.addMimeHeader(name, wsifAp.getProperty(name));
            }
        }

        Trc.exit(axisAp);
        return axisAp;
    }

    public static WSIFAttachmentPart axisToWsifAttachmentPart(AttachmentPart axisAp)
        throws WSIFException {
        Trc.entry(null, axisAp);

        WSIFAttachmentPart wsifAp = null;
        try {
            wsifAp = new WSIFAttachmentPart(axisAp.getDataHandler());
        } catch (SOAPException se) {
            Trc.exception(se);
            throw new WSIFException(
                "Could not convert WSIFAttachmentPart to Axis AttachmentPart. "
                    + se);
        }

        Iterator it = axisAp.getAllMimeHeaders();
        while (it.hasNext()) {
            MimeHeader mh = (MimeHeader) it.next();
            wsifAp.setProperty(mh.getName(), mh.getValue());
        }

        Trc.exit(wsifAp);
        return wsifAp;
    }

}
