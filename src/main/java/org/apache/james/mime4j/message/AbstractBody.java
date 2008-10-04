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


/**
 * Abstract <code>Body</code> implementation providing the parent
 * functionality required by bodies.
 *
 * 
 * @version $Id: AbstractBody.java,v 1.2 2004/10/02 12:41:11 ntherning Exp $
 */
public abstract class AbstractBody implements Body {
    private Entity parent = null;
    protected boolean disposed = false;
    
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
        if (disposed)
            throw new IllegalStateException("AbstractBody has been disposed");

        this.parent = parent;
    }

    /**
     * Subclasses should override this method if they have allocated resources
     * that need to be freed explicitly (e.g. cannot be simply reclaimed by the
     * garbage collector). Subclasses that override this method should invoke
     * super.dispose().
     * 
     * The default implementation marks this AbstractBody as disposed.
     * 
     * @see org.apache.james.mime4j.message.Disposable#dispose()
     */
    public void dispose() {
        if (disposed)
            return;

        disposed = true;

        parent = null;
    }

    /**
     * Ensures that the <code>dispose</code> method of this abstract body is
     * called when there are no more references to it.
     *
     * Leave them out ATM (https://issues.apache.org/jira/browse/MIME4J-72?focusedCommentId=12636007#action_12636007)
    protected void finalize() throws Throwable {
        dispose();
    }
     */

}
