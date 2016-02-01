package pubsub;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

public class Subscriber1 {

    private MessageProducer replyProducer;

    public Subscriber1() throws JMSException {

        // Getting JMS connection from the server
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(Publisher.url);
        Connection connection = connectionFactory.createConnection();

        // need to setClientID value, any string value you wish
        connection.setClientID(getClass().getSimpleName());

        try{
            connection.start();
        }catch(Exception e){
            System.err.println("NOT CONNECTED!!!");
        }
        final Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        final Topic topic = session.createTopic(Publisher.messagetopicName);

        this.replyProducer = session.createProducer(null);
        this.replyProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

        // need to use createDurableSubscriber() method instead of createConsumer() for topic
        // MessageConsumer consumer = session.createConsumer(topic);
        MessageConsumer consumer = session.createDurableSubscriber(topic, getClass().getSimpleName());

        MessageListener listner = new MessageListener() {
            public void onMessage(Message message) {
                try {
                    if (message instanceof TextMessage) {
                        TextMessage textMessage = (TextMessage) message;
                        System.out.println("Received message" + textMessage.getText() + "'");
                    }
                    //we can also send reply message to publisher
/*                    TextMessage response = session.createTextMessage();
                    response.setText("affirmative1");
                    //Set the correlation ID from the received message to be the correlation id of the response message
                    //this lets the client identify which message this is a response to if it has more than
                    //one outstanding message to the server
                    response.setJMSCorrelationID(message.getJMSCorrelationID());
                    replyProducer.send(message.getJMSReplyTo(), response);*/
                } catch (JMSException e) {
                    System.out.println("Caught:" + e);
                    e.printStackTrace();
                }
            }
        };

        consumer.setMessageListener(listner);
        //connection.close();
    }

    public static void main(String[] args) throws JMSException {
        new Subscriber1();
    }
}