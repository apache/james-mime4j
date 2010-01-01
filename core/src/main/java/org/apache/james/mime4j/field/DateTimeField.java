package org.apache.james.mime4j.field;

import java.util.Date;

import org.apache.james.mime4j.field.ParseException;

public interface DateTimeField extends ParsedField {

    public abstract Date getDate();

    public abstract ParseException getParseException();

}