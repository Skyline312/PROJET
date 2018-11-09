import java.util.ArrayList;
import java.io.Serializable;

@SuppressWarnings("serial")
public class Topic implements Serializable
{
	static int topicCount = 0;
	private int id;
	private String name;
	private ArrayList<String> logs;
	
	public Topic(String _name)
	{
		topicCount++;
		this.name = _name;
		this.logs = new ArrayList<>();
		this.id = topicCount;
	}
	
	public String GetName()
	{
		return this.name;
	}
	
	public ArrayList<String> GetLogs()
	{
		return this.logs;
	}
	
	public int GetId()
	{
		return this.id;
	}
	
	public static void SedIdCounter(int value)
	{
		topicCount = value;
	}
	
	@Override
	public String toString() 
	{
		String logstring = "";
		for(int i = 0 ; i<logs.size();i++)
		{
			logstring += logs.get(i);
		}
		
		return this.name + "//" + this.id + "//" + logstring + "--";
	}
}
