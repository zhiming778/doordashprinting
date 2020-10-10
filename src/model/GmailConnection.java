package model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.activation.CommandMap;
import javax.activation.MailcapCommandMap;
import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.event.MessageCountAdapter;
import javax.mail.event.MessageCountEvent;

import org.apache.pdfbox.pdmodel.PDDocument;

import com.sun.mail.iap.ProtocolException;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.protocol.IMAPProtocol;

import view.UpdateListener;

public class GmailConnection {

    private UpdateListener updateListener;
    private String senderAddress;
    private AtomicBoolean isConnected;
    private IMAPFolder folder;

    public GmailConnection(UpdateListener updateListener, String senderAddress) {
        this.updateListener = updateListener;
        this.senderAddress = senderAddress;
        isConnected = new AtomicBoolean(false);
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
        // props.put("mail.imaps.usesocketchannels", "true"); //Only for IdleManager
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

    public void fetch(String user, String password) {
        if (user.equals("") || password.equals(""))
            return;
        setCommandMap();
        Session session = createSession();
        IMAPFolder folder = null;
        String host = "imap.gmail.com";
        new ConnectionThread(session, host, user, password, isConnected).start();
        new KeepAliveThread(isConnected).start();
    }

    private class ConnectionThread extends Thread {
        private Store store;
        private String host, user, password;
        private AtomicBoolean isConnected;
        private final Session session;
        private final MyMessageCountAdapter msgCountAdapter;
        private final Map<String, String> dishes;
        private final Map<String, String> required;
        private final Map<String, String> choice;
        private final int RECONNECT_INTERVAL = 60000; // 60 seconds

        public ConnectionThread(final Session session, String host, String user, String password,
                AtomicBoolean isConnected) {
            super();
            this.session = session;
            this.host = host;
            this.user = user;
            this.password = password;
            this.isConnected = isConnected;
            DictionaryLoader loader = new DictionaryLoader();
            dishes = loader.load("dishes.txt");
            required = loader.load("required.txt");
            choice = loader.load("choice.txt");
            msgCountAdapter = new MyMessageCountAdapter();
        }

        @Override
        public void run() {
            super.run();
            boolean isInitializing = true;
            while (true) {
                try {
                    if (isConnected.get())
                        folder.idle();
                    else {
                        if (!isInitializing)
                            Thread.sleep(RECONNECT_INTERVAL);
                        else
                            isInitializing = false;
                        connect();
                    }
                } catch (MessagingException e) {
                    System.out.println(e.getClass().getName() + ": Try to reconnect in 1 minute.");
                    isConnected.set(false);
                    updateListener.setNoConnectionVisibility(true);
                    outputNoConnectionLog();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        private void outputNoConnectionLog() {
            try {
                FileWriter fw = new FileWriter("error_log.txt", true);
                Calendar c = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("[MMM dd HH:mm:ss]");
                fw.write(sdf.format(c.getTime()) + " No Connection.\r\n");
                fw.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void connect() throws MessagingException {
            Store store = session.getStore("imaps");
            store.connect(host, 993, user, password);
            folder = (IMAPFolder) store.getFolder("INBOX");
            folder.open(Folder.READ_ONLY);
            int count = folder.getMessageCount();
            if (count > 0) {
                updateListener.clearList();
                Message[] messages = folder.getMessages((count > 5) ? (count - 5) : 1, count);
                for (Message msg : messages) {
                    handleMessage(msg, false);
                }
            }
            folder.addMessageCountListener(msgCountAdapter);
            isConnected.set(true);
            updateListener.setNoConnectionVisibility(false);
        }

        private void handleMessage(Message msg, Boolean ifPrint) {
            try {
                if (!msg.getFrom()[0].toString().equals(senderAddress))
                    return;
                // convert order
                BodyPart part = ((Multipart) msg.getContent()).getBodyPart(1);
                PDDocument document = PDDocument.load(part.getInputStream());
                Converter converter = new Converter(document, dishes, required, choice);
                Order order = converter.convert();
                updateListener.update(order, ifPrint);
            } catch (MessagingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private class MyMessageCountAdapter extends MessageCountAdapter {
            @Override
            public void messagesAdded(MessageCountEvent event) {
                Message[] messages = event.getMessages();
                for (Message msg : messages) {
                    handleMessage(msg, true);
                }
            }
        }
    }

    private class KeepAliveThread extends Thread {
        private AtomicBoolean isConnected;

        public KeepAliveThread(AtomicBoolean isConnected) {
            super();
            this.isConnected = isConnected;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(300000);   //send NOOP command every 5 minutes
//                    Thread.sleep(5000); // for test only
                    if (isConnected.get()) {
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
                    System.out.println(e.getClass().getName() + ": failed to send NOOP command.");
                }
            }
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

}
