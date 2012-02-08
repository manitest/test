package Servidor;

import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Scanner;

import Cliente.UserUsr;

class Proceso extends servidor implements Runnable {
	
	//Declaraciones
	private Socket s;
	private ObjectOutputStream outOb;
	private Scanner in;
	
	private UserUsr usrUsr;
	private String ip;
	private String name;
	private String timeOn;
	
	
	//Constructor.
	public Proceso(Socket s){
		this.s = s;
	}
	
	@Override
	public void run(){
		String msj;
		
		try{
			//Genera los canales.
			in = new Scanner(s.getInputStream()); // In
			outOb = new ObjectOutputStream(s.getOutputStream()); //Out
		} catch(Exception e) { System.out.println("Error inesperado generando los canales.");}

		//Limpia la consola del chat entrante.
		envObjAUsrConcreto(this, "#clear");
		
		//Anota la hora de entrada.
		timeOn = getDateTime();
		//Anota la ip.
		ip = s.getInetAddress().toString();
		//Anota el nombre.
		if(!isNameAvailable(name = in.nextLine())){
			//Si esta en uso no permite la conexion.
			envObjAUsrConcreto(this, "#nameEnUso");
			
			//Informa por consola.
			System.out.println("Rehusado el user "+ip+" por nombre en uso '"+name+"'");
			//Cierra la conexion.
			cierreConexion(false);
		} else {
			
			//Envia al nuevo usuario los users actualmente online.
			sendOnlineUsers();
			
			//Añade al array el usuario.
			usrUsr = new UserUsr(name, ip, timeOn);
			ts.add(this);
			
			//Informa en consola del servdior y a los usuarios online.
			System.out.println("Entra "+(ip)+", nombre: "+name);
			enviarObjATodos(usrUsr);
			
			try{	
				//Recibe los mensajes que mande el usuario.			
				while(!(msj = in.nextLine()).equalsIgnoreCase("/quit"))
					procesarMsjUsr(this, name, msj);
				
			} catch(Exception e){
				System.out.println("Error con el usuario "+ip+", nombre "+name);
			}
		
			//Se cierra la conexion por escribir 'quit' o por un error.
			cierreConexion(true);
		}
	}
	
	//Cierre de conexion.
	public void cierreConexion(boolean estabaDentro){
		
		//Elimina al usuario del array.
		ts.remove(this);
		
		//Cierra las cosillas.
		try{
			outOb.close();
			in.close();
			s.close(); 
		} catch(Exception e) {}
		
		//Anuncia el cierre de la conexion si etaba dentro.
		if(estabaDentro) {
			System.out.println("Cerrando conexion con "+ip+", nombre "+name);
			enviarObjATodos(usrUsr);
		}
	}
	
	
	//Funcion de recivir objeto.
	public void recibirObj(Object o){
		try{ outOb.writeObject(o); } catch(Exception e) {}
	}	
	
	//Funcion de enviar objeto a todos.
	public void enviarObjATodos(Object o){
		for(Proceso p : ts){
			envObjAUsrConcreto(p, o);
		}
	}
	
	// Funcion enviar objeto a user concreto.
	public void envObjAUsrConcreto(Proceso p, Object o){
		p.recibirObj(o);
	}
	
	//Funcion de enviar mensaje de usuario.
	public void procesarMsjUsr(Proceso p, String usr, String mj){
		//Si empieza con '/' es un comando.
		if(mj.charAt(0) == '/')
			comprobarComando(p, mj);
		else
		//Si no, lo mando a los usuarios con el formato.
			enviarObjATodos("("+getDateTime()+")  -<b>"+usr+"</b>: "+mj);
	}
	
	//Comprueba y procesa el comando.
	public void comprobarComando(Proceso p, String mj){
		if(mj.equalsIgnoreCase("/who")){
			// who, lista todos los usuarios.
			envObjAUsrConcreto(p, "");
			envObjAUsrConcreto(p, " > Usuarios conectados:");
			for(int i = 0; i < ts.size(); i++){
				if(i != ts.size()-1)
					envObjAUsrConcreto(p, "  |  "+ts.get(i).name);
				else
					envObjAUsrConcreto(p, "  L "+ts.get(i).name+"");
			}
			envObjAUsrConcreto(p, "");
		} else if(mj.substring(0,10).equalsIgnoreCase("/changename")){
			// Changename, cambia el nombre.
			//TODO.
		} else {
			// comando invalido.
			envObjAUsrConcreto(p, "");
			envObjAUsrConcreto(p, "Comando erroneo");
			envObjAUsrConcreto(p, "");
		}
	}
	
	//Envia los usuarios online al cliente, excepto el mismo.
	public void sendOnlineUsers(){
		for(Proceso p : ts)
			this.recibirObj(p.usrUsr);
	}
	
	//Comprueba si el nombre esta disponible.
	public boolean isNameAvailable(String name){
		boolean result = true;
		for(Proceso p : ts)
			if(p.name.equals(name))
				result = false;
		return result;				
	}
	
	//Obtiene la hora actual.
	public String getDateTime() {
		DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
		Date date = new Date(System.currentTimeMillis());
		
		return dateFormat.format(date);
	}
	
}