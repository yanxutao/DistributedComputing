import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class ServerThread implements Runnable {

	private Socket clientSocket;
	private ArrayList<Socket> clientList;

	public ServerThread() {

	}

	public ServerThread(Socket clientSocket, ArrayList<Socket> clientList) {
		this.clientSocket = clientSocket;
		this.clientList = clientList;
	}

	public void run() {

		while (true) {
			try {
				InputStream is = clientSocket.getInputStream();

				BufferedReader reader = new BufferedReader(
					new InputStreamReader(is, "UTF-8"));

				StringBuffer sbf = new StringBuffer();
				String strRead = null;
				while ((strRead = reader.readLine()) != null) {
					if (strRead.equals("END_OF_CHAT")) {
						clientList.remove(clientSocket);
						System.out.println("A client quit the chat!\n");
						return;
					}
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

				System.out.println(currentTime + "\n" + result);

				result += "END_OF_MESSAGE\n";

				for (int i = 0; i < clientList.size(); i++) {
					OutputStream outToClient = clientList.get(i)
						.getOutputStream();
					outToClient.write(result.getBytes("UTF-8"));
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
