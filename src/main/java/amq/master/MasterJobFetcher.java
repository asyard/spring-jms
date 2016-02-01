package amq.master;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import amq.Constants;

import javax.jms.*;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by asy
 */
public class MasterJobFetcher implements MessageListener {

    private static final Logger logger = Logger.getLogger(MasterJobFetcher.class.getName());

    public static int ackMode;
    public static String messageQueueName;

    private Session session;
    private boolean transacted = false;
    private MessageProducer replyProducer;
    private MessageHandler messageHandler;

    private HashMap<String, String> slaveInfo;

    static {
        messageQueueName = "master.messages";
        ackMode = Session.AUTO_ACKNOWLEDGE;
    }

    public MasterJobFetcher() {
        createMessageBroker();
        slaveInfo = new HashMap<String, String>();
        //Delegating the handling of messages to another class, instantiate it before setting up JMS so it
        //is ready to handle messages
        this.messageHandler = new MessageHandler();
        this.setupMessageQueueConsumer();
    }

    private void createMessageBroker() {
        try {
            //This message broker is embedded
            BrokerService broker = new BrokerService();
            broker.addConnector(Constants.messageBrokerUrl);
            //BrokerService broker = BrokerFactory.createBroker(new URI(messageBrokerUrl));
            broker.setPersistent(false);
            broker.setUseJmx(false);
            broker.start();
            logger.info("Broker service started. Listening uri " + Constants.messageBrokerUrl);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Broker Service could not be started ", e);
        }
    }

    private void setupMessageQueueConsumer() {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(Constants.messageBrokerUrl);
        Connection connection;
        try {
            connection = connectionFactory.createConnection();
            connection.start();
            this.session = connection.createSession(this.transacted, ackMode);
            Destination adminQueue = this.session.createQueue(messageQueueName);

            //Setup a message producer to respond to messages from clients, we will get the destination
            //to send to from the JMSReplyTo header field from a Message
            this.replyProducer = this.session.createProducer(null);
            this.replyProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

            //Set up a consumer to consume messages off of the admin queue
            MessageConsumer consumer = this.session.createConsumer(adminQueue);
            consumer.setMessageListener(this);
        } catch (JMSException e) {
            logger.log(Level.SEVERE, "Message queue setup failed ", e);
        }
    }

    public void onMessage(Message message) {
        try {
            TextMessage response = this.session.createTextMessage();
            if (message instanceof TextMessage) {
                TextMessage responseMessage = messageHandler.handleProtocolMessage(message, response);
                this.replyProducer.send(message.getJMSReplyTo(), responseMessage);
            } else {
                logger.warning("message is not a text message.");
            }
        } catch (JMSException e) {
            logger.log(Level.SEVERE, "Message could not be consumed", e);
        }
    }

    public static void main(String[] args) {
        new MasterJobFetcher();
    }


}
