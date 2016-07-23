package com.example.callnsmsnotify;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

/**
 * Created by nika on 7/9/15.
 */
public class DB implements Runnable {
    public static ArrayList<EmailRow> emailTable = null;
    String path = "";

    public DB(String loc) {
        path = loc;
        loadDB();

        if (emailTable == null) {
            emailTable = new ArrayList<>();

        }

    }

    void saveDB() {
        try {
            if (new File(path).exists()) {
                new File(path).delete();
            }

            FileOutputStream fos = new FileOutputStream(path);
            //GZIPOutputStream gos = new GZIPOutputStream(fos);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(emailTable);
            oos.flush();
            oos.close();
            //gos.close();
            fos.close();
        } catch (Exception e) {


        }

    }

    void loadDB() {
        try {

            FileInputStream fis = new FileInputStream(path);
            // GZIPInputStream gos = new GZIPInputStream(fos);
            ObjectInputStream ois = new ObjectInputStream(fis);
            emailTable = (ArrayList<EmailRow>) ois.readObject();
            ois.close();
            //  gos.close();
            fis.close();
        } catch (Exception e) {

            emailTable = null;
        }

    }


    void sync() {
        for (EmailRow er : emailTable) {
            if (!er.getSent())
                if (SendMail.sendEmail(MainActivity.MAIL_TO, er.getSubject(), er.getBody())) {
                    er.setSent(true);
                } else {
                    er.setSent(false);

                }

        }

    }

    void clean() {

        for (int i = 0; i < emailTable.size(); i++) {
            EmailRow r = emailTable.get(i);
            if (r.getSent()) {
                emailTable.remove(r);
                i = 0;
            }
        }


    }

    void add(EmailRow e) {

        emailTable.add(e);
        saveDB();
    }

    public void run() {
        sync();
        clean();
        saveDB();

    }


}
