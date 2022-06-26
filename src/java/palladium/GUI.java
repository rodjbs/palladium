package palladium;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import clojure.java.api.Clojure;
import clojure.lang.IFn;

public class GUI implements ActionListener {

    private JTextArea textArea;

    public GUI() {
        JFrame frame = new JFrame("test");
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("File");
        menu.setMnemonic(KeyEvent.VK_F);
        menuBar.add(menu);
        JMenuItem menuItem;
        menuItem = new JMenuItem("Save...", KeyEvent.VK_S);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
        menu.add(menuItem);
        menuItem = new JMenuItem("Open...", KeyEvent.VK_O);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
        menu.add(menuItem);
        menuItem = new JMenuItem("Compile", KeyEvent.VK_C);
        menu.add(menuItem);
        menu.setActionCommand("compile");
        menu.addActionListener(this);
        menuItem = new JMenuItem("Run", KeyEvent.VK_R);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK));
        menu.add(menuItem);
        frame.setJMenuBar(menuBar);
        textArea = new JTextArea();
        textArea.setFont(textArea.getFont().deriveFont(16.0f));
        textArea.setColumns(60);
        textArea.setRows(30);
        JScrollPane scrollPane = new JScrollPane(textArea);
        frame.add(scrollPane);
        frame.pack();
        frame.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getActionCommand().equals("compile")) {
            // typecheck
            // compile
            // build
        }
    }
}
