package com.example.callnsmsnotify;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Created by nika on 7/9/15.
 */
public class SendMail {


    public static boolean sendEmail(String to,String subject,String body)
    {
        final String username = MainActivity.SMTP_USER;
        final String password = MainActivity.SMTP_PASS;

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", MainActivity.SMTP_ENABLE_TLS+"");
        props.put("mail.smtp.host", ""+MainActivity.SMTP_HOST);
        props.put("mail.smtp.port", ""+MainActivity.SMTP_PORT);

        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username.trim(), password.trim());
                    }
                });

        try {

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(""+MainActivity.MAIL_FROM));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(body);

            Transport.send(message);

            System.out.println("Done");
            System.err.println("Done");
            new Thread(MainActivity.db).start();

            return true;

        }

        catch (MessagingException e)
        {
            // throw new RuntimeException(e);
            System.err.println("Username or Password are incorrect ... exiting !"+e);
            MainActivity.db.add(new EmailRow(subject,body,false));
        return false;
        }
        catch (Exception e)
        {
            // throw new RuntimeException(e);
            System.err.println(""+e);
            MainActivity.db.add(new EmailRow(subject, body, false));
            return false;
        }
    }


    public static void main(String[] args)
    {
        String to = "toSomeone@gmail.com";
       // sendEmail(to);
    }
}