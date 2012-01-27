package Servidor;

//Importacion de rusta.
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Scanner;

class servidor {
	   
	  //CONFIGURACIONES
	  //Configuracion de servidor
	  static int puerto = 1398; //Puerto del servidor
	  //Configuracion del SQL.
	  static int prt_SQL = 3306;
	  static String usr_SQL = "llera_pablo";
	  static String pwd_SQL = "XNQTnnewBl11";

	  
	  //INICIALIZACIONES.
	  //Inicializa cosillas
	  static Thread proc;
	  static ServerSocket srvr;
	  static Socket skt;
	  public static ArrayList<user> ts;
	  public static proceso px;
	  public static long time;
	  
	
	public static void main(String args[]) {    
		  ts = new ArrayList<user>();
		  
	  
      try {
    	 //Esperar conexion
    	 srvr = new ServerSocket(puerto);
    	 System.out.println("Esperando conexiones en el puerto "+puerto);
    	 
    	 while(true){
    		 //Enlaza conexiones
	         skt = srvr.accept();
	         
	         proc = new Thread(new proceso(skt));

	         proc.start();
    	 }
      }
      
      catch(Exception e) {
         System.out.println("Error en el servidor");
      }
   }
}


class proceso extends servidor implements Runnable {
	
	private Socket s;
	private PrintWriter outd;
	private Scanner in;
	
	private int ind;
	private String msj;
	private String ip;
	private String name;
	
	
	public proceso(Socket s){
		this.s = s;
	}
	
        @Override
	public void run(){
		
		try{
	        //Genera los canales.
	        in = new Scanner(s.getInputStream()); // In
	        outd = new PrintWriter(s.getOutputStream(), true); // Out
	        
	        //Anota la ip.
	        ip = s.getInetAddress().toString();
	        
	        
	        //Da el mensaje de bienvenida.
	        outd.println("Por favor introduce tu nombre de usuario");
	        
	        
	        //Da el nombre introducido a la sesion y la registra.
	        if((name = in.nextLine()).equalsIgnoreCase("/quit"))
	        	cierreConexion(false, outd, in, s);
	        else {	       
		        //Añade al array el usuario con el indice del anterior + 1.
		        if(ts.isEmpty())
		        	ind = -1;
		        else
		        	ind = ts.get(ts.size()-1).ind;
		        
		        ind++;
		        ts.add(ind, new user(ind, this, name, ip));
		        
		        //Limpia la consola del chat entrante.
		        envMsjCUsr(this, "#clear");
                        //Envia al nuevo usuario los users actualmente online.
                        sendOnlineUsers(this, name);
		        //Informa en consola del servdior y a los usuarios online.
		        System.out.println("Establecida "+(ip)+", nombre: "+name);
		        envMsj("");
		        envMsj(" *** "+name+" se ha conectado ***");
                        envMsj("#lon "+name);
		        envMsj("");
		        
		        
		        //Recibe los mensajes.	        
		        while(!(msj = in.nextLine()).equalsIgnoreCase("/quit")){
			        envMsjUsr(this, name, msj);
		        }
		        
		        //Se cierra la conexion por escribir 'quit'
		        cierreConexion(true, outd, in, s);
			}
		}
		catch(Exception e){
			System.out.println("Error con el usuario "+ip+", nombre "+name);
                        e.printStackTrace();
		}
	}
	
	//Cierre de conexion.
	public void cierreConexion(boolean dentro, PrintWriter outd, Scanner in, Socket s) throws Exception{
		
		if(dentro){
	        //Anuncia el cierre de la conexion.
	        System.out.println("Cerrando conexion con "+ip+", nombre "+name);
	        envMsj("");
	        envMsj(" *** "+name+" abandona el chat ***");
                envMsj("#lof "+name);
	        envMsj("");
	        
	    //Cierra los canales y el socket
	        ts.remove(ind);
		}
        outd.close();
        in.close();
        s.close();
	}
	
	
	//Funcion de recivir mensaje.
	public void recMsj(String mj){
		outd.println(mj);
	}
	
	//Funcion de enviar mensaje de usuario.
	public void envMsjUsr(proceso p, String usr, String mj){
		if(mj.charAt(0) == '/') comprobarComando(p, mj);
		else {
			for(user u : ts){
				u.p.recMsj("("+getDateTime()+")  -"+usr+": "+mj);
			}
		}
	}
	
	public void comprobarComando(proceso p, String mj){
		if(mj.equalsIgnoreCase("/who")){
			// who, lista todos los usuarios.
			envMsjCUsr(p, "");
			envMsjCUsr(p, " > Usuarios conectados:");
			for(int i = 0; i < ts.size(); i++){
				if(i != ts.size()-1)
					envMsjCUsr(p, "  |  "+ts.get(i).p.name);
				else
					envMsjCUsr(p, "  L "+ts.get(i).p.name+"");
			}
			envMsjCUsr(p, "");
		} else {
			// comando invalido.
			envMsjCUsr(p, "Comando erroneo");
			envMsjCUsr(p, "");
		}
	}
	
	// Funcino enviar Mensaje a user concreto.
	public void envMsjCUsr(proceso p, String mj){
		p.recMsj(mj);
	}
	
	//Funcion de enviar mensaje.
	public void envMsj(String mj){
		for(user u : ts){
			u.p.recMsj(mj);
		}
	}
	
    public String getDateTime() {
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        return dateFormat.format(date);
    }
    
    public void sendOnlineUsers(proceso p, String name){
        for(user u : ts)
        if(!u.usr.equals(name))
                envMsjCUsr(p, "#lon "+u.usr);
    }
}


//Tipo user.
class user{
	
	int ind;
	proceso p;
	String usr;
	String ip;
	
	user(int ind, proceso p, String usr, String ip){
		this.ind = ind;
		this.p = p;
		this.usr = usr;
		this.ip = ip;
	}
}