package org.apache.james.mime4j.field.address.parser;

import org.apache.james.mime4j.codec.EncoderUtil;
import org.apache.james.mime4j.dom.address.DomainList;
import org.apache.james.mime4j.dom.address.Mailbox;

public class MailboxImpl extends Mailbox {

    private static final long serialVersionUID = 2836627303544263659L;

    public MailboxImpl(String name, DomainList route, String localPart,
            String domain) {
        super(name, route, localPart, domain);
    }


    /**
     * Creates an unnamed mailbox without a route. Routes are obsolete.
     * 
     * @param localPart
     *            The part of the e-mail address to the left of the "@".
     * @param domain
     *            The part of the e-mail address to the right of the "@".
     */
    public MailboxImpl(String localPart, String domain) {
        this(null, null, localPart, domain);
    }

    /**
     * Creates an unnamed mailbox with a route. Routes are obsolete.
     * 
     * @param route
     *            The zero or more domains that make up the route. May be
     *            <code>null</code>.
     * @param localPart
     *            The part of the e-mail address to the left of the "@".
     * @param domain
     *            The part of the e-mail address to the right of the "@".
     */
    public MailboxImpl(DomainList route, String localPart, String domain) {
        this(null, route, localPart, domain);
    }

    /**
     * Creates a named mailbox without a route. Routes are obsolete.
     * 
     * @param name
     *            the name of the e-mail address. May be <code>null</code>.
     * @param localPart
     *            The part of the e-mail address to the left of the "@".
     * @param domain
     *            The part of the e-mail address to the right of the "@".
     */
    public MailboxImpl(String name, String localPart, String domain) {
        this(name, null, localPart, domain);
    }

    @Override
    public String getEncodedString() {
        StringBuilder sb = new StringBuilder();

        if (getName() != null) {
            sb.append(EncoderUtil.encodeAddressDisplayName(getName()));
            sb.append(" <");
        }

        sb.append(EncoderUtil.encodeAddressLocalPart(getLocalPart()));

        // domain = dot-atom / domain-literal
        // domain-literal = [CFWS] "[" *([FWS] dtext) [FWS] "]" [CFWS]
        // dtext = %d33-90 / %d94-126
        if (getDomain() != null) {
            sb.append('@');
            sb.append(getDomain());
        }

        if (getName() != null) {
            sb.append('>');
        }

        return sb.toString();
    }

}
