import java.io.Serializable;

@SuppressWarnings("serial")
public class User implements Serializable
{
	private String username;
	private String password;
	private int topic;
	
	public User(String _name)
	{
		this.username = _name;
	}
	
	public User(String _name, String pwd)
	{
		this.username = _name;
		this.password = pwd;
		this.topic = 0;
	}
	
	public User(String _name, String pwd, int _tpc)
	{
		this.username = _name;
		this.password = pwd;
		this.topic = _tpc;
	}
	
	public String GetName()
	{
		return this.username;
	}
	
	public String GetPassword()
	{
		return this.password;
	}
	
	public int GetTopic()
	{
		return this.topic;
	}
	
	public void SetTopic(int value)
	{
		this.topic = value;
	}
	
	public void SetPassword(String newpwd)
	{
		if(newpwd != "" && newpwd != this.password)
			this.password = newpwd;
		else
			System.out.println("Error, new password empty or equal to actual password");
			
	}
	
	@Override
	public String toString() 
	{
		return (this.username + "//" + this.password + "//" + this.topic + "--");  
	}
}
