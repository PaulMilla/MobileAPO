package com.house_panini.paulm.apo_app;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import org.json.JSONException;

public class RequirementsPage extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requirements_page);
        if (ApoOnline.sessionId == null) { launchLogin(); return;}
        createUI();
    }

    private void createUI() {
        TextView textView = (TextView) findViewById(R.id.requirements_text);
//        textView.setText("PHPSESSID: "+ApoOnline.sessionId);
        try {
            textView.setText("JSONObject: " + ApoOnline.getRequirements().toString(2));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_requirements_page, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                return true;
            case R.id.action_logout:
                ApoOnline.logout();
                launchLogin();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void launchLogin() {
        Intent myIntent = new Intent(RequirementsPage.this, LoginActivity.class);
        RequirementsPage.this.startActivity(myIntent);
        finish();
    }
}
