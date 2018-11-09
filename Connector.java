import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Connector implements Runnable
{
	static ArrayList<ClientHandler> activeClients;	//list of active client handlers
	static ArrayList<User> clients;	//user list
	static ArrayList<Topic> topics;	//topics list
	static File userFile;		// user list save file
	static File topicFile;		//topics list save file

	public Connector()
	{
		// instantiate variables 
		activeClients = new ArrayList<>();
		clients = new ArrayList<>();
		userFile = new File("logs.txt");
		topicFile = new File("topics.txt");
		topics = new ArrayList<>();
	}

	/**
	 * Active clients getter
	 * @return active client handler list
	 */
	public ArrayList<ClientHandler> GetActiveClients()
	{
		return activeClients;
	}

	/**
	 * client list getter
	 * @return client list
	 */
	public ArrayList<User> GetClients()
	{
		return clients;
	}

	/**
	 * topics list getter
	 * @return topics arraylist
	 */
	public ArrayList<Topic> GetTopics()
	{
		return topics;
	}

	@Override
	/**
	 * main function 
	 */
	public void run() 
	{
		Initialize();	//read users and topics lists
		try 
		{
			ManageClients();	//manage client requests
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}

	/**
	 * Imports users and topics lists from files
	 */
	public static void Initialize()
	{

		int maxTopicId = -1;

		if(userFile.exists() && !userFile.isDirectory())	// checks if the file actually exists 
		{ 
			try
			{	//open stream
				InputStream is = new FileInputStream(userFile.getName());
				InputStream buffer = new BufferedInputStream(is);
				ObjectInput input = new ObjectInputStream (buffer);
				try
				{
					do
						clients.add((User)(input.readObject()));	//read all users from file
					while(true);
				}
				catch(EOFException e)
				{
				}
				input.close();
				buffer.close();
				is.close();

			}
			catch(ClassNotFoundException Ce)
			{
				Ce.printStackTrace();
			}
			catch(IOException IOe)
			{
				IOe.printStackTrace();
			}
			
		} 
		else 
			System.out.println("Le fichier de sauvegarde des users n'existe pas.");

		//same process for topics importation
		if(topicFile.exists() && !topicFile.isDirectory()) 
		{ 
			try
			{
				InputStream tis = new FileInputStream(topicFile.getName());
				InputStream tbuffer = new BufferedInputStream(tis);
				ObjectInput tinput = new ObjectInputStream (tbuffer);
				try
				{
					do
					{
						topics.add((Topic)(tinput.readObject()));
						if(topics.get(topics.size()-1).GetId() > maxTopicId)
							maxTopicId = topics.get(topics.size()-1).GetId();
					}
					while(true);
				}
				catch(EOFException e)
				{
				}
				tinput.close();
				tbuffer.close();
				tis.close();

			}
			catch(ClassNotFoundException Ce)
			{
				Ce.printStackTrace();
			}
			catch(IOException IOe)
			{
				IOe.printStackTrace();
			}
			Topic.SedIdCounter(maxTopicId);
		} 
		else 
			System.out.println("Le fichier de sauvegarde des topics n'existe pas.");
	}
	/**
	 * saves users and topics lists to file
	 * @throws IOException
	 */
	public static void SaveData() throws IOException 
	{
		try 
		{
			// open stream
			FileOutputStream fileOut = new FileOutputStream(userFile.getName());
			ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);

			FileOutputStream tfileOut = new FileOutputStream(topicFile.getName());
			ObjectOutputStream tobjectOut = new ObjectOutputStream(tfileOut);
			
			//add every users
			for(int i = 0; i<clients.size(); i++)
				objectOut.writeObject(clients.get(i));
			
			// add every topics
			for(int i = 0; i<topics.size(); i++)
				tobjectOut.writeObject(topics.get(i));

			objectOut.close();
			tobjectOut.close();
		} 
		catch (IOException ioex) 
		{
			ioex.printStackTrace();
		}
	}

	/**
	 * Handles client connections and assigns it a separate thread
	 * @throws IOException
	 */
	public static void ManageClients() throws IOException
	{
		ServerSocket ss = new ServerSocket(1234);        
		Socket s; 
		int i = 0;

		while (true)  
		{ 
			System.out.println("connector GO");
			s = ss.accept(); 	//accept connection

			// get IO streams
			DataInputStream dis = new DataInputStream(s.getInputStream()); 
			DataOutputStream dos = new DataOutputStream(s.getOutputStream()); 
			ClientHandler mtch = new ClientHandler(s, dis, dos);  // instantiate handler
			Thread t = new Thread(mtch);	//assign client handler to thread
			System.out.println("Adding this client" +i +" to active client list"); 
			activeClients.add(mtch);	//add client to active clients list
			t.start(); //launch thread
			i++; //counts the number of connections

		} 
	}
}
