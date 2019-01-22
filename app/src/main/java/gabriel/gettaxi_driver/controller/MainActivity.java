package gabriel.gettaxi_driver.controller;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RelativeLayout;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import dmax.dialog.SpotsDialog;
import gabriel.gettaxi_driver.R;
import gabriel.gettaxi_driver.models.backend.DB_Manager;
import gabriel.gettaxi_driver.models.backend.DB_ManagerFactory;
import gabriel.gettaxi_driver.models.backend.Driver_Service;
import gabriel.gettaxi_driver.models.backend.GetTaxiConst;
import gabriel.gettaxi_driver.models.datasource.List_DBManager;
import gabriel.gettaxi_driver.models.entities.Driver;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static android.support.v4.content.ContextCompat.getSystemService;
import static gabriel.gettaxi_driver.models.datasource.List_DBManager.driverLocation;
import static java.security.AccessController.getContext;

public class MainActivity extends Activity implements View.OnClickListener, LocationListener {

    //region ***** ATTRIBUTES *****

    private RelativeLayout layout;
    private Button btnSignIn;
    private Button btnSignUp;
    private MaterialEditText edtEmail;
    private MaterialEditText edtPassword;
    private CheckBox stayConnected;
    private android.app.AlertDialog waitingSpots;

    private MaterialEditText edtFirstName;
    private MaterialEditText edtLastName;
    private MaterialEditText edtPhoneNumber;
    private MaterialEditText edtID;
    private MaterialEditText edtCreditCard;
    private MaterialEditText edtEmailSignUp;
    private MaterialEditText edtPasswordSignUp1;
    private MaterialEditText edtPasswordSignUp2;

    private SharedPreferences sp;
    private SharedPreferences.Editor spEditor;
    private Bundle saveDialog;

    private FirebaseUser usersDB;
    private DatabaseReference drivers;
    private FirebaseAuth auth;

    private SignIn signIn;
    private SignUp signUp;

    private static final int PERMS_CALL_ID = 1234;
    private LocationManager lm;
    private DB_Manager dbManager;

    //endregion


    @Override
    protected void onResume() {
        super.onResume();
        askPermission();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMS_CALL_ID)
            askPermission();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initializesFont();
        setContentView(R.layout.activity_splash);

        askPermission();
        initializesViewsVariables();
    }

    private void askPermission()
    {
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]
                            {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.VIBRATE,
                                    Manifest.permission.CALL_PHONE, Manifest.permission.SEND_SMS}, PERMS_CALL_ID);
        }

        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.VIBRATE) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]
                    {Manifest.permission.CALL_PHONE, Manifest.permission.SEND_SMS, Manifest.permission.VIBRATE}, PERMS_CALL_ID);
        }

        lm = (LocationManager)  getSystemService(LOCATION_SERVICE);
        if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER))
        {
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 2, (LocationListener) this);
        }
        if (lm.isProviderEnabled(LocationManager.PASSIVE_PROVIDER))
        {
            lm.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 5000, 2, (LocationListener) this);
        }
        if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
        {
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 2, (LocationListener) this);
        }
    }

    void initializesFont()
    {
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("font/arkhip_font.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());
    }

    void initializesViewsVariables()
    {
        signIn = new SignIn();
        signUp = new SignUp();

        layout = (RelativeLayout) findViewById(R.id.splashView);
        btnSignIn = (Button) findViewById(R.id.btnSignIn);
        btnSignIn.setOnClickListener(this);
        btnSignUp = (Button) findViewById(R.id.btnSignUp);
        btnSignUp.setOnClickListener(this);

        sp = PreferenceManager.getDefaultSharedPreferences(this);
        spEditor = sp.edit();

        saveDialog = new Bundle();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        drivers = database.getReference(GetTaxiConst.DriverConst.DRIVERS);
        auth = FirebaseAuth.getInstance();

        waitingSpots = new SpotsDialog.Builder()
                .setContext(MainActivity.this)
                .setTheme(R.style.Custom)
                .build();

        dbManager = DB_ManagerFactory.getDB_Manager();
    }

    //region ***** LOCATION *****

    @Override
    public void onLocationChanged(Location location) {
        driverLocation = location;
        lm.removeUpdates(this);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    //endregion

    //region ***** CLASSES SIGNIN SIGNOUT *****

    public class SignIn
    {
        void findViewSignIn(View registerLayout)
        {
            edtEmail = registerLayout.findViewById(R.id.signIn_email);
            edtPassword = registerLayout.findViewById(R.id.signIn_password);
            stayConnected = registerLayout.findViewById(R.id.signIn_chkStayConnected);
        }

        private void showSignInDialog()
        {
            AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
            dialog.setTitle("Sign In");
            dialog.setMessage("Please user email to login in");

            LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
            View registerLayout = inflater.inflate(R.layout.activity_card_signin, null);

            findViewSignIn(registerLayout);
            putOutSharedPreferences();
            dialog.setView(registerLayout);

            //Set button
            dialog.setPositiveButton("CONNEXION", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    waitingSpots.show();
                    dialog.dismiss();

                    if (checkFieldsSignIn() == -1) return;
                    else connectToAccount();


                }
            });

            dialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            dialog.show();
        }

        private int checkFieldsSignIn()
        {
            if (TextUtils.isEmpty(edtEmail.getText().toString()))
            {
                Snackbar.make(layout, "Please enter an email address", Snackbar.LENGTH_LONG).show();
                return -1;
            }

            if (TextUtils.isEmpty(edtPassword.getText().toString()))
            {
                Snackbar.make(layout, "Please enter a password", Snackbar.LENGTH_LONG).show();
                return -1;
            }

            if (!edtEmail.getText().toString().matches(
                    "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])"))
            {
                Snackbar.make(layout, "Please enter a correct email address", Snackbar.LENGTH_LONG).show();
                return -1;
            }

            if (edtPassword.getText().toString().length() < 6)
            {
                Snackbar.make(layout, "Password too short !", Snackbar.LENGTH_LONG).show();
                return -1;
            }

            return 0;
        }

        private void connectToAccount()
        {
            String mSP = sp.getString(GetTaxiConst.DriverConst.EMAIL, "");
            String m = edtEmail.getText().toString();
            String pSP = sp.getString(GetTaxiConst.DriverConst.PASSWORD, "");
            String p = edtPassword.getText().toString();

            // If the user is present in the SharedPreferences..., else checks on the firebase
            if (mSP.equals(m) && pSP.equals(p))
            {
                Snackbar.make(layout, "Login Successful", Snackbar.LENGTH_LONG).show();
                saveInSharedPreferences();
                auth.signInWithEmailAndPassword(mSP, pSP).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {

                        drivers.child(auth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                 Driver driver = dataSnapshot.getValue(Driver.class);
                                 Intent intent = new Intent(MainActivity.this, Welcome.class);
                                 List_DBManager.currentDriver = driver;

                                 startActivity(intent);
                                 waitingSpots.dismiss();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }

                });
            }
            else
            {
                auth.signInWithEmailAndPassword(edtEmail.getText().toString(), edtPassword.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {

                                drivers.child(auth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        Driver driver = dataSnapshot.getValue(Driver.class);
                                        Intent intent = new Intent(MainActivity.this, Welcome.class);

                                        List_DBManager.currentDriver = driver;

                                        saveInSharedPreferences();

                                        startActivity(intent);
                                        waitingSpots.dismiss();
                                        //TODO***** ACHTÜNG FINISH ****
                                        finish();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                waitingSpots.dismiss();

                                final AlertDialog.Builder dialogToSignUp = new AlertDialog.Builder(MainActivity.this);
                                dialogToSignUp
                                        .setTitle(R.string.app_name)

                                        .setMessage("Ooooppps! It seams that you haven't got any account. Let's create it")
                                        .setPositiveButton("CREATE IT", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                                signUp.showSignUpDialog();
                                            }
                                        })
                                        .setNegativeButton("NOT NOW", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        });
                                dialogToSignUp.create().show();
                            }
                        });
            }
        }
    }

    public class SignUp
    {
        private void showSignUpDialog()
        {
            AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
            dialog.setTitle("Sign Up");
            dialog.setMessage("Please enter your personal information to log up");

            LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
            View registerLayout = inflater.inflate(R.layout.activity_card_signup, null);

            findViewSignUp(registerLayout);
            dialog.setView(registerLayout);

            //Set button
            dialog.setPositiveButton("REGISTER", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    dialog.dismiss();
                    waitingSpots.show();

                    saveData();

                    if (checkFieldsSignUp() == -1) return;
                    else logUptoAccount();
                }
            });

            dialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    saveData();
                    dialog.dismiss();

                }
            });

            showData();
            dialog.show();
        }

        private void findViewSignUp(View registerLayout)
        {
            edtFirstName = registerLayout.findViewById(R.id.signUp_firstName);
            edtLastName = registerLayout.findViewById(R.id.signUp_lastName);
            edtEmailSignUp = registerLayout.findViewById(R.id.signUp_email);
            edtPhoneNumber = registerLayout.findViewById(R.id.signUp_phoneNumber);
            edtID = registerLayout.findViewById(R.id.signUp_ID);
            edtCreditCard = registerLayout.findViewById(R.id.signUp_creditCard);
            edtPasswordSignUp1 = registerLayout.findViewById(R.id.signUp_password1);
            edtPasswordSignUp2 = registerLayout.findViewById(R.id.signUp_password2);
        }

        private int checkFieldsSignUp()
        {
            if (TextUtils.isEmpty(edtFirstName.getText().toString()))
            {
                Snackbar.make(layout, "Please enter a first name", Snackbar.LENGTH_LONG).show();
                return -1;
            }

            if (TextUtils.isEmpty(edtLastName.getText().toString()))
            {
                Snackbar.make(layout, "Please enter a last name", Snackbar.LENGTH_LONG).show();
                return -1;
            }

            if (TextUtils.isEmpty(edtID.getText().toString()))
            {
                Snackbar.make(layout, "Please enter an ID number", Snackbar.LENGTH_LONG).show();
                return -1;
            }

            if (TextUtils.isEmpty(edtEmailSignUp.getText().toString()))
            {
                Snackbar.make(layout, "Please enter an email address", Snackbar.LENGTH_LONG).show();
                return -1;
            }

            if (TextUtils.isEmpty(edtPhoneNumber.getText().toString()))
            {
                Snackbar.make(layout, "Please enter a phone number", Snackbar.LENGTH_LONG).show();
                return -1;
            }

            if (TextUtils.isEmpty(edtCreditCard.getText().toString()))
            {
                Snackbar.make(layout, "Please enter a credit card", Snackbar.LENGTH_LONG).show();
                return -1;
            }

            if (!edtEmailSignUp.getText().toString().matches(
                    "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])"))
            {
                Snackbar.make(layout, "Please enter a correct email address", Snackbar.LENGTH_LONG).show();
                return -1;
            }

            if (!edtPhoneNumber.getText().toString().matches("(?:(?:\\+|00)972|0)\\s*[1-9](?:[\\s.-]*\\d{2}){4}"))
            {
                Snackbar.make(layout, "Please enter a correct phone number", Snackbar.LENGTH_LONG).show();
                return -1;
            }

            if (edtID.getText().toString().length() != 9)
            {
                Snackbar.make(layout, "Please enter a correct ID number", Snackbar.LENGTH_LONG).show();
                return -1;
            }

            if (edtCreditCard.getText().toString().length() != 16)
            {
                Snackbar.make(layout, "Please enter a correct credit card number", Snackbar.LENGTH_LONG).show();
                return -1;
            }

            if (!edtPasswordSignUp1.getText().toString().equals(edtPasswordSignUp2.getText().toString()))
            {
                Snackbar.make(layout, "The passwords are different", Snackbar.LENGTH_LONG).show();
                return -1;
            }

            return 0;
        }

        private void logUptoAccount()
        {
            final Driver driver = new Driver();
            driver.setFirstName(edtFirstName.getText().toString());
            driver.setLastName(edtLastName.getText().toString());
            driver.setPhoneNumber(edtPhoneNumber.getText().toString());
            driver.setId(edtID.getText().toString());
            driver.setCreditCard(edtCreditCard.getText().toString());
            driver.setEmail(edtEmailSignUp.getText().toString());

            auth.createUserWithEmailAndPassword(driver.getEmail(), edtPasswordSignUp1.getText().toString())
                    .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {

                            FirebaseUser user = auth.getCurrentUser();
                            String key = user.getUid();

                            driver.setUID(key);
                            drivers.child(key).setValue(driver).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    createSimpleAlertDialog("Account created Successfully");
                                }
                            });

                            List_DBManager.currentDriver = driver;

                            Intent intent = new Intent(MainActivity.this, Welcome.class);
                            startActivity(intent);
                            waitingSpots.dismiss();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            createSimpleAlertDialog("Account creation Failure\n" + e.getMessage());
                            waitingSpots.dismiss();
                        }
                    });
        }

    }

    //endregion

    //region ***** SAVE / RETRIEVE DATA INTO BUNDLE *****

    void showData()
    {
        if (saveDialog != null)
        {
            edtFirstName.setText(saveDialog.getString("First Name"));
            edtLastName.setText(saveDialog.getString("Last Name"));
            edtID.setText(saveDialog.getString("ID"));
            edtEmailSignUp.setText(saveDialog.getString("Email"));
            edtPhoneNumber.setText(saveDialog.getString("Phone Number"));
            edtCreditCard.setText(saveDialog.getString("Credit Card"));
            edtEmailSignUp.setText(saveDialog.getString("Email"));
        }
    }

    void saveData()
    {
        saveDialog.putString("First Name", edtFirstName.getText().toString());
        saveDialog.putString("Last Name", edtLastName.getText().toString());
        saveDialog.putString("ID", edtID.getText().toString());
        saveDialog.putString("Email", edtEmailSignUp.getText().toString());
        saveDialog.putString("Phone Number", edtPhoneNumber.getText().toString());
        saveDialog.putString("Credit Card", edtCreditCard.getText().toString());
    }

    //endregion

    //region ***** SAVE / RETRIEVE DATA INTO SHAREDPREFERENCES *****

    private void saveInSharedPreferences() {
        spEditor.putString(GetTaxiConst.DriverConst.EMAIL, edtEmail.getText().toString());
        spEditor.putString(GetTaxiConst.DriverConst.PASSWORD, edtPassword.getText().toString());
        spEditor.putString("isChecked", String.valueOf(stayConnected.isChecked()));
        spEditor.commit();
    }

    private void putOutSharedPreferences()
    {
        if (sp.getString("isChecked", "false").equals("true"))
        {
            edtEmail.setText(sp.getString(GetTaxiConst.DriverConst.EMAIL, "user@mail.com"));
            edtPassword.setText(sp.getString(GetTaxiConst.DriverConst.PASSWORD, "000000"));
            stayConnected.setChecked(true);
        }
    }

    //endregion

    //region ***** ADMINISTRATION *****

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public void onClick(View v) {
        if (v == btnSignIn)
            signIn.showSignInDialog();
        if (v == btnSignUp)
            signUp.showSignUpDialog();
    }

    private void createSimpleAlertDialog(String message)
    {
        AlertDialog.Builder creationOK = new AlertDialog.Builder(MainActivity.this);
        creationOK.setTitle(R.string.app_name);
        creationOK.setMessage(message);
        creationOK.setPositiveButton("OK", null);
        creationOK.show();
    }

    //endregion

}
