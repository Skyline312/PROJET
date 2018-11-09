import java.io.*; 
import java.net.*; 
import java.util.Scanner; 

public class Client  
{ 
	final static int ServerPort = 1234; 

	/**
	 * Sends message via output stream
	 * @param sn input scanner
	 * @param dos	data output stream
	 * @param s	user socket
	 */
	public static synchronized void SendMessage(Scanner sn, DataOutputStream dos, Socket s)
	{
		Thread sendMessage = new Thread(new Runnable()  
		{ 
			@Override
			public void run() { 
				boolean running = true;
				String msg = null;
				while (running) 
				{ 
					// read the message to deliver
					msg = sn.nextLine(); 
					if(msg.equals("logout"))
					{
						try 
						{
							dos.writeUTF(msg); 
							dos.close();
						} 
						catch (IOException e) 
						{
							e.printStackTrace();
						}
					}
					else
					{
						try 
						{ 
							// write on the output stream 
							dos.writeUTF(msg); 
						} 
						catch (IOException e) 
						{ 
							e.printStackTrace(); 
						} 
					}
				} 
			} 
		}); 
		sendMessage.start(); 
	}

	/**
	 * Receives message via input stream
	 * @param sn input scanner
	 * @param dis	data input stream
	 * @param s	user socket
	 */
	public static synchronized void ReadMessage(Scanner sn, DataInputStream dis, Socket s)
	{
		Thread readMessage = new Thread(new Runnable()  
		{ 
			@Override
			public void run() 
			{ 

				while (true) 
				{ 
					try 
					{ 
						// read the message sent to this client 
						String msg = dis.readUTF(); 
						System.out.println(msg);
					} 
					catch (IOException e) 
					{ 
						try {
							dis.close();
						} catch (IOException e1) {

							e1.printStackTrace();
						}
					} 
				} 
			} 
		}); 
		readMessage.start();
	}

	public static void main(String args[]) throws UnknownHostException, IOException  
	{ 
		Scanner scn = new Scanner(System.in); //instantiate scanner
		InetAddress ip = InetAddress.getByName("localhost"); 
		Socket s = new Socket(ip, ServerPort); 	//open socket

		//open streams
		DataInputStream dis = new DataInputStream(s.getInputStream()); 
		DataOutputStream dos = new DataOutputStream(s.getOutputStream()); 

		// Initialize IO
		SendMessage(scn, dos, s);
		ReadMessage(scn, dis, s);
	} 
} 