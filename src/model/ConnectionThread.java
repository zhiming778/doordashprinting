package model;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Store;
import javax.mail.event.MessageCountAdapter;
import javax.mail.event.MessageCountEvent;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripperByArea;

import com.sun.mail.imap.IMAPFolder;

import view.UpdateListener;

public class ConnectionThread extends Thread {
    private final MyMessageCountAdapter msgCountAdapter;
    private String userName;
    private String password;
    private String senderAddress;
    private String host;
    private final int RECONNECT_INTERVAL = 60000; // 60 seconds
    private Store store;
    private Converter converter;
    private DaemonThread daemonThread;
    private AtomicBoolean isConnected;
    private IMAPFolder folder;

    private UpdateListener updateListener;

    public ConnectionThread(Store store, String userName, String password, String host, String senderAddress,
            Map<String, String> dishes, Map<String, String> required, Map<String, String> choices,
            UpdateListener listener) {
        super();
        isConnected = new AtomicBoolean(false);
        this.userName = userName;
        this.password = password;
        this.senderAddress = senderAddress;
        this.host = host;
        this.store = store;
        msgCountAdapter = new MyMessageCountAdapter();
        converter = new Converter(dishes, required, choices);
        updateListener = listener;
    }

    @Override
    public void run() {
        super.run();
        boolean isInitializing = true;
        while (true) {
            try {
                if (isConnected.get()) {
                    folder.idle(); // it blocks. sending NOOP message will make folder out of idle state.
                    System.out.println("after folder");
                } else {
                    if (!isInitializing)
                        Thread.sleep(RECONNECT_INTERVAL);
                    else
                        isInitializing = false;
                    connect();
                }
            } catch (MessagingException e) {
//                System.out.println(e.getClass().getName() + ": Try to reconnect in 1 minute.");
                isConnected.set(false);
                if (daemonThread != null)
                    daemonThread.setIsConnected(false);
                updateListener.setNoConnectionVisibility(true);
                outputNoConnectionLog();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void connect() throws MessagingException {
        if (!store.isConnected())
            store.connect(host, 993, userName, password);
        folder = (IMAPFolder) store.getFolder("INBOX");
        folder.open(Folder.READ_ONLY);
        int count = folder.getMessageCount();
        if (count > 0) {
            updateListener.clearList();
            Message[] messages = folder.getMessages(count > 6 ? count - 6 : 1, count);
            for (Message msg : messages) {
                handleMessage(msg, false);
            }
        }
        folder.addMessageCountListener(msgCountAdapter);
        isConnected.set(true);
        updateListener.setNoConnectionVisibility(false);
        daemonThread = new DaemonThread(true, folder);
        daemonThread.start();
    }

    private void handleMessage(Message msg, Boolean ifPrint) {
        try {
            if (!msg.getFrom()[0].toString().equals(senderAddress))
                return;
            // convert order
            BodyPart part = ((Multipart) msg.getContent()).getBodyPart(1);
            if (part == null)
                return;
            PDDocument document = PDDocument.load(part.getInputStream());
            if (document == null)
                return;
            Order order = converter.convert(document, new PDFTextStripperByArea());
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
}
