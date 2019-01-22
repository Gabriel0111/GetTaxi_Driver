package gabriel.gettaxi_driver.controller;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;
import java.util.Objects;

import gabriel.gettaxi_driver.R;
import gabriel.gettaxi_driver.models.backend.GetTaxiConst;
import gabriel.gettaxi_driver.models.datasource.List_DBManager;
import gabriel.gettaxi_driver.models.entities.ClientRequest;
import gabriel.gettaxi_driver.models.entities.ClientRequestStatus;
import gabriel.gettaxi_driver.models.entities.Driver;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class PresentationClient extends AppCompatActivity implements View.OnClickListener
{
    private ClientRequest chosenClientRequest;

    private TextView clientStatus;

    private Button btnCallClient;
    private Button btnEmailClient;
    private Button btnEndTrip;
    private Button btnAddToContact;

    private Drawable iconInTreatment;
    private Drawable iconEnded;
    private ImageView clientIconStatus;

    private Toolbar toolbar;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (chosenClientRequest.getClientRequestStatus() == ClientRequestStatus.AWAITING)
            getMenuInflater().inflate(R.menu.menu_presentation_client, menu);
        else
            getMenuInflater().inflate(R.menu.menu_empty, menu);

       return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getActionBar().setDisplayHomeAsUpEnabled(false);

        initializesFont();
        setContentView(R.layout.activity_presentation_client);

        findViews();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private void initializesFont() {
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("font/GoogleSans.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());
    }

    void findViews()
    {
        Bundle bundle = getIntent().getExtras();
        chosenClientRequest = (ClientRequest) bundle.get(GetTaxiConst.DriverConst.CHOSEN_CLIENT);

        TextView clientName = findViewById(R.id.presentC_nameClient);
                 clientStatus = findViewById(R.id.presentC_status);
        TextView clientSourceAddress = findViewById(R.id.presentC_sourceAddress);
        TextView clientDestinationAddress = findViewById(R.id.presentC_destinationAddress);
        TextView clientTravelTime = findViewById(R.id.presentC_travelTime);
        TextView clientDepartureTime = findViewById(R.id.presentC_departureTime);
        TextView clientArrivalTime = findViewById(R.id.presentC_arrivalTime);
                 clientIconStatus = findViewById(R.id.presentC_iconState);
        TextView clientTravelDistance = findViewById(R.id.presentC_travelDistance);
        TextView clientTravelPrice = findViewById(R.id.presentC_price);
                 btnCallClient = findViewById(R.id.all_trips_btnCallClient);
                 btnEmailClient = findViewById(R.id.all_trips_btnEmailClient);
                 btnEndTrip = findViewById(R.id.all_trips_btnEndTrip);
                 btnAddToContact = findViewById(R.id.all_trips_btnAddToContact);

        toolbar = findViewById(R.id.toolbar);

        Drawable iconAwaiting = getResources().getDrawable(R.drawable.ic_trip_awaiting);
        iconInTreatment = getResources().getDrawable(R.drawable.ic_trip_treatment);
        iconEnded = getResources().getDrawable(R.drawable.ic_trip_ended);

        clientName.setText(chosenClientRequest.getClientName());
        clientSourceAddress.setText(chosenClientRequest.getSourceAddress());
        clientDestinationAddress.setText(chosenClientRequest.getDestinationAddress());
        clientDepartureTime.setText(getStringTime(chosenClientRequest.getDepartureTime()));
        clientArrivalTime.setText(getStringTime(chosenClientRequest.getArrivalTime()));
        btnCallClient.setOnClickListener(this);
        btnEmailClient.setOnClickListener(this);
        btnEndTrip.setOnClickListener(this);
        btnAddToContact.setOnClickListener(this);

        if (chosenClientRequest.getClientRequestStatus() == ClientRequestStatus.AWAITING)
        {
            btnCallClient.setVisibility(View.INVISIBLE);
            btnEmailClient.setVisibility(View.INVISIBLE);
            btnEndTrip.setVisibility(View.INVISIBLE);
            btnAddToContact.setVisibility(View.INVISIBLE);
        }

        switch (chosenClientRequest.getClientRequestStatus())
        {
            case AWAITING:
                clientStatus.setText("Awaiting");
                clientIconStatus.setImageDrawable(iconAwaiting);
                break;
            case IN_TREATMENT:
                clientStatus.setText("In Treatment");
                clientIconStatus.setImageDrawable(iconInTreatment);
                break;
            case ENDED:
                clientStatus.setText("Ended");
                clientIconStatus.setImageDrawable(iconEnded);
                break;
        }

        clientTravelPrice.setText(chosenClientRequest.getTravelPrice() + " ₪");

        int travelDistance = (int) chosenClientRequest.getTravelDistance();
        String result = travelDistance > 1000 ? travelDistance/1000 + " km" : travelDistance + " m";
        clientTravelDistance.setText(result);

        CalculTravelTime task =  new CalculTravelTime();
        task.execute(chosenClientRequest, clientTravelTime);
    }

    String getStringTime(Date date)
    {
        return String.valueOf(date.getHours()) + "h" +
                ((date.getMinutes() != 0) ? (String.valueOf(date.getMinutes())) : "");
    }


    @Override
    public void onClick(View v)
    {
        Intent intent;
        ClientRequest currentClient = chosenClientRequest;
        if (v == btnCallClient)
        {
            intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + currentClient.getPhoneNumber()));
            startActivity(intent);
        }
        else if (v == btnEmailClient)
        {
            String body = "Hello " + currentClient.getClientName() + " I'm " +
                    List_DBManager.currentDriver.getFirstName() + " " +  List_DBManager.currentDriver.getLastName() + '\n' +
                    "I'ld like to tell you something:\n";
            intent = new Intent(Intent.ACTION_SENDTO)
                    .setType("text/plain")
                    .putExtra(Intent.EXTRA_EMAIL, currentClient.getEmail())
                    .putExtra(Intent.EXTRA_SUBJECT, "Your Travel with GetTaxi")
                    .putExtra(Intent.EXTRA_TEXT, body);
            startActivity(Intent.createChooser(intent, "Send Email"));
        }
        else if (v == btnEndTrip)
        {
            List_DBManager.currentClient = chosenClientRequest;
            chosenClientRequest.setClientRequestStatus(ClientRequestStatus.ENDED);

            DatabaseReference changedClient = FirebaseDatabase.getInstance().getReference(GetTaxiConst.ClientConst.CLIENTS + "/" + chosenClientRequest.getPhoneNumber());
            changedClient.setValue(chosenClientRequest).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    createSimpleAlertDialog("You ended the trip of " + chosenClientRequest.getClientName());
                }
            });

            clientStatus.setText("Ended");
            clientIconStatus.setImageDrawable(iconEnded);

            btnCallClient.setVisibility(View.INVISIBLE);
            btnEmailClient.setVisibility(View.INVISIBLE);
            btnEndTrip.setVisibility(View.INVISIBLE);
            btnAddToContact.setVisibility(View.INVISIBLE);
        }
    }

    private void createSimpleAlertDialog(String message)
    {
        AlertDialog.Builder creationOK = new AlertDialog.Builder(PresentationClient.this);
        creationOK.setTitle(R.string.app_name);
        creationOK.setMessage(message);
        creationOK.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                PresentationClient.this.finish();
            }
        });
        creationOK.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_favorite)
        {
            List_DBManager.currentClient = chosenClientRequest;
            chosenClientRequest.setDriverID(List_DBManager.currentDriver.getId());
            chosenClientRequest.setClientRequestStatus(ClientRequestStatus.IN_TREATMENT);

            DatabaseReference changedClient = FirebaseDatabase.getInstance().getReference(GetTaxiConst.ClientConst.CLIENTS + "/" + chosenClientRequest.getPhoneNumber());
            changedClient.setValue(chosenClientRequest).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    createSimpleAlertDialog("Congratulation. You are now driving the client " + chosenClientRequest.getClientName());
                }
            });

            clientStatus.setText("In Treatment");
            clientIconStatus.setImageDrawable(iconInTreatment);

            btnCallClient.setVisibility(View.VISIBLE);
            btnEmailClient.setVisibility(View.VISIBLE);
            btnEndTrip.setVisibility(View.VISIBLE);
            btnAddToContact.setVisibility(View.VISIBLE);

            sendConfirmationSMS();
        }

            return true;
        }

    private void sendConfirmationSMS()
    {
        int permissionCheck = ContextCompat.checkSelfPermission(PresentationClient.this, Manifest.permission.SEND_SMS);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            SmsManager smsManager = SmsManager.getDefault();

            Driver driver = List_DBManager.currentDriver;
            String message = "The Driver " + driver.getFirstName() + " " + driver.getLastName() +
                    "\nPhone Number: " + driver.getPhoneNumber() + " will take you in charge in a few minutes. Be Patient !!";

            smsManager.sendTextMessage(chosenClientRequest.getPhoneNumber(), null, message, null, null);
            Toast.makeText(PresentationClient.this, "You send a SMS to " + chosenClientRequest.getClientName(), Toast.LENGTH_SHORT).show();
        }
        else
        {
            //Toast.makeText(context,"you haven't permission",Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions((Activity) PresentationClient.this, new String[]{Manifest.permission.SEND_SMS},0);
        }
    }

}

