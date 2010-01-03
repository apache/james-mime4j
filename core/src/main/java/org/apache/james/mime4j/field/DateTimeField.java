package org.apache.james.mime4j.field;

import java.util.Date;

public interface DateTimeField extends ParsedField {

    public abstract Date getDate();

}