/*
 *  Copyright 2004 the mime4j project
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.mime4j.field;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mime4j.field.address.AddressList;
import org.mime4j.field.address.Mailbox;
import org.mime4j.field.address.MailboxList;
import org.mime4j.field.address.parser.ParseException;

public class MailboxField extends Field {
    private static Log log = LogFactory.getLog(MailboxField.class);

    private Mailbox mailbox;
    private ParseException parseException;

    public Mailbox getMailbox() {
        return mailbox;
    }

    public ParseException getParseException() {
        return parseException;
    }

    /**
     * Attempts to parse the body into an e-mail address.
     * If address is invalid, it will be set to null.
     */
    protected void parseBody(String body) {

        try {
            MailboxList mailboxList = AddressList.parse(body).flatten();
            if (mailboxList.size() > 0) {
                mailbox = mailboxList.get(0);
            }
        }
        catch (ParseException e) {
            if (log.isDebugEnabled()) {
                log.debug("Parsing value '" + body + "': "+ e.getMessage());
            }
            parseException = e;
        }
    }

}
