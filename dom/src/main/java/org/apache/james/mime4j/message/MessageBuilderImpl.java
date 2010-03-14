package org.apache.james.mime4j.message;

import java.io.IOException;
import java.io.InputStream;

import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.dom.Message;
import org.apache.james.mime4j.dom.MessageBuilder;

public class MessageBuilderImpl extends MessageBuilder {

    @Override
    public Message newMessage() {
        return new MessageImpl();
    }

    @Override
    public Message newMessage(Message source) {
        return new MessageImpl(source);
    }

    @Override
    public Message parse(InputStream source) throws MimeException, IOException {
        return new MessageImpl(source);
    }

}
