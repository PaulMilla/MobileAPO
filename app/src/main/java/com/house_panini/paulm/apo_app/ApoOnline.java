package com.house_panini.paulm.apo_app;

import android.os.AsyncTask;
import android.util.Log;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashMap;

public class ApoOnline {
    //TODO: Update these via values/strings.xml
    private static final String APO_ROOT = "http://apoonline.org/alpharho/";
    private static final String HOME_PAGE = APO_ROOT+"memberhome.php";
    private static final String LOGOUT_PAGE = APO_ROOT+"memberhome.php?action=logout";

    static String sessionId;
    static HashMap<String, String> requirements;

    public static Document login (String email, String pass) throws IllegalArgumentException, IOException {
        Log.v("APO.login", "Attempting to connect to "+HOME_PAGE);
        Connection.Response res = Jsoup.connect(HOME_PAGE)
                .data("email", email, "password", pass)
                .method(Connection.Method.POST)
                .execute();
        Document doc = res.parse();

        sessionId = res.cookie("PHPSESSID");
        Log.d("APO.login", "Using PHPSESSID: "+sessionId);

        if (!validateDoc(doc)) throw new IllegalArgumentException("Incorrect Login");
        parseRequirements(doc);
        return doc;
    }

    public static Document getPage (String page) throws IllegalArgumentException, IOException {
        Log.d("APO.getPage", "Using PHPSESSID: " + sessionId);
        Document doc = Jsoup.connect(page).cookie("PHPSESSID", sessionId).get();

        if (!validateDoc(doc)) throw new IllegalArgumentException("Incorrect Login");
        return doc;
    }

    protected static boolean validateDoc (Document doc) {
        Elements elements = doc.getElementsByClass("content-header");
        return !elements.html().contains("Log In");
    }

    protected static void parseRequirements (Document doc) {
        HashMap<String, String> results = new HashMap<>();
        // TODO: Parse requirements and store it in results
        results.put("Voe Credit Progress", "0 of 1");
        results.put("Service to Nation Hr. Progress", "0 of 2");
        results.put("Service to Community Hr. Progress", "0 of 2");
        results.put("Service to Campus Hr. Progress", "2 of 2");
        results.put("Service to Fraternity Hr. Progress", "0 of 2");
        results.put("Leadership Event Credit Progress", "0 of 4");
        results.put("Blood Drive Hour Progress", "0 of 2");
        results.put("Friendship Event Progress", "1.5 of 5");
        results.put("Flag Event Progress", "0 of 3");
        results.put("Service Hour Progress", "2 of 35");
        results.put("Chapter Meeting Progress", "2 of 6");
        results.put("Dues Progress", "0 of 136");
        requirements = results;
    }

    public static HashMap<String, String> getRequirements() {
        return requirements;
    }

    public static void logout() {
        Log.d("APO.logout", "Using PHPSESSID: "+sessionId);
        new UserLogoutTask().execute((Void) null);
    }

    // Not sure if making this static is best practice, but oh well
    static class UserLogoutTask extends AsyncTask<Void, Void, Void> {
        UserLogoutTask() { }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Jsoup.connect(LOGOUT_PAGE).cookie("PHPSESSID", sessionId).get();
            } catch (IOException e) {
                // Connection to APO Online failed, but we don't really care
            } finally {
                sessionId = null;
            }
            return null;
        }
    }
}
