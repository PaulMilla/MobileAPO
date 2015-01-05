package com.house_panini.paulm.apo_app;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class RequirementsFragment extends Fragment {

    public RequirementsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_requirements, container, false);

        JSONObject json = ApoOnline.getRequirements();
        LinearLayout myLayout = (LinearLayout) rootView.findViewById(R.id.requirements_layout);

        Iterator<String> iterator = json.keys();
        while (iterator.hasNext()) {
            final String nextTitle = iterator.next();
            View card = View.inflate(getActivity(), R.layout.requirement_card, null);

            TextView title_text = (TextView) card.findViewById(R.id.title_text);
            title_text.setText(nextTitle);

            //BUG: Rotating device displays incorrectly displays the progress bar
            ProgressBar progressBar = (ProgressBar) card.findViewById(R.id.progress_bar);
            try {
                JSONObject value = json.getJSONObject(nextTitle);
                int max = value.getInt("max");
                int progress = value.getInt("progress");
                progressBar.setMax(max);
                progressBar.setProgress(progress);
                //TODO: Display "progress/max" as fraction or "progress of max"
            } catch (JSONException e) {
//                throw new RuntimeException(e);
            }

            card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //TODO: Something when clicked
                    String text = nextTitle+" pressed!";
                    Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
                }
            });

            myLayout.addView(card);
        }

        return rootView;
    }
}
