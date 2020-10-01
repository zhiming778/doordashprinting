import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.activation.CommandMap;
import javax.activation.MailcapCommandMap;
import javax.mail.AuthenticationFailedException;
import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.event.MessageCountAdapter;
import javax.mail.event.MessageCountEvent;

import org.apache.pdfbox.pdmodel.PDDocument;

import com.sun.mail.iap.ProtocolException;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.protocol.IMAPProtocol;

import model.Converter;
import model.JUtils;
import model.Order;
import view.UpdateListener;

public class GmailConnection {

    private UpdateListener updateListener;
    private String senderAddress;

    public GmailConnection(UpdateListener updateListener, String senderAddress) {
        this.updateListener = updateListener;
        this.senderAddress = senderAddress;
    }

    private Session createSession() {
        Session session = Session.getInstance(createProperties(), null);
//        session.setDebug(true);
        return session;
    }

    private Properties createProperties() {
        Properties props = new Properties();
        props.put("mail.imap.usesocketchannels", "true");
        props.put("mail.imap.ssl.enable", "true");
        props.put("mail.imap.port", "993");
        props.put("mail.imap.auth", "true");
        props.put("mail.imap.starttls.enable", "true");
        props.put("mail.store.protocol", "imaps");
        props.put("mail.imap.sasl.enable", "true");
        // props.put("mail.imaps.usesocketchannels", "true");
        return props;
    }

    private void setCommandMap() {
        MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
        mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
        mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
        mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
        mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
        mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822");
        CommandMap.setDefaultCommandMap(mc);
    }

    public void fetch(String user, String password, Map<String, String> dishes, Map<String, String> required,
            Map<String, String> choice) {
        setCommandMap();
        try {
            Session session = createSession();
            Store store = session.getStore("imaps");
            final String host = "imap.gmail.com";
            store.connect(host, 993, user, password);
            ExecutorService es = Executors.newCachedThreadPool();
            IMAPFolder folder = (IMAPFolder) store.getFolder("INBOX");
            folder.open(Folder.READ_ONLY);
            int count = folder.getMessageCount();
            if (count > 0) {
                Message[] messages = folder.getMessages((count > 6) ? (count - 6) : 1, count);
                for (Message msg : messages) {
                    handleMessage(msg, false, dishes, required, choice);
                }
            }
            folder.addMessageCountListener(new MyMessageCountAdapter(dishes, required, choice));
            new Thread() {

                @Override
                public void run() {
                    try {
                        while (true)
                            folder.idle();
                    } catch (MessagingException e) {
                        e.printStackTrace();
                    }
                }

            }.start();
            new Thread() {
                @Override
                public void run() {
                    try {
                        while (true) {
                            Thread.sleep(300000);
                            JUtils.print("send noop command");
                            // Perform a NOOP just to keep alive the connection
                            folder.doCommand(new IMAPFolder.ProtocolCommand() {
                                public Object doCommand(IMAPProtocol p) throws ProtocolException {
                                    System.out.println("send NOOP to server............");
                                    p.simpleCommand("NOOP", null);
                                    return null;
                                }
                            });
                        }
                    } catch (InterruptedException e) {
                        JUtils.print("The thread closed.");
                    } catch (MessagingException e) {
                        e.printStackTrace();
                    }
                }

            }.start();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (AuthenticationFailedException e) {
            e.getMessage();
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    private void handleMessage(Message msg, Boolean ifPrint, Map<String, String> dishes, Map<String, String> required,
            Map<String, String> choice) {
        try {
            if (!msg.getFrom()[0].toString().equals(senderAddress))
                return;
            // convert order
            BodyPart part = ((Multipart) msg.getContent()).getBodyPart(1);
            PDDocument document = PDDocument.load(part.getInputStream());
            
            byte[] lines = part.getInputStream().readAllBytes();
            System.out.println(new String(Base64.getDecoder().decode(lines)));
            
            
            Converter converter = new Converter(document, dishes, required, choice);
            Order order = converter.convert();
            updateListener.update(order, ifPrint);
        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    // Only for test
    private void saveOrders(Message[] messages) {
        int count = messages.length;
        for (int i = 0; i < count; i++) {
            try {
                if (!messages[i].getFrom()[0].toString().equals(senderAddress))
                    return;
                // convert order
                BodyPart part = ((Multipart) messages[i].getContent()).getBodyPart(1);
                PDDocument document = PDDocument.load(part.getInputStream());
                document.save(new File("test" + i + ".pdf"));
                document.close();
            } catch (MessagingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private class MyMessageCountAdapter extends MessageCountAdapter {
        private final Map<String, String> dishes, required, choice;

        public MyMessageCountAdapter(Map<String, String> dishes, Map<String, String> required,
                Map<String, String> choice) {
            super();
            this.dishes = dishes;
            this.required = required;
            this.choice = choice;
        }

        @Override
        public void messagesAdded(MessageCountEvent event) {
            Message[] messages = event.getMessages();
            for (Message msg : messages) {
                handleMessage(msg, true, dishes, required, choice);
            }
        }
    }

}
