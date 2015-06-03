package com.house_panini.paulm.apo_app;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ApoOnline {
    //TODO: Update these via values/strings.xml
    private static final String APO_ROOT = "http://apoonline.org/alpharho/";
    private static final String HOME_PAGE = APO_ROOT+"memberhome.php";
    private static final String LOGOUT_PAGE = APO_ROOT+"memberhome.php?action=logout";

    public static final String OPTION_RECORDS = "View Detailed Records";
    public static final String OPTION_STANDINGS = "View Current Standings";
    public static final String OPTION_EVENTS = "View Related Events";

    static String sessionId;
    static JSONObject requirements;
    static Map<String, List<RelatedEventsFragment.Event>> relatedEvents = new HashMap<>();

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
                    options.put(button.text(), APO_ROOT+button.attr("href"));
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

    /**
     * Uses Jsoup to parse through the provided document for related events.
     * Results are then cached into relatedEvents for future use.
     * @param title Requirements title the related events will be stored under
     * @param url   URL to the page containing the related events
     * @return      The newly created list containing all related events
     */
    protected static List<RelatedEventsFragment.Event> parseRelated(String title, String url) {
        List<RelatedEventsFragment.Event> list = new LinkedList<>();
        Document doc;
        try {
            doc = getPage(url);
        } catch (IOException e) {
            Log.e("parseRelated", "Couldn't connect to APO Online!");
            return list;
        }

        Element contentBody = doc.select("div.content-body").first();

//        if(contentBody.children().size() == 1) {
//            Log.d("parseRelated", contentBody.text());
//            return list;
//        }

        for (Element child : contentBody.children()) {
            Element event_title = child.select("div.calendar-title").first();
            Iterator<Element> data;
            try {
                data = event_title.children().iterator();
            } catch (NullPointerException e) {
                return list;
            }

            RelatedEventsFragment.Event event = new RelatedEventsFragment.Event();

            Element link = data.next();
            event.displayName = link.text();
            event.href = HOME_PAGE+link.attr("href");

            event.attending = data.next().ownText();

            Element tags = data.next();
            for (Element tag : tags.children()) {
                event.tags.add(tag.text());
            }

            event.weekday = child.select("div.calendar-imagebox-weekday").text();
            event.month = child.select("div.calendar-imagebox-month").text();
            event.date = child.select("div.calendar-imagebox-date").text();

            list.add(event);
        }

        relatedEvents.put(title, list);
        return list;
    }

    public static List<RelatedEventsFragment.Event> getRelated(String title, String url) {
        return getRelated(title, url, false);
    }

    /**
     * Public method to grab a list of related events. If there is no cached copy, if the cached
     * copy shows an empty list, or if the parameter forced is true then we return the output of
     * parseRelated(). Vice-versa, if we do have a non-empty cached copy and forced is false, we
     * return the cached copy instead of attempting to make another connection to apoonline.org.
     * @param title     Requirements title the related events will be stored under
     * @param url       URL to the page containing the related events
     * @param forced    Weather we want to force a connection to apoonline and refresh our cache
     * @return          A list of related events, parsed from the url string provided
     */
    public static List<RelatedEventsFragment.Event> getRelated(String title, String url, boolean forced) {
        if (!forced && relatedEvents.containsKey(title) && !relatedEvents.get(title).isEmpty()) {
            return relatedEvents.get(title);
        } else {
            return parseRelated(title, url);
        }
    }

    public static JSONObject getEventInfo(String url) {
        JSONObject json = new JSONObject();

        Document doc;
        try {
            doc = getPage(url);
        } catch (IOException e) {
            Log.e("getEvent", "Couldn't connect to APO Online!");
            return json;
        }

        Element ele;
        try { //JSONException
            //Parse Event Date & Time
            try {
                ele = doc.select("div [onmouseover=\"Tip('Event Date & Time')\"]")
                        .first().parent().nextElementSibling();
                json.put("date", ele.text());
            } catch (NullPointerException e) {
                json.put("date", "No Date & Time");
            }

            //Parse Event Location
            try {
                ele = doc.select("div [onmouseover=\"Tip('Event Location')\"]")
                        .first().parent().nextElementSibling();
                json.put("location", ele.text());
            } catch (NullPointerException e) {
                json.put("location", "No Event Location");
            }

            //Parse Event Information
            try {
                ele = doc.select("div [onmouseover=\"Tip('Event Information')\"]")
                        .first().parent().nextElementSibling();
                json.put("description", ele.html());
            } catch (NullPointerException e) {
                json.put("description", "No Event Information");
            }

            //Parse Sign-ups Lock
            try {
                ele = doc.select("div [onmouseover=\"Tip('Sign-ups Lock')\"]")
                        .first().parent().nextElementSibling();
                json.put("lockDate", ele.text());
            } catch (NullPointerException e) {
                json.put("lockDate", "No Lock Information");
            }

            //Parse Sign-ups Close
            try {
                ele = doc.select("div [onmouseover=\"Tip('Sign-ups Close')\"]")
                        .first().parent().nextElementSibling();
                json.put("closeDate", ele.text());
            } catch (NullPointerException e) {
                json.put("closeDate", "No Close Information");
            }

            //Parse Tags
            try {
                JSONArray tags = new JSONArray();
                ele = doc.select("div [onmouseover=\"Tip('Credit Towards Requirements')\"]")
                        .first().parent().nextElementSibling().child(0);
                for (Element element : ele.children()) {
                    tags.put(element.text());
                }
                json.put("tags", tags);
            } catch (NullPointerException e) {
                Log.e("getEvent", "No tags found");
            }

            //Parse Event Coordinator
            try {
                ele = doc.select("span:matches(^Event Coordinator:$)")
                        .first().nextElementSibling(); //skip icon

                //FUTURE: Link to user page
                ele = ele.nextElementSibling();
                json.put("coordName", ele.text());

                //FUTURE: Allow options to call/copy
                ele = ele.nextElementSibling();
                json.put("coordPhone", ele.text());

                ele = ele.nextElementSibling();
                json.put("coordEmail", ele.text());
            } catch (NullPointerException e) {
                Log.e("getEvent", "No Event Coordinator");
            }

            //Parse Attendees
            int shift = 0;
            Elements eles = doc.select("div#shift_"+shift);
            for(; !eles.isEmpty(); eles = doc.select("div#shift_"+ (++shift))) {
                JSONObject currentShift = new JSONObject();
                ele = eles.first().child(0);

                Element header = ele.child(0);
                Elements e;
                if(!(e = header.select("span")).isEmpty()) {
                    currentShift.put("shiftTime", e.text());
                }

                Element body = ele.child(1).select("table > tbody").first();
                Iterator<Element> row = body.children().iterator();
                JSONObject attendees = new JSONObject();
                while(row.hasNext()) {
                    Element r = row.next();
                    JSONObject attendee = new JSONObject();

                    String spot = r.child(0).text().charAt(0)+"";
                    String brother = r.child(1).child(0).text(); //FUTURE: Drivers
                    String phone = r.child(2).text();
                    String comment = r.child(3).text();
                    attendee.put("brother", brother);
                    attendee.put("phone", phone);
                    attendee.put("comment", comment);

                    attendees.put(spot, attendee);
                }
                currentShift.put("attendees", attendees);
                json.put("shift"+shift, currentShift);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return json;
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
                ApoOnline.getPage(LOGOUT_PAGE);
            } catch (Exception e) {
                // Connection to APO Online failed, but we don't really care
            } finally {
                sessionId = null;
            }
            return null;
        }
    }
}
