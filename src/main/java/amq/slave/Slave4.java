package amq.slave;

import amq.Constants;

import javax.jms.JMSException;
import javax.jms.MessageListener;
import java.util.logging.Level;

/**
 * Created by asy
 */
public class Slave4 extends AbstractSlave implements MessageListener {

    public static void main(String[] args) {
        new Slave4()
        ;
    }

    public Slave4() {
        super();
        try {
            //This class will handle the messages to the temp queue as well
            responseConsumer.setMessageListener(this);
            logger.info(getClass().getSimpleName() + " started");
            sendMessageToMaster(Constants.SLAVE_INTRODUCTION_MSG + getClass().getSimpleName());
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doSlaveSpecificOperations() throws JMSException {
        try {
            Thread.sleep(1400);
        } catch (InterruptedException e) {
            logger.log(Level.SEVERE, "Thread sleep interrupted ", e);
        }
        sendMessageToMaster(Constants.SLAVE_REPLY_MSG);
    }


}
