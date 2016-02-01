package amq.slave;

import amq.Constants;

import javax.jms.JMSException;
import java.util.logging.Level;

/**
 * Created by asy
 */
public class Slave3 extends AbstractSlave {

    public static void main(String[] args) {
        new Slave3()
        ;
    }

    public Slave3() {
        super();
        try {
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
            Thread.sleep(1300);
        } catch (InterruptedException e) {
            logger.log(Level.SEVERE, "Thread sleep interrupted ", e);
        }
        sendMessageToMaster(Constants.SLAVE_REPLY_MSG);
    }

}
