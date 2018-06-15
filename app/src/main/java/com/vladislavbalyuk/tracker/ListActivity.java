package com.vladislavbalyuk.tracker;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class ListActivity extends AppCompatActivity {

    Menu optionsMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_list, menu);

        optionsMenu = menu;
        ListActivityFragment fragment = (ListActivityFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_list);
        optionsMenu.getItem(0).setVisible(fragment.isMenuItemVisible());

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_remove) {
            ListActivityFragment fragment = (ListActivityFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_list);
            fragment.deleteEnabled();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void setItemMenuVisible(boolean visible){
        optionsMenu.getItem(0).setVisible(visible);
    }


}
