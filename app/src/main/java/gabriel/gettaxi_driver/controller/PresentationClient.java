package gabriel.gettaxi_driver.controller;

import android.Manifest;
import android.app.Activity;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.util.Log;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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

    ContentProvider contentProvider;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (chosenClientRequest.getClientRequestStatus() == ClientRequestStatus.AWAITING)
            getMenuInflater().inflate(R.menu.menu_presentation_client, menu);
        else
            getMenuInflater().inflate(R.menu.menu_empty, menu);

       return super.onCreateOptionsMenu(menu);
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

    @RequiresApi(api = Build.VERSION_CODES.M)
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

                 contentProvider = new ContentProvider();

        contentProvider.AccessContact();

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

        if (chosenClientRequest.getClientRequestStatus() == ClientRequestStatus.AWAITING
            || chosenClientRequest.getClientRequestStatus() == ClientRequestStatus.ENDED)
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
            intent = new Intent(Intent.ACTION_SEND)
                    .setType("message/rfc822")
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
        else if (v == btnAddToContact)
        {
           contentProvider.SaveContact();
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

    public class ContentProvider {

        final static String LOG_TAG = "GabrielTag";
        final private int REQUEST_MULTIPLE_PERMISSIONS = 124;

        public void SaveContact() {

            //https://developer.android.com/reference/android/provider/ContactsContract.RawContacts
            ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
            int rawContactInsertIndex = ops.size();

            ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                    .build());
            //INSERT NAME
            ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID,rawContactInsertIndex)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, chosenClientRequest.getClientName()) // Name of the person
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME, chosenClientRequest.getClientName()) // Name of the person
                    .build());
            //INSERT MOBILE
            ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID,   rawContactInsertIndex)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, chosenClientRequest.getPhoneNumber()) // Number of the person
                    .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                    .build()); //
        /*//INSERT FAX
        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID,   rawContactInsertIndex)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, szFax)
                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_FAX_WORK)
                .build()); //*/
            //INSERT EMAIL
            ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID,   rawContactInsertIndex)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Email.DATA, chosenClientRequest.getEmail())
                    .withValue(ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.TYPE_WORK)
                    .build()); //
      /*//INSERT ADDRESS: FULL, STREET, CITY, REGION, POSTCODE, COUNTRY
        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                .withValue(ContactsContract.Data.MIMETYPE,ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS, m_szAddress)
                .withValue(ContactsContract.CommonDataKinds.StructuredPostal.TYPE, ContactsContract.CommonDataKinds.StructuredPostal.TYPE_WORK)
                .withValue(ContactsContract.CommonDataKinds.StructuredPostal.STREET, m_szStreet)
                .withValue(ContactsContract.CommonDataKinds.StructuredPostal.CITY, m_szCity)
                .withValue(ContactsContract.CommonDataKinds.StructuredPostal.REGION, m_szState)
                .withValue(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE, m_szZip)
                .withValue(ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY, m_szCountry)
                //.withValue(StructuredPostal.TYPE, StructuredPostal.TYPE_WORK)
                .build());*/
        //INSERT NOTE
        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID,   rawContactInsertIndex)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Note.NOTE, "Created by GetTaxi App (c)")
                .build()); //
            //Add a custom colomn to identify this contact
        /*ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID,   rawContactInsertIndex)
                .withValue(ContactsContract.Data.MIMETYPE, MIMETYPE_RADUTOKEN)
                .withValue(ContactsContract.Data.DATA1, szToken)
                .build());*/
            //INSERT imAGE
        /*ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                .withValue(ContactsContract.Data.MIMETYPE,ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Photo.PHOTO,
                        stream.toByteArray())
                .build());*/
            // SAVE CONTACT IN BCR Structure
            Uri newContactUri = null;
            //PUSH EVERYTHING TO CONTACTS
            try
            {
                ContentProviderResult[] res = getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
                if (res[0] != null)
                {
                    newContactUri = res[0].uri;
                    //02-20 22:21:09 URI added contact:content://com.android.contacts/raw_contacts/612
                    Log.d(LOG_TAG, "URI added contact:"+ newContactUri);
                    showMessageOKCancel(chosenClientRequest.getClientName() + " has been saved in you Contacts", null);
                }
                else Log.e(LOG_TAG, "Contact not added.");
            }
            catch (RemoteException e)
            {
                // error
                Log.e(LOG_TAG, "Error (1) adding contact.");
                newContactUri = null;
            }
            catch (OperationApplicationException e)
            {
                // error
                Log.e(LOG_TAG, "Error (2) adding contact.");
                newContactUri = null;
            }
            Log.d(LOG_TAG, "Contact added to system contacts.");

            if (newContactUri == null) {
                Log.e(LOG_TAG, "Error creating contact");

            }
        }
        /* From Android 6.0 Marshmallow,
        the application will not be granted any permissions at installation time.
        Instead, the application has to ask the user for permissions one-by-one at runtime
        with an alert message. The developer has to call for it manually.*/
        @RequiresApi(api = Build.VERSION_CODES.M)
        private void AccessContact()
        {
            List<String> permissionsNeeded = new ArrayList<String>();
            final List<String> permissionsList = new ArrayList<String>();
            if (!addPermission(permissionsList, Manifest.permission.READ_CONTACTS))
                permissionsNeeded.add("Read Contacts");
            if (!addPermission(permissionsList, Manifest.permission.WRITE_CONTACTS))
                permissionsNeeded.add("Write Contacts");
            if (permissionsList.size() > 0) {
                if (permissionsNeeded.size() > 0) {
                    requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                            REQUEST_MULTIPLE_PERMISSIONS);
                }
                requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                        REQUEST_MULTIPLE_PERMISSIONS);
            }
        }

        @RequiresApi(api = Build.VERSION_CODES.M)
        private boolean addPermission(List<String> permissionsList, String permission) {
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsList.add(permission);

                if (!shouldShowRequestPermissionRationale(permission))
                    return false;
            }
            return true;
        }

        private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
            new android.app.AlertDialog.Builder(PresentationClient.this)
                    .setMessage(message)
                    .setPositiveButton("OK", okListener)
                    .create()
                    .show();
        }
    }


}

