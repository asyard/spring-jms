package springamq;

import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.jms.listener.SessionAwareMessageListener;
import org.springframework.stereotype.Service;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;

@Service
public class JmsMessageListener implements SessionAwareMessageListener {

    public JmsMessageListener() {
    }

    public void onMessage(Message message, Session session) throws JMSException {
        // This is the received message
        System.out.println("Receive: " + ((ActiveMQTextMessage) message).getText());

        // Let's prepare a reply message - a "ACK" String
        ActiveMQTextMessage textMessage = new ActiveMQTextMessage();
        textMessage.setText("ACK");

        System.out.println("Slave going to send a message : " + textMessage.getText() + " to : " + message.getJMSReplyTo());
        // Message send back to the replyTo address of the income message.
        // Like replying an email somehow.
        MessageProducer producer = session.createProducer(message.getJMSReplyTo());
        producer.send(textMessage);
    }
}