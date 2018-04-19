package app.beacon.com.don8;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


public class adminActivity extends AppCompatActivity {

    private EditText assignBusker1;
    private Button mSaveChanges;

    //Firebase Variables
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private DatabaseReference myRef;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        //Get UI elements
        assignBusker1 = (EditText) findViewById(R.id.assignBusker1);
        mSaveChanges = (Button) findViewById(R.id.saveChanges);

        //Get Firebase auth instance.
        mAuth = FirebaseAuth.getInstance();
        //Get Firebase database instance and reference.
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        //Get Firebase storage instance and reference.
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        mSaveChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Admins input is converted to a string and assigned to a variable.
                String assignBusker = assignBusker1.getText().toString();
                //Firebase Database reference is used to set the buskers UUID value in firebase.
                myRef.child("beacon ownership").child("Beacon 1").child("UUID").setValue(assignBusker);
                //Intent used to navigate the admin to the login activity when the save changes button is clicked.
                Intent a = new Intent(adminActivity.this, loginActivity.class );
                startActivity(a);
            }
        });
    }
}
