package com.house_panini.paulm.apo_app;

import android.util.Log;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;

public class ApoOnline {
    static String sessionId;

    public static Document login (String email, String pass) throws IllegalArgumentException, IOException {
        Log.d("APO.login", "Attempting to connect to apo online!");
        Connection.Response res = Jsoup.connect("http://apoonline.org/alpharho/memberhome.php")
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
}
