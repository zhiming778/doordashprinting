package view;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JButton;

public class CustomButton extends JButton {

    private final int WIDTH = 100;
    private final int HEIGHT = 40;

    public CustomButton(String text) {
        super(text);
        final Dimension dimension = new Dimension(WIDTH, HEIGHT);
        setPreferredSize(dimension);
        setBorder(BorderFactory.createMatteBorder(-2, -2, -2, 1, Color.GRAY));
        setFocusPainted(false);
    }
    
}
