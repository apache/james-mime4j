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

package org.apache.james.mime4j.samples.tree;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Date;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

import org.apache.james.mime4j.field.AddressListField;
import org.apache.james.mime4j.field.ContentTypeField;
import org.apache.james.mime4j.field.DateTimeField;
import org.apache.james.mime4j.field.UnstructuredField;
import org.apache.james.mime4j.field.address.Mailbox;
import org.apache.james.mime4j.field.address.MailboxList;
import org.apache.james.mime4j.message.BinaryBody;
import org.apache.james.mime4j.message.Body;
import org.apache.james.mime4j.message.BodyPart;
import org.apache.james.mime4j.message.Entity;
import org.apache.james.mime4j.message.Header;
import org.apache.james.mime4j.message.Message;
import org.apache.james.mime4j.message.Multipart;
import org.apache.james.mime4j.message.TextBody;
import org.apache.james.mime4j.parser.Field;

/**
 * Displays a parsed Message in a window. The window will be divided into
 * two panels. The left panel displays the Message tree. Clicking on a 
 * node in the tree shows information on that node in the right panel.
 *
 * Some of this code have been copied from the Java tutorial's JTree section.
 */
public class MessageTree extends JPanel implements TreeSelectionListener {
    private static final long serialVersionUID = 1L;

    private JPanel contentPane;
    private JTextArea textView;
    private JTree tree;

    /**
     * Wraps an Object and associates it with a text. All message parts 
     * (headers, bodies, multiparts, body parts) will be wrapped in
     * ObjectWrapper instances before they are added to the JTree instance.
     */
    public static class ObjectWrapper {
        private String text = "";
        private Object object = null;
        
        public ObjectWrapper(String text, Object object) {
            this.text = text;
            this.object = object;
        }
        
        @Override
        public String toString() {
            return text;
        }
        
        public Object getObject() {
            return object;
        }
    }
    
    /**
     * Creates a new <code>MessageTree</code> instance displaying the 
     * specified <code>Message</code>.
     * 
     * @param message the message to display.
     */
    public MessageTree(Message message) {
        super(new GridLayout(1,0));

        DefaultMutableTreeNode root = createNode(message);

        tree = new JTree(root);
        tree.getSelectionModel().setSelectionMode(
                TreeSelectionModel.SINGLE_TREE_SELECTION);

        tree.addTreeSelectionListener(this);

        JScrollPane treeView = new JScrollPane(tree);

        contentPane = new JPanel(new GridLayout(1,0));
        JScrollPane contentView = new JScrollPane(contentPane);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setLeftComponent(treeView);
        splitPane.setRightComponent(contentView);

        Dimension minimumSize = new Dimension(100, 50);
        contentView.setMinimumSize(minimumSize);
        treeView.setMinimumSize(minimumSize);
        splitPane.setDividerLocation(250);
        splitPane.setPreferredSize(new Dimension(750, 500));

        add(splitPane);
        
        textView = new JTextArea();
        textView.setEditable(false);
        textView.setLineWrap(true);
        contentPane.add(textView);
    }
    
    /**
     * Create a node given a Multipart body.
     * Add the Preamble, all Body parts and the Epilogue to the node.
     * 
     * @param multipart the Multipart.
     * @return the root node of the tree.
     */
    private DefaultMutableTreeNode createNode(Header header) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(
                new ObjectWrapper("Header", header));

        for (Field field : header.getFields()) {
            String name = field.getName();
            
            node.add(new DefaultMutableTreeNode(new ObjectWrapper(name, field)));
        }        

        return node;
    }
    
    /**
     * Create a node given a Multipart body.
     * Add the Preamble, all Body parts and the Epilogue to the node.
     * 
     * @param multipart the Multipart.
     * @return the root node of the tree.
     */
    private DefaultMutableTreeNode createNode(Multipart multipart) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(
                new ObjectWrapper("Multipart", multipart));

        node.add(new DefaultMutableTreeNode(
                       new ObjectWrapper("Preamble", multipart.getPreamble())));
        for (BodyPart part : multipart.getBodyParts()) {
            node.add(createNode(part));
        }
        node.add(new DefaultMutableTreeNode(
                       new ObjectWrapper("Epilogue", multipart.getEpilogue())));

        return node;
    }
    
    /**
     * Creates the tree nodes given a MIME entity (either a Message or 
     * a BodyPart).
     * 
     * @param entity the entity.
     * @return the root node of the tree displaying the specified entity and 
     *         its children.
     */
    private DefaultMutableTreeNode createNode(Entity entity) {
        
        /*
         * Create the root node for the entity. It's either a
         * Message or a Body part.
         */
        String type = "Message";
        if (entity instanceof BodyPart) {
            type = "Body part";
        }
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(
                                            new ObjectWrapper(type, entity));

        /*
         * Add the node encapsulating the entity Header.
         */
        node.add(createNode(entity.getHeader()));
        
        Body body = entity.getBody();
        
        if (body instanceof Multipart) {
            /*
             * The body of the entity is a Multipart.
             */
            
            node.add(createNode((Multipart) body));
        } else if (body instanceof Message) {
            /*
             * The body is another Message.
             */
            
            node.add(createNode((Message) body));
            
        } else {
            /*
             * Discrete Body (either of type TextBody or BinaryBody).
             */
            type = "Text body";
            if (body instanceof BinaryBody) {
                type = "Binary body";
            }
            
            type += " (" + entity.getMimeType() + ")";
            node.add(new DefaultMutableTreeNode(new ObjectWrapper(type, body)));
            
        }
        
        return node;
    }
    
    /**
     * Called whenever the selection changes in the JTree instance showing
     * the Message nodes.
     * 
     * @param e the event.
     */
    public void valueChanged(TreeSelectionEvent e) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                tree.getLastSelectedPathComponent();

        textView.setText("");
        
        if (node == null) {
            return;
        }
        
        Object o = ((ObjectWrapper) node.getUserObject()).getObject();

        if (node.isLeaf()) {
            
            if (o instanceof TextBody){
                /*
                 * A text body. Display its contents.
                 */
                TextBody body = (TextBody) o;
                StringBuilder sb = new StringBuilder();
                try {
                    Reader r = body.getReader();
                    int c;
                    while ((c = r.read()) != -1) {
                        sb.append((char) c);
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                textView.setText(sb.toString());
                
            } else if (o instanceof BinaryBody){
                /*
                 * A binary body. Display its MIME type and length in bytes.
                 */
                BinaryBody body = (BinaryBody) o;
                int size = 0;
                try {
                    InputStream is = body.getInputStream();
                    while ((is.read()) != -1) {
                        size++;
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                textView.setText("Binary body\n"
                               + "MIME type: " 
                                   + body.getParent().getMimeType() + "\n" 
                               + "Size of decoded data: " + size + " bytes");
                
            } else if (o instanceof ContentTypeField) {
                /*
                 * Content-Type field.
                 */
                ContentTypeField field = (ContentTypeField) o;
                StringBuilder sb = new StringBuilder();
                sb.append("MIME type: " + field.getMimeType() + "\n");
                Map<String, String> params = field.getParameters();
                for (String name : params.keySet()) {
                    sb.append(name + " = " + params.get(name) + "\n");
                }
                textView.setText(sb.toString());
                
            } else if (o instanceof AddressListField) {
                /*
                 * An address field (From, To, Cc, etc)
                 */
                AddressListField field = (AddressListField) o;
                MailboxList list = field.getAddressList().flatten();
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < list.size(); i++) {
                    Mailbox mb = list.get(i);
                    sb.append(mb.getDisplayString() + "\n");
                }
                textView.setText(sb.toString());
                
            } else if (o instanceof DateTimeField) {
                Date date = ((DateTimeField) o).getDate();
                textView.setText(date.toString());                
            } else if (o instanceof UnstructuredField){
                textView.setText(((UnstructuredField) o).getValue());                
            } else if (o instanceof Field){
                textView.setText(((Field) o).getBody());                
            } else {
                /*
                 * The Object should be a Header or a String containing a 
                 * Preamble or Epilogue.
                 */
                textView.setText(o.toString());                
            }
            
        }
    }
    
    /**
     * Creates and displays the gui.
     * 
     * @param message the Message to display in the tree.
     */
    private static void createAndShowGUI(Message message) {
        /*
         * Create and set up the window.
         */
        JFrame frame = new JFrame("MessageTree");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        /*
         * Create and set up the content pane.
         */
        MessageTree newContentPane = new MessageTree(message);
        newContentPane.setOpaque(true);
        frame.setContentPane(newContentPane);

        /*
         * Display the window.
         */
        frame.pack();
        frame.setVisible(true);
    }
    
    public static void main(String[] args) {
        try {
            
            final Message message = new Message(new FileInputStream(args[0]));
            
            javax.swing.SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    createAndShowGUI(message);
                }
            });
        
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("Wrong number of arguments.");
            System.err.println("Usage: org.mime4j.samples.tree.MessageTree"
                             + " path/to/message");
        } catch (FileNotFoundException e) {
            System.err.println("The file '" + args[0] + "' could not be found.");
        } catch (IOException e) {
            System.err.println("The file '" + args[0] + "' could not be read.");
        }
    }

}
