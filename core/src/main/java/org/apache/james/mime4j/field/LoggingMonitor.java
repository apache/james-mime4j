/**
 * 
 */
package org.apache.james.mime4j.field;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.james.mime4j.codec.DecodeMonitor;

public final class LoggingMonitor extends DecodeMonitor {
    private static Log log = LogFactory.getLog(LoggingMonitor.class);
    
    public static DecodeMonitor MONITOR = new LoggingMonitor();

    @Override
    public boolean warn(String error, String dropDesc) {
        if (dropDesc != null) {
            log.warn(error+"; "+dropDesc);
        } else {
            log.warn(error);
        }
        return false;
    }
    
    public boolean isListening() {
        return true;
    }
}