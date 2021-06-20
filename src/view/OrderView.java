package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.FontUIResource;

import model.Order;

public class OrderView extends JFrame implements ActionListener {
    public static final int ACTION_PRINT = 0;
    public static final int ACTION_ABOUT = 1;
    public static final int ACTION_SETTING = 2;

    private JTextArea taDetail;
    private CustomButton btnPrint, btnSetting, btnAbout;
    private JList<String> list;
    private JScrollPane scrollPane;
    private final ListSelectionListener selectionListener;
    private static String TITLE = "Print OrderDash";
    private OnOrderListener onOrderListener;
    private JLabel lbNoConnection;

    public interface OnOrderListener {
        void itemClick(int index);

        void buttonClick(int actionType, int index);
    }

    public OrderView() {
        super(TITLE);
        initFrame();
        selectionListener = new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    int index = list.getSelectedIndex();
                    onOrderListener.itemClick(index);
                }
            }
        };

        list = initList();

        Border emptyBorder = BorderFactory.createEmptyBorder(5, 5, 5, 5);
        scrollPane = new JScrollPane();
        scrollPane.setViewportView(list);
        scrollPane.setPreferredSize(new Dimension(250, 700));
        scrollPane.setHorizontalScrollBar(null);

        taDetail = new JTextArea();
        taDetail.setPreferredSize(new Dimension(400, 700));
        taDetail.setBorder(emptyBorder);

        lbNoConnection = new JLabel("No connection. Try to reconnect in 1 minute.");
        lbNoConnection.setBorder(emptyBorder);
        lbNoConnection.setOpaque(true);
        lbNoConnection.setVisible(false);
        lbNoConnection.setBackground(Color.RED);
        lbNoConnection.setForeground(Color.WHITE);

        add(scrollPane, BorderLayout.CENTER);
        add(taDetail, BorderLayout.EAST);
        add(createButtonPanel(), BorderLayout.NORTH);
        add(lbNoConnection, BorderLayout.SOUTH);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnPrint) {
            int index = list.getSelectedIndex();
            if (index >= 0)
                onOrderListener.buttonClick(ACTION_PRINT, index);
        } else if (e.getSource() == btnAbout) {
            onOrderListener.buttonClick(ACTION_ABOUT, -1);
        } else if (e.getSource() == btnSetting) {
            onOrderListener.buttonClick(ACTION_SETTING, -1);
        }
    }

    public void setOnOrderListener(OnOrderListener onOrderListener) {
        this.onOrderListener = onOrderListener;
    }

    private JPanel createButtonPanel() {
        JPanel btnPanel = new JPanel();
        btnPrint = new CustomButton("Print");
        btnSetting = new CustomButton("Setting");
        btnAbout = new CustomButton("About");
        btnPrint.addActionListener(this);
        btnSetting.addActionListener(this);
        btnAbout.addActionListener(this);
        btnPanel = new JPanel();
        btnPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        btnPanel.add(btnPrint);
        btnPanel.add(btnSetting);
        btnPanel.add(btnAbout);
        return btnPanel;
    }

    private JList<String> initList() {
        JList<String> list = new JList<>();
        list = new JList<String>();
        list.setPreferredSize(new Dimension(200, 1000));

        list.setCellRenderer(new MyListCellRenderer());
        list.setLayoutOrientation(JList.VERTICAL);
        return list;
    }

    private void initFrame() {
        setFont();

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
        BorderLayout layout = new BorderLayout();
        setLayout(layout);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void setFont() {
        Enumeration<Object> keys = UIManager.getDefaults().keys();
        Iterator<Object> iterator = keys.asIterator();
        FontUIResource font = new FontUIResource(Font.SERIF, Font.PLAIN, 24);
        while (iterator.hasNext()) {
            Object key = iterator.next();
            if (UIManager.get(key) instanceof FontUIResource) {
                UIManager.put(key, font);
            }
        }
    }

    private void setListHeight(int count) {
        list.setFixedCellHeight(46);
        Dimension dimension = list.getPreferredSize();
        dimension.height = 46 * count;
        list.setPreferredSize(dimension);
    }

    public void setDetailText(Order order) {
        taDetail.setText("");
        taDetail.append("Id:\t" + order.getId() + "\n");
        taDetail.append("Name:\t" + order.getName() + "\n");
        taDetail.append("Date:\t" + order.getDate() + "\n");
        taDetail.append("# of items:\t" + order.getNumOfItems() + "\n");
        taDetail.append("=========================\n");
        String[] items = order.getItems();
        for (String item : items) {
            taDetail.append(item + "\n");
        }
        taDetail.append("=========================\n");
        taDetail.append("Total:\t" + order.getTotal());
    }

    public void setListData(String[] data) {
        list.removeListSelectionListener(selectionListener);
        list.removeAll();
        list.setListData(data);
        list.addListSelectionListener(selectionListener);
        setListHeight(data.length);
    }

    public void setNoConnectionVisibility(boolean visible) {
        lbNoConnection.setVisible(visible);
    }

    public void clearListData() {
        list.removeAll();
    }

    public JButton getPrintButton() {
        return btnPrint;
    }

    public JButton getSettingButton() {
        return btnSetting;
    }

    public JButton getAboutButton() {
        return btnAbout;
    }
}
