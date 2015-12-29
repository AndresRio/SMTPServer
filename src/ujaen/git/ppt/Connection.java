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
	Mail ma = new Mail ();
	Mailbox mb;
	boolean transaccion=false;
	boolean autenticado=false;
	String usuariorem="";
	String usuariodest="";
	String argumento2 ="" ;
	String PruebaRecogida="PruebaRecogida";
	
	String Mensaje="Mensaje";
	String mensaje2="";
	

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
		Mail ma= new Mail();
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
					int identificador=m.getCommandId();
					String argumento=m.getArguments();
					
					
				    
					// TODO: Máquina de estados del protocolo
					switch (identificador) {
					case S_HELO:
						outputData = RFC5321.getReply(RFC5321.R_250) + SP + inputData + CRLF;
						output.write(outputData.getBytes());
						output.flush();
 						break;
					
					case S_MAIL:
						outputData = RFC5321.getReply(RFC5321.R_250) + SP + "Sender: "+argumento + CRLF;
						output.write(outputData.getBytes());
						output.flush();
						ma=new Mail();
						usuariorem=argumento.trim();//recogemos remitente 
						ma.setMailfrom(argumento); //lo guardamos en la varible Mailfrom de la clase Mail.
						
						break;

                       
					case S_RCPT:
						outputData = RFC5321.getReply(RFC5321.R_250) + SP + "Recipient: "+argumento + CRLF;
						output.write(outputData.getBytes());
						output.flush();
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
                        	outputData = RFC5321.getReply(RFC5321.R_250) + SP + "Usuario: "+argumento +" no reconocido, vuelva a introducir usuario valido"+ CRLF;
    						output.write(outputData.getBytes());
    						output.flush();
                        	System.out.println("ERR Usuario no reconocido");}
						break;
					case S_DATA:
						if(autenticado)
						{
							outputData = RFC5321.getReply(RFC5321.R_354) + SP + "  enter mail, end with line containing only ." + CRLF;
							output.write(outputData.getBytes());
							output.flush();
							transaccion=true;
							
							}
						else{}
						break;
					case S_RSET:
						outputData = RFC5321.getReply(RFC5321.R_250) + SP + "Rset OK, introduzca remitente" + CRLF;
						output.write(outputData.getBytes());
						output.flush();
						identificador=2;//regresamos al estado MAIL TO

					
						break;

					case S_QUIT:
						outputData = RFC5321.getReply(RFC5321.R_221) + SP + "Sesion finalizada, si desea mandar otro correo mande HELO" + CRLF;
						output.write(outputData.getBytes());
						output.flush();
						break;
					
					}
					if(transaccion){
						
						ma.addMailLine(inputData);
						//mecanismo de transparencia
					
						if(inputData.contains(RFC5322.CRLF+RFC5322.ENDMSG)){//comprobación de que llegue \r\n.\r\n para finalizar correo
							String newma=ma.getMail();
							mb.newMail(newma);}//cierre if 1 y creamos el fichero
						if (inputData.indexOf(".") > 0) {//comprobación de que llegue un punto inicial, seguido de más caracteres
							String[] aux1 = inputData.split(".");
							String aux2=aux1[1];
							if(aux2.length()==inputData.length()-1){
								 String aux3= aux2.substring(1, inputData.length());//borrar el punto inicial
								ma = new Mail();
								 ma.addMailLine(aux3);//pasamos la cadena caracteres sin el . inicial
								 mb.newMail(ma.getMail());
							}
							
						
						}//cierre if 2
						}	

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
