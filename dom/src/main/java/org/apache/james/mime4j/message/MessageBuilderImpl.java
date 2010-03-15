package org.apache.james.mime4j.message;

import java.io.IOException;
import java.io.InputStream;

import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.codec.DecodeMonitor;
import org.apache.james.mime4j.dom.Message;
import org.apache.james.mime4j.dom.MessageBuilder;
import org.apache.james.mime4j.storage.StorageProvider;

public class MessageBuilderImpl extends MessageBuilder {

    private StorageProvider storageProvider;
    private DecodeMonitor decodeMonitor = null;

    public MessageBuilderImpl(StorageProvider storageProvider) {
        this.storageProvider = storageProvider;
    }

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
        return new MessageImpl(source, null, storageProvider, null, decodeMonitor);
    }

    @Override
    public void setDecodeMonitor(DecodeMonitor decodeMonitor) {
        this.decodeMonitor = decodeMonitor;
    }

}
