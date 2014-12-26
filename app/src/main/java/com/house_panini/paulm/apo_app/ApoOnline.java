package com.house_panini.paulm.apo_app;

import android.os.AsyncTask;
import android.util.Log;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;

public class ApoOnline {
    //TODO: Update these via values/strings.xml
    private static final String APO_ROOT = "http://apoonline.org/alpharho/";
    private static final String HOME_PAGE = APO_ROOT+"memberhome.php";
    private static final String LOGOUT_PAGE = APO_ROOT+"memberhome.php?action=logout";
    static String sessionId;

    public static Document login (String email, String pass) throws IllegalArgumentException, IOException {
        Log.d("APO.login", "Attempting to connect to apo online!");
        Connection.Response res = Jsoup.connect(HOME_PAGE)
                .data("email", email, "password", pass)
                .method(Connection.Method.POST)
                .execute();
        Document doc = res.parse();
        Log.v("APO.login", "Received web page:\n"+doc.toString());

        sessionId = res.cookie("PHPSESSID");
        Log.i("APO.login", "Using PHPSESSID: "+sessionId);

        if (!validateDoc(doc)) throw new IllegalArgumentException("Incorrect Login");
        return doc;
    }

    public static Document getPage (String page) throws IllegalArgumentException, IOException {
        Log.i("APO.getPage", "Using PHPSESSID: "+sessionId);
        Document doc = Jsoup.connect(page).cookie("PHPSESSID", sessionId).get();

        if (!validateDoc(doc)) throw new IllegalArgumentException("Incorrect Login");
        return doc;
    }

    protected static boolean validateDoc(Document doc) {
        Elements elements = doc.getElementsByClass("content-header");
        return !elements.html().contains("Log In");
    }

    public static void logout() throws IOException {
        Log.i("APO.logout", "Using PHPSESSID: "+sessionId);
        UserLogoutTask logout = new UserLogoutTask();
        logout.execute((Void) null);
    }

    // Not sure if making this static was best practice
    static class UserLogoutTask extends AsyncTask<Void, Void, Void> {
        UserLogoutTask() { }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Jsoup.connect(LOGOUT_PAGE).cookie("PHPSESSID", sessionId).get();
            } catch (IOException e) {
                // Connection to APO Online failed
            } finally {
                sessionId = null;

            }
            return null;
        }
    }
}
