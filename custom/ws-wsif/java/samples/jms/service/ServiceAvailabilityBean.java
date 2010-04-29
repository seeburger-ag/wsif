package jms.service;

import javax.ejb.MessageDrivenBean;
import javax.ejb.MessageDrivenContext;
import javax.ejb.EJBException;
import javax.jms.*;
import javax.naming.NamingException;
import javax.naming.InitialContext;

/**
 * This is a simple MDB that processes a message containing an integer
 * that represents a zip code, and returns a message containing a 
 * boolean indicating whether DSL service is available at that zip code 
 * or not.
 * Internally the bean just returns true for zip codes < 50000 and
 * false otherwise.
 */
public class ServiceAvailabilityBean implements MessageDrivenBean, MessageListener {
    MessageDrivenContext context = null;

    public void setMessageDrivenContext(MessageDrivenContext context) {
	this.context = context;
    }
    public void ejbCreate() throws EJBException { }

    public void ejbRemove() {
	context = null;
    }

    public void onMessage(Message msg) {
	try {
	    TextMessage  message = (TextMessage)msg;
	    // assume we have an integer
	    int zipCode = new Integer(message.getText()).intValue();
	    // return true if zip code < 50000, false otherwise
	    if (zipCode<50000)
		sendMessage(message,"true");
	    else
		sendMessage(message,"false");
	} catch(Exception e) { 
	    // aargh - this should not happen usually
	    System.out.println(e); 
	    e.printStackTrace();
	}
    }

    public void sendMessage(TextMessage requestMsg, String serviceAvailable) 
	throws NamingException, JMSException {
	// wait for some time
	InitialContext jndiContext = new InitialContext();
	QueueConnectionFactory factory = (QueueConnectionFactory)
	    jndiContext.lookup("ConnectionFactory");
	QueueConnection connect = factory.createQueueConnection();
	QueueSession session = connect.createQueueSession(false,Session.AUTO_ACKNOWLEDGE);
	Queue queue = (Queue)requestMsg.getJMSReplyTo();
	QueueSender sender = session.createSender(queue);
	TextMessage message = session.createTextMessage();
	// set the correlation ID
	message.setJMSCorrelationID(requestMsg.getJMSMessageID());
	// set the text
	message.setText(serviceAvailable);
	sender.send(message);
	sender.close();
	session.close();
	connect.close();
    }
}	

