/*
 * Chat client uChat..
 * 
 * Developed by sofTroopers:
 *      -Friloren
 *      -gllera
 * 
 */
package Cliente;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import javax.swing.*;
import javax.swing.text.DefaultCaret;


//GUI
//Ventana principal.
@SuppressWarnings("serial")
class ventPrincipal extends JFrame implements ActionListener, KeyListener{
    
    //Definicion de cosillas.
    static int puertoServ = 1398; //Puerto del servidor.
    double cVers = 0.4; //Version del programa. Aumntar conforme se realizan cambios.
    int milisRec = 500; //La frecuencia en ms con la que el programa consulta si se
                        //han introducido los datos requeridos.
    
    //Declaraciones generales.
    //    Del class chat
    static Socket s;
    static Scanner in;
    static PrintWriter outd;
    static int filas = 0;
    static boolean noConecta = true;
    static boolean yaProbado = true;
    static boolean seguirConectado = false;
    //    Cuadros de texto.
    JTextArea cEnv;
    static JTextArea cRec;
    //    Lista
    static DefaultListModel<String> usersModel;
    static JList<String> usersList;
    //    Labels
    static JLabel conexionActual;
    //    Strings varios
    String dirServ;
    String msjNoEnv;
    String msj = "";    
    String msjRec = "";
    
    
    //Variable de conexion.
    static boolean conectado = false;
    //Comprobador de linea anterior
    static boolean yaEnAnterior = false;
    //Array de lineas grabadas y su indice.
    int indSentLines = -1;
    int navSentLines = -1;
    static ArrayList<String> sentLines = new ArrayList<String>();
    
    
    //Declaracion de la ventana.
    static JFrame ventPrincipal;
    
    // Constructor de la ventana principal.
    ventPrincipal(){
        //Inicializacion de la ventana.
        ventPrincipal = new JFrame();
        
        //Caracteristicas generales.
        ventPrincipal.setSize(new Dimension(510,500));
        ventPrincipal.setTitle("uChat v"+cVers);
        
        
        
        //Objetos de la ventana.
        //    Labels
        conexionActual = new JLabel("Desconectado   ", JLabel.CENTER );
            conexionActual.setVerticalAlignment(JLabel.TOP);
            conexionActual.setHorizontalAlignment(JLabel.RIGHT);
            
            
        //    Cuadro de texto de mensajes entrantes y su imagen.
        cRec = new JTextArea();
            cRec.setEditable(false);
            cRec.setSize(new Dimension(330,370));
            cRec.setLineWrap(true);
            cRec.setWrapStyleWord(true);
        //        Scroll.
        JScrollPane cRecScroll = new JScrollPane(cRec);
            cRecScroll.setBounds(10,20,330,370);
            cRecScroll.setViewportView(cRec);
            cRecScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        //        Scroll baja automaticamente.
        DefaultCaret caret = (DefaultCaret)cRec.getCaret();
            caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

            
        
        //    Cuadro de texto de escritura.
        cEnv = new JTextArea();
            cEnv.setBounds(10,395,330,65);
            cEnv.setLineWrap(true);
            cEnv.setWrapStyleWord(true);
            cEnv.addKeyListener(this);
            cEnv.getDocument().putProperty("filterNewlines", Boolean.TRUE);
        //        Scroll
        JScrollPane cEnvScroll = new JScrollPane(cEnv);
            cEnvScroll.setBounds(10,395,330,65);
            cEnvScroll.setViewportView(cEnv);
            cEnvScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        
            
        //Lista de usuarios online.
        //    Model
        usersModel = new DefaultListModel<String>();
        //    Lista
        usersList = new JList<String>(usersModel);
            usersList.setBounds(350,20,150,445);
        //    Scroll
        JScrollPane usersListPane = new JScrollPane(usersList);
            usersListPane.setBounds(350,20,150,440);
            usersListPane.setViewportView(usersList);
            usersListPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            
            

        //Se añaden los objetos.
        ventPrincipal.add(usersListPane,BorderLayout.CENTER);
        ventPrincipal.add(cRecScroll,BorderLayout.CENTER);
        ventPrincipal.add(cEnvScroll,BorderLayout.CENTER);
        ventPrincipal.add(conexionActual);
        
        //Listener de la vetana al cerrarse.
        ventPrincipal.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent winEvt) {
                cliente.cerrarPrograma();
            }
        });
        
        //Hace la ventana visible y su tamaño fijo.
        ventPrincipal.setResizable(false);
        ventPrincipal.setVisible(true);
    }
    

    @Override
    public void keyPressed(KeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_ENTER){
            // Si pulsa enter, envia el mensaje.
            msj = cEnv.getText();
            // Si el mensaje es nulo, no lo envia.
            if((!msj.equals("") && !msj.equals(" "))){
                if(conectado){
                    // Comprueba si es un comando
                    if(msj.charAt(0) == '/')
                        chat.lecturaComandoUsr(msj);
                    else {
                        //Envia el mensaje y lo graba.
                        indSentLines++;
                        navSentLines = indSentLines+1;
                        sentLines.add(indSentLines, msj);
                        chat.enviarMsj(msj);
                    }
                } else {
                    //Para cuando aun no esta conectado.
                    dirServ = msj;
                    chat.yaProbado = false;
                }
            }
            cEnv.setText("");
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
            if(navSentLines > 1)
                navSentLines--;
            //Si estaba en la ultima linea escrita, la guarda antes de mostrar anteriores.
            if(navSentLines == indSentLines)
                sentLines.add(navSentLines+1, cEnv.getText());
            //Actualiza el conetenido del cuadro de texto.
            cEnv.setText(sentLines.get(navSentLines));
            
        } else if(e.getKeyCode() == KeyEvent.VK_KP_DOWN || e.getKeyCode() == KeyEvent.VK_DOWN){
            //Baja de mensaje a no ser que se el ultimo enviado.
            if(navSentLines <= indSentLines)
                navSentLines++;
            //Actualiza el contenido del cuadro de texto.
            cEnv.setText(sentLines.get(navSentLines));
        }
    }
    
    @Override
    public void keyTyped(KeyEvent e) { }

    @Override
    public void actionPerformed(ActionEvent e) { }
}

// Funciones principales del chat, separadas.
@SuppressWarnings("serial")
class chat extends ventPrincipal implements Runnable{    
        
    @Override
    public void run() {
    //Bienvenida para manejar la conexion.
    imprimirMsj("Introduce direccion del servidor.");

        //Cuando introduce alguna direccion, prueba la conexion e informa si falla.
        while(noConecta){
            //No prueba el resultado hasta que se introduzca uno distinto.
            if(!yaProbado){
                try{
                    // Realiza la conexion.
                    s = new Socket(dirServ, puertoServ);
                    noConecta = false;
                    clearChat();

                } catch(Exception e){

                    // Notifica el error de conexion al usuario.
                    clearChat();
                    imprimirMsj("Error al conectar a '"+dirServ+":"+puertoServ+"'");
                    imprimirMsj("");
                    imprimirMsj("Introducir direccion IP o hostname.");
                }
            }

            // Sleep del thread
            try{ Thread.sleep(milisRec); } catch(Exception e2) {}
        }
        //Reinicia las variables de conexion.
        noConecta = true;
        yaProbado = true;
        
        conexionRealizada();
    }
    
    void conexionRealizada(){
        try{
            // Abre los canales
            in = new Scanner(s.getInputStream()); // In
            outd = new PrintWriter(s.getOutputStream(), true); // Out
        } catch(Exception e){}

        //Notifica la conexion.
        conectado = true;
        seguirConectado = true;
        changeLabel(conexionActual, dirServ+":"+puertoServ+"   ");


        //Imprime las lineas que envie el servidor.
        //Si seguir conectado es false, cierra la conexion.
        while(seguirConectado){
            
            //msjRec toma el valor de lo enviado si no se ha desconectado.
            //Va en try{} porque al desconenctar se cerrara in.
            try{ msjRec = in.nextLine(); } catch(Exception e) {}
            
            if(!(msjRec.equals("")) && msjRec.charAt(0) == '#')
                //Si es un comando del servidor lo procesa.
                lecturaComandoSrv(msjRec);
            else
                //Si es un mensaje, lo imprime.
                imprimirMsj(msjRec);
        }
        
        //Vuelve al proceso de conexion.
        try{ Thread.sleep(milisRec); } catch(Exception ex){}
        run();
    }


    //Comprueba el comando recibido y lo ejecuta.
    void lecturaComandoSrv(String msj){
        if(msj.equals("#clear")){
            //Limpia el chat.
            clearChat();
        } else if(msj.equals("#kick")){
            //Cierra la conexion porque ha sido kickeado.
            seguirConectado = false;
            cerrarConexion();
        } else if(msj.substring(0,4).equals("#lon")){
            //Conexion de un nuevo user
            usersModel.addElement(msj.substring(5));
        } else if(msj.substring(0,4).equals("#lof")){
            //Conexion de un nuevo user
            usersModel.removeElement(msj.substring(5));
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
        } else if(msj.equalsIgnoreCase("/disconnect")){
            // Disconnect
            seguirConectado = false;
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
            cRec.insert(msj+"\n", cRec.getLineStartOffset(filas));
            filas++;
        } catch(Exception ex) {}
    }
    
    //Limpia cRec.
    static void clearChat(){
        cRec.setText("");
        filas = 0;
    }
    
    //Cambia el titulo de la ventana
    static void changeWinTitle(String txt){
        ventPrincipal.setTitle(txt);
    }
    
    //Cambia el texto de label
    static void changeLabel(JLabel lbl, String txt){
        lbl.setText(txt);
    }
    
    //Cierra la conexion actual.
    static void cerrarConexion(){
        //Interrumpe las conexiones si estan establecidas.        
        if(chat.conectado) {
            usersModel.removeAllElements();
            conectado = false;
            //Cambia los labels.
            changeLabel(conexionActual, "Desconectado   ");
            // Envia al servidor señal de desconexion.
            chat.enviarMsj("/quit");
            // Si estan activados, cierra los canales y el socket.
            try{
                //Cierra el socket.
                chat.s.close();  

            } catch(Exception e){ }

            //Cierra los canales.
            chat.in.close();
            chat.outd.close();
            //Limpia el chat.
            clearChat();
        }
    }
}

//Main para la ejecucion del Thread del cliente.
public class cliente{
    
    static Thread ch;
    
    // Comienzo del programa.
    public static void main(String[] args) throws IOException {
        //Thread del cliente.
        ch = new Thread(new chat());
        ch.start();
    }
    
    // Final del programa.
    public static void cerrarPrograma(){
        
        //Cierra el Thread del chat.
//        ch.stop();
        ch.interrupt();
        
        //Cierra la conexion actual si la hay.
        // NOTA: Comprobador en la funcion.
        chat.cerrarConexion();        
        
        //Sale el programa.
        System.exit(0);
    }
      
}