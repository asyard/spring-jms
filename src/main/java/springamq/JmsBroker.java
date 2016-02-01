package springamq;

import org.apache.activemq.broker.BrokerService;

/**
 * Created by asy
 */
public class JmsBroker {

    private String uri;

    public JmsBroker(String uri) {
        this.uri = uri;
    }

    public void startBroker() {
        try {
            //This message broker is embedded
            BrokerService broker = new BrokerService();
            broker.setPersistent(false);
            broker.setUseJmx(false);
            broker.addConnector("tcp://"+ uri);
            broker.start();
            while (true) {

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
