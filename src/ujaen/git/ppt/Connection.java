package ujaen.git.ppt;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

import ujaen.git.ppt.mail.Mail;
import ujaen.git.ppt.mail.Mailbox;
import ujaen.git.ppt.smtp.RFC5321;
import ujaen.git.ppt.smtp.RFC5322;
import ujaen.git.ppt.smtp.SMTPMessage;


public class Connection implements Runnable, RFC5322 {

	Mailbox mb;
	boolean transaccion=false;
	boolean autenticado=false;
	String usuariorem="";
	String usuariodest="";
	String argumento2 ="" ;
	String cabecera="";
	String Date="Date";
	String From="From";
	String Mensaje="Mensaje";
	

	protected Socket mSocket;
	protected int mEstado = S_HELO;
	private boolean mFin = false;

	public Connection(Socket s) {
		mSocket = s;
		mEstado = 0;
		mFin = false;
	}

	@Override
	public void run() {

		String inputData = null;
		String outputData = "";
		String code = "";

		if (mSocket != null) {
			try {
				// Inicialización de los streams de entrada y salida
				DataOutputStream output = new DataOutputStream(
						mSocket.getOutputStream());
				BufferedReader input = new BufferedReader(
						new InputStreamReader(mSocket.getInputStream()));

				// Envío del mensaje de bienvenida
				String response = RFC5321.getReply(RFC5321.R_220) + SP + RFC5321.MSG_WELCOME
						+ RFC5322.CRLF;
				output.write(response.getBytes());
				output.flush();

				while (!mFin && ((inputData = input.readLine()) != null)) {
					
					
				
					
					// Todo análisis del comando recibido
					SMTPMessage m = new SMTPMessage(inputData);
					String comando=m.getCommand();
					int identificador=m.getCommandId();
					String argumento=m.getArguments();
					System.out.println("Comando->"+comando);
					System.out.println("Identificador->"+identificador);
					System.out.println("Argumentos->"+argumento);
					System.out.println("Recibido->"+inputData);
					
				    Mail ma= new Mail();
					// TODO: Máquina de estados del protocolo
					switch (identificador) {
					case S_HELO:
						System.out.println("HELO OK");
 						break;
					case S_EHLO:
						System.out.println("EHLO OK");
						break;
					case S_MAIL:
						
						usuariorem=argumento.trim();//recogemos remitente 
						ma.setMailfrom(argumento); //lo guardamos en la varible Mailfrom de la clase Mail.
						
						break;

                       
					case S_RCPT:
						
						usuariodest=argumento.trim();
						mb = new Mailbox(usuariodest);
						boolean correcto=mb.checkRecipient(usuariodest); // Comprobamos si el usuario existe.
                        
                        if(correcto)
                        {
                           System.out.println("OK Usuario reconocido");
                           ma.setRcptto(usuariodest);// guardamos el usuario destino en la variable Rcptto de la clase Mail.
                            autenticado=true; // Activamos el indicador de que el usuario se ha identificado correctamente.
                            break;
                        }
                        else
                        {
                        	System.out.println("ERR Usuario no reconocido");}
						break;
					case S_DATA:
						if(autenticado)
						{
							transaccion=true;
							
							}
						else{}
						break;
					case S_RSET:
						System.out.println("RSET OK");

					
						break;

					case S_QUIT:
						System.out.println("QUIT OK");
						break;
					
					}
					if(transaccion){
						
						if (inputData.indexOf(":") > 0) {
							String[] RecMes = inputData.split(":");
							cabecera=RecMes[0];
							argumento2=RecMes[1];
							cabecera=cabecera.trim();
							argumento2=argumento2.trim();
						}
						switch(cabecera){
						case "Date":
							System.out.println("Prueba argumento Date"+ argumento2);
							break;
						case "From":
							System.out.println("Prueba argumento From"+ argumento2);
							break;
						case "Subject":
							System.out.println("Prueba argumento asunto"+ argumento2);
							break;
						case "To":
							System.out.println("Prueba argumento To"+ argumento2);
							cabecera = Mensaje;
							break;
						case "Mensaje":
							System.out.println("Prueba mensaje recibido: "+ inputData);
						
						}
							
						
							}
						
					
					

					// TODO montar la respuesta
					// El servidor responde con lo recibido
					outputData = RFC5321.getReply(RFC5321.R_220) + SP + inputData + CRLF;
					output.write(outputData.getBytes());
					output.flush();

				}
				System.out.println("Servidor [Conexión finalizada]> "
						+ mSocket.getInetAddress().toString() + ":"
						+ mSocket.getPort());

				input.close();
				output.close();
				mSocket.close();
			} catch (SocketException se) {
				se.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	}
}
