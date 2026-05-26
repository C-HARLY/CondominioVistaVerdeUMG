package logic;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;
public class EmailService {

    public static void enviarRecibo(String destinatario, double monto, String mes, int anio, int numCasa) {
    // Configuración de credenciales
    final String remitente = "tu_correo_nuevo@gmail.com"; // Asegúrate de poner el correo real
    final String contrasena = "jqlh rqhj gmwq cgsk";    // Tu clave de 16 letras

    // Configuración del servidor SMTP de Google
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
        // Usamos el constructor que permite definir el nombre que verá el usuario
        message.setFrom(new InternetAddress(remitente, "Condominio Vista Verde"));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
        message.setSubject("Recibo de Pago - Casa " + numCasa);
        
        // Cuerpo del mensaje en formato HTML profesional
        String cuerpo = "<html><body>" +
                        "<h3>Recibo de Pago - Condominio Vista Verde</h3>" +
                        "<p>Estimado residente de la <b>Casa " + numCasa + "</b>,</p>" +
                        "<p>Le confirmamos que se ha registrado exitosamente su pago por un monto de " +
                        "<b>Q" + monto + "</b>, correspondiente al mes de <b>" + mes + " de " + anio + "</b>.</p>" +
                        "<p>Gracias por estar al día con sus obligaciones.</p>" +
                        "<br><p>Atentamente,<br><i>Administración Vista Verde</i></p>" +
                        "</body></html>";

        message.setContent(cuerpo, "text/html; charset=utf-8");

        Transport.send(message);
        System.out.println("Correo enviado exitosamente a: " + destinatario);

    } catch (Exception e) {
        // En un entorno profesional, esto se registra en un log
        System.err.println("Error al enviar el correo: " + e.getMessage());
    }
    }
}