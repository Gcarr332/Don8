package app.beacon.com.don8;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.Calendar;

public class editBuskerProfileActivity extends AppCompatActivity {

    private static final String TAG = "SavedProfile";
    private Button mSaveProfile;
    private EditText mFullName;
    private EditText mDateOfBirth;
    private EditText mBio;
    private RadioGroup mRadioGroup;
    private String Gender;
    private Button btnChoose, btnUpload;
    private ImageView imageView;
    private Uri filePath;

    //request code defined as an instance variable.
    private final int PICK_IMAGE_REQUEST = 71;

    //Firebase
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthorListener;
    private DatabaseReference myRef;
    private DatePickerDialog.OnDateSetListener mDateSetListener;

    FirebaseStorage storage;
    StorageReference storageReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_busker_profile);

        //Initialise Views
        //These instances points to the elements created in the activity_busker_profile xml file
        mSaveProfile = (Button) findViewById(R.id.btnSaveProfile);
        mFullName = (EditText) findViewById(R.id.fullName);
        mDateOfBirth = (EditText) findViewById(R.id.dateOfBirth);
        mBio = (EditText) findViewById(R.id.bio);
        mRadioGroup = (RadioGroup)findViewById(R.id.radioGroup);
        btnChoose = (Button) findViewById(R.id.btnChoose);
        imageView = (ImageView) findViewById(R.id.imgView);


        btnChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();

            }

        });

mAuth = FirebaseAuth.getInstance();
mFirebaseDatabase = FirebaseDatabase.getInstance();
myRef = mFirebaseDatabase.getReference();
storage = FirebaseStorage.getInstance();
storageReference = storage.getReference();

        mAuthorListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {

                } else {

                }
            }
        };

  myRef.addValueEventListener(new ValueEventListener() {
      @Override
      public void onDataChange(DataSnapshot dataSnapshot) {
         Object value = dataSnapshot.getValue();
      }

      @Override
      public void onCancelled(DatabaseError databaseError) {

      }
  });

        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                String mGender = ((RadioButton)findViewById(mRadioGroup.getCheckedRadioButtonId())).getText().toString();
                Gender = mGender;
            }
        });

        mDateOfBirth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(
                        editBuskerProfileActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        mDateSetListener,
                        year,month,day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month = month + 1;

                String date = day + "/" + month + "/" + year;
                mDateOfBirth.setText(date);
            }
        };

        mSaveProfile.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String Name = mFullName.getText().toString();
            String DOB = mDateOfBirth.getText().toString();
            String Bio = mBio.getText().toString();

            if(!Name.equals("") && !DOB.equals("") && !Bio.equals("") ){
                FirebaseUser user = mAuth.getCurrentUser();
                String userID = user.getUid();
                myRef.child("users").child(userID).child("full name").setValue(Name);
                myRef.child("users").child(userID).child("age").setValue(DOB);
                myRef.child("users").child(userID).child("gender").setValue(Gender);
                myRef.child("users").child(userID).child("bio").setValue(Bio);
            }
            uploadImage();
        }
    });
    }

    /*When this method is called, a new Intent instance is created.
    The intent type is set to image, and its action is set to get some content. The intent creates an image
    chooser dialog that allows the user to browse through the device gallery to select the image.
    */
    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null )
        {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                imageView.setImageBitmap(bitmap);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }


    }

    private void uploadImage() {

        if(filePath != null)
        {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Saving Profile Details...");
            progressDialog.show();
            FirebaseUser user = mAuth.getCurrentUser();
            String userID = user.getUid();

            StorageReference ref = storageReference.child(userID);
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            startActivity(new Intent(editBuskerProfileActivity.this, loginActivity.class));

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();

                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Uploaded "+(int)progress+"%");
                        }
                    });
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthorListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthorListener != null) {
            mAuth.removeAuthStateListener(mAuthorListener);
        }
    }
}
