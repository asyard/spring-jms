package pubsub;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;

import javax.jms.*;

public class Publisher implements MessageListener {

    public static String messagetopicName;
    public static String url;

    private Destination ackDestination;

    static {
        url = "tcp://localhost:61617";
        messagetopicName = "master.veryfunnytopic";
    }

    public Publisher() {
        try {
            //This message broker is embedded
            BrokerService broker = new BrokerService();
            broker.setPersistent(false);
            broker.setUseJmx(false);
            broker.addConnector(url);
            broker.start();
        } catch (Exception e) {
            //Handle the exception appropriately
        }
    }

    private Session session;

    private void setupPublishSubscribe() throws JMSException {
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);
        Connection connection = connectionFactory.createConnection();
        connection.start();

        // JMS messages are sent and received using a Session. We will
        // create here a non-transactional session object. If you want
        // to use transactions you should set the first parameter to 'true'
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        Topic topic = session.createTopic(messagetopicName);

        MessageProducer producer = session.createProducer(topic);

        while (true) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            ackDestination = session.createQueue("ackQueue");
            MessageConsumer consumer = this.session.createConsumer(ackDestination);
            consumer.setMessageListener(this);

            // We will send a small text message saying 'Hello'

            TextMessage message = session.createTextMessage();
            message.setJMSReplyTo(ackDestination);
            message.setText("HELLO JMS WORLD");
            // Here we are sending the message!
            producer.send(message);
            System.out.println("Sent message '" + message.getText() + "'");
        }
    }

    public static void main(String[] args) {
        try {
            new Publisher().setupPublishSubscribe();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMessage(Message message) {
        try {
            TextMessage response = session.createTextMessage();
            if (message instanceof TextMessage) {
                TextMessage txtMsg = (TextMessage) message;
                String messageText = txtMsg.getText();
                System.out.println("> received : " + messageText);
            } else {
                System.out.println("message is not a text message.");
            }
        } catch (JMSException e) {
            //Handle the exception appropriately
        }
    }
}