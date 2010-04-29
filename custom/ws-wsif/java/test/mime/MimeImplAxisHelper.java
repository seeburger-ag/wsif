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

package mime;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.axis.AxisEngine;
import org.apache.axis.Message;
import org.apache.axis.MessageContext;
import org.apache.axis.attachments.AttachmentPart;
import org.apache.wsif.attachments.WSIFAttachmentPart;
import org.apache.wsif.providers.soap.apacheaxis.WSIFAXISUtils;

/**
 * This helper class encapsulates Axis-specific code that needs to be 
 * run from the backend Mime service. Most of the MimeImpl service is 
 * common to soap and axis and only the parts that are axis-specific are
 * separated out into this helper, to allow the other parts to run under
 * a soap server.
 * 
 * @author Mark Whitlock
 */

public class MimeImplAxisHelper {
    private MimeImplAxisHelper() {
    }

    /**
     * getUnreferencedAttachments returns a list of DataHandlers and
     * a list of maps of the mime headers that were set on those attachments.
     */
    public static List getUnreferencedAttachments() throws Exception {
        MessageContext mc = AxisEngine.getCurrentMessageContext();
        Message m = mc.getRequestMessage();
        ArrayList waps = new ArrayList();

        Iterator it = m.getAttachments();
        while (it.hasNext()) {
            Object next = it.next();
            if (next instanceof AttachmentPart) {
                AttachmentPart ap = (AttachmentPart) next;
                waps.add(WSIFAXISUtils.axisToWsifAttachmentPart(ap));
            }
        }
        return waps;
    }
    
    /**
     * 
     */
    public static void putUnreferencedAttachments(List waps) throws Exception {
        System.out.println("putUnreferencedAttachments entry");
        MessageContext mc = AxisEngine.getCurrentMessageContext();
        Message m = mc.getResponseMessage();

        Iterator it = waps.iterator();
        while (it.hasNext()) {
            Object next = it.next();
            if (next instanceof WSIFAttachmentPart) {
                WSIFAttachmentPart wap = (WSIFAttachmentPart) next;
                System.out.println("putUnreferencedAttachments found " + wap);
                m.addAttachmentPart(
                    WSIFAXISUtils.wsifToAxisAttachmentPart(wap));
            }

        }
        System.out.println("putUnreferencedAttachments exit");

    }

}
