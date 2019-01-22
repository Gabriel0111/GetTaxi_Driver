package gabriel.gettaxi_driver.controller;

import android.Manifest;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.LinkedList;
import java.util.List;

import gabriel.gettaxi_driver.R;
import gabriel.gettaxi_driver.models.backend.DB_Manager;
import gabriel.gettaxi_driver.models.backend.DB_ManagerFactory;
import gabriel.gettaxi_driver.models.backend.Driver_Service;
import gabriel.gettaxi_driver.models.backend.GetTaxiConst;
import gabriel.gettaxi_driver.models.datasource.List_DBManager;
import gabriel.gettaxi_driver.models.entities.Driver;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class Welcome extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        AllTrips.OnFragmentInteractionListener,
        CurrentTrip.OnFragmentInteractionListener
{
    private Driver currentDriver;
    private DB_Manager dbManager = DB_ManagerFactory.getDB_Manager();

    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        initializesFont();
        setContentView(R.layout.activity_welcome);

        initializesNavigationDrawer();
    }

    void initializesFont()
    {
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("font/GoogleSans.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());
    }

    void initializesNavigationDrawer()
    {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        currentDriver = dbManager.getCurrentDriver();

        View headerEmail = navigationView.getHeaderView(0);
        TextView emailCurrentDriver = headerEmail.findViewById(R.id.nav_header_lblEmailDriver);
        emailCurrentDriver.setText(currentDriver.getEmail());
        TextView nameCurrentDriver = headerEmail.findViewById(R.id.nav_header_lblNameDriver);
        nameCurrentDriver.setText(currentDriver.getFirstName() + " " + currentDriver.getLastName());



        registerReceiver(
                new MyBroadcastReceiver(),
                new IntentFilter(GetTaxiConst.DriverConst.NEW_ORDER));

        startService(new Intent(getBaseContext(), Driver_Service.class));
    }


    @Override
    protected void attachBaseContext (Context newBase){
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public void onBackPressed () {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu){
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.welcome, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item){
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected (MenuItem item){
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_all_trips) {
            setTitle("All Waiting Trips");
            AllTrips allWTFragment = new AllTrips();

            dbManager.setMode(GetTaxiConst.ClientConst.WAITING_CLIENTS);

            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.content_welcome, allWTFragment, "all_trips");
            fragmentTransaction.commit();

        } else if (id == R.id.nav_current_trip) {
            setTitle("History Trips");
            AllTrips allWTFragment = new AllTrips();

            dbManager.setMode(GetTaxiConst.ClientConst.HISTORY_CLIENTS);

            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.content_welcome, allWTFragment, "all_trips");
            fragmentTransaction.commit();

        } else if (id == R.id.nav_exit) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(Welcome.this, MainActivity.class));

        } else if (id == R.id.nav_web) {
            startActivity(new Intent(Welcome.this, GetTaxiWeb.class));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onFragmentInteraction (Uri uri) {

    }
}
