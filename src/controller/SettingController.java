package controller;

import java.util.prefs.Preferences;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;

import model.PrefContract;
import view.SettingDialog;
import view.SettingDialog.OnSettingListener;

public class SettingController implements OnSettingListener {
    private SettingDialog dialog;

    public SettingController(SettingDialog dialog) {
        this.dialog = dialog;
        initPreferences(dialog);
        dialog.pack();
        dialog.setVisible(true);

    }

    public void setOnSettingListener() {
        dialog.setOnSettingListener((OnSettingListener) this);
    }

    @Override
    public void save(String valPrinter, String valUsername, String valPassoword, String valsender) {
        Preferences prefs = Preferences.userNodeForPackage(PrefContract.class);
        prefs.put(PrefContract.KEY_PRINTER, valPrinter);
        prefs.put(PrefContract.KEY_USERNAME, valUsername);
        prefs.put(PrefContract.KEY_PASSWORD, valPassoword);
        prefs.put(PrefContract.KEY_SENDER, valsender);
    }

    private void initPreferences(SettingDialog dialog) {
        Preferences prefs = Preferences.userNodeForPackage(PrefContract.class);
        String valPrinter = prefs.get(PrefContract.KEY_PRINTER, PrefContract.DEF_PRINTER);
        String valUsername = prefs.get(PrefContract.KEY_USERNAME, PrefContract.DEF_USERNAME);
        String valPassoword = prefs.get(PrefContract.KEY_PASSWORD, PrefContract.DEF_PASSWORD);
        String valSender = prefs.get(PrefContract.KEY_SENDER, PrefContract.DEF_SENDER);
        dialog.setPrinters(loadPrinters(valPrinter));
        dialog.setUsername(valUsername);
        dialog.setPassword(valPassoword);
        dialog.setSender(valSender);
    }

    private String[] loadPrinters(String def) {
        PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
        String[] printers = new String[services.length + 1];
        printers[0] = def;
        for (int i = 1; i <= services.length; i++)
            printers[i] = services[i - 1].getName();
        return printers;
    }
}