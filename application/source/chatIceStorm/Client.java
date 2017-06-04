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
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import Chat.*;
import Ice.Current;
import Ice.Object;

public class Client extends Ice.Application {

	private static String username;
	
	private static JFrame frame = new JFrame();
	private static JTextArea history = new JTextArea();
	private static JTextArea current = new JTextArea();
	private static JButton send = new JButton("Send");

	private static MessagePrx message;

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
					message.sendOrReceive(username + ": " + current.getText() + "\n");
					current.setText("");
				}
			}
		});

		send.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				message.sendOrReceive(username + ": " + current.getText() + "\n");
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

	public class MessageI extends _MessageDisp {

		private static final long serialVersionUID = 1L;

		@Override
		public void sendOrReceive(String message, Current __current) {
			Calendar calendar = Calendar.getInstance();
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
				"yyyy/MM/dd HH:mm:ss");
			String currentTime = simpleDateFormat
				.format(calendar.getTime());

			history.append(currentTime + "\n" + message + "\n");
			history.setCaretPosition(history.getText().length());
		}
	}

	@Override
	public int run(String[] args) {

		IceStorm.TopicManagerPrx manager = IceStorm.TopicManagerPrxHelper
			.checkedCast(
			communicator().propertyToProxy("TopicManager.Proxy"));
		if (manager == null) {
			System.err.println("invalid proxy");
			return 1;
		}

		// Retrieve the topic.
		IceStorm.TopicPrx topic;
		String topicName = "chat";
		try {
			topic = manager.retrieve(topicName);
		} catch (IceStorm.NoSuchTopic e) {
			try {
				topic = manager.create(topicName);
			} catch (IceStorm.TopicExists ex) {
				System.err.println(appName()
					+ ": temporary failure, try again.");
				return 1;
			}
		}

		// subscriber
		Ice.ObjectAdapter adapter = communicator().createObjectAdapter(
			"Clock.Subscriber");

		//
		// Add a servant for the Ice object. If --id is used the
		// identity comes from the command line, otherwise a UUID is
		// used.
		//
		// id is not directly altered since it is used below to detect
		// whether subscribeAndGetPublisher can raise
		// AlreadySubscribed.
		//
		String id = null;
		Ice.Identity subId = new Ice.Identity(id, "");
		if (subId.name == null) {
			subId.name = java.util.UUID.randomUUID().toString();
		}
		Ice.ObjectPrx subscriber = adapter.add((Object) new MessageI(), subId);

		// Activate the object adapter before subscribing.
		adapter.activate();

		// Set up the proxy.
		subscriber = subscriber.ice_oneway();

		java.util.Map<String, String> qos = new java.util.HashMap<String, String>();
		try {
			topic.subscribeAndGetPublisher(qos, subscriber);
		} catch (IceStorm.AlreadySubscribed e) {
			e.printStackTrace();
			return 1;
		} catch (IceStorm.InvalidSubscriber e) {
			e.printStackTrace();
			return 1;
		} catch (IceStorm.BadQoS e) {
			e.printStackTrace();
			return 1;
		}

		// Get the topic's publisher object, and create a Clock proxy with
		// the mode specified as an argument of this application.
		Ice.ObjectPrx publisher = topic.getPublisher();
		publisher = publisher.ice_oneway();
		message = MessagePrxHelper.uncheckedCast(publisher);

		initGUI();

		// shutdown
		shutdownOnInterrupt();
		communicator().waitForShutdown();

		topic.unsubscribe(subscriber);

		return 0;
	}

	public static void main(String[] args) {
		
		if (args.length < 1) {
			System.out.println("usage: java ChatClient username");
			System.out.println("\"username\" can be specified casually!");
			System.out.println("A meaningful \"username\" can tell others who you are!");
			return;
		} else {
			username = args[0];
		}
		
		Client app = new Client();
		int status = app.main("ChatClient", args, "config.chat");
		System.exit(status);
	}
}
