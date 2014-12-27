package com.house_panini.paulm.apo_app;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Iterator;

public class ApoOnline {
    //TODO: Update these via values/strings.xml
    private static final String APO_ROOT = "http://apoonline.org/alpharho/";
    private static final String HOME_PAGE = APO_ROOT+"memberhome.php";
    private static final String LOGOUT_PAGE = APO_ROOT+"memberhome.php?action=logout";

    static String sessionId;
    static JSONObject requirements;

    public static Document login (String email, String pass) throws IllegalArgumentException, IOException {
        Log.v("APO.login", "Attempting to connect to " + HOME_PAGE);
        Connection.Response res = Jsoup.connect(HOME_PAGE)
                .data("email", email, "password", pass)
                .method(Connection.Method.POST)
                .execute();
        Document doc = res.parse();

        sessionId = res.cookie("PHPSESSID");
        Log.d("APO.login", "Using PHPSESSID: " + sessionId);

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
        requirements = new JSONObject();
        Element reqs = doc.select("div.content-body").first();
        Iterator<Element> iterator = reqs.children().iterator();
        while (iterator.hasNext()) {
            //Header
            Element header = iterator.next();
            //update link same as View Detailed Records!
            //Element header_update = header.children().first();

            //Progress
            Element progressBarContainer = iterator.next().select("div > div").first();
            Iterator<Element> progressBar_iterator = progressBarContainer.children().iterator();
            Element progressBar = progressBar_iterator.next();
            Element span = progressBar_iterator.next().child(0);
            Elements infoBarButtons = progressBarContainer.nextElementSibling().select("a.infobarbutton");

            //Parsing
            try {
                JSONObject block = new JSONObject();
                block.put("percent", progressBar.attr("init-value"));
                block.put("fraction", span.text().replace(" of ","/"));
                JSONObject options = new JSONObject();
                for (Element button : infoBarButtons) {
                    options.put(button.text(), button.attr("href"));
                }
                block.put("options", options);
                requirements.put(header.ownText(), block);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static JSONObject getRequirements() {
        return requirements;
    }

    public static void logout() {
        Log.d("APO.logout", "Using PHPSESSID: " + sessionId);
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
