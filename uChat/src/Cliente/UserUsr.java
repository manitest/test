package Cliente;

//Tipo user para usuarios.
@SuppressWarnings("serial")
public class UserUsr implements java.io.Serializable{
	String name;
	String ip;
	String timeOn;
	
	public UserUsr(String name, String ip, String timeOn){
		this.name = name;
		this.ip = ip;
		this.timeOn = timeOn;
	}
	
	public interface Serializable{
	}
}
