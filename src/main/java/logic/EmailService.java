package logic;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;

/*
 * Servicio para enviar correos electrónicos de forma automática.
 * Se conecta a la cuenta de Gmail del condominio para mandar los recibos.
 */
public class EmailService {

    /*
     * Arma y envía el recibo de pago al correo del residente.
     * Al usar este método desde el Controlador, hay que mandarle todos estos datos:
     * destinatario (correo), nombrePropietario (ej. Juan Perez), monto, mes, año y número de casa.
     */
    public static void enviarRecibo(String destinatario, String nombrePropietario, double monto, String mes, int anio, int numCasa) {
        
        // 1. Datos de la cuenta que envía el correo
        final String remitente = "administracion.vistaverde@gmail.com"; 
        final String contrasena = "jqlhrqhjgmwqcgsk"; 

        // 2. Configuraciones de seguridad para que Google nos deje entrar
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(remitente, contrasena);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(remitente, "Condominio Vista Verde"));
            // A quién le llega el correo
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
            // El título del correo
            message.setSubject("Recibo de Pago - Casa " + numCasa);
            
            // 3. El diseño visual del correo
            // Agregamos el %s en el saludo para que ahí se ponga el nombre del propietario
                String cuerpo = """
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <meta charset="UTF-8">
                        <style>
                            body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f4f7f6; margin: 0; padding: 20px; }
                            .container { max-width: 500px; margin: 0 auto; background-color: #ffffff; padding: 30px; border-radius: 10px; box-shadow: 0 4px 15px rgba(0,0,0,0.05); }
                            .header { text-align: center; border-bottom: 2px solid #2e7d32; padding-bottom: 15px; margin-bottom: 20px; }
                            .header h2 { color: #2e7d32; margin: 0; font-size: 24px; }
                            .content { color: #444444; font-size: 16px; line-height: 1.6; }
                            .monto-box { background-color: #e8f5e9; color: #1b5e20; text-align: center; font-size: 28px; font-weight: bold; padding: 15px; border-radius: 8px; margin: 25px 0; }
                            .detalles { width: 100%%; border-collapse: collapse; margin-bottom: 25px; }
                            .detalles td { padding: 10px; border-bottom: 1px solid #eeeeee; }
                            .detalles td:first-child { font-weight: bold; color: #555555; }
                            .footer { text-align: center; font-size: 12px; color: #999999; margin-top: 30px; border-top: 1px solid #eeeeee; padding-top: 15px; }
                        </style>
                    </head>
                    <body>
                        <div class="container">
                            <div class="header">
                                <h2>🏢 Vista Verde</h2>
                            </div>
                            <div class="content">
                                <p>Hola, <b>%s</b> (Casa %d).</p>
                                <p>Hemos registrado tu pago de mantenimiento exitosamente. Aquí tienes los detalles de tu transacción:</p>

                                <div class="monto-box">
                                    Q %,.2f                             
                                </div>

                                <table class="detalles">
                                    <tr>
                                        <td>Mes cubierto:</td>
                                        <td>%s</td>
                                    </tr>
                                    <tr>
                                        <td>Año:</td>
                                        <td>%d</td>
                                    </tr>
                                    <tr>
                                        <td>Estado:</td>
                                        <td><span style="color: #2e7d32; font-weight: bold;">Pagado ✓</span></td>
                                    </tr>
                                </table>

                                <p>Gracias por mantenerte al día con tus contribuciones. ¡Que tengas un excelente día!</p>
                            </div>
                            <div class="footer">
                                Este es un comprobante automático generado por el Sistema de Administración Vista Verde.<br>
                                Por favor, no respondas a este correo.
                            </div>
                        </div>
                    </body>
                    </html>
                    """.formatted(nombrePropietario, numCasa, monto, mes, anio);

            // Armamos el correo para que reconozca tildes y ñ
            message.setContent(cuerpo, "text/html; charset=utf-8");

            // Lo enviamos
            Transport.send(message);
            System.out.println("Correo enviado con éxito a: " + destinatario);

        } catch (Exception e) {
            System.err.println("Ocurrió un error al intentar enviar el correo: " + e.getMessage());
        }
    }
}