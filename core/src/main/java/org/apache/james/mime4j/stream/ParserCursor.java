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

package org.apache.james.mime4j.stream;

/**
 * <p>
 * This class represents a context of a parsing operation:
 * </p>
 * <ul>
 *  <li>the current position the parsing operation is expected to start at</li>
 *  <li>the bounds limiting the scope of the parsing operation</li>
 * </ul>
 * <p>
 * Copied from Apache HttpCore project
 * </p>
 */
public class ParserCursor {

    private final int lowerBound;
    private final int upperBound;
    private int pos;

    public ParserCursor(int lowerBound, int upperBound) {
        super();
        if (lowerBound < 0) {
            throw new IndexOutOfBoundsException("Lower bound cannot be negative");
        }
        if (lowerBound > upperBound) {
            throw new IndexOutOfBoundsException("Lower bound cannot be greater then upper bound");
        }
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.pos = lowerBound;
    }

    public int getLowerBound() {
        return this.lowerBound;
    }

    public int getUpperBound() {
        return this.upperBound;
    }

    public int getPos() {
        return this.pos;
    }

    public void updatePos(int pos) {
        if (pos < this.lowerBound) {
            throw new IndexOutOfBoundsException("pos: "+pos+" < lowerBound: "+this.lowerBound);
        }
        if (pos > this.upperBound) {
            throw new IndexOutOfBoundsException("pos: "+pos+" > upperBound: "+this.upperBound);
        }
        this.pos = pos;
    }

    public boolean atEnd() {
        return this.pos >= this.upperBound;
    }

    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append('[');
        buffer.append(this.lowerBound);
        buffer.append('>');
        buffer.append(this.pos);
        buffer.append('>');
        buffer.append(this.upperBound);
        buffer.append(']');
        return buffer.toString();
    }

}
