package view;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.util.Enumeration;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.FontUIResource;

public class SettingDialog extends JDialog {

    private static String TITLE = "Setting";
    private JButton btnSave, btnCancel;
    private JTextField tfPrinter, tfUsername, tfPassword, tfSender;

    public SettingDialog(JFrame frame) throws HeadlessException {
        super(frame, TITLE);
        initFrame(frame);
        JPanel mainPanel = new JPanel();
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        add(mainPanel);

        JLabel lbPrinter = new JLabel("Printer", SwingConstants.LEFT);
        JLabel lbUsername = new JLabel("Username", SwingConstants.LEFT);
        JLabel lbPassword = new JLabel("Password", SwingConstants.LEFT);
        JLabel lbSender = new JLabel("Sender", SwingConstants.LEFT);

        tfPrinter = new JTextField(20);
        tfUsername = new JTextField(20);
        tfPassword = new JTextField(20);
        tfSender = new JTextField(20);

        btnSave = new JButton("Save");
        btnCancel = new JButton("Cancel");

        constraints.anchor = GridBagConstraints.LINE_START;
        constraints.insets = new Insets(4, 4, 4, 4);
        constraints.gridx = 0;
        constraints.gridy = 0;
        mainPanel.add(lbPrinter, constraints);
        constraints.gridx++;
        mainPanel.add(tfPrinter, constraints);
        constraints.gridx = 0;
        constraints.gridy++;
        mainPanel.add(lbUsername, constraints);
        constraints.gridx++;
        mainPanel.add(tfUsername, constraints);
        constraints.gridx = 0;
        constraints.gridy++;
        mainPanel.add(lbPassword, constraints);
        constraints.gridx++;
        mainPanel.add(tfPassword, constraints);
        constraints.gridx = 0;
        constraints.gridy++;
        mainPanel.add(lbSender, constraints);
        constraints.gridx++;
        mainPanel.add(tfSender, constraints);

        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.weightx = 0.5;
        constraints.gridx = 0;
        constraints.gridy++;
        mainPanel.add(btnSave, constraints);
        constraints.gridx++;
        mainPanel.add(btnCancel, constraints);

    }

    private void initFrame(JFrame frame) {
        setFont();
        setLocationRelativeTo(frame);
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
    }

    private void setFont() {
        Enumeration<Object> keys = UIManager.getDefaults().keys();
        Iterator<Object> iterator = keys.asIterator();
        FontUIResource font = new FontUIResource(Font.SERIF, Font.PLAIN, 18);
        while (iterator.hasNext()) {
            Object key = iterator.next();
            if (UIManager.get(key) instanceof FontUIResource) {
                UIManager.put(key, font);
            }
        }
    }

    public String getPrinter() {
        return tfPrinter.getText();
    }

    public String getUsername() {
        return tfUsername.getText();
    }

    public String getPassword() {
        return tfPassword.getText();
    }

    public String getSender() {
        return tfSender.getText();
    }

    public JButton getSaveButton() {
        return btnSave;
    }

    public JButton getCancelButton() {
        return btnCancel;
    }

    public void setPrinter(String printer) {
        tfPrinter.setText(printer);
    }

    public void setUsername(String username) {
        tfUsername.setText(username);
    }

    public void setPassword(String password) {
        tfPassword.setText(password);
    }

    public void setSender(String sender) {
        tfSender.setText(sender);
    }

    public static void main(String[] args) {

    }
}
