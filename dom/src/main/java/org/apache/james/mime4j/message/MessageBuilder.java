/****************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 *   http://www.apache.org/licenses/LICENSE-2.0                 *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 ****************************************************************/

package org.apache.james.mime4j.message;

import org.apache.james.mime4j.codec.DecodeMonitor;
import org.apache.james.mime4j.dom.BinaryBody;
import org.apache.james.mime4j.dom.Body;
import org.apache.james.mime4j.dom.FieldParser;
import org.apache.james.mime4j.dom.Message;
import org.apache.james.mime4j.dom.Multipart;
import org.apache.james.mime4j.dom.TextBody;
import org.apache.james.mime4j.dom.address.Address;
import org.apache.james.mime4j.dom.address.AddressList;
import org.apache.james.mime4j.dom.address.Mailbox;
import org.apache.james.mime4j.dom.address.MailboxList;
import org.apache.james.mime4j.dom.field.ParseException;
import org.apache.james.mime4j.dom.field.ParsedField;
import org.apache.james.mime4j.stream.BodyDescriptorBuilder;
import org.apache.james.mime4j.stream.Field;
import org.apache.james.mime4j.stream.MimeConfig;
import org.apache.james.mime4j.stream.NameValuePair;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Deprecated: please use {@link org.apache.james.mime4j.dom.Message.Builder Message.Builder} instead
 * This builder will create message that do not respect the
 * <code>Message.getDate()</code> contract regarding the return
 * value when the message do not have a Date header
 * See <a href="https://issues.apache.org/jira/browse/MIME4J-262">MIME4J-262</a>
 */
@Deprecated
@SuppressWarnings("deprecation")
public class MessageBuilder {
    private final Message.Builder builder;

    public MessageBuilder() {
        this(Message.Builder.of());
    }

    private MessageBuilder(Message.Builder builder) {
        this.builder = builder;
    }

    public static MessageBuilder create() {
        return new MessageBuilder();
    }

    public static MessageBuilder createCopy(Message other) {
        return new MessageBuilder(Message.Builder.of(other));
    }

    public static MessageBuilder read(InputStream is) throws IOException {
        return new MessageBuilder(Message.Builder.of(is));
    }


    public MessageBuilder enableFlatMode() {
        builder.enableFlatMode();

        return this;
    }

    public MessageBuilder disableFlatMode() {
        builder.disableFlatMode();

        return this;
    }

    public MessageBuilder enableContentDecoding() {
        builder.enableContentDecoding();

        return this;
    }

    public MessageBuilder disableContentDecoding() {
        builder.disableContentDecoding();

        return this;
    }

    public MessageBuilder setField(Field field) {
        builder.setField(field);

        return this;
    }

    public MessageBuilder addField(Field field) {
        builder.addField(field);

        return this;
    }

    public Field getField(String name) {
        return builder.getField(name);
    }

    public <F extends Field> F getField(final String name, final Class<F> clazz) {
        return builder.getField(name, clazz);
    }

    public List<Field> getFields() {
        return builder.getFields();
    }

    public <F extends Field> List<F> getFields(final String name, final Class<F> clazz) {
        return builder.getFields(name, clazz);
    }

    public MessageBuilder removeFields(String name) {
        builder.removeFields(name);

        return this;
    }

    public MessageBuilder clearFields() {
        builder.clearFields();

        return this;
    }

    public boolean containsField(String name) {
        return builder.containsField(name);
    }

    public String getMimeType() {
        return builder.getMimeType();
    }

    public String getCharset() {
        return builder.getCharset();
    }

    public String getContentTransferEncoding() {
        return builder.getContentTransferEncoding();
    }

    public MessageBuilder setContentType(String mimeType, NameValuePair... parameters) {
        builder.setContentType(mimeType, parameters);

        return this;
    }

    public MessageBuilder setContentTransferEncoding(String contentTransferEncoding) {
        builder.setContentTransferEncoding(contentTransferEncoding);

        return this;
    }

    public String getDispositionType() {
        return builder.getDispositionType();
    }

    public MessageBuilder setContentDisposition(String dispositionType) {
        builder.setContentDisposition(dispositionType);

        return this;
    }

    public MessageBuilder setContentDisposition(String dispositionType, String filename) {
        builder.setContentDisposition(dispositionType, filename);

        return this;
    }

    public MessageBuilder setContentDisposition(String dispositionType, String filename, long size) {
        builder.setContentDisposition(dispositionType, filename, size);

        return this;
    }

    public MessageBuilder setContentDisposition(String dispositionType, String filename, long size, Date creationDate, Date modificationDate, Date readDate) {
        builder.setContentDisposition(dispositionType, filename, size, creationDate, modificationDate, readDate);

        return this;
    }

    public Body getBody() {
        return builder.getBody();
    }

    public MessageBuilder setBody(Multipart multipart) {
        builder.setBody(multipart);

        return this;
    }

    public MessageBuilder setBody(Message message) {
        builder.setBody(message);

        return this;
    }

    public MessageBuilder setBody(Body body) {
        builder.setBody(body);

        return this;
    }

    public MessageBuilder setBody(TextBody textBody) {
        builder.setBody(textBody);

        return this;
    }

    public MessageBuilder setBody(BinaryBody binaryBody) {
        builder.setBody(binaryBody);

        return this;
    }

    public MessageBuilder setBody(String text, Charset charset) throws IOException {
        builder.setBody(text, charset);

        return this;
    }

    public MessageBuilder setBody(String text, String subtype, Charset charset) throws IOException {
        builder.setBody(text, subtype, charset);

        return this;
    }

    public MessageBuilder setBody(byte[] bin, String mimeType) throws IOException {
        builder.setBody(bin, mimeType);

        return this;
    }


    public String getFilename() {
        return builder.getFilename();
    }

    public long getSize() {
        return builder.getSize();
    }

    public Date getCreationDate() {
        return builder.getCreationDate();
    }

    public Date getModificationDate() {
        return builder.getModificationDate();
    }

    public Date getReadDate() {
        return getReadDate();
    }

    public String getMessageId() {
        return builder.getMessageId();
    }

    public MessageBuilder setMessageId(String messageId) {
        builder.setMessageId(messageId);

        return this;
    }

    public MessageBuilder generateMessageId(String hostname) {
        builder.generateMessageId(hostname);

        return this;
    }

    public String getSubject() {
        return builder.getSubject();
    }

    public MessageBuilder setSubject(String subject) {
        builder.setSubject(subject);

        return this;
    }

    public Date getDate() {
        return builder.getDate();
    }

    public MessageBuilder setDate(Date date) {
        builder.setDate(date);

        return this;
    }

    public MessageBuilder setDate(Date date, TimeZone zone) {
        builder.setDate(date, zone);

        return this;
    }

    public Mailbox getSender() {
        return builder.getSender();
    }

    public MessageBuilder setSender(Mailbox sender) {
        builder.setSender(sender);

        return this;
    }

    public MessageBuilder setSender(String sender) throws ParseException {
        builder.setSender(sender);

        return this;
    }

    public MailboxList getFrom() {
        return builder.getFrom();
    }

    public MessageBuilder setFrom(String... from) throws ParseException {
        builder.setFrom(from);

        return this;
    }

    public MessageBuilder setFrom(Collection<Mailbox> from) {
        builder.setFrom(from);

        return this;
    }

    public MessageBuilder setFrom(Mailbox from) {
        builder.setFrom(from);

        return this;
    }

    public MessageBuilder setFrom(String from) throws ParseException {
        builder.setFrom(from);

        return this;
    }

    public MessageBuilder setFrom(Mailbox... from) {
        builder.setFrom(from);

        return this;
    }

    public AddressList getTo() {
        return builder.getTo();
    }

    public MessageBuilder setTo(String... to) throws ParseException {
        builder.setTo(to);

        return this;
    }

    public MessageBuilder setTo(Collection<? extends Address> to) {
        builder.setTo(to);

        return this;
    }

    public MessageBuilder setTo(Address to) {
        builder.setTo(to);

        return this;
    }

    public MessageBuilder setTo(String to) throws ParseException {
        builder.setTo(to);

        return this;
    }

    public MessageBuilder setTo(Address... to) {
        builder.setTo(to);

        return this;
    }

    public AddressList getCc() {
        return builder.getCc();
    }

    public MessageBuilder setCc(Address... cc) {
        builder.setCc(cc);

        return this;
    }

    public MessageBuilder setCc(Collection<? extends Address> cc) {
        builder.setCc(cc);

        return this;
    }

    public MessageBuilder setCc(Address cc) {
        builder.setCc(cc);

        return this;
    }

    public AddressList getBcc() {
        return builder.getBcc();
    }

    public MessageBuilder setBcc(Address... bcc) {
        builder.setBcc(bcc);

        return this;
    }

    public MessageBuilder setBcc(Collection<? extends Address> bcc) {
        builder.setBcc(bcc);

        return this;
    }

    public MessageBuilder setBcc(Address bcc) {
        builder.setBcc(bcc);

        return this;
    }

    public MessageBuilder copy(Message other) {
        builder.copy(other);

        return this;
    }

    public MessageBuilder parse(InputStream is) throws IOException {
        builder.parse(is);

        return this;
    }

    public AddressList getReplyTo() {
        return builder.getReplyTo();
    }

    public MessageBuilder setReplyTo(Address... replyTo) {
        builder.setReplyTo(replyTo);

        return this;
    }

    public MessageBuilder setReplyTo(Collection<? extends Address> replyTo) {
        builder.setReplyTo(replyTo);

        return this;
    }

    public MessageBuilder setReplyTo(Address replyTo) {
        builder.setReplyTo(replyTo);

        return this;
    }

    public MessageBuilder use(MimeConfig config) {
        builder.use(config);

        return this;
    }

    public MessageBuilder use(DecodeMonitor monitor) {
        builder.use(monitor);

        return this;
    }

    public MessageBuilder use(BodyDescriptorBuilder bodyDescBuilder) {
        builder.use(bodyDescBuilder);

        return this;
    }

    public MessageBuilder use(FieldParser<?> fieldParser) {
        builder.use(fieldParser);

        return this;
    }

    public MessageBuilder use(BodyFactory bodyFactory) {
        builder.use(bodyFactory);

        return this;
    }

    public Message build() {
        if (getDate() == null) {
            setDate(new Date());
        }

        return builder.build();
    }

}
