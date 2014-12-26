package com.house_panini.paulm.apo_app;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.io.IOException;


public class RequirementsPage extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        TODO: Uncomment when done using example html
//        if (ApoOnline.sessionId == null) { launchLogin(); }
        setContentView(R.layout.activity_requirements_page);
        TextView textView = (TextView) findViewById(R.id.requirements_text);
        textView.setText("PHPSESSID: "+ApoOnline.sessionId);
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
                try {
                    ApoOnline.logout();
                } catch (IOException e) {
                    // Couldn't connect to APO Online
                } finally {
                    launchLogin();
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    /* BUG: Logging in, pressing the "Log In" button, pressing the back
     * button to exit the application, and opening the application again will
     * try to load the RequirementsPage again.
     */
    private void launchLogin() {
        Intent myIntent = new Intent(RequirementsPage.this, LoginActivity.class);
        RequirementsPage.this.startActivity(myIntent);
        finish();
    }
}
