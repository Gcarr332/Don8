package app.beacon.com.don8;

import android.app.Activity;
import android.app.Dialog;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.estimote.mustard.rx_goodness.rx_requirements_wizard.Requirement;
import com.estimote.mustard.rx_goodness.rx_requirements_wizard.RequirementsWizardFactory;
import com.estimote.proximity_sdk.proximity.EstimoteCloudCredentials;
import com.estimote.proximity_sdk.proximity.ProximityAttachment;
import com.estimote.proximity_sdk.proximity.ProximityObserver;
import com.estimote.proximity_sdk.proximity.ProximityObserverBuilder;
import com.estimote.proximity_sdk.proximity.ProximityZone;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;


public class userHomeActivity extends AppCompatActivity
implements NavigationView.OnNavigationItemSelectedListener {

    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;

    private ProximityObserver proximityObserver;
    private ProximityObserver.Handler observationHandler;

    private CircleImageView navLogo;
    private CircleImageView buskerProfilePicture;
    private Dialog myDialog;
    private TextView navUserEmail;
    private TextView buskerName;
    private TextView buskerEmail;
    private TextView buskerGender;
    private TextView buskerBio;
    private String userID;
    private int paymentAmount;
    private DatabaseReference myRef;
    private FirebaseDatabase mFirebaseDatabase;
    private ProgressDialog downloadDialog;
    private String Busker_1_UUID;
    PayPalConfiguration mConfiguration;
    Intent mService;
    int mPaypalRequestCode = 999;
    String mPaypalClientId = "AebZTiqjUQ_nUat_Y6rShq1TqRy40C4WnzPJR0yRRthflc1H8Dfhy7QPurji_DVqMZWbfN_IWsCU8YDH";
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();


    EstimoteCloudCredentials cloudCredentials =
    new EstimoteCloudCredentials("don8-14g", "2813ab0ee27531cbbf78c73dc85eb42f");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        myDialog = new Dialog(this);
        downloadDialog = new ProgressDialog(userHomeActivity.this);

        //get firebase auth instance
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        myRef = mFirebaseDatabase.getReference();
        final FirebaseUser user = auth.getCurrentUser();
        userID = user.getUid();

        Busker_1_UUID = getUUID(this).toString();
        // mResponse = (TextView) findViewById(R.id.response);

        final NavigationView mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        final View headerLayout = mNavigationView.inflateHeaderView(R.layout.nav_header_user_home);

        navUserEmail = (TextView) headerLayout.findViewById(R.id.userEmail);
        navLogo = (CircleImageView) findViewById(R.id.navLogo);

        //sandbox for testing and production for real.
        mConfiguration = new PayPalConfiguration()
                .environment(PayPalConfiguration.ENVIRONMENT_SANDBOX)
                .clientId(mPaypalClientId);

        mService = new Intent(this, PayPalService.class);
        mService.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, mConfiguration); //configuration above
        startService(mService); // paypal service listening to calls to paypal app.

        FragmentManager fragmentManager= getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.content_frame
                        ,new homeFragment())
                .commit();

        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    // user auth state is changed - user is null
                    // launch login activity
                    startActivity(new Intent(userHomeActivity.this, loginActivity.class));

                }
            }
        };

        //Called anytime there is a change made in the database or initially when the activity is started.
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String emailAddress = dataSnapshot.child("users").child(userID).child("email").getValue(String.class);

                navUserEmail.setText(emailAddress);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        RequirementsWizardFactory
                .createEstimoteRequirementsWizard()
                .fulfillRequirements(getApplicationContext(),
                        // onRequirementsFulfilled
                        new Function0<Unit>() {
                            @Override public Unit invoke() {
                                Log.d("app", "requirements fulfilled");
                                startProximityObservation();
                                return null;
                            }
                        },
                        // onRequirementsMissing
                        new Function1<List<? extends Requirement>, Unit>() {
        @Override public Unit invoke(List<? extends Requirement> requirements) {
            Log.e("app", "requirements missing: " + requirements);
            return null;
        }
    },
        // onError
        new Function1<Throwable, Unit>() {
        @Override public Unit invoke(Throwable throwable) {
            Log.e("app", "requirements error: " + throwable);
            return null;
        }
    });
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
        getMenuInflater().inflate(R.menu.user_home_activity, menu);
        return true;
    }


    public void setImageUrl(Context context, String image) {
        SharedPreferences prefs = context.getSharedPreferences("myAppPackage", 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(userID.toString(), image);
        editor.commit();
    }

    public static String getUUID(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("myAppPackage", 0);
        return prefs.getString("UUID", "");

    }

    @Override
    public void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authListener != null) {
            auth.removeAuthStateListener(authListener);
        }
    }

    @Override
    protected void onDestroy() {
        observationHandler.stop();
        super.onDestroy();
    }

    //sign out method
    public void signOut() {
        auth.signOut();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.user_sign_out) {
//            startActivity(new Intent(buskerHomeActivity.this, loginActivity.class));
//            Toast.makeText(buskerHomeActivity.this, "Signed Out", Toast.LENGTH_SHORT).show();
            signOut();
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        //create a fragment manager object
        FragmentManager fragmentManager = getFragmentManager();

        if (id == R.id.nav_home) {
            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame
                            , new homeFragment())
                    .commit();
        } else if (id == R.id.nav_settings) {
            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame
                            , new settingsFragment())
                    .commit();
        }  else if (id == R.id.nav_history) {
            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame
                            , new historyFragment())
                    .commit();
        } else if (id == R.id.nav_help) {
            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame
                            , new helpFragment())
                    .commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void startProximityObservation() {
        this.proximityObserver =
                new ProximityObserverBuilder(getApplicationContext(), cloudCredentials)
        .withOnErrorAction(new Function1<Throwable, Unit>() {
        @Override
        public Unit invoke(Throwable throwable) {
            Log.e("app", "proximity observer error: " + throwable);
            return null;
        }
    })
            .withBalancedPowerMode()
            .build();

        // The first zone is for the 1st busker.
        // The actions will be triggered when entering/exiting the buskers proximity.
        final ProximityZone zone1 = this.proximityObserver.zoneBuilder()
                .forAttachmentKeyAndValue("Busker", "1")
                .inNearRange()
                .withOnEnterAction(new Function1<ProximityAttachment, Unit>() {
                    @Override
                    public Unit invoke(ProximityAttachment attachment) {
                        Log.d("app", "A busker is playing nearby!");
                        showPopup();
                        return null;
                    }
                })
                .withOnExitAction(new Function1<ProximityAttachment, Unit>() {
                    @Override
                    public Unit invoke(ProximityAttachment attachment) {
                        Log.d("app", "You're leaving the area!");
                        hidePopup();
                        return null;
                    }
                })
                .create();

        // Add zones to ProximityObserver and start observation!
        observationHandler =
                proximityObserver
                        .addProximityZone(zone1)
                        .start();
    }

    //Method to display pop-up showing buskers profile details when users enter proximity of their beacon.
    public void showPopup(){

        myDialog.setContentView(R.layout.popup_busker);
        myDialog.show();

        buskerProfilePicture = (CircleImageView) myDialog.findViewById(R.id.buskerOne);
        buskerName = (TextView) myDialog.findViewById(R.id.buskerOneName);
        buskerBio = (TextView)  myDialog.findViewById(R.id.buskerOneBio);
        buskerGender = (TextView) myDialog.findViewById(R.id.buskerOneGender);
        buskerEmail = (TextView)  myDialog.findViewById(R.id.buskerOneEmail);

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String username = dataSnapshot.child("users").child(Busker_1_UUID).child("full name").getValue(String.class);
                String emailAddress = dataSnapshot.child("users").child(Busker_1_UUID).child("email").getValue(String.class);
                String bio = dataSnapshot.child("users").child(Busker_1_UUID).child("bio").getValue(String.class);
                String sex = dataSnapshot.child("users").child(Busker_1_UUID).child("gender").getValue(String.class);

                buskerName.setText(username);
                buskerEmail.setText("Email: " + emailAddress);
                buskerBio.setText("Bio: " + bio);
                buskerGender.setText("Gender: " + sex);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        String url = "https://firebasestorage.googleapis.com/v0/b/don8-1eb66.appspot.com/o/" + Busker_1_UUID + "?alt=media&token=<token>";
        // diskCacheStrategy() used to handle the disk cache and  memory cache is skipped using skipMemoryCache() method.
        // This allows the busker to be able to change their profile picture on their profile page so users can view the most recent busker profile picture.
        Glide.with(getApplicationContext()).load(url).skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE).into(buskerProfilePicture);
    }

    public void hidePopup(){
        myDialog.hide();
    }

    void pay(View view){
        //Switch statement to determine the Paypal donation amount selected by the user.
        switch(view.getId())
        {
            case R.id.payment1:
            paymentAmount = 1;
            break;
            case R.id.payment2:
            paymentAmount = 2;
            break;
            case R.id.payment3:
            paymentAmount = 3;
            break;
            case R.id.payment4:
            paymentAmount = 4;
            break;
            case R.id.payment5:
            paymentAmount = 5;
            break;
            default:
            throw new RuntimeException("Unknown button ID");
        }

        PayPalPayment payment = new PayPalPayment(new BigDecimal(paymentAmount), "GBP", "Test payment with Paypal",
                PayPalPayment.PAYMENT_INTENT_SALE);

        Intent intent = new Intent(this, PaymentActivity.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, mConfiguration);
        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payment);
        startActivityForResult(intent, mPaypalRequestCode);
    }

 public void downloadImage() {

     final StorageReference islandRef = storageRef.child(Busker_1_UUID);
     String url = "https://firebasestorage.googleapis.com/v0/b/don8-1eb66.appspot.com/o/" + Busker_1_UUID + "?alt=media&token=<token>";
     System.out.print(islandRef);
     setImageUrl(this, url);

     File localFile = null;

     try {
         // Local temp file has been created
         localFile = File.createTempFile("images", ".jpg", getExternalFilesDir(Environment.DIRECTORY_PICTURES));
         //  addToGallery(this, Uri.fromFile(localFile));
     } catch (IOException e) {
         e.printStackTrace();
     }
     System.out.println("localFile=" + localFile.getAbsolutePath());

     islandRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
         @Override
         public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

             System.out.println("Busker Profile Picture Downloaded");
             downloadDialog.hide();
         }
     }).addOnFailureListener(new OnFailureListener() {
         @Override
         public void onFailure(@NonNull Exception exception) {
             // Handle any errors
             downloadDialog.hide();
         }
     }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
         @Override
         public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
             //calculating progress percentage
             double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
             //displaying percentage in progress dialog
             downloadDialog.setMessage("Uploaded " + ((int) progress) + "%...");
             downloadDialog.show();
         }
     });
 }

    protected void onActivityResult(int requestCode,int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == mPaypalRequestCode)
        {
            if(resultCode == Activity.RESULT_OK)
            {
                //Confirming payment worked to avoid fraud
                PaymentConfirmation confirmation = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);

                if(confirmation != null)
                {
                    String state = confirmation.getProofOfPayment().getState();

                    if(state.equals("approved")) {
                        Toast.makeText(userHomeActivity.this, "Payment Successful", Toast.LENGTH_SHORT).show();
                        //perform download
                        downloadImage();
                    }
                    else
                        Toast.makeText(userHomeActivity.this, "Payment Error", Toast.LENGTH_SHORT).show();
                }
                else
                    Toast.makeText(userHomeActivity.this, "Confirmation is null", Toast.LENGTH_SHORT).show();
            }
            hidePopup();
        }
    }
}
