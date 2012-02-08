package Cliente;

import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

import javax.swing.JLabel;

//Funciones principales del chat, separadas.
class chat implements Runnable{
	
	//Declaraciones.
	static Socket s;
	//static Scanner in;
	static ObjectInputStream inOb;
	static PrintWriter outd;
	static int filas = 0;
	static String dirServ;
	static javax.swing.text.Element cRecElement = null;
	//  Variable de conexion.
	static boolean conectado = false;
	//	UserUsr conectados.
	static int indLocal = -1;
	static ArrayList<UserUsr> usersOnline = new ArrayList<UserUsr>();
	//  chat
	static final chat ch = new chat();

	@Override
	public void run() {
		while(true){
			
			dirServ = ventPrincipal.direccionText.getText();
			//Limpia el chat.
			clearChat();
			
			//Empieza la conexion del socket.
			try{
				// Realiza la conexion.
				s = new Socket(dirServ, ventPrincipal.puertoServ);
				conectado = true;

			} catch(Exception e){
				// Notifica el error de conexion al usuario.
				imprimirMsj("Error al conectar a '"+dirServ+":"+ventPrincipal.puertoServ+"'");
			}

			if(conectado){
				try{
					// Abre los canales
					inOb = new ObjectInputStream(s.getInputStream()); // In
					outd = new PrintWriter(s.getOutputStream(), true); // Out
				} catch(Exception e){}

				//Notifica la conexion.
				changeLabel(ventPrincipal.conexionActual,
						"Conectado a "+dirServ+":"+ventPrincipal.puertoServ);
				ventPrincipal.conexionButton.setText("Desconectar");
				ventPrincipal.cEnv.setEnabled(true);
				
				//Envia su username.
				if(ventPrincipal.usernameText.getText().equals(""))
					outd.println("Anonymous");
				else
					outd.println(ventPrincipal.usernameText.getText());


				//Imprime las lineas que envie el servidor.
				//Si conectado es false, cierra la conexion.
				while(conectado){
					//Va en try{} porque al desconenctar se cerrara in.
					try{ lecturaObjeto(inOb.readObject()); } catch(Exception e) {}
				}
				
				//Conexion actual cerrada.
				clearChat();
				ventPrincipal.conexionButton.setText("Conectar");
				imprimirMsj("Introduce la direccion del servidor y conecta."); 
			}
			
			//Pausa el thread actual.
			synchronized(ch){
				try { ch.wait(); } catch(Exception e) {}
			}
		}
	}
	
	static UserUsr userSearch(String name){
		UserUsr result = null;
		for(UserUsr u : usersOnline){
			if(u.name.equalsIgnoreCase(name))
				result = u;
		}
		return result;
	}
	
	static void lecturaObjeto(Object o){
		if(o instanceof String){
			//Si es un String.
			String msj = (String)o;
			if(!(msj.equals("")) && msj.charAt(0) == '#')
				//Si es un comando del servidor lo procesa.
				lecturaComandoSrv(msj);
			else
				//Si es un mensaje, lo imprime.
				imprimirMsj(msj);
		} else {
			//Si no, es un objeto de tipo UserUsr.
			UserUsr usr = (UserUsr)o;
			if(!usersOnline.contains(usr)){
				//Se conecta un usuario nuevo.
				try{
					usersOnline.add(usr);
					ventPrincipal.usersModel.addElement(usr.name);
				} catch(Exception e){e.printStackTrace();}
			} else {
				//Se desconecta un usuario.
				try{
					usersOnline.remove(usr);
					ventPrincipal.usersModel.removeElement(usr.name);
				} catch(Exception e) {e.printStackTrace();}
			}
		}
	}

	//Comprueba el comando recibido y lo ejecuta.
	static void lecturaComandoSrv(String msj){
		if(msj.equals("#clear")){
			//Limpia el chat.
			clearChat();
		} else if(msj.equals("#kick")){
			//Cierra la conexion.
			cerrarConexion();
			//Informa en el label que ha sido kickeado.
			changeLabel(ventPrincipal.conexionActual, "Has sido kickeado.");
		} else if(msj.equals("#nameEnUso")){
			//El nombre esta en uso.
			cerrarConexion();
			//Informa en el label que el nombre esta en uso.
			changeLabel(ventPrincipal.conexionActual,
					"El nombre "+ventPrincipal.usernameText.getText()+" ya esta en uso.");
		}
	}
	
	//Comprueba que comando se ha escrito y lo ejecuta.
	static void lecturaComandoUsr(String msj){
		if(msj.equalsIgnoreCase("/quit")){
			// Quit
			cliente.cerrarPrograma();
		} else if(msj.equalsIgnoreCase("/who")){
			// Who
			enviarMsj(msj);
		} else if(msj.equalsIgnoreCase("/clear")){
			// Clear
			clearChat();
		} else if(msj.length()>10 && msj.substring(0,10).equalsIgnoreCase("/changename")){
			// Changename
			enviarMsj(msj);
		} else if(msj.equalsIgnoreCase("/disconnect")){
			// Disconnect
			cerrarConexion();
		} else if(msj.equalsIgnoreCase("/comandos")){
			// Comandos
			imprimirMsj("");
			imprimirMsj(" > Comandos");
			imprimirMsj("  |  /who  (Muestra usuarios conectados)");
			imprimirMsj("  |  /clear  (Limpia la ventana de chat)");
			imprimirMsj("  |  /disconnect  (Sale del servidor)");
			imprimirMsj("  |  /quit  (Cierra este cliente)");
			imprimirMsj("  L /comandos  (Muestra esta lista)");
			imprimirMsj("");
		} else {
			// Comando erroneo
			imprimirMsj("");
			imprimirMsj(" > Comando erroneo, para ver los comandos escribe /comandos");
			imprimirMsj("");
		}
	}
	
	//Envia mensajes al servidor.
	static void enviarMsj(String msj){
		outd.println(msj);
	}
	
	//Imprime mensajes en el cuadro de texto cRec.
	//Es importante que se use esta funcino para mantener la cuenta de las filas.
	static void imprimirMsj(String msj){
		try{
			if(cRecElement == null) {
				cRecElement = ventPrincipal.hDoc.getRootElements()[0].getElement(0).getElement(0);
			}
			ventPrincipal.hDoc.insertBeforeEnd(cRecElement, 
					"<font face=\"verdana\" size=\"3\">"+msj+"</font><br>"
			);
		} catch(Exception ex) { ex.printStackTrace(); }
	}
	
	//Limpia cRec.
	static void clearChat(){
		ventPrincipal.cRec.setText("");
	}
	
	//Cambia el texto de label
	static void changeLabel(JLabel lbl, String txt){
		lbl.setText(txt);
	}
	
	//Inicia la conexion.
	static void inicioThread(){
		synchronized(ch){
			ch.notifyAll();
		}
	}
	
	//Cierra la conexion actual.
	static void cerrarConexion(){
		//Interrumpe las conexiones si estan establecidas.		
		if(chat.conectado) {
			//Borra los elementos de usuarios online.
			usersOnline.clear();
			ventPrincipal.usersModel.removeAllElements();
			//Pone conectado en false.
			conectado = false;
			//Desactiva cEnv.
			ventPrincipal.cEnv.setEnabled(false);
			//Cambia el boton.
			ventPrincipal.conexionButton.setText("Conectar");
			//Cambia los labels.
			changeLabel(ventPrincipal.conexionActual, "Desconectado");
			// Envia al servidor señal de desconexion.
			chat.enviarMsj("/quit");
			// Si estan activados, cierra los canales y el socket.
			try{
				//Cierra el socket.
				chat.s.close();
				//Cierra los canales.
				chat.inOb.close();
				chat.outd.close();
			} catch(Exception e){ }
		}
	}
}
