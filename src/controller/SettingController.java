package controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.prefs.Preferences;

import javax.swing.JFrame;

import model.PrefContract;
import view.SettingDialog;

public class SettingController {
        private SettingDialog dialog;

    public SettingController(JFrame frame) {
        dialog = new SettingDialog(frame);
        initPreferences(dialog);
        setButtonsListeners(dialog);
        dialog.pack();
        dialog.setVisible(true);
        
    }
    
    private void setButtonsListeners(SettingDialog dialog) {
        dialog.getSaveButton().addActionListener(new SaveActionListener(dialog));
        dialog.getCancelButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        });
    }

    private void initPreferences(SettingDialog dialog) {
        Preferences prefs = PrefContract.getPrefs();
        String valPrinter = prefs.get(PrefContract.KEY_PRINTER, PrefContract.DEF_PRINTER);
        String valUsername = prefs.get(PrefContract.KEY_USERNAME, PrefContract.DEF_USERNAME);
        String valPassoword = prefs.get(PrefContract.KEY_PASSWORD, PrefContract.DEF_PASSWORD);
        String valSender = prefs.get(PrefContract.KEY_SENDER, PrefContract.DEF_SENDER);
        dialog.setPrinter(valPrinter);
        dialog.setUsername(valUsername);
        dialog.setPassword(valPassoword);
        dialog.setSender(valSender);
    }

    private static class SaveActionListener implements ActionListener {
        private SettingDialog dialog;

        SaveActionListener(SettingDialog dialog) {
            this.dialog = dialog;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String valPrinter = dialog.getPrinter();
            String valUsername = dialog.getUsername();
            String valPassoword = dialog.getPassword();
            String valsender = dialog.getSender();
            Preferences prefs = PrefContract.getPrefs();
            prefs.put(PrefContract.KEY_PRINTER, valPrinter);
            prefs.put(PrefContract.KEY_USERNAME, valUsername);
            prefs.put(PrefContract.KEY_PASSWORD, valPassoword);
            prefs.put(PrefContract.KEY_SENDER, valsender);
            dialog.dispose();
        }

    }
}
