
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {

	public Server() {

	}

	public static void main(String[] args) {

		int port = 2229;
		ArrayList<Socket> clientList = new ArrayList<Socket>();

		try {
			@SuppressWarnings("resource")
			ServerSocket serverSocket = new ServerSocket(port);
			
			while (true) {
				Socket clientSocket = serverSocket.accept();
				clientList.add(clientSocket);
				
				Thread chatServerThread = new Thread(new ServerThread(
					clientSocket, clientList));
				chatServerThread.start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
