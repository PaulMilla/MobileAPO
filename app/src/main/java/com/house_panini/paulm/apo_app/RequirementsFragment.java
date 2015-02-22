package com.house_panini.paulm.apo_app;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class RequirementsFragment extends Fragment {

    private FragmentActivity myContext;

    public RequirementsFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        myContext = (FragmentActivity) activity;
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_requirements, container, false);

        LinearLayout myLayout = (LinearLayout) rootView.findViewById(R.id.requirements_layout);
        JSONObject json = ApoOnline.getRequirements();

        Iterator<String> iterator = json.keys();
        while (iterator.hasNext()) {
            final String nextTitle = iterator.next();
            View card = View.inflate(getActivity(), R.layout.requirement_card, null);

            TextView title_text = (TextView) card.findViewById(R.id.title_text);
            title_text.setText(nextTitle);

            //BUG: Rotating device displays incorrectly displays the progress bar
            try {
                // Set the Progress Bar
                ProgressBar progressBar = (ProgressBar) card.findViewById(R.id.progress_bar);
                JSONObject value = json.getJSONObject(nextTitle);
                int max = value.getInt("max");
                int progress = value.getInt("progress");
                progressBar.setMax(max);
                progressBar.setProgress(progress);
                //TODO: Display "progress/max" as fraction or "progress of max"

                // Set the Options
                final JSONObject options = value.getJSONObject("options");
                card.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            // Get the link for Related Events
                            String href = options.getString(ApoOnline.OPTION_EVENTS);

                            // Create new fragment and transition
                            RelatedEventsFragment newFragment = RelatedEventsFragment.newInstance(nextTitle, href);
                            FragmentTransaction transaction = myContext.getSupportFragmentManager().beginTransaction();

                            // Replace whatever is in the fragment_container view with this fragment
                            transaction.replace(R.id.container, newFragment);
                            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                            transaction.addToBackStack(null);

                            // Commit the transaction
                            transaction.commit();
                        } catch (JSONException e) {
                            // Ex: "Dues Progress" doesn't have any options
                            Log.w("onClick", nextTitle+" has no '"+ApoOnline.OPTION_EVENTS+"' option");
                            Log.w("onClick", options.toString());
                        }
                    }
                });
            } catch (JSONException e) {
                Log.e("OnClickListener", e.toString());
            }

            myLayout.addView(card);
        }

        return rootView;
    }
}
