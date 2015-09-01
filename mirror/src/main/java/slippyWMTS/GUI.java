package slippyWMTS;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class GUI extends JFrame implements Runnable, StatusListener {

	private static final int MAX_LINES = 10000;
	private static final int GET_LAYERS = 6;//10;
	private JTextArea log;
	private JProgressBar status;
	private Thread thread;

	GUI() {
		setTitle("geoportal");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		Container pane = getContentPane();
		pane.setLayout(new BorderLayout());
		pane.add(new JScrollPane(log = new JTextArea()), BorderLayout.CENTER);
		setSize(new Dimension(640, 480));
		pane.add(status = new JProgressBar(0, 1000), BorderLayout.NORTH);
		status.setStringPainted(true);;
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
		thread = new Thread(this) {
			public void run() {
				String endpoint = "http://mapy.geoportal.gov.pl/wss/service/WMTS/guest/wmts/TOPO";
//				String[] sets = { ".*EPSG:.*:2180", ".*EPSG:.*:4326" };
				String[] sets = { ".*EPSG:.*:4326" };
				for (String set : sets) {
					for (int i = 0; i <= GET_LAYERS; i++) {
						try {
							final Fetch fetcher = new Fetch(endpoint, i, set) {
								protected void error(String msg) throws IOException {
									showError(msg);
									super.error(msg);
								};
							};
							fetcher.status = GUI.this;
							fetcher.fetch();
						} catch (Exception e) {
							showError(e.toString());
						}
					}
				}
				showError("Koniec");
			}
		};
		thread.start();
	}

	public static void main(String[] args) {
		EventQueue.invokeLater(new GUI());
	}

	@Override
	public void progress(double percent) {
		status.setValue((int) (status.getMaximum() * percent));
	}

	@Override
	public void text(String string) {
		status.setString(string);
	}
}
