package org.apache.james.mime4j;

/**
 * Enumerates events which can be monitored.
 */
public final class Event { 

    /** Indicates that a body part ended prematurely. */
    public static final Event MIME_BODY_PREMATURE_END 
        = new Event("Body part ended prematurely. " +
                "Boundary detected in header or EOF reached."); 
    /** Indicates that unexpected end of headers detected.*/
    public static final Event HEADERS_PREMATURE_END 
        = new Event("Unexpected end of headers detected. " +
                "Higher level boundary detected or EOF reached.");
    /** Indicates that unexpected end of headers detected.*/
    public static final Event INALID_HEADER 
        = new Event("Invalid header encountered");
    
    private final String code;
    
    public Event(final String code) {
        super();
        if (code == null) {
            throw new IllegalArgumentException("Code may not be null");
        }
        this.code = code;
    }
    
    public int hashCode() {
        return code.hashCode();
    }

    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (this == obj) return true;
        if (obj instanceof Event) {
            Event that = (Event) obj;
            return this.code.equals(that.code);
        } else {
            return false;
        }
    }
    
    public String toString() {
        return code;
    }
    
}