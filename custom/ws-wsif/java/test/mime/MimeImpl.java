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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;

import org.apache.wsif.WSIFConstants;
import org.apache.wsif.WSIFMessage;
import org.apache.wsif.attachments.WSIFAttachmentPart;
import org.apache.wsif.providers.InvocationHelper;

/**
 * Mime service used by MimeTest 
 * @author Mark Whitlock
 */

public class MimeImpl {
	private static final boolean verbose = false;
	private List stored = new ArrayList();
	private String errText = null;

    public String dataHandlerToString(DataHandler dh) {
        try {
            InputStream is = dh.getInputStream();
            byte[] bBuff = new byte[is.available()];
            is.read(bBuff);
            String sBuff = new String(bBuff);
            return sBuff;
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return null;
        }
    }

    public DataHandler stringToDataHandler(String buff) {
        try {
            FileDataSource fds = getTempFile("txt");
            fds.getOutputStream().write(buff.getBytes());
            DataHandler dh = new DataHandler(fds);
            return dh;
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return null;
        }
    }

    public String plainTextToString(DataHandler ds) {
        try {
            InputStream is = ds.getInputStream();
            byte[] bBuff = new byte[is.available()];
            is.read(bBuff);
            String sBuff = new String(bBuff);
            return sBuff;
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return null;
        }
    }

    public DataHandler stringToPlainText(String buff) {
        try {
            FileDataSource fds = getTempFile("txt");
            fds.getOutputStream().write(buff.getBytes());
            return new DataHandler(fds);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return null;
        }
    }

    public DataHandler bounceImage(DataHandler ds) {
        try {
            InputStream is = ds.getInputStream();
            byte[] bBuff = new byte[is.available()];
            is.read(bBuff);

// Debug code.....            
//            byte least=0;
//            for (int i=0; i<bBuff.length; i++) {
//            	if (bBuff[i]<least) least=bBuff[i];
//            }
//            
//            print("least="+least);
//            
//            DataHandler dh1 =
//                new DataHandler(
//                    new FileDataSource(
//                        "C:\\wsad-5\\eclipse\\workspace\\xml-axis-wsif\\java\\test\\mime\\axis.jpg"));
//            InputStream is1 = dh1.getInputStream();
//            byte[] bBuff1 = new byte[is1.available()];
//            is1.read(bBuff1);
//            print("bBuff.length="+bBuff.length+" bBuff1.length="+bBuff1.length);
//            
//            boolean matches = true;
//            int count0=0;
//            int count1=0;
//            for (int i=0; i<bBuff.length; i++) {
//            	if (bBuff[i]!=bBuff1[i]) {
//            		print("Failing index "+i+" "+bBuff[i]+" "+bBuff1[i]);
//            		matches = false;
//            	}
//            	else if (bBuff[i]<-99) print("bBuff["+i+"]="+bBuff[i]);
//
//            	if (bBuff[i]<-99) count0++;           	
//            	if (bBuff1[i]<-99) count1++;           	
//            }
//            print("matches="+matches);
//            print("count0="+count0);
//            print("count1="+count1);
//            

//            This commented out code displays the image
//            to check that it is correct.
//
//            Image im = new ImageIcon(bBuff).getImage();
//            WSIFFrame.display(im, "Backend image");
//            int t = 0;
//            try {
//                t = Integer.parseInt(
//                    TestUtilities.getWsifProperty("wsif.displaytime"));
//            } finally {
//                if (t <= 0)
//                    t = 2000;
//            }
//            Thread.sleep(t);
//
            FileDataSource fds = getTempFile("jpg");
            fds.getOutputStream().write(bBuff);
            DataHandler dh = new DataHandler(fds);
            return dh;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            WSIFFrame.close();
        }
    }

    public DataHandler bounceImage2(DataHandler ds) {
        return bounceImage(ds);
    }

    //TODO: See bugzilla bug 15837
    public DataHandler bounceImage4(boolean b, DataHandler ds) {
//    	return bounceImage4(ds, b);
//    }
//
//    public DataHandler bounceImage4(DataHandler ds, boolean b) {
        if (b && (ds != null))
            return bounceImage(ds);
        else
            return null;
    }

    public String orMultiMimeParts(DataHandler dh) {
    	return dh.getContentType();
    }

    public String andMultiMimeParts(DataHandler dh1, DataHandler dh2) {
        try {
            InputStream is1 = dh1.getInputStream();
            byte[] bBuff1 = new byte[is1.available()];
            is1.read(bBuff1);
            StringBuffer sBuff = new StringBuffer(new String(bBuff1));
            
            InputStream is2 = dh2.getInputStream();
            byte[] bBuff2 = new byte[is2.available()];
            is2.read(bBuff2);
            sBuff.append(new String(bBuff2));
            
            return sBuff.toString();
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return null;
        }
    }

    // MUST IMPLEMENT ???    
    // multiOutMimeParts and multiInoutMimeParts are only 
    // invoked through the stubless interface

    public String noContent(String s) {
    	return s;
    }

    public String typeStar(DataHandler dh) {
    	return dataHandlerToString(dh);
    }

    public String soapBodyParts1(boolean shouldBounce, DataHandler dh) {
        return "1";
    }

    public String soapBodyParts2(DataHandler dh) {
        return "2";
    }

    public String soapBodyParts3(boolean shouldBounce) {
        return "3";
    }

    public String soapBodyParts4() {
        return "4";
    }

    public String arrayOfBinary(DataHandler dh) {
    	return dataHandlerToString(dh);
    }

    public DataHandler optionalSoapBody(DataHandler ds) {
        print("MimeImpl.optionalSoapBody ds=",ds);

    	String s = dataHandlerToString(ds);
        print("MimeImpl.optionalSoapBody s=",s);

    	DataHandler dh = stringToDataHandler(s);
        print("MimeImpl.optionalSoapBody dh=",dh);

    	return dh;
    }

    public String mixMimeParts(
        String pos1,
        DataHandler dh1,
        String pos2,
        DataHandler dh2,
        String pos3)
    {
        return pos1
            + dataHandlerToString(dh1)
            + pos2
            + dataHandlerToString(dh2)
            + pos3;
    }
    
    public String unref() {
        List l = getUnrefAttachments();

        String s = new String();
        try {
            Iterator it = l.iterator();
            while (it.hasNext()) {
                Object next = it.next();
                if (next instanceof WSIFAttachmentPart) {

                    WSIFAttachmentPart wap = (WSIFAttachmentPart) next;
                    if (wap.getProperty("AnotherMimeHeader") != null) {
                        Iterator itProp = wap.getPropertyIterator();
                        while (itProp.hasNext()) {

                            String prop = (String) itProp.next();
                            if ("Content-Location".equals(prop)
                                || "Content-Id".equals(prop)
                                || !prop.startsWith("Content-")) {
                                s =
                                    s
                                        + prop
                                        + "="
                                        + wap.getProperty(prop)
                                        + ":";
                            }
                        }
                    }
                    DataHandler dh = wap.getDataHandler();
                    s = s + dataHandlerToString(dh) + ":";
                }
            }
        } catch (Exception e) {
            errText += e.toString();
            System.err.println("MimeImpl.unref==>" + errText);
            e.printStackTrace();
            return errText;
        }
        return s;
    }
    
    public void putRefDhInputOnly(DataHandler dh) {
        print("MimeImpl.putRefDhInputOnly dh=",dh);
        this.stored.add(dataHandlerToString(dh));
        print("MimeImpl.putRefDhInputOnly stored=",stored);
    }

    public DataHandler getRefDh() {
        print("MimeImpl.getRefDh stored=",stored);
        DataHandler dh = stringToDataHandler((String)this.stored.get(0));
        print("MimeImpl.getRefDh dh=",dh);
        return dh;
    }

    public void putUnrefDhInputOnly() {
        List l = getUnrefAttachments();
        DataHandler dh = ((WSIFAttachmentPart) l.get(0)).getDataHandler();
        this.stored.add(dataHandlerToString(dh));
        print("MimeImpl.putUnrefDhInputOnly stored=",stored);
    }

    public DataHandler getUnrefDh() {
        print("MimeImpl.getUnrefDh stored=", stored);
        ArrayList l = new ArrayList();
        Iterator it = stored.iterator();
        while (it.hasNext()) {
            WSIFAttachmentPart wap =
                new WSIFAttachmentPart(stringToDataHandler((String) it.next()));
            l.add(wap);
        }
        if (!putUnrefAttachments(l)) {
            System.err.println("getUnrefDh failed " + errText);
        }

        try {
            FileDataSource fds = getTempFile("txt");
            fds.getOutputStream().write(
                "Spurious referenced DataHandler".getBytes());
            return new DataHandler(fds);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return null;
        }
    }

//    public void getUnrefDh() {
//        MessageContext mc = AxisEngine.getCurrentMessageContext();
//        Message m = mc.getResponseMessage();
//        PlainTextDataSource ptds =
//            new PlainTextDataSource("UnrefAttachment", "Hello!");
//        DataHandler dh = new DataHandler(ptds);
//        AttachmentPart ap = new AttachmentPart(dh);
//        m.addAttachmentPart(ap);
//    }
    
    /* ******************* ERRORS *********************** */
    
    public String badNoPart(DataHandler dh) {
    	return dataHandlerToString(dh);
    }

    public String badPart(DataHandler dh) {
    	return dataHandlerToString(dh);
    }
    
    public String badNested(DataHandler dh) {
    	return dataHandlerToString(dh);
    }

    public String badMixSoapMime(DataHandler dh) {
    	return dataHandlerToString(dh);
    }

    public String badMultipleSoapBodies(DataHandler dh) {
    	return dataHandlerToString(dh);
    }

    public String badSoapBodyType(DataHandler dh) {
    	return dataHandlerToString(dh);
    }

    /* ***************** UTILITY METHODS *************** */
    
    private FileDataSource getTempFile(String type) throws IOException {
        File f = File.createTempFile("WSIFMimeTest", "."+type);
        f.deleteOnExit();
        return new FileDataSource(f);
    }
    
    private List getUnrefAttachments() {
        List l = null;
        errText = new String();
        try {
            WSIFMessage context = InvocationHelper.getMessageContext();
            l =
                (List) context.getObjectPart(
                    WSIFConstants.CONTEXT_REQUEST_UNREFERENCED_ATTACHMENT_PARTS);
        } catch (Exception e) {
            print("MimeImpl.getUnrefAttachments - Java - ", e);
            errText += e.toString() + "\n";
        }

        if (l == null) {
            try {
                Class c = Class.forName("mime.MimeImplAxisHelper");
                Method m = c.getMethod("getUnreferencedAttachments", null);
                Object o = m.invoke(null, null);
                l = (List) o;
            } catch (Exception e) {
                print("MimeImpl.getUnrefAttachments - Axis - ", e);
                errText += e.toString() + "\n";
            }
        }
        return l;
    }
    
    private boolean putUnrefAttachments(List l) {
        errText = new String();
        boolean success = true;

        try {
            WSIFMessage context = InvocationHelper.getMessageContext();
            context.setObjectPart(
                WSIFConstants.CONTEXT_RESPONSE_UNREFERENCED_ATTACHMENT_PARTS,
                l);
        } catch (Exception e) {
            print("MimeImpl.putUnrefAttachments - Java - ", e);
            success = false;
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            errText += "Java attempt failed with " + sw.toString() + "\n";
        }

        if (!success) {
            try {
                Class c = Class.forName("mime.MimeImplAxisHelper");
                Method m =
                    c.getMethod(
                        "putUnreferencedAttachments",
                        new Class[] { List.class });
                m.invoke(null, new Object[] { l });
                success = true;
            } catch (Exception e) {
                print("MimeImpl.putUnrefAttachments - Axis - ", e);
                success = false;
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                errText += "Axis attempt failed with " + sw.toString() + "\n";
            }
        }

        print("MimeImpl.putUnrefAttachments - returning ", new Boolean(success));
        return success;
    }
    
    private void print(Object o1) {
    	if (verbose) {
    		System.out.println(o1==null?"<null>":o1.toString());
    	}
    }
    
    private void print(Object o1, Object o2) {
    	if (verbose) {
    		System.out.println((o1==null?"<null>":o1.toString())+
    		                   (o2==null?"<null>":o2.toString()));
    	}
    }
    
    private void print(Object o1, Object o2, Object o3) {
    	if (verbose) {
    		System.out.println((o1==null?"<null>":o1.toString())+
    		                   (o2==null?"<null>":o2.toString())+
    		                   (o3==null?"<null>":o3.toString()));
    	}
    }
    
    private void print(Object o1, Object o2, Object o3, Object o4) {
    	if (verbose) {
    		System.out.println((o1==null?"<null>":o1.toString())+
    		                   (o2==null?"<null>":o2.toString())+
    		                   (o3==null?"<null>":o3.toString())+
    		                   (o4==null?"<null>":o4.toString()));
    	}
    }
}
