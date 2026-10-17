package edu.unimagdalena.universitystore.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Envía correos a los clientes. Si no hay un servidor de correo configurado (variables SPRING_MAIL_HOST,
 * SPRING_MAIL_PORT, SPRING_MAIL_USERNAME y SPRING_MAIL_PASSWORD), el correo no sale: se escribe en el
 * log, así se puede ver qué se habría enviado. Un fallo al enviar nunca rompe la operación que lo pidió.
 */
@Service
public class EmailService {
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final ObjectProvider<JavaMailSender> mailSender;
    private final String from;

    public EmailService(ObjectProvider<JavaMailSender> mailSender, @Value("${store.mail.from}") String from) {
        this.mailSender = mailSender;
        this.from = from;
    }

    public void send(String to, String subject, String body) {
        JavaMailSender sender = mailSender.getIfAvailable();
        if (sender == null) {
            log.info("Correo sin enviar (no hay servidor de correo configurado)\nPara: {}\nAsunto: {}\n{}", to, subject, body);
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        try {
            sender.send(message);
            log.info("Correo enviado a {}: {}", to, subject);
        } catch (RuntimeException e) {
            log.warn("No se pudo enviar el correo a {} ({}): {}", to, subject, e.getMessage());
        }
    }
}
