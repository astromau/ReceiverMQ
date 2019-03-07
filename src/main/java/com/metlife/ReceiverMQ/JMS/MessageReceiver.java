package com.metlife.ReceiverMQ.JMS;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

public class JMSListener implements MessageListener{

	@Override
	public void onMessage(Message message) {
		try {
			System.out.println(message.getObjectProperty("nombre"));
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
