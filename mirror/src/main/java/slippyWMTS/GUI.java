package slippyWMTS;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class GUI extends JFrame implements Runnable {

	private static final int MAX_LINES = 10000;
	private JTextArea log;
	private JLabel status;
	private Thread thread;

	GUI() {
		setTitle("geoportal");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		Container pane = getContentPane();
		pane.setLayout(new BorderLayout());
		pane.add(new JScrollPane(log = new JTextArea()), BorderLayout.CENTER);
		pane.add(status = new JLabel("Starting..."), BorderLayout.NORTH);	
		setSize(new Dimension(640, 480));
		setLocationRelativeTo(null);
	}

	protected void error(final String string) {
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

	protected void info(final String msg) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				status.setText(msg);
			}			
		});
	}

	public void run() {
		setVisible(true);
		thread = new Thread(this) {
			public void run() {

				String endpoint = "http://mapy.geoportal.gov.pl/wss/service/WMTS/guest/wmts/TOPO";
				String[] sets = { ".*EPSG:.*:2180", ".*EPSG:.*:4326" };
				for (String set : sets) {
					for (int i = 0; i <= 10; i++) {
						error("Warstwa: "+i);
						try {
							new Fetch(endpoint, i, set) {
								@Override
								protected void log(String x) {
									info(x);
									super.log(x);
								}

								protected void error(String msg) throws IOException {
									error(msg);
									super.error(msg);
								};
							}.fetch();
						} catch (Exception e) {
							error(e.toString());
						}
					}
				}
				info("Koniec");
				error("Koniec");
			}
		};
		thread.start();
	}

	public static void main(String[] args) {
		EventQueue.invokeLater(new GUI());
	}
}
