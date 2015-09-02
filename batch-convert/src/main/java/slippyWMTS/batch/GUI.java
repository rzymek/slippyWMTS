package slippyWMTS.batch;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class GUI extends JFrame implements Runnable {

    private static final int MAX_LINES = 10000;
    private static final int GET_LAYERS = 6;//10;
    private JTextArea log;
    private Thread thread;

    GUI() {
        setTitle("geoportal conv");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        Container pane = getContentPane();
        pane.setLayout(new BorderLayout());
        pane.add(new JScrollPane(log = new JTextArea()), BorderLayout.CENTER);
        setSize(new Dimension(640, 480));
        setLocationRelativeTo(null);
    }

    protected void showError(final String string) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                log.append(string);
                log.append("\n");
                if (log.getLineCount() > MAX_LINES) {
                    final String text = log.getText();
                    log.setText(text.substring(text.length() / 2));
                }

            }
        });
    }

    public void run() {
        setVisible(true);
        if(!new File("EPSG4326").isDirectory()){
            JOptionPane.showMessageDialog(this, "Uruchom w tym samym katalogu co /EPSG4326","EPSG4326",JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        thread = new Thread(new Convert() {
            @Override
            protected void progress(String s) {
                showError(s);
            }
        });
        thread.start();
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(new GUI());
    }
}

