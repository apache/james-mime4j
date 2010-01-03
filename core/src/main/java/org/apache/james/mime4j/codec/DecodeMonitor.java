package org.apache.james.mime4j.codec;


/**
 * This class is used to drive how decoder/parser should deal with malformed
 * and unexpected data.
 * 
 * 2 basic implementations are provided:
 * STRICT return "true" on any occourence.
 * SILENT ignores any problem.
 * 
 * @see org.apache.james.mime4j.field.impl.LoggingMonitor for an example
 * about logging malformations via Commons-logging.
 */
public class DecodeMonitor {

    /**
     * The STRICT monitor throws an exception on every event.
     */
    public static final DecodeMonitor STRICT = new DecodeMonitor() {

        @Override
        public boolean warn(String error, String dropDesc) {
            return true;
        }

        @Override
        public boolean isListening() {
            return true;
        }
    };
    
    /**
     * The SILENT monitor ignore requests.
     */
    public static final DecodeMonitor SILENT = new DecodeMonitor();
    
    public boolean warn(String error, String dropDesc) {
        return false;
    }

    public boolean isListening() {
        return false;
    }

}
