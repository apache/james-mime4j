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

import java.io.IOException;
import java.io.OutputStream;

/**
 * Abstract implementation of a single message body; that is, a body that does
 * not contain (directly or indirectly) any other child bodies. It also provides
 * the parent functionality required by bodies.
 */
public abstract class SingleBody implements Body {

    private Entity parent = null;

    /**
     * Sole constructor.
     */
    protected SingleBody() {
    }

    /**
     * @see org.apache.james.mime4j.message.Body#getParent()
     */
    public Entity getParent() {
        return parent;
    }

    /**
     * @see org.apache.james.mime4j.message.Body#setParent(org.apache.james.mime4j.message.Entity)
     */
    public void setParent(Entity parent) {
        this.parent = parent;
    }

    /**
     * Writes this single body to the given stream.
     * 
     * @param out
     *            the stream to write to.
     * @throws IOException
     *             in case of an I/O error
     */
    public abstract void writeTo(OutputStream out) throws IOException;

    /**
     * Returns a copy of this <code>SingleBody</code> (optional operation).
     * <p>
     * The general contract of this method is as follows:
     * <ul>
     * <li>Invoking {@link #getParent()} on the copy returns <code>null</code>.
     * That means that the copy is detached from the parent entity of this
     * <code>SingleBody</code>. The copy may get attached to a different
     * entity later on.</li>
     * <li>The underlying content does not have to be copied. Instead it may be
     * shared between multiple copies of a <code>SingleBody</code>.</li>
     * <li>If the underlying content is shared by multiple copies the
     * implementation has to make sure that the content gets deleted when the
     * last copy gets disposed of (and not before that).</li>
     * </ul>
     * <p>
     * This implementation always throws an
     * <code>UnsupportedOperationException</code>.
     * 
     * @return a copy of this <code>SingleBody</code>.
     * @throws UnsupportedOperationException
     *             if the <code>copy</code> operation is not supported by this
     *             single body.
     */
    public SingleBody copy() {
        throw new UnsupportedOperationException();
    }

    /**
     * Subclasses should override this method if they have allocated resources
     * that need to be freed explicitly (e.g. cannot be simply reclaimed by the
     * garbage collector).
     * 
     * The default implementation of this method does nothing.
     * 
     * @see org.apache.james.mime4j.message.Disposable#dispose()
     */
    public void dispose() {
    }

}
