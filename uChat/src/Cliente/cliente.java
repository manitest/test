/*
 * Chat client uChat.
 * 
 * Developed by sofTroopers:
 *	  -Friloren
 *	  -gllera
 * 
 */

package Cliente;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.DefaultCaret;
import javax.swing.text.html.HTMLDocument;


//GUI
//Ventana principal.
@SuppressWarnings("serial")
class ventPrincipal extends JFrame implements ActionListener, KeyListener{
	
	//Definicion de cosillas.
	static int puertoServ = 1398;	//Puerto del servidor.
	double cVers = 0.62;			//Version del programa. Aumentar conforme se realizan cambios.
	
	
	//Declaraciones generales.
	//	Cuadros de texto.
	static JTextArea cEnv;
	static JTextPane cRec;
	//	HTML Doc
	static HTMLDocument hDoc;
	//	Field
	static JTextField direccionText;
	static JTextField usernameText;
	//	List.
	static DefaultListModel<String> usersModel;
	static JList<String> usersList;
	//	Labels
	static JLabel conexionActual;
	//	Buttons
	static JButton conexionButton;

	//Thread de conexion.
	static Thread conexion = new Thread(new chat());
	
	
	//Array de lineas grabadas y su indice.
	int indSentLines = -1;
	int navSentLines = 0;
	static ArrayList<String> sentLines = new ArrayList<String>();
	
	
	//Declaracion de la ventana.
	static JFrame ventPrincipal;
	
	// Constructor de la ventana principal.
	ventPrincipal(){
		//Inicializacion de la ventana.
		ventPrincipal = new JFrame();
		
		//Caracteristicas generales.
		ventPrincipal.setSize(new Dimension(530,530));
		ventPrincipal.setTitle("uChat v"+cVers);
		
		
		//Objetos de la ventana.
		
		//Menu de conexion
		//  Labels
		conexionActual = new JLabel("Desconectado.");
			conexionActual.setBounds(10,470,510,20);
		JLabel direccionLab = new JLabel("Direccion:");
			direccionLab.setBounds(10, 0, 100, 20);
		JLabel usernameLab = new JLabel("Username");
			usernameLab.setBounds(220,0,100,20);
		//	JTextFields
		direccionText = new JTextField();
			direccionText.setBounds(10, 20, 200, 24);
		usernameText = new JTextField();
			usernameText.setBounds(220,20,140,24);
		//  Buttons
		conexionButton = new JButton("Conectar");
			 conexionButton.setBounds(370,6,150,38);
		//	JTextFields
		direccionText = new JTextField();
			 direccionText.setBounds(10, 20, 200, 24);
		//  Buttons
		conexionButton = new JButton("Conectar");
			 conexionButton.setBounds(370,5,150,40);
			 
			
		//	Cuadro de texto de mensajes entrantes y su imagen.
		cRec = new JTextPane();
			cRec.setContentType("text/html");
			cRec.setEditable(false);
		//		Scroll.
		hDoc = (HTMLDocument)cRec.getDocument();
			chat.imprimirMsj("Introduce la direccion del servidor y conecta.");
		JScrollPane cRecScroll = new JScrollPane(cRec);
			cRecScroll.setBounds(10,50,350,345);
			cRecScroll.setViewportView(cRec);
			cRecScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		//		Scroll baja automaticamente.
		DefaultCaret caret = (DefaultCaret)cRec.getCaret();
			caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

			
		
		//	Cuadro de texto de escritura.
		cEnv = new JTextArea();
			cEnv.setEnabled(false);
			cEnv.setLineWrap(true);
			cEnv.setWrapStyleWord(true);
			cEnv.addKeyListener(this);
			cEnv.getDocument().putProperty("filterNewlines", Boolean.TRUE);
		//		Scroll
		JScrollPane cEnvScroll = new JScrollPane(cEnv);
			cEnvScroll.setBounds(10,400,350,65);
			cEnvScroll.setViewportView(cEnv);
			cEnvScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		
			
		//Lista de usuarios online.
		//	Model
		usersModel = new DefaultListModel<String>();
		//	Lista
		usersList = new JList<String>(usersModel);		
		//	Scroll
		final JScrollPane usersScrollPane = new JScrollPane(usersList);
			usersScrollPane.setBounds(370,50,150,415);
			usersScrollPane.setViewportView(usersList);
			usersScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			

		//Se añaden los objetos.
		//Boton de conexion.
		ventPrincipal.add(conexionButton,BorderLayout.CENTER);
		//Label de conexion actual.
		ventPrincipal.add(conexionActual,BorderLayout.CENTER);
		//Menu conexion.
		ventPrincipal.add(direccionLab,BorderLayout.CENTER);
		ventPrincipal.add(direccionText,BorderLayout.CENTER);
		ventPrincipal.add(usernameLab, BorderLayout.CENTER);
		ventPrincipal.add(usernameText, BorderLayout.CENTER);
		//Otros.
		ventPrincipal.add(usersScrollPane,BorderLayout.CENTER);
		ventPrincipal.add(cRecScroll,BorderLayout.CENTER);
		ventPrincipal.add(cEnvScroll,BorderLayout.CENTER);
		ventPrincipal.add(new JLabel(""));
		
		
		//Listener de mouse para el popup de la lista.
		//	Popup de detalles de los usuarios.
		final JPopupMenu usersDetailPopUp = new JPopupMenu();
			final JLabel usrIP = new JLabel();
			final JLabel usrTimeOn = new JLabel();
			final JLabel usrName = new JLabel();
				usrName.setFont(new Font(usrName.getFont().getName(),Font.BOLD,usrName.getFont().getSize()));
				
			usersDetailPopUp.setBackground(Color.lightGray);
			usersDetailPopUp.setBorder(BorderFactory.createLineBorder(Color.black));
			usersDetailPopUp.add(usrName);
			usersDetailPopUp.add(usrIP);
			usersDetailPopUp.add(usrTimeOn);
		//	MouseListener.
		usersList.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				
				String userSelect;
				UserUsr usr;
				Point  p = e.getPoint();
				
				if((userSelect = usersList.getSelectedValue()) != null){
					usr = chat.usersOnline.get(usersList.getSelectedIndex());//chat.userSearch(userSelect);
					usrName.setText(userSelect);
					usrIP.setText(usr.ip);
					usrTimeOn.setText(usr.timeOn);
					usersDetailPopUp.show(usersScrollPane, p.x, p.y);
				} 
			}
		});


		//Listener de la vetana al cerrarse.
		ventPrincipal.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent winEvt) {
				cliente.cerrarPrograma();
			}
			@Override
			public void windowOpened(WindowEvent e){
				direccionText.requestFocus();
			}
		});
		
		//Listener del boton.
		conexionButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e){
				//Se ejecuta cuando se pulsa el boton.
				if(chat.conectado){
					//Si esta conectado, desconecta.
					chat.cerrarConexion();						
				} else
					//Inicia o reinicia el thread de conexion.
					if(conexion.isAlive())
						chat.inicioThread();
					else
						conexion.start();
			}
		});
		
		
		//Hace la ventana visible y su tamaño fijo.
		ventPrincipal.setResizable(false);
		ventPrincipal.setVisible(true);
	}
	

	@Override
	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_ENTER){
			//Obtiene el mensaje.
			String msj = cEnv.getText();
			// Si el mensaje es nulo o no esta conectado, no lo envia.
			if(!msj.equals("") && !msj.equals(" ")){
				//Graba el mensaje.
				indSentLines++;
				sentLines.add(indSentLines, msj);
				navSentLines = indSentLines+1;
				// Comprueba si es un comando.
				if(msj.charAt(0) == '/')
					chat.lecturaComandoUsr(msj);
				else {
					//Si no lo es lo envia.
					// Comprueba si es un comando.
					if(msj.charAt(0) == '/')
						chat.lecturaComandoUsr(msj);
					else {
						//Envia el mensaje.
						chat.enviarMsj(msj);
					}
				}
				cEnv.setText("");
			}
		}
	}


	@Override
	public void keyReleased(KeyEvent e) {
		//Al soltar enter limpia el cuadro de texto.
		if(e.getKeyCode() == KeyEvent.VK_ENTER){
			cEnv.setText("");
			
		//Navegacion por los mensajes enviados.
		} else if(e.getKeyCode() == KeyEvent.VK_KP_UP || e.getKeyCode() == KeyEvent.VK_UP){
			//Sube de mensaje excepto si es el primero.
			if(navSentLines > 0)
				navSentLines--;
			//Si estaba en la ultima linea escrita, la guarda antes de mostrar anteriores.
			if(navSentLines == indSentLines)
				sentLines.add(navSentLines+1, cEnv.getText());
			//Actualiza el conetenido del cuadro de texto.
			if(navSentLines >= 0)
				cEnv.setText(sentLines.get(navSentLines));
			
		} else if(e.getKeyCode() == KeyEvent.VK_KP_DOWN || e.getKeyCode() == KeyEvent.VK_DOWN){
			//Baja de mensaje a no ser que se el ultimo enviado.
			if(navSentLines <= indSentLines){
				navSentLines++;
				//Actualiza el contenido del cuadro de texto.
				cEnv.setText(sentLines.get(navSentLines));
			}
		}
	}	
	@Override
	public void keyTyped(KeyEvent e) { }
	@Override
	public void actionPerformed(ActionEvent e) { }
}

// Funciones principales del chat, separadas.
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

	
	static void lecturaObjeto(Object o) throws Exception{
		switch(o.getClass().getName()){
		
		case "java.lang.String":
			//Si es un String.
			String msj = (String)o;
			
			if(!(msj.equals("")) && msj.charAt(0) == '#')
				//Si es un comando del servidor lo procesa.
				lecturaComandoSrv(msj);
			else
				//Si es un mensaje, lo imprime.
				imprimirMsj(msj);
			break;
		
		case "Cliente.UserUsr":
			//Si es un UserUsr.
			UserUsr usr = (UserUsr)o;
			
			if(!usersOnline.contains(usr)){
			//Se conecta un usuario nuevo.
				usersOnline.add(usr);
				ventPrincipal.usersModel.addElement(usr.name);
			} else {
			//Se desconecta un usuario.
				usersOnline.remove(usr);                           
				ventPrincipal.usersModel.removeElement(usr.name);
			}
			break;
		
		default:
			//There shouldn't be any other objects.
		}
	}

	//Comprueba el comando recibido y lo ejecuta.
	static void lecturaComandoSrv(String msj){
		switch(msj){
		case "#clear":
			//Limpia el chat.
			clearChat();
			break;
			
		case "#kick":
			//Cierra la conexion.
			cerrarConexion();
			//Informa en el label que ha sido kickeado.
			changeLabel(ventPrincipal.conexionActual, "Has sido kickeado.");
			break;
			
		case "#nameEnUso":
			//El nombre esta en uso.
			cerrarConexion();
			//Informa en el label que el nombre esta en uso.
			changeLabel(ventPrincipal.conexionActual,
					"El nombre "+ventPrincipal.usernameText.getText()+" ya esta en uso.");
			break;
			
		//case "#custom":
		//Add your custom commands here.
			
		default:
			//No debería mandar otros mensajes.
		}
	}
	
	//Comprueba que comando se ha escrito y lo ejecuta.
	static void lecturaComandoUsr(String msj){
		switch(msj){
		case "/quit":
			// Quit
			cliente.cerrarPrograma();
			break;
			
		case "/who":
			// Who
			enviarMsj(msj);
			break;
			
		case "/clear":
			// Clear
			clearChat();
			break;
			
		case "/changename":
			// Changename
			enviarMsj(msj);
			break;
			
		case "/disconnect":
			// Disconnect
			cerrarConexion();
			break;
			
		case "/comandos":
			// Comandos
			imprimirMsj("");
			imprimirMsj(" > Comandos");
			imprimirMsj("  |  /who  (Muestra usuarios conectados)");
			imprimirMsj("  |  /clear  (Limpia la ventana de chat)");
			imprimirMsj("  |  /disconnect  (Sale del servidor)");
			imprimirMsj("  |  /quit  (Cierra este cliente)");
			imprimirMsj("  L /comandos  (Muestra esta lista)");
			imprimirMsj("");
			break;
		
		//case "/custom":
		//Add your custom commands here.
			
		default:
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
			ventPrincipal.usersList.removeAll();
			ventPrincipal.usersList.list();
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