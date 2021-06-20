package model;

import javax.mail.MessagingException;

import com.sun.mail.iap.ProtocolException;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.protocol.IMAPProtocol;

public class DaemonThread extends Thread {
    private boolean isConnected;
    private IMAPFolder folder;

    public DaemonThread(boolean isConnected, IMAPFolder folder) {
        super();
        this.isConnected = isConnected;
        this.folder = folder;
    }

    public void setIsConnected(boolean isConnected) {
        this.isConnected = isConnected;
    }

    public void setFolder(IMAPFolder folder) {
        this.folder = folder;
    }

    @Override
    public void run() {
        while (isConnected) {
            try {
                Thread.sleep(300000); // send NOOP command every 5 minutes
//                    Thread.sleep(5000); // for test only
                // Perform a NOOP just to keep alive the connection
                folder.doCommand(new IMAPFolder.ProtocolCommand() {
                    public Object doCommand(IMAPProtocol p) {
                        System.out.println(Thread.currentThread() + " : send NOOP to server............");
                        try {
                            p.simpleCommand("NOOP", null);
                        } catch (ProtocolException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                });
            } catch (InterruptedException e) {
                JUtils.print("The thread closed.");
            } catch (MessagingException e) {
                System.out.println(Thread.currentThread() + " : failed to send NOOP command.");
            }
        }
    }
}