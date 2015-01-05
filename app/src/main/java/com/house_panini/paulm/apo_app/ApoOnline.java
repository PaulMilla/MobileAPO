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

    /**
     * The way APO Online is implemented there is no "login page". Instead the
     * server will throw out a login page to any url if it determines the
     * PHPSESSID cookie is either outdated or unauthorized.
     *
     * Due to this it might be worth looking into merging login() and getPage()
     *
     * @param email                         Clear text email address
     * @param pass                          Clear text password
     * @return                              Returns org.jsou.nodes.Document,
     *                                      jsoup's representation of a web page
     * @throws IllegalArgumentException     thrown to signal caller of an invalid login.
     * @throws IOException                  thrown when connection to apoonline.org can't be established
     */
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

    /**
     * Checks the document for the "Log In" prompt.
     *
     * We don't want to check for the "password incorrect" prompt because
     * an expired cookie will also bring up the same prompt.
     * @param doc       The jsoup document to be checked
     * @return          False if we've been logged out, true otherwise
     */
    protected static boolean validateDoc (Document doc) {
        Elements elements = doc.getElementsByClass("content-header");
        return !elements.html().contains("Log In");
    }

    /**
     * Uses Jsoup to parse through the provided document for requirements and
     * hyperlinks for options. Results are then stored as a JSONObject into
     * the class static variable requirements. Due to the fragile nature of
     * this, this function is instead protected. Other classes instead
     * must call getRequirements().
     *
     * Note: While we could essentially hard code the url for requirements,
     * the login procedure already returns the requirements page. As such,
     * it is more efficient to pass it here than it is to make another
     * connection to apoonline.org
     *
     * @param doc   The jsoup document to be parsed
     */
    protected static void parseRequirements (Document doc) {
        requirements = new JSONObject();
        Element reqs = doc.select("div.content-body").first();
        Iterator<Element> iterator = reqs.children().iterator();
        while (iterator.hasNext()) {
            //Header
            Element header = iterator.next();
            //Update link is the same as View Detailed Records!

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
                String[] fraction = span.text().split(" of ");
                block.put("progress", fraction[0]);
                block.put("max", fraction[1]);
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

    /**
     * Getter method for the cached requirements json
     * @return  JSONObject representation of the previous pareRequirements()
     *          or null if it hasn't been filled yet
     */
    public static JSONObject getRequirements() {
        return requirements != null ? requirements : new JSONObject();
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
