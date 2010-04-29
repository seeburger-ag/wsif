package com.myeis.services.myeis.myeis;
import java.io.*;
import com.myeis.services.*;
import org.apache.wsif.providers.jca.WSIFFormatHandler_JCA;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import org.xml.sax.XMLReader;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.AttributesImpl;



public class CustomerFormatHandler implements WSIFFormatHandler_JCA {
private Customer fieldCustomer = null;
public void read(InputStream inputStream) throws IOException {
	try {
		ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
		Object obj = (Object)objectInputStream.readObject();
		if(obj instanceof Customer){
			this.fieldCustomer = (Customer)obj;
		}else if (obj instanceof com.myeis.services.internal.CustomerDataObject){
			com.myeis.services.internal.CustomerDataObject aCustomer = (com.myeis.services.internal.CustomerDataObject)obj;
			this.fieldCustomer = new Customer();
			this.fieldCustomer.setLastName(aCustomer.getLastName());
			this.fieldCustomer.setFirstName(aCustomer.getFirstName());
			this.fieldCustomer.setNumber(aCustomer.getNumber());
		}
	} catch (ClassNotFoundException e) {
		e.printStackTrace();
		throw new IOException("class not found !");
	}		
}

public void write(OutputStream outputStream) throws IOException {
    ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
    objectOutputStream.writeObject(this.fieldCustomer);
    objectOutputStream.flush();
}

public void setObjectPart(Object objectPart) {
	this.fieldCustomer = (Customer)objectPart;
}

public Object getObjectPart() {
	return this.fieldCustomer;
}

public Object getObjectPart(Class sourceClass) {
	try{
		if (sourceClass.isAssignableFrom(Class.forName("javax.xml.transform.sax.SAXSource"))){
			SAXSource saxSource = new SAXSource(new InputSource());
			XMLReader reader = new ContentReader();
			saxSource.setXMLReader(reader);
			return saxSource;
		}
	}
	catch (Throwable e) {
	}
	return null;
}

public Object getElement(String elementName) {return null;}
public void setElement(String elementName, Object element) {}
public Object getElement(String name, int index) {return null;}
public void setElement(String name, int index, Object value) {}
public javax.xml.namespace.QName getPartQName(){
        return null;
    }
    
    public void setPartQName(javax.xml.namespace.QName qname){
    }
public void setInteractionSpec(javax.resource.cci.InteractionSpec arg0) {
        return;
    }
public class ContentReader implements org.xml.sax.XMLReader {
	private ContentHandler fieldContentHandler = null;
	public org.xml.sax.ContentHandler getContentHandler() {return null;}
	public org.xml.sax.DTDHandler getDTDHandler() {return null;}
	public org.xml.sax.EntityResolver getEntityResolver() {return null;}
	public org.xml.sax.ErrorHandler getErrorHandler() {return null;}
	public boolean getFeature(String name) throws org.xml.sax.SAXNotSupportedException, org.xml.sax.SAXNotRecognizedException {return false;}
	public Object getProperty(String name) throws org.xml.sax.SAXNotSupportedException, org.xml.sax.SAXNotRecognizedException {return null;}
	public void setDTDHandler(org.xml.sax.DTDHandler handler) {}
	public void setEntityResolver(org.xml.sax.EntityResolver resolver) {}
	public void setErrorHandler(org.xml.sax.ErrorHandler handler) {}
	public void setFeature(String name, boolean value) throws org.xml.sax.SAXNotSupportedException, org.xml.sax.SAXNotRecognizedException {}
	public void setProperty(String name, Object value) throws org.xml.sax.SAXNotSupportedException, org.xml.sax.SAXNotRecognizedException {}
	public void parse(String systemId) throws java.io.IOException, org.xml.sax.SAXException {}
	public void parse(org.xml.sax.InputSource input) throws java.io.IOException, org.xml.sax.SAXException {
		try {
			Attributes attributes = new AttributesImpl();
			this.fieldContentHandler.startDocument();
		this.fieldContentHandler.startElement("", "number", "number", attributes);
				this.fieldContentHandler.characters(fieldCustomer.getNumber().toCharArray(), 0, fieldCustomer.getNumber().length());
		this.fieldContentHandler.endElement("", "number", "number");
		this.fieldContentHandler.startElement("", "firstName", "firstName", attributes);
			this.fieldContentHandler.characters(fieldCustomer.getFirstName().toCharArray(), 0, fieldCustomer.getFirstName().length());
		this.fieldContentHandler.endElement("", "firstName", "firstName");
		this.fieldContentHandler.startElement("", "lastName", "lastName", attributes);
			this.fieldContentHandler.characters(fieldCustomer.getLastName().toCharArray(), 0, fieldCustomer.getLastName().length());
		this.fieldContentHandler.endElement("", "lastName", "lastName");
			this.fieldContentHandler.endDocument();
		} catch (Exception e) {
			e.printStackTrace();
		}			
	}
	public void setContentHandler(org.xml.sax.ContentHandler handler) {
		this.fieldContentHandler = handler;
	}
}  
}