import java.io.*;
import java.net.*;

public class Talk {

	private static int port = 12987;
	private static String defaultServer = "localhost";

	public static void main(String[] args) {

		int inputArgOption = analyseInput(args); // Analyzes input arg string
		Client clientTalk = new Client();
		Server serverTalk = new Server();

		switch (inputArgOption) {
		case 0: {
			try {
				clientTalk.setAddress(defaultServer, port);
				clientTalk.runClient();

			} catch (Exception e) {
				System.out.println("Client cannot connect to server: " + defaultServer + ":" + Integer.toString(port)
						+ "\n" + e.getMessage());
			}
			break;
		}

		case 1: {
			serverTalk.setAddress(defaultServer, port);
			serverTalk.runServer();
			break;
		}

		case 2: {
			try {
				clientTalk.setAddress(defaultServer, port);
				clientTalk.runClient();
			} catch (Exception e) {
				serverTalk.setAddress(defaultServer, port);
				serverTalk.runServer();
			}
			break;
		}

		case 3: {
			printOptions();
			break;
		}
		case -1: {
			System.out.println("\n!! Invalid Input: Type -help to get instructions.");
		}

		}

	}

	public static int analyseInput(String inputStr[]) {
		int result = -1;
		try {
			for (int i = 0; i < inputStr.length; i++) {
				if (inputStr[i].equalsIgnoreCase("-h")) // client side
				{
					result = 0;
					if (i + 1 < inputStr.length) {
						if (!inputStr[i + 1].equalsIgnoreCase("-p")) {
							defaultServer = inputStr[i + 1];
							i++;
						} else {
							port = Integer.parseInt(inputStr[i + 2]);
							i += 2;
						}
					}
				} else if (inputStr[i].equalsIgnoreCase("-s")) // Server
				{
					result = 1;
					if (i + 1 < inputStr.length) {
						if (inputStr[i + 1].equalsIgnoreCase("-p")) {
							port = Integer.parseInt(inputStr[i + 2]);
							i += 2;
						} else
							result = -1;
					}
				} else if (inputStr[i].equalsIgnoreCase("-a")) // Auto
				{
					result = 2;
					if (i + 1 < inputStr.length) {
						if (!inputStr[i + 1].equalsIgnoreCase("-p")) {
							defaultServer = inputStr[i + 1];
							i++;
						} else {
							port = Integer.parseInt(inputStr[i + 2]);
							i += 2;
						}
					}
				} else if (inputStr[i].equalsIgnoreCase("-p")) {
					port = Integer.parseInt(inputStr[i + 1]);
					i++;
					break;
				} else if (inputStr[i].equalsIgnoreCase("-help")) // help
				{
					result = 3;
					break;
				} else
					result = -1;
			}
		} catch (Exception e) {
			result = -1;
		}

		return result;
	}

	
	public static class Server implements Runnable {
		private int port;
		private static ServerSocket serverSocket = null;
		private static Socket server = null;

		public void setAddress(String hostAddr, int hostPort) {
			port = hostPort;
		}

		public void runServer() {
			Thread worker = null;
			BufferedReader inB = null;

			try {
				serverSocket = new ServerSocket(port);

			} catch (Exception e) {
				System.out
						.println("Server unable to listen on port: " + Integer.toString(port) + "\n" + e.getMessage());
				return;
			}
			try {
				System.out.println("Waiting for client on port " + serverSocket.getLocalPort() + "...");
				server = serverSocket.accept();
				System.out.println("Successfully connected to " + server.getRemoteSocketAddress());

				worker = new Thread(new Server());
				InputStream inFromClient = server.getInputStream();
				worker.start();
				inB = new BufferedReader(new InputStreamReader(inFromClient));
				while (!Thread.currentThread().isInterrupted()) {
					if (inB.ready()) {
						System.out.println("[Remote] " + inB.readLine());
					}
				}

				try {
					worker.interrupt();
					worker.join();

					serverSocket.close();
					server.close();
				} catch (Exception e2) {
					System.out.println(e2.getMessage());
				}

			} catch (SocketTimeoutException s) {
				System.out.println("Socket timed out!");

			} catch (IOException e) {
				System.out.println(e.getMessage());

				try {
					worker.interrupt();
					worker.join();

					serverSocket.close();
					server.close();
				} catch (Exception e2) {
					System.out.println(e2.getMessage());
				}

			}

		}

		public void run() {
			String inputBuffer;
			PrintWriter out = null;
			BufferedReader keyin = null;
			try {

				out = new PrintWriter(server.getOutputStream(), true);
				keyin = new BufferedReader(new InputStreamReader(System.in));
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
			while (!Thread.currentThread().isInterrupted()) {

				try {

					inputBuffer = keyin.readLine();
					if (!inputBuffer.equalsIgnoreCase("status"))
						out.println(inputBuffer);
					else
						System.out.println("Local  address:port = " + server.getLocalAddress().toString() + ":"
								+ Integer.toString(server.getLocalPort()) + "\nRemote address:port = "
								+ server.getRemoteSocketAddress());
				} catch (Exception e) {
					try {

						serverSocket.close();
						server.close();
						Thread.currentThread().interrupt();
						System.exit(0);

					} catch (Exception e2) {
						System.out.println(e2.getMessage());

					}
					System.exit(0);

				}
			}

		}
	}

	public static class Client implements Runnable {
		private String serverName;
		private int port;
		private static Socket client = null;

		public void setAddress(String hostAddr, int hostPort) {
			serverName = hostAddr;
			port = hostPort;
		}

		public void runClient() throws Exception {
			BufferedReader inB = null;
			Thread worker = null;

			try {
				client = new Socket(serverName, port);
			} catch (Exception e) {
				throw e;
			}
			System.out.println("Successfully connected to server: " + client.getRemoteSocketAddress());

			try {
				InputStream inFromServer = client.getInputStream();

				inB = new BufferedReader(new InputStreamReader(inFromServer));
				worker = new Thread(new Client());
				worker.start();
				while (!Thread.currentThread().isInterrupted()) {
					if (inB.ready()) {
						System.out.println("[remote] " + inB.readLine());
					}
				}
				try {
					client.close();
					worker.interrupt();
					worker.join();
				} catch (Exception e) {
					System.out.println(e.getMessage());
				}

			} catch (IOException e) {

				System.out.println(e.getMessage());
				client.close();
			} catch (Exception e) {
				System.out.println(e.getMessage());
				client.close();

			}

		}

		public void run() {
			String inputBuffer;
			PrintWriter out = null;
			BufferedReader keyin = null;

			try {
				out = new PrintWriter(client.getOutputStream(), true);
				keyin = new BufferedReader(new InputStreamReader(System.in));
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
			while (!Thread.currentThread().isInterrupted()) {

				try {

					inputBuffer = keyin.readLine();
					if (!inputBuffer.equalsIgnoreCase("status"))
						out.println(inputBuffer);
					else
						System.out.println("Local  address:port = " + client.getLocalAddress().toString() + ":"
								+ Integer.toString(client.getLocalPort()) + "\nRemote address:port = "
								+ client.getRemoteSocketAddress());

				} catch (Exception e) {

					try {

						client.close();
						Thread.currentThread().interrupt();

					} catch (Exception e2) {
						System.out.println(e2.getMessage());
					}
					return;
				}
			}

		}
	}

	public static void printOptions() {
		System.out.println("\n----------------------TALK PROGRAM------------------\n");
		System.out.println(":: Programmed By: Sara Mohammadi Achajelooei  \n\n");
		System.out.println("-h [host name | IP Address] [-p Port Number] -- Run in client mode. \n");
		System.out.println("-s [-p Port Number] -- Run in server mode. \n");
		System.out.println("-a [host name | IP Address] [-p Port Number] -- Run in auto mode.\n");
		System.out.println("-help -- Helps with instructions.\n");
		System.out.println("----------------------------------------------------\n");
	}
} // end of main

