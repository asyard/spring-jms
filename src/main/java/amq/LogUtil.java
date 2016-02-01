package amq;

import java.io.OutputStream;
import java.util.Date;
import java.util.logging.*;

/**
 * Created by asy
 */
public class LogUtil {

    public static Handler getConsoleHandler() {
        // per default java console handler uses err -> use out instead
        ConsoleHandler _consoleHandler = new ConsoleHandler() {
            protected synchronized void setOutputStream(OutputStream out) throws SecurityException
            {
                super.setOutputStream(System.out);
            }
        };
        Formatter formatter = new Formatter() {
            @Override
            public String format(LogRecord arg0) {
                StringBuilder b = new StringBuilder().append(new Date()).append(" ").append(arg0.getSourceClassName())
                        .append(" ").append(arg0.getSourceMethodName()).append(" ").append(arg0.getLevel()).append(" ").append(arg0.getMessage())
                        .append(System.getProperty("line.separator"));
                return b.toString();
            }
        };
        _consoleHandler.setFormatter(formatter);
        _consoleHandler.setLevel(Level.ALL);

        return _consoleHandler;
    }

}
