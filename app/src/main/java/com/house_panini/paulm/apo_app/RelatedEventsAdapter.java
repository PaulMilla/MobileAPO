package com.house_panini.paulm.apo_app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.house_panini.paulm.apo_app.RelatedEventsFragment.Event;

import java.util.List;

public class RelatedEventsAdapter extends ArrayAdapter<Event> {

    // declaring our ArrayList of items
    private List<Event> objects;

    /* Here we must override the constructor for ArrayAdapter
     * the only variable we care about now is ArrayList<Item> objects,
     * because it is the list of objects we want to display.
     */
    public RelatedEventsAdapter (Context context, List<Event> objects) {
        //TODO: Figure out why we need this hardcoded simple_list_item_1 here
        super(context, android.R.layout.simple_list_item_1, objects);
        this.objects = objects;
    }

    /* We are overriding the getView method here - this is what defines how each
     * list item will look.
     */
    public View getView(int position, View convertView, ViewGroup parent) {

        // assign the view we are converting to a local variable
        View v = convertView;

        // first check to see if the view is null. if so, we have to inflate it.
        // to inflate it basically means to render, or show, the view.
        if (v == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.relatedevents_list_item, null);
        }

		/*
		 * Recall that the variable position is sent in as an argument to this method.
		 * The variable simply refers to the position of the current object in the list. (The ArrayAdapter
		 * iterates through the list we sent it)
		 *
		 * Therefore, i refers to the current Item object.
		 */
        Event i = objects.get(position);

        if (i != null) {

            // This is how you obtain a reference to the TextViews.
            // These TextViews are created in the XML files we defined.

            TextView date       = (TextView) v.findViewById(R.id.date);
            TextView event_name = (TextView) v.findViewById(R.id.event_name);

            // check to see if each individual textview is null.
            // if not, assign some text!
            if (date != null){
                date.setText(i.month+" "+i.date);
            }
            if (event_name != null) {
                event_name.setText(i.toString());
            }
        }

        // the view must be returned to our activity
        return v;

    }

}