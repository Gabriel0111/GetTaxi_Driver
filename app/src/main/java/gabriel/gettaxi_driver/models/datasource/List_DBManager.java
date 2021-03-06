package gabriel.gettaxi_driver.models.datasource;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import gabriel.gettaxi_driver.controller.AllTrips;
import gabriel.gettaxi_driver.controller.MainActivity;
import gabriel.gettaxi_driver.models.backend.DB_Manager;
import gabriel.gettaxi_driver.models.backend.GetTaxiConst;
import gabriel.gettaxi_driver.models.entities.ClientRequest;
import gabriel.gettaxi_driver.models.entities.ClientRequestStatus;
import gabriel.gettaxi_driver.models.entities.Driver;

import static android.content.Context.LOCATION_SERVICE;

public class List_DBManager implements DB_Manager {

    private static ArrayList<Driver> drivers;
    private static ArrayList<ClientRequest> clientRequests;
    private DatabaseReference driversRef, clientsRef;

    public static Driver currentDriver;
    public static ClientRequest currentClient;
    public static Location driverLocation;

    public static String modeList = "";

    /**
     * In the constructor, initializes now the lists
     */
    public List_DBManager()
    {
        drivers = new ArrayList<>();
        clientRequests = new ArrayList<>();

        driversRef = FirebaseDatabase.getInstance().getReference(GetTaxiConst.DriverConst.DRIVERS);
        clientsRef = FirebaseDatabase.getInstance().getReference(GetTaxiConst.ClientConst.CLIENTS);

        initializeLists();
    }

    private void initializeLists() {

        driversRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("NewApi")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                drivers.clear();
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                for (DataSnapshot child: children) {
                    Driver d = child.getValue(Driver.class);
                    drivers.add(d);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        clientsRef.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NewApi")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                clientRequests.clear();
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();

                for (DataSnapshot child: children) {
                    ClientRequest cr = child.getValue(ClientRequest.class);

                    if (cr.getTravelTime() == null)
                        cr.setTravelTime("0");

                    clientRequests.add(cr);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void refreshLists() {
        initializeLists();
    }

    @Override
    public void notifyToClientList(final NotifyDataChange<ClientRequest> notifyDataChange)
    {
        clientsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s)
            {
                ClientRequest c = dataSnapshot.getValue(ClientRequest.class);
                clientRequests.add(c);
                notifyDataChange.OnDataAdded(c);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s)
            {
                ClientRequest clientChanged = dataSnapshot.getValue(ClientRequest.class);
                int i = 0;
                for (ClientRequest client : clientRequests)
                {
                    if(client.getPhoneNumber().equals(clientChanged.getPhoneNumber()))
                    {
                        clientRequests.set(i, client);
                        break;
                    }
                    i++;
                }

                notifyDataChange.OnDataChanged(clientChanged);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                notifyDataChange.OnFailure(databaseError.toException());
            }
        });
    }

    @Override
    public void stopNotifyToClientList() {

    }

    //region OTHERS

    public Driver getCurrentDriver()
    {
        return currentDriver;
    }

    public void setCurrentDriver(Driver driver)
    {
        currentDriver = driver;
    }

    public List<ClientRequest> getHistoryClients()
    {
        List<ClientRequest> result = new ArrayList<>();
        for (ClientRequest c : clientRequests)
        {
            if (!c.getDriverID().equals("0") && c.getDriverID().equals(currentDriver.getId()))
                result.add(c);
        }

        return result;
    }

    @Override
    public int getEarnDriver() {
        int earn = 0;
        for (ClientRequest c : clientRequests)
        {
            if (c.getClientRequestStatus() == ClientRequestStatus.ENDED && c.getDriverID().equals(currentDriver.getId()))
                earn += Double.valueOf(c.getTravelPrice());
        }

        return earn;
    }

    @Override
    public void addDriver(Driver driver) {}

    @Override
    public void updateDriver(Driver driver) {
        int positionFormerDriver = getPositionDriver(driver);
        if (positionFormerDriver != -1)
        {
            drivers.remove(positionFormerDriver);
            addDriver(driver);
        }
    }

    @Override
    public void updateClientRequest(ClientRequest clientRequest) { }

    public ArrayList<Driver> getDrivers() {
        return drivers;
    }

    @Override
    public void setDrivers(ArrayList<Driver> drivers) {
        List_DBManager.drivers = drivers;
    }


    public List<ClientRequest> getClientRequests() {
        return clientRequests;
    }

    @Override
    public void setClientRequests(ArrayList<ClientRequest> clientRequests) {
        this.clientRequests = clientRequests;
    }

    public int getPositionDriver(Driver driver)
    {
        int position = -1;

        for (Driver d : drivers)
        {
            if (d.getId().equals(driver.getId()))
                position = drivers.indexOf(d);
        }

        return position;
    }

    public int getPositionClientRequest(ClientRequest clientRequest)
    {
        int position = -1;

        for (ClientRequest cr : clientRequests)
        {
            if (cr.getPhoneNumber().equals(clientRequest.getPhoneNumber()))
                position = drivers.indexOf(cr);
        }

        return position;
    }

    public void calculPrice(double distance, ClientRequest clientRequest)
    {
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        df.setDecimalSeparatorAlwaysShown(true);

        double priceTakingInCharge = 1.20 * 4.2;
        double priceAKm = 1.05 * 4.2;

        double result = priceTakingInCharge +
                (priceAKm * distance/1000);

        clientRequest.setTravelPrice(String.valueOf(df.format(result)));
    }

    @Override
    public void setMode(String mode) {
        modeList = mode;
    }

    @Override
    public String getMode() {
        return modeList;
    }

    //endregion
}
