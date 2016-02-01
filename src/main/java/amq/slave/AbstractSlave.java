package amq.slave;

import org.apache.activemq.ActiveMQConnectionFactory;
import amq.Constants;
import amq.LogUtil;
import amq.master.MasterJobFetcher;

import javax.jms.*;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Created by asy
 */
public abstract class AbstractSlave implements MessageListener {

    protected final Logger logger = Logger.getLogger(getClass().getName());

    protected boolean transacted = false;
    protected MessageProducer producer;
    protected Session session;
    protected Destination tempQueue;
    protected MessageConsumer responseConsumer;

    public AbstractSlave() {
        modifyLogger();
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(Constants.messageBrokerUrl);
        Connection connection;
        try {
            connection = connectionFactory.createConnection();
            connection.start();
            session = connection.createSession(transacted, MasterJobFetcher.ackMode);
            Destination adminQueue = session.createQueue(MasterJobFetcher.messageQueueName);

            //Setup a message producer to send message to the queue the server is consuming from
            this.producer = session.createProducer(adminQueue);
            this.producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

            //Create a temporary queue that this client will listen for responses on then create a consumer
            //that consumes message from this temporary queue...for a real application a client should reuse
            //the same temp queue for each message to the server...one temp queue per client
            tempQueue = session.createTemporaryQueue();
            responseConsumer = session.createConsumer(tempQueue);
        } catch (JMSException e) {
            logger.log(Level.SEVERE, "Slave setup failed ", e);
        }
    }

    private void modifyLogger() {
        LogManager.getLogManager().reset();
        logger.addHandler(LogUtil.getConsoleHandler());
        LogManager.getLogManager().addLogger(logger);
    }

    protected void sendMessageToMaster(String message) throws JMSException {
        //Now create the actual message you want to send
        TextMessage txtMessage = session.createTextMessage();
        txtMessage.setText(message + getClass().getSimpleName());
        //Set the reply to field to the temp queue you created above, this is the queue the server will respond to
        txtMessage.setJMSReplyTo(tempQueue);
        //Set a correlation ID so when you get a response you know which sent message the response is for
        //If there is never more than one outstanding message to the server then the
        //same correlation ID can be used for all the messages...if there is more than one outstanding
        //message to the server you would presumably want to associate the correlation ID with this
        //message somehow...a Map works good
        String correlationId = this.createRandomString();
        txtMessage.setJMSCorrelationID(correlationId);

        logger.info("> sending message to " + Constants.messageBrokerUrl + " for destination queue : " + this.producer.getDestination().toString());
        this.producer.send(txtMessage);
    }

    private String createRandomString() {
        Random random = new Random(System.currentTimeMillis());
        long randomLong = random.nextLong();
        return Long.toHexString(randomLong);
    }


    // MessageListener.onMessage
    public void onMessage(Message message) {
        String messageText = null;
        try {
            if (message instanceof TextMessage) {
                TextMessage textMessage = (TextMessage) message;
                messageText = textMessage.getText();
                logger.info("> Received messageText = " + messageText);
                doSlaveSpecificOperations();
            }
        } catch (JMSException e) {
            logger.log(Level.SEVERE, "Message could not be processed", e);
        }
    }

    protected abstract void doSlaveSpecificOperations() throws JMSException;

}
