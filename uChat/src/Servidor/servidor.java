/*
 * Chat server uChat.
 * 
 * Developed by sofTroopers:
 *	  -Friloren
 *	  -gllera
 * 
 */

package Servidor;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

class servidor {

	//CONFIGURACIONES
	//Configuracion de servidor
	static int puerto = 1398;	//Puerto del servidor.
	static double cVer = 0.25;	//Version del servidor.
	//Configuracion de SQL.
	static int prt_SQL = 3306; 	//Puerto de SQL.


	//INICIALIZACIONES.
	//Inicializa cosillas
	static Thread conexionThread;
	public static ArrayList<Proceso> ts;
	public static long time;
	  
	
	public static void main(String args[]) {	
		ts = new ArrayList<Proceso>();
		  
	  
		try {
			//Esperar conexion
			ServerSocket srvr = new ServerSocket(puerto);
			System.out.println("uChat Server v"+cVer);
			System.out.println("Esperando conexiones en el puerto "+puerto);

			while(true){
				//Enlaza la conexion
				Socket skt = srvr.accept();
				//Define el nuevo thread.
				conexionThread = new Thread(new Proceso(skt));
				//Comienza el thread.
				conexionThread.start();
			}
		}
		
		catch(Exception e) {
			System.out.println("Error en el servidor");
		}
	}
}