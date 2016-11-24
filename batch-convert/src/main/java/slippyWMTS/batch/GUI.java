package slippyWMTS.batch;

import javax.swing.*;
import java.awt.*;

public class GUI extends JFrame implements Runnable {

    private static final int MAX_LINES = 10000;
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
        EventQueue.invokeLater(() -> {
            log.append(string);
            log.append("\n");
            if (log.getLineCount() > MAX_LINES) {
                final String text = log.getText();
                log.setText(text.substring(text.length() / 2));
            }

        });
    }

    public void run() {
        setVisible(true);
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

