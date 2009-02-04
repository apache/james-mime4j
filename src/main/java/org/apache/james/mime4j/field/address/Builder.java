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

package org.apache.james.mime4j.field.address;

import org.apache.james.mime4j.codec.DecoderUtil;
import org.apache.james.mime4j.field.address.parser.ASTaddr_spec;
import org.apache.james.mime4j.field.address.parser.ASTaddress;
import org.apache.james.mime4j.field.address.parser.ASTaddress_list;
import org.apache.james.mime4j.field.address.parser.ASTangle_addr;
import org.apache.james.mime4j.field.address.parser.ASTdomain;
import org.apache.james.mime4j.field.address.parser.ASTgroup_body;
import org.apache.james.mime4j.field.address.parser.ASTlocal_part;
import org.apache.james.mime4j.field.address.parser.ASTmailbox;
import org.apache.james.mime4j.field.address.parser.ASTname_addr;
import org.apache.james.mime4j.field.address.parser.ASTphrase;
import org.apache.james.mime4j.field.address.parser.ASTroute;
import org.apache.james.mime4j.field.address.parser.Node;
import org.apache.james.mime4j.field.address.parser.SimpleNode;
import org.apache.james.mime4j.field.address.parser.Token;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Transforms the JJTree-generated abstract syntax tree into a graph of
 * org.apache.james.mime4j.field.address objects.
 */
class Builder {

    private static Builder singleton = new Builder();

    public static Builder getInstance() {
        return singleton;
    }

    public AddressList buildAddressList(ASTaddress_list node) {
        List<Address> list = new ArrayList<Address>();
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            ASTaddress childNode = (ASTaddress) node.jjtGetChild(i);
            Address address = buildAddress(childNode);
            list.add(address);
        }
        return new AddressList(list, true);
    }

    public Address buildAddress(ASTaddress node) {
        ChildNodeIterator it = new ChildNodeIterator(node);
        Node n = it.next();
        if (n instanceof ASTaddr_spec) {
            return buildAddrSpec((ASTaddr_spec) n);
        } else if (n instanceof ASTangle_addr) {
            return buildAngleAddr((ASTangle_addr) n);
        } else if (n instanceof ASTphrase) {
            String name = buildString((ASTphrase) n, false);
            Node n2 = it.next();
            if (n2 instanceof ASTgroup_body) {
                return new Group(name, buildGroupBody((ASTgroup_body) n2));
            } else if (n2 instanceof ASTangle_addr) {
                name = DecoderUtil.decodeEncodedWords(name);
                return new Mailbox(name, buildAngleAddr((ASTangle_addr) n2));
            } else {
                throw new IllegalStateException();
            }
        } else {
            throw new IllegalStateException();
        }
    }

    private MailboxList buildGroupBody(ASTgroup_body node) {
        List<Mailbox> results = new ArrayList<Mailbox>();
        ChildNodeIterator it = new ChildNodeIterator(node);
        while (it.hasNext()) {
            Node n = it.next();
            if (n instanceof ASTmailbox)
                results.add(buildMailbox((ASTmailbox) n));
            else
                throw new IllegalStateException();
        }
        return new MailboxList(results, true);
    }

    public Mailbox buildMailbox(ASTmailbox node) {
        ChildNodeIterator it = new ChildNodeIterator(node);
        Node n = it.next();
        if (n instanceof ASTaddr_spec) {
            return buildAddrSpec((ASTaddr_spec) n);
        } else if (n instanceof ASTangle_addr) {
            return buildAngleAddr((ASTangle_addr) n);
        } else if (n instanceof ASTname_addr) {
            return buildNameAddr((ASTname_addr) n);
        } else {
            throw new IllegalStateException();
        }
    }

    private Mailbox buildNameAddr(ASTname_addr node) {
        ChildNodeIterator it = new ChildNodeIterator(node);
        Node n = it.next();
        String name;
        if (n instanceof ASTphrase) {
            name = buildString((ASTphrase) n, false);
        } else {
            throw new IllegalStateException();
        }

        n = it.next();
        if (n instanceof ASTangle_addr) {
            name = DecoderUtil.decodeEncodedWords(name);
            return new Mailbox(name, buildAngleAddr((ASTangle_addr) n));
        } else {
            throw new IllegalStateException();
        }
    }

    private Mailbox buildAngleAddr(ASTangle_addr node) {
        ChildNodeIterator it = new ChildNodeIterator(node);
        DomainList route = null;
        Node n = it.next();
        if (n instanceof ASTroute) {
            route = buildRoute((ASTroute) n);
            n = it.next();
        } else if (n instanceof ASTaddr_spec) {
            // do nothing
        }
        else
            throw new IllegalStateException();

        if (n instanceof ASTaddr_spec)
            return buildAddrSpec(route, (ASTaddr_spec) n);
        else
            throw new IllegalStateException();
    }

    private DomainList buildRoute(ASTroute node) {
        List<String> results = new ArrayList<String>(node.jjtGetNumChildren());
        ChildNodeIterator it = new ChildNodeIterator(node);
        while (it.hasNext()) {
            Node n = it.next();
            if (n instanceof ASTdomain)
                results.add(buildString((ASTdomain) n, true));
            else
                throw new IllegalStateException();
        }
        return new DomainList(results, true);
    }

    private Mailbox buildAddrSpec(ASTaddr_spec node) {
        return buildAddrSpec(null, node);
    }

    private Mailbox buildAddrSpec(DomainList route, ASTaddr_spec node) {
        ChildNodeIterator it = new ChildNodeIterator(node);
        String localPart = buildString((ASTlocal_part) it.next(), true);
        String domain = buildString((ASTdomain) it.next(), true);
        return new Mailbox(route, localPart, domain);
    }

    private String buildString(SimpleNode node, boolean stripSpaces) {
        Token head = node.firstToken;
        Token tail = node.lastToken;
        StringBuilder out = new StringBuilder();

        while (head != tail) {
            out.append(head.image);
            head = head.next;
            if (!stripSpaces)
                addSpecials(out, head.specialToken);
        }
        out.append(tail.image);

        return out.toString();
    }

    private void addSpecials(StringBuilder out, Token specialToken) {
        if (specialToken != null) {
            addSpecials(out, specialToken.specialToken);
            out.append(specialToken.image);
        }
    }

    private static class ChildNodeIterator implements Iterator<Node> {

        private SimpleNode simpleNode;
        private int index;
        private int len;

        public ChildNodeIterator(SimpleNode simpleNode) {
            this.simpleNode = simpleNode;
            this.len = simpleNode.jjtGetNumChildren();
            this.index = 0;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

        public boolean hasNext() {
            return index < len;
        }

        public Node next() {
            return simpleNode.jjtGetChild(index++);
        }

    }
}
