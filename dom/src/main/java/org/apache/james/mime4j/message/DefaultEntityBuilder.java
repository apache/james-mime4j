/* BEGIN LICENSE
 * Copyright © Blue Mind SAS, 2012-2014
 *
 * This file is part of Blue Mind. Blue Mind is a messaging and collaborative
 * solution.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of either the GNU Affero General Public License as
 * published by the Free Software Foundation (version 3 of the License)
 * or the CeCILL as published by CeCILL.info (version 2 of the License).
 *
 * There are special exceptions to the terms and conditions of the
 * licenses as they are applied to this program. See LICENSE.txt in
 * the directory of this program distribution.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * See LICENSE.txt
 * END LICENSE
 */
package org.apache.james.mime4j.message;

import java.io.IOException;
import java.io.InputStream;
import java.util.Stack;

import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.dom.Body;
import org.apache.james.mime4j.dom.Entity;
import org.apache.james.mime4j.dom.Header;
import org.apache.james.mime4j.dom.Message;
import org.apache.james.mime4j.dom.Multipart;
import org.apache.james.mime4j.parser.ContentHandler;
import org.apache.james.mime4j.stream.BodyDescriptor;
import org.apache.james.mime4j.stream.Field;
import org.apache.james.mime4j.stream.RawField;
import org.apache.james.mime4j.util.ByteArrayBuffer;
import org.apache.james.mime4j.util.ByteSequence;

/**
 * Blue Mind class to extend default mime4j parsing
 * 
 * @author tom
 * 
 */
public class DefaultEntityBuilder implements ContentHandler {

	private final Entity entity;
	private final BodyFactory bodyFactory;
	private final Stack<Object> stack;

	public DefaultEntityBuilder(final Entity entity,
			final BodyFactory bodyFactory) {
		this.entity = entity;
		this.bodyFactory = bodyFactory;
		this.stack = new Stack<Object>();
	}

	private void expect(Class<?> c) {
		if (!c.isInstance(stack.peek())) {
			throw new IllegalStateException("Internal stack error: "
					+ "Expected '" + c.getName() + "' found '"
					+ stack.peek().getClass().getName() + "'");
		}
	}

	/**
	 * @see org.apache.james.mime4j.parser.ContentHandler#startMessage()
	 */
	public void startMessage() throws MimeException {
		if (stack.isEmpty()) {
			stack.push(this.entity);
		} else {
			expect(Entity.class);
			Message m = new MessageImpl();
			((Entity) stack.peek()).setBody(m);
			stack.push(m);
		}
	}

	/**
	 * @see org.apache.james.mime4j.parser.ContentHandler#endMessage()
	 */
	public void endMessage() throws MimeException {
		expect(Message.class);
		stack.pop();
	}

	/**
	 * @see org.apache.james.mime4j.parser.ContentHandler#startHeader()
	 */
	public void startHeader() throws MimeException {
		stack.push(new HeaderImpl());
	}

	/**
	 * @see org.apache.james.mime4j.parser.ContentHandler#field(RawField)
	 */
	public void field(Field field) throws MimeException {
		expect(Header.class);
		((Header) stack.peek()).addField(field);
	}

	/**
	 * @see org.apache.james.mime4j.parser.ContentHandler#endHeader()
	 */
	public void endHeader() throws MimeException {
		expect(Header.class);
		Header h = (Header) stack.pop();
		expect(Entity.class);
		((Entity) stack.peek()).setHeader(h);
	}

	/**
	 * @see org.apache.james.mime4j.parser.ContentHandler#startMultipart(org.apache.james.mime4j.stream.BodyDescriptor)
	 */
	public void startMultipart(final BodyDescriptor bd) throws MimeException {
		expect(Entity.class);

		final Entity e = (Entity) stack.peek();
		final String subType = bd.getSubType();
		final Multipart multiPart = new MultipartImpl(subType);
		e.setBody(multiPart);
		stack.push(multiPart);
		preMultipart(multiPart);
	}

	/**
	 * The given Multipart is added into the mime tree
	 * 
	 * @param multiPart
	 */
	public void preMultipart(Multipart multiPart) {
	}

	/**
	 * @see org.apache.james.mime4j.parser.ContentHandler#body(org.apache.james.mime4j.stream.BodyDescriptor,
	 *      java.io.InputStream)
	 */
	public void body(BodyDescriptor bd, final InputStream is)
			throws MimeException, IOException {
		expect(Entity.class);

		// NO NEED TO MANUALLY RUN DECODING.
		// The parser has a "setContentDecoding" method. We should
		// simply instantiate the MimeStreamParser with that method.

		// final String enc = bd.getTransferEncoding();

		final Body body;

		/*
		 * final InputStream decodedStream; if (MimeUtil.ENC_BASE64.equals(enc))
		 * { decodedStream = new Base64InputStream(is); } else if
		 * (MimeUtil.ENC_QUOTED_PRINTABLE.equals(enc)) { decodedStream = new
		 * QuotedPrintableInputStream(is); } else { decodedStream = is; }
		 */

		if (bd.getMimeType().startsWith("text/")) {
			body = bodyFactory.textBody(is, bd.getCharset());
		} else {
			body = bodyFactory.binaryBody(is);
		}

		Entity entity = ((Entity) stack.peek());
		entity.setBody(body);

		postBody(entity, bd, body);
	}

	/**
	 * Called once the {@link Body} b has been
	 * 
	 * @param e
	 * @param b
	 */
	public void postBody(Entity e, BodyDescriptor bd, Body b) {
	}

	/**
	 * @see org.apache.james.mime4j.parser.ContentHandler#endMultipart()
	 */
	public void endMultipart() throws MimeException {
		Object multipart = stack.pop();
		postMultipart(multipart);
	}

	/**
	 * Called when parsing of multipart ends
	 * 
	 * @param multipart
	 *            {@link Multipart} object ?
	 */
	public void postMultipart(Object multipart) {

	}

	/**
	 * @see org.apache.james.mime4j.parser.ContentHandler#startBodyPart()
	 */
	public void startBodyPart() throws MimeException {
		expect(Multipart.class);

		BodyPart bodyPart = new BodyPart();
		((Multipart) stack.peek()).addBodyPart(bodyPart);
		stack.push(bodyPart);
	}

	/**
	 * @see org.apache.james.mime4j.parser.ContentHandler#endBodyPart()
	 */
	public void endBodyPart() throws MimeException {
		expect(BodyPart.class);
		stack.pop();
	}

	/**
	 * @see org.apache.james.mime4j.parser.ContentHandler#epilogue(java.io.InputStream)
	 */
	public void epilogue(InputStream is) throws MimeException, IOException {
		expect(MultipartImpl.class);
		ByteSequence bytes = loadStream(is);
		((MultipartImpl) stack.peek()).setEpilogueRaw(bytes);
	}

	/**
	 * @see org.apache.james.mime4j.parser.ContentHandler#preamble(java.io.InputStream)
	 */
	public void preamble(InputStream is) throws MimeException, IOException {
		expect(MultipartImpl.class);
		ByteSequence bytes = loadStream(is);
		((MultipartImpl) stack.peek()).setPreambleRaw(bytes);
	}

	/**
	 * Unsupported.
	 * 
	 * @see org.apache.james.mime4j.parser.ContentHandler#raw(java.io.InputStream)
	 */
	public void raw(InputStream is) throws MimeException, IOException {
		throw new UnsupportedOperationException("Not supported");
	}

	private static ByteSequence loadStream(InputStream in) throws IOException {
		ByteArrayBuffer bab = new ByteArrayBuffer(64);

		int b;
		while ((b = in.read()) != -1) {
			bab.append(b);
		}

		return bab;
	}

	public BodyFactory getBodyFactory() {
		return bodyFactory;
	}

}
