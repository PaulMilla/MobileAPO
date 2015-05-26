package com.house_panini.paulm.apo_app;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ListView;

import java.util.LinkedList;
import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class RelatedEventsFragment extends ListFragment {

    private static final String ARG_URL = "url";
    private static final String ARG_TITLE = "title";
    private String url;
    private String title;

    private OnFragmentInteractionListener mListener;


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public RelatedEventsFragment() { }

    /**
     * Apparently using factory methods is the Google way of constructing a
     * new Fragment rather than just have a constructor with parameters.
     * @param title The title/heading of the requirement
     * @param href  The http link for the related events page
     * @return      A new class with an attached bundle based off given parameters
     */
    public static RelatedEventsFragment newInstance(String title, String href) {
        RelatedEventsFragment fragment = new RelatedEventsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_URL, href);
        args.putString(ARG_TITLE, title);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            url = getArguments().getString(ARG_URL);
            title = getArguments().getString(ARG_TITLE);
            new UserRelatedEventsTask(title, url).execute((Void) null);
        }
    }

    @Override
    public void onAttach(Activity activity) {
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


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            mListener.onFragmentInteraction(ApoOnline.getRelated(title, url).get(position));
        }
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
        public void onFragmentInteraction(Event event);
    }


    class UserRelatedEventsTask extends AsyncTask<Void, Void, Void> {
        String url;
        String title;
        List<Event> relatedEvents;

        UserRelatedEventsTask(String _title, String _url) {
            url = _url;
            title = _title;
        }

        @Override
        protected Void doInBackground(Void... params) {
            relatedEvents = ApoOnline.getRelated(title, url);
            return null;
        }

        @Override
        protected void onPostExecute(final Void success) {
            // Always check isAdded() before calling getActivity(), setListAdapter(),
            // or setEmptyText() or risk having a NullPointerException if a user presses
            // the back button before the fragment finished loading.
            if(isAdded()) {
//                ArrayAdapter<Event> adapter = new ArrayAdapter<>(getActivity(),
//                        android.R.layout.simple_list_item_1, android.R.id.text1, relatedEvents);

                RelatedEventsAdapter adapter = new RelatedEventsAdapter(getActivity(), relatedEvents);
                setListAdapter(adapter);

                // Text to display when adapter is empty
                setEmptyText("No Related Events Found");
            }
        }
    }

    public static class Event {
        public String displayName;
        public String href;
        public String weekday;
        public String month;
        public String date;
        public String attending;
        public LinkedList<String> tags = new LinkedList<>();

        public Event () {}

        @Override
        public String toString() {
            return displayName;
        }
    }
}
