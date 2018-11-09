import java.io.*;
  
public class Server  
{ 
	
	public static void main(String[] args) throws IOException, InterruptedException  
	{
		System.out.println("Server GO");
		Connector ClientConnector = new Connector();	//Instantiate the connector
		ClientConnector.run();	
	}
} 