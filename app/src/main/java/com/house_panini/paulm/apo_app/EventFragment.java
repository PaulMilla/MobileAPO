package com.house_panini.paulm.apo_app;

import android.app.Activity;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link EventFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class EventFragment extends Fragment {

    private static final String ARG_URL = "url";
    private String url;
    private OnFragmentInteractionListener mListener;
    private FragmentActivity myContext;

    // Required empty public constructor
    public EventFragment() { }

    /**
     * Factory method for a new EventFragment
     * @param href
     * @return
     */
    public static EventFragment newInstance(String href) {
        EventFragment fragment = new EventFragment();
        Bundle args = new Bundle();
        args.putString(ARG_URL, href);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_event, container, false);
        if(getArguments() != null) {
            url = getArguments().getString(ARG_URL);
            new ParseEventTask(url, rootView).execute((Void) null);
        }
        // Inflate the layout for this fragment
        return rootView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        myContext = (FragmentActivity) activity;
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

    class ParseEventTask extends AsyncTask<Void, Void, Void> {
        String url;
        View rootView;
        JSONObject eventInfo;

        public ParseEventTask(String _url, View _rootView) {
            url = _url;
            rootView = _rootView;
        }

        @Override
        protected Void doInBackground(Void... params) {
            eventInfo = ApoOnline.getEventInfo(url);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            //TODO: Find the correct way to hook everything up
            TextView event_date = (TextView) rootView.findViewById(R.id.event_date);
            TextView event_description = (TextView) rootView.findViewById(R.id.event_description);
            TextView event_location = (TextView) rootView.findViewById(R.id.event_location);
            TextView lock_date = (TextView) rootView.findViewById(R.id.event_lock);
            TextView close_date = (TextView) rootView.findViewById(R.id.event_close);
            TextView coord_name = (TextView) rootView.findViewById(R.id.event_coord_name);
            TextView coord_phone = (TextView) rootView.findViewById(R.id.event_coord_phone);
            TextView coord_email = (TextView) rootView.findViewById(R.id.event_coord_email);
            //TODO: Attendees
            try {
                event_date.setText(eventInfo.getString("date"));
                event_description.setText(eventInfo.getString("description"));
                event_location.setText(eventInfo.getString("location"));
                lock_date.setText(eventInfo.getString("lockDate"));
                close_date.setText(eventInfo.getString("closeDate"));
                if(eventInfo.has("coordName")) {
                    coord_name.setText(eventInfo.getString("coordName"));
                    coord_phone.setText(eventInfo.getString("coordPhone"));
                    coord_email.setText(eventInfo.getString("coordEmail"));
                }
                if(eventInfo.has("tags")) {
                    LinearLayout tags = (LinearLayout) rootView.findViewById(R.id.event_tags);
                    JSONArray allTags = eventInfo.getJSONArray("tags");
                    for(int i = 0; i < allTags.length(); ++i) {
                        String tagName = allTags.getString(i);
                        TextView newTag = new TextView(myContext);
                        newTag.setText(tagName);
                        tags.addView(newTag);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
