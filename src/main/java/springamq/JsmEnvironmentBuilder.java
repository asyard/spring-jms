package springamq;

/**
 * Created by asy
 */
public class JsmEnvironmentBuilder {

    public static String brokerUri = "127.0.0.1:61616";

    public static void main(String[] args) {
        JmsBroker jmsBroker = new JmsBroker(brokerUri);
        jmsBroker.startBroker();

    }

}
