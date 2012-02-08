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
					usr = chat.userSearch(userSelect);
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
