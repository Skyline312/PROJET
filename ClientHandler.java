import java.io.*; 
import java.util.*;
import java.net.*;
import java.text.SimpleDateFormat; 

class ClientHandler implements Runnable  
{ 
	Scanner scn = new Scanner(System.in);	//scanner for user input 
	User currentUser;	//user attached to client handler
	final DataInputStream dis; //input stream
	final DataOutputStream dos; //output stream
	Socket s;

	// constructor 
	public ClientHandler(Socket s, DataInputStream dis, DataOutputStream dos) 
	{ 
		this.dis = dis; 
		this.dos = dos; 
		this.s = s; 
	} 

	/**
	 * Authentication function
	 * @return boolean indicating if user properly logged in
	 * @throws IOException
	 */
	public boolean Connect() throws IOException
	{
		boolean logd = false;
		String received = null;

		this.dos.writeUTF("Welcome\n1 : login\n2 : sign up");
		
		// loop main menu until valid input
		do
		{
			if(received != "0" && received != "1" && received != "2" && received != null)
				this.dos.writeUTF("Wrong command");
			
			received = this.dis.readUTF();

			// prevents entering login mode if user database empty
			if(received.contentEquals("1") && Connector.clients.size() == 0)
			{
				received = "-1";
				this.dos.writeUTF("No user in database");
			}
		}
		while(!received.contentEquals("0") && !received.contentEquals("1") && !received.contentEquals("2"));
		
		if(received.contentEquals("1"))
			logd = Login();
		else if(received.contentEquals("2"))
			logd = SignUp();

		return logd;	
	}
	
	/**
	 * topic management func
	 * @throws IOException
	 */
	public void ManageTopics() throws IOException
	{
		String received = null;
		
		this.dos.writeUTF("1 : Create new topic\n2 : Join topic");
		do
		{
			if(received != "1" && received != "2" && received != null)
				this.dos.writeUTF("Wrong command");
			received = this.dis.readUTF();

			// prevent user from entering topic selection mode if topic list empty
			if(received.contentEquals("2") && Connector.topics.size() == 0)
			{
				received = "-1";
				this.dos.writeUTF("No topic in database");
			}
			else if(received.contentEquals("2") && Connector.topics.size() != 0)
			{
				TopicSelection();
			}
			else
				CreateTopic();
		}
		while(!received.contentEquals("1") && !received.contentEquals("2"));
	}
	
	/**
	 * creates new topic and assigns the current user to it
	 * @throws IOException
	 */
	public void CreateTopic() throws IOException 
	{	
		Topic newTopic = null;
		String name = null;
		String received = null;
		boolean confirmed = false;
		boolean nameDuplicate = false;
		
		while(!confirmed)	//loop until topic name confirmed by user
		{
			this.dos.writeUTF("Topic name ?");
			name = this.dis.readUTF();
			nameDuplicate = false;
			
			//checks that topic doesn't already exist
			for(int i = 0 ; i < Connector.topics.size(); i++)
			{
				if(Connector.topics.get(i).GetName().equals(name))
					nameDuplicate = true;
			}
			if(!nameDuplicate)
			{
				// name choice confirmation
				this.dos.writeUTF("Confirm " + name + " ? [y/n]");
				do
				{
					received = this.dis.readUTF();
					received = received.toLowerCase();
					if(received.equals("y"))
						confirmed = true;
				}
				while(!received.equals("y") && !received.equals("n"));
				
				//if confirmed, create topic and attach current user to it
				if(confirmed)
				{
					newTopic = new Topic(name);
					Connector.topics.add(newTopic);
					currentUser.SetTopic(newTopic.GetId());
				}
			}
			else
				this.dos.writeUTF("Topic name already exists");
		}

		Connector.SaveData();	//backup users and topics lists
	}

	/**
	 * topic selection func
	 * @throws IOException
	 */
	public void TopicSelection() throws IOException
	{
		String received;
		int choice = 0;
		
		ViewTopic();	//display topics
		do
		{
			received = this.dis.readUTF();	//read client choice
			choice = (int)received.toCharArray()[0] - 48;	//convert char number to int number
			if(choice >= 0 && choice < Connector.topics.size())	//check if valid input
				currentUser.SetTopic(Connector.topics.get(choice).GetId()); //set active topic to user
		}
		while(choice < 0 || choice >= Connector.topics.size());
		
		Connector.SaveData();	//backup data
		
		// load logs
		for(int i = 0 ; i< Connector.topics.get(choice).GetLogs().size(); i++)
			this.dos.writeUTF(Connector.topics.get(choice).GetLogs().get(i));
	}
	
	/**
	 * displays topics list
	 * @throws IOException
	 */
	public void ViewTopic() throws IOException
	{
		if(Connector.topics.size() == 0)
			this.dos.writeUTF("No topic in database");
		else
			for(int i = 0 ; i < Connector.topics.size() ; i++)
				this.dos.writeUTF(i + " : " + Connector.topics.get(i).GetName());
		this.dos.writeUTF("----------");
	}

	/**
	 * login function
	 * @return true if user logged in, false if user failed authentication
	 * @throws IOException
	 */
	public boolean Login() throws IOException
	{
		boolean logedin = false;
		boolean userOK = false;
		boolean passworkOK = false;
		int count = 0;
		int i = 0;
		String received;
		this.dos.writeUTF("Username ?");
		
		do //loop until valid choice (user in database)
		{
			i = 0;
			received = this.dis.readUTF();
			while(i< Connector.clients.size() && !userOK)
			{
				if(Connector.clients.get(i).GetName().equals(received))
					userOK = true;
				else
					i++;
			}
			if(!userOK)
				this.dos.writeUTF("Username not found");

		}
		while(!userOK);

		this.dos.writeUTF("Password ?");
		
		do	//loop until valid password or failed 3times
		{
			received = this.dis.readUTF();
			if(Connector.clients.get(i).GetPassword().equals(received))
			{
				passworkOK = true;
				logedin = true;
				currentUser = Connector.clients.get(i);
			}
			else
			{
				count++;
				this.dos.writeUTF("Wrong Password. Trial left : " + (3 - count));
			}
		}
		while(!passworkOK && count < 3);
		if(!passworkOK)
			this.dos.writeUTF("Login failed");

		return logedin;
	}

	/**
	 * signup func
	 * @return true if user signed up correctly, false if not
	 * @throws IOException
	 */
	public boolean SignUp() throws IOException
	{
		User newUser;
		String received;
		String username;
		boolean logedin = false;
		boolean ok = true;
		
		do	// loops until valid choice (no name duplicate)
		{
			ok = true;
			if(!ok)
				this.dos.writeUTF("Error, user already exists");

			this.dos.writeUTF("Username ?");
			received = this.dis.readUTF();
			for (User U : Connector.clients)  
			{ 
				if(U.GetName().equals(received))
					ok = false;
			} 
		}
		while(!ok);
		
		username = received;
		this.dos.writeUTF("Password?");
		do
			received = this.dis.readUTF();
		while(received == null || received == "");
		
		newUser = new User(username, received);	//instantiate new user
		Connector.clients.add(newUser);	//add to clients list
		Connector.SaveData();	//backup data
		currentUser = newUser;	//attach new user to client handler
		logedin = true;
		this.dos.writeUTF("You've been added to userlist");

		return logedin;
	}

	@Override
	/**
	 * main func
	 */
	public void run() 
	{ 
		SimpleDateFormat sdf = new SimpleDateFormat ("E yyyy.MM.dd 'at' hh:mm:ss");	//date  format for message timestamp
		String message;	//formated message
		String received; // user input
		boolean connected = false;	//active connection
		
		try 
		{
			do {}
			while(!Connect());
			
			//select or create topic when user logged in
			ManageTopics();
			
			// broadcast to topic's users that a new user connected
			for (ClientHandler mc : Connector.activeClients)  
			{ 
				if(mc.currentUser.GetTopic() == this.currentUser.GetTopic())
					mc.dos.writeUTF(sdf.format(new Date()) + " : "+ currentUser.GetName() + "[LOGGED IN]"); 
			}
		}
		catch (IOException e1) 
		{
			e1.printStackTrace();
		}
		connected = true;

		while (connected)  
		{ 
			try
			{ 
				// receive the string 
				received = dis.readUTF(); 

				//if special command logout
				if(received.equals("#logout"))
				{ 
					connected=false; //disconnect
					Connector.activeClients.remove(this);	//remove from active clients list
					this.dis.close();	//close streams
					this.dos.close();
					System.out.println(this.currentUser.GetName() + " left the server");
					
					// notify that user left
					for (ClientHandler mc : Connector.activeClients)  
					{ 
						if(mc.currentUser.GetTopic() == this.currentUser.GetTopic())
							mc.dos.writeUTF(sdf.format(new Date()) + " : "+ currentUser.GetName() + "[LOGGED OUT]");
					}
					
					Connector.SaveData();	//backup data
					break; 
				}

				// broadcast message
				for (ClientHandler mc : Connector.activeClients)  
				{ 
					if(mc.currentUser.GetTopic() == this.currentUser.GetTopic())
					{
						message = sdf.format(new Date()) + " : "+ currentUser.GetName() + " : " + received;
						mc.dos.writeUTF(message);
						Connector.topics.get(this.currentUser.GetTopic()-1).GetLogs().add(message);
					}
				} 
				Connector.SaveData();	// backup data
			} 
			catch (IOException e) 
			{ 
				System.out.println("Connection terminated by " + this.currentUser.GetName());
				connected = false;
			}

		} 
		try
		{ 
			Connector.SaveData();
			// closing resources 
			this.dis.close(); 
			this.dos.close(); 

		}
		catch(IOException e)
		{ 
			e.printStackTrace(); 
		} 
	} 
}