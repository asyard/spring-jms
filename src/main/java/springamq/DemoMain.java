package springamq;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by asy
 */
public class DemoMain {

    public static void main(String[] args) {
        // init spring context
        ApplicationContext ctx = new ClassPathXmlApplicationContext("app-context-server.xml");


        ActiveMQConnectionFactory activeMQConnectionFactory = ctx.getBean(ActiveMQConnectionFactory.class);
        activeMQConnectionFactory.setBrokerURL("tcp://127.0.0.1:61616");

        // get bean from context
        JmsMessageSender jmsMessageSender = (JmsMessageSender)ctx.getBean("jmsMessageSender");

        // send to default destination
        jmsMessageSender.send("hello JMS");

        // send to a code specified destination
/*        Queue queue = new ActiveMQQueue("AnotherDest");
        jmsMessageSender.send(queue, "hello Another Message");*/

        // close spring application context
        //((ClassPathXmlApplicationContext)ctx).close();


        while (true);

    }


}
