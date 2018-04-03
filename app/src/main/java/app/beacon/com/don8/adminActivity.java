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

    //Firebase
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private DatabaseReference myRef;

    FirebaseStorage storage;
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        assignBusker1 = (EditText) findViewById(R.id.assignBusker1);
        mSaveChanges = (Button) findViewById(R.id.saveChanges);

        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        mSaveChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String assignBusker = assignBusker1.getText().toString();
                myRef.child("beacon ownership").child("Beacon 1").child("UUID").setValue(assignBusker);

                Intent a = new Intent(adminActivity.this, loginActivity.class );
                startActivity(a);
            }
        });
    }
}
