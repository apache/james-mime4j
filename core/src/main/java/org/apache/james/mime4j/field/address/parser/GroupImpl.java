package org.apache.james.mime4j.field.address.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.apache.james.mime4j.codec.EncoderUtil;
import org.apache.james.mime4j.field.address.Group;
import org.apache.james.mime4j.field.address.Mailbox;
import org.apache.james.mime4j.field.address.MailboxList;

public class GroupImpl extends Group {

    private static final long serialVersionUID = 3689132038741323839L;


    /**
     * @param name
     *            The group name.
     * @param mailboxes
     *            The mailboxes in this group.
     */
    public GroupImpl(String name, Mailbox... mailboxes) {
        this(name, new MailboxList(Arrays.asList(mailboxes), true));
    }

    /**
     * @param name
     *            The group name.
     * @param mailboxes
     *            The mailboxes in this group.
     */
    public GroupImpl(String name, Collection<Mailbox> mailboxes) {
        this(name, new MailboxList(new ArrayList<Mailbox>(mailboxes), true));
    }

    public GroupImpl(String name, MailboxList mailboxes) {
        super(name, mailboxes);
    }
    
    @Override
    public String getEncodedString() {
        StringBuilder sb = new StringBuilder();

        sb.append(EncoderUtil.encodeAddressDisplayName(getName()));
        sb.append(':');

        boolean first = true;
        for (Mailbox mailbox : getMailboxes()) {
            if (first) {
                first = false;
            } else {
                sb.append(',');
            }

            sb.append(' ');
            sb.append(mailbox.getEncodedString());
        }

        sb.append(';');

        return sb.toString();
    }

}
