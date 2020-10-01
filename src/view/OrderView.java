package view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import model.Order;

public class OrderView extends JFrame {
    private JTextArea taDetail;
    private JPanel btnPanel;
    private JButton btnPrint;
    private JList<String> list;
    private JScrollPane scrollPane;
    private final ListSelectionListener selectionListener;

    public OrderView(String title, OnItemClickListener itemClicklistener, OnPrintListener printListener) {
        super(title);
        setFont(new Font(Font.SANS_SERIF, 0, 28));
        BorderLayout layout = new BorderLayout();
        setLayout(layout);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Font font = new Font(Font.SANS_SERIF, 0, 24);
        selectionListener = new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    int index = list.getSelectedIndex();
                    itemClicklistener.onItemClick(index);
                }
            }
        };
        btnPrint = new JButton("Print");
        btnPrint.setFont(font);
        btnPrint.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                int index = list.getSelectedIndex();
                if (index >= 0) {
                    printListener.onPrint(index);
                }
            }
        });
        list = new JList<String>();
        list.setPreferredSize(new Dimension(200, 1000));
        list.setFont(font);
        
        list.setCellRenderer(new MyListCellRenderer());
        list.setLayoutOrientation(JList.VERTICAL);

        scrollPane = new JScrollPane();
        scrollPane.setViewportView(list);
        scrollPane.setPreferredSize(new Dimension(250, 700));
        scrollPane.setHorizontalScrollBar(null);

        taDetail = new JTextArea();
        taDetail.setPreferredSize(new Dimension(400, 700));
        taDetail.setFont(font);
        btnPanel = new JPanel();

        btnPanel.setLayout(new FlowLayout());
        btnPanel.add(btnPrint);
        add(scrollPane, BorderLayout.WEST);
        add(taDetail, BorderLayout.EAST);
        add(btnPanel, BorderLayout.SOUTH);

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
        taDetail.append("==========================================\n");
        String[] items = order.getItems();
        for (String item : items) {
            taDetail.append(item + "\n");
        }
        taDetail.append("==========================================\n");
        taDetail.append("Total:\t" + order.getTotal());
    }

    public void setListData(String[] data) {
        list.removeListSelectionListener(selectionListener);
        list.removeAll();
        list.setListData(data);
        list.addListSelectionListener(selectionListener);
        setListHeight(data.length);
    }

    public JButton getPrintButton() {
        return btnPrint;
    }

}
