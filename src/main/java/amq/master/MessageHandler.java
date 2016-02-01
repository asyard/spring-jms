package amq.master;

import amq.Constants;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MessageHandler {

    private static final Logger logger = Logger.getLogger(MessageHandler.class.getName());


    public TextMessage handleProtocolMessage(Message message, TextMessage response) throws JMSException {
        TextMessage txtMsg = (TextMessage) message;
        String messageText = txtMsg.getText();
        logger.info("> received : " + messageText);
        String responseText;

        if (messageText == null) {
            return null;
        }

        if (messageText.startsWith(Constants.SLAVE_INTRODUCTION_MSG)) {
            responseText = "I recognize your protocol message";
        } else if (messageText.startsWith(Constants.SLAVE_REPLY_MSG)) {
            //TODO long operation
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                logger.log(Level.SEVERE, "Thread sleep interrupted ", e);
            }
            responseText = Constants.MASTER_REQUEST_MSG;
        } else {
            responseText = "Unknown protocol message: " + messageText;
        }

        //Set the correlation ID from the received message to be the correlation id of the response message
        //this lets the client identify which message this is a response to if it has more than
        //one outstanding message to the server
        response.setJMSCorrelationID(message.getJMSCorrelationID());

        //Send the response to the Destination specified by the JMSReplyTo field of the received message,
        //this is presumably a temporary queue created by the client
        response.setText(responseText);
        logger.info("> Replying response to " + message.getJMSReplyTo() + " with message : " + responseText);
        return response;

    }
}