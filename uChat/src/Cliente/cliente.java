/*
 * Chat client uChat.
 * 
 * Developed by sofTroopers:
 *	  -Friloren
 *	  -gllera
 * 
 */

package Cliente;
import java.io.IOException;

//Main para la ejecucion del Thread del cliente.
public class cliente{
	
	// Comienzo del programa.
	public static void main(String[] args) throws IOException {
		//Genera la ventana.
		new ventPrincipal();
	}
	
	// Final del programa.
	public static void cerrarPrograma(){
		
		//Cierra la conexion actual si la hay.
		// NOTA: Comprobador en la funcion.
		chat.cerrarConexion();		
		
		//Sale el programa.
		System.exit(0);
	}
	  
}
