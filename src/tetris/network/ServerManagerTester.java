package tetris.network;

import java.rmi.RemoteException;
import java.util.Scanner;

public class ServerManagerTester {

	public static void main(String args[]) {
		if (args.length < 2) {
			System.out.println("USAGE: java ServerManagerTester hostname port\n\thostname - name of current machine from a machine on an external network\n\tport - port number to use for internal server to be connected to by other servers.");
		}
		
		Connection self = new Connection(args[0], Integer.parseInt(args[1]));
		
		ServerManager manager = null;
		try {
			manager = new ServerManager(self);
		} catch (RemoteException e) {
			System.err.println("Failed to setup local server. Shutting down...");
			e.printStackTrace();
			System.exit(-1);
		}
		
		
		Scanner in = new Scanner(System.in);
		String command = "";
		
		printOptions();
		while (!command.startsWith("q")) {
			printReceived(manager);
			
			System.out.print("> ");
			command = in.nextLine();
			
			if (command.length() > 0) {
				switch (command.charAt(0)) {
				case 'q':
					System.out.println("Quiting chat...");
					manager.close();
					break;
				case 'c':
					handleConnect(manager, command);
					break;
				case 's':
					handleSend(manager, command);
					break;
				case 'h':
					printOptions();
					break;
				}
			}
		}
	}
	
	public static void handleConnect(ServerManager manager, String command) {
		String[] whitespaceSplit = command.split("\\s+");
		
		if (whitespaceSplit.length < 3) {
			System.err.println("Not enough arguments for connection command.");
			return;
		}
		else {
			Connection connection = new Connection(whitespaceSplit[1], Integer.parseInt(whitespaceSplit[2]));
			
			try {
				manager.connect(connection);
			} catch (RemoteException e) {
				System.err.println("Failed to connect to server \"" + connection.getHost() + "\" on port " + connection.getPort());
				e.printStackTrace();
				return;
			}
		}
	}
	
	public static void handleSend(ServerManager manager, String command) {
		if (command.indexOf('"') == -1 || command.indexOf('"') == command.lastIndexOf('"')) {
			System.err.println("Send failed: could not find correct format for quotation marks.");
			return;
		}
		
		String messageString = command.substring(command.indexOf('"') + 1, command.lastIndexOf('"'));
		manager.send(messageString);
		
		System.out.println("|" + manager.getConnection().getHost() + ":" + manager.getConnection().getPort() + "| \t" + messageString);
	}
	
	public static void printReceived(ServerManager manager) {
		Message message;
		while ((message = manager.receive()) != null) {
			if (message.getContent() instanceof String) {
				System.out.println("|" + message.getSource().getHost() + ":" + message.getSource().getPort() + "| \t" + ((String) message.getContent()));
			}
		}
	}
	
	public static void printOptions() {
		System.out.println("Commands:\n\tq - quit\n\t(c)onnect hostname port - connect to the specified hostname on port\n\t(s)end \"messge\" - send message within the quotation marks\n\t(h)elp - print this message again");
	}
}
