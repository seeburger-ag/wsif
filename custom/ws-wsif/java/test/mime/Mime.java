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

import java.awt.Image;

import javax.activation.DataHandler;

import org.apache.wsif.attachments.WSIFAttachmentPart;

public interface Mime {
    public String dataHandlerToString(DataHandler dh);
    public DataHandler stringToDataHandler(String buff);

    public String plainTextToString(String s);
    public String stringToPlainText(String buff);

    public Image bounceImage(Image im);
    public DataHandler bounceImage2(DataHandler dh);
//    public DataHandler bounceImage4(DataHandler dh, boolean b);
//
//    //TODO: See bugzilla bug 15837
    public DataHandler bounceImage4(boolean b, DataHandler dh);

    // Support for javax.xml.transform.Source and 
    // javax.mail.internet.MimeMultiPart is not implemented yet.
    // public String dataSourceToString(DataSource ds);
    // public DataSource stringToDataSource(String buff);

    public String orMultiMimeParts(DataHandler dh);
    public String andMultiMimeParts(DataHandler dh, DataHandler dh2);

    // multiOutMimeParts and multiInoutMimeParts are only 
    // invoked through the stubless interface

    public String noContent(String s);
    public String typeStar(DataHandler dh);
    public String soapBodyParts1(boolean shouldBounce, DataHandler dh);
    public String soapBodyParts2(DataHandler dh);
    public String soapBodyParts3(boolean shouldBounce);
    public String soapBodyParts4();
    public String arrayOfBinary(DataHandler dh);
    public DataHandler optionalSoapBody(DataHandler dh);
    public String mixMimeParts(
        String pos1,
        DataHandler dh1,
        String pos2,
        DataHandler dh2,
        String pos3);
    public String unref();
//    public String unref(DataHandler dh1, DataHandler dh2);
    public String dataHandlerToString(WSIFAttachmentPart ap);

    public void putRefDhInputOnly(DataHandler dh);
    public DataHandler getRefDh();
    public void putUnrefDhInputOnly();
    public void getUnrefDh();

    public String badNoPart(DataHandler dh) throws Exception;
    public String badPart(DataHandler dh) throws Exception;
    public String badNested(DataHandler dh) throws Exception;
    public String badMixSoapMime(DataHandler dh) throws Exception;
    public String badMultipleSoapBodies(DataHandler dh) throws Exception;
    public String badSoapBodyType(DataHandler dh) throws Exception;
}
