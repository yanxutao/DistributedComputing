import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class Client {

	private static String username;

	private static JFrame frame = new JFrame();
	private static JTextArea history = new JTextArea();
	private static JTextArea current = new JTextArea();
	private static JButton send = new JButton("Send");

	public static Socket clientSocket;

	public Client() {

	}

	public static void main(String[] args) {
		if (args.length < 1) {
			System.out.println("usage: java ChatClient username");
			System.out.println("\"username\" can be specified casually!");
			System.out
				.println("A meaningful \"username\" can tell others who you are!");
			return;
		} else {
			username = args[0];
		}
		initGUI();
		receiveMessage();
	}

	public static void initGUI() {

		GridBagLayout layout = new GridBagLayout();
		frame.getContentPane().setLayout(layout);

		GridBagConstraints constraints = new GridBagConstraints();

		constraints.fill = GridBagConstraints.BOTH;
		constraints.anchor = GridBagConstraints.EAST;
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.gridwidth = 2;
		constraints.gridheight = 1;
		constraints.weightx = 1;
		constraints.weighty = 1;
		constraints.ipadx = 0;
		constraints.ipady = 50;
		constraints.insets = new Insets(25, 25, 10, 25);
		frame.add(new JScrollPane(history), constraints);

		constraints.fill = GridBagConstraints.BOTH;
		constraints.anchor = GridBagConstraints.EAST;
		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.gridwidth = 1;
		constraints.gridheight = 1;
		constraints.weightx = 1;
		constraints.weighty = 0;
		constraints.ipadx = 260;
		constraints.ipady = 10;
		constraints.insets = new Insets(10, 25, 20, 10);
		frame.add(new JScrollPane(current), constraints);

		constraints.fill = GridBagConstraints.BOTH;
		constraints.anchor = GridBagConstraints.EAST;
		constraints.gridx = 1;
		constraints.gridy = 1;
		constraints.gridwidth = 1;
		constraints.gridheight = 1;
		constraints.weightx = 1;
		constraints.weighty = 0;
		constraints.ipadx = 0;
		constraints.ipady = 0;
		constraints.insets = new Insets(10, 10, 20, 25);
		frame.add(send, constraints);

		history.setEditable(false);
		send.setEnabled(false);

		history.setLineWrap(true);
		current.setLineWrap(true);

		int width = 480;
		int height = 400;
		frame.setTitle("Chat " + username);
		frame.setSize(width, height);
		frame.setVisible(true);
		frame.setResizable(false);
		current.requestFocus();

		current.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				if (current.getText().equals("")) {
					send.setEnabled(false);
				} else {
					send.setEnabled(true);
				}
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				if (current.getText().equals("")) {
					send.setEnabled(false);
				} else {
					send.setEnabled(true);
				}
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				if (current.getText().equals("")) {
					send.setEnabled(false);
				} else {
					send.setEnabled(true);
				}
			}
		});

		current.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void keyReleased(KeyEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void keyPressed(KeyEvent e) {
				if (current.getText().equals("")) {
					return;
				}
				if ((e.getKeyCode() == KeyEvent.VK_ENTER)
					&& (((InputEvent) e)
						.isControlDown())) {
					try {
						OutputStream outToServer = clientSocket
							.getOutputStream();

						String message = username + ":\n" + current.getText()
							+ "\n";
						message += "END_OF_MESSAGE\n";
						outToServer.write(message.getBytes("UTF-8"));
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					current.setText("");
				}
			}
		});

		send.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					OutputStream outToServer = clientSocket.getOutputStream();

					String message = username + ":\n" + current.getText()
						+ "\n";
					message += "END_OF_MESSAGE\n";
					outToServer.write(message.getBytes());
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				current.setText("");
			}
		});

		frame.addWindowListener(new WindowListener() {

			@Override
			public void windowOpened(WindowEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowIconified(WindowEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowDeiconified(WindowEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowDeactivated(WindowEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowClosing(WindowEvent e) {
				try {
					OutputStream outToServer = clientSocket.getOutputStream();

					String message = current.getText() + "\n";
					message += "END_OF_CHAT\n";
					outToServer.write(message.getBytes());
				} catch (IOException e1) {
					e1.printStackTrace();
				}

				System.exit(0);
			}

			@Override
			public void windowClosed(WindowEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowActivated(WindowEvent e) {
				// TODO Auto-generated method stub

			}
		});
	}

	public static void receiveMessage() {

		String hostname = "127.0.0.1";
		int port = 2229;
		try {
			clientSocket = new Socket(hostname, port);
		} catch (IOException e) {
			e.printStackTrace();
		}

		while (true) {
			try {
				InputStream is = clientSocket.getInputStream();
				BufferedReader reader = new BufferedReader(
					new InputStreamReader(is, "UTF-8"));

				StringBuffer sbf = new StringBuffer();
				String strRead = null;
				while ((strRead = reader.readLine()) != null) {
					if (strRead.equals("END_OF_MESSAGE")) {
						break;
					}
					sbf.append(strRead);
					sbf.append("\n");
				}

				String result = sbf.toString();
				Calendar calendar = Calendar.getInstance();
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
					"yyyy/MM/dd HH:mm:ss");
				String currentTime = simpleDateFormat
					.format(calendar.getTime());

				history.append(currentTime + "\n" + result + "\n");
				history.setCaretPosition(history.getText().length());

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}