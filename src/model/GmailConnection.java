package model;

import java.util.Map;
import java.util.Properties;

import javax.activation.CommandMap;
import javax.activation.MailcapCommandMap;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;

import view.UpdateListener;


public class GmailConnection {

    private Session createSession() {
        Session session = Session.getInstance(createProperties(), null);
//        session.setDebug(true);
        return session;
    }

    public void fetch(String userName, String password, String senderAddress, UpdateListener listener) {
        if (userName.equals("") || password.equals(""))
            return;
        String host = "imap.gmail.com";
        setCommandMap();
        Session session = createSession();
        Store store = null;
        try {
            store = session.getStore("imaps");
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }
        DictionaryLoader loader = new DictionaryLoader();
        Map<String, String> dishes = loader.load("dishes.txt");
        Map<String, String> required = loader.load("required.txt");
        Map<String, String> choices = loader.load("choices.txt");
        new ConnectionThread(store, userName, password, host, senderAddress, dishes, required, choices, listener).start();
    }

    
//    private void saveOrders(Message[] messages) {
//        int count = messages.length;
//        for (int i = 0; i < count; i++) {
//            try {
//                if (!messages[i].getFrom()[0].toString().equals(senderAddress))
//                    return;
//                // convert order
//                BodyPart part = ((Multipart) messages[i].getContent()).getBodyPart(1);
//                PDDocument document = PDDocument.load(part.getInputStream());
//                document.save(new File("test" + i + ".pdf"));
//                document.close();
//            } catch (MessagingException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//        }
//    }

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
}
