package view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

public class MyListCellRenderer extends JLabel implements ListCellRenderer<String> {

    @Override
    public Component getListCellRendererComponent(JList<? extends String> list, String value, int index,
            boolean isSelected, boolean cellHasFocus) {
        setText(value);
        setFont(new Font(Font.SANS_SERIF, 0, 28));
        setOpaque(true);
        setBorder(BorderFactory.createEtchedBorder());
        if (isSelected)
            setBackground(Color.LIGHT_GRAY);
        else
            setBackground(Color.WHITE);
        return this;
    }

  

   

}
