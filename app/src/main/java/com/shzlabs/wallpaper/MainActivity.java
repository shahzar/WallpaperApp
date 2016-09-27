package com.shzlabs.wallpaper;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.crashlytics.android.Crashlytics;
import com.shzlabs.wallpaper.data.model.OrderSelectedEvent;

import org.greenrobot.eventbus.EventBus;

import butterknife.ButterKnife;
import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    Context ctx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ButterKnife.bind(this);
        Fabric.with(this, new Crashlytics());
        ctx = this;

        // Drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        switch (PreferenceManager.getDefaultSharedPreferences(ctx).getString("default_display_type", "1")){
            case "0": {
                // Latest
                navigationView.getMenu().getItem(1).setChecked(true);
                break;
            }
            case "1": {
                // Popular
                navigationView.getMenu().getItem(0).setChecked(true);
                break;
            }

        }

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        // TODO Remove this, added for testing purpose.
        /*if (id == R.id.action_start_fragment) {
            ArrayList<Wallpaper> wallpaperList = new ArrayList<>();
            ImageSliderFragment fragment = ImageSliderFragment.newInstance(0);
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            fragment.show(ft, "ImageSliderFragment");
            return true;
        }*/


        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_popular) {
            orderBy(MainFragment.ORDER_BY_POPULAR);
        } else if (id == R.id.nav_latest) {
            orderBy(MainFragment.ORDER_BY_LATEST);
        } else if (id == R.id.nav_share) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            String content = Uri.encode("*") + getResources().getString(R.string.app_name) + Uri.encode("*") +
                            "\n" + getResources().getString(R.string.app_share_content) + "\n\n" +
                            getResources().getString(R.string.app_link);
            intent.putExtra(Intent.EXTRA_TEXT, content);
            startActivity(Intent.createChooser(intent, "Share app"));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void orderBy(String order) {
        if(!MainFragment.order.equals(order)){
            MainFragment.order = order;
            EventBus.getDefault().post(new OrderSelectedEvent(order));
        }
    }
}
