package app.beacon.com.don8;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;


public class profileFragment extends Fragment {

    View myView;

    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;
    private TextView name;
    private CircleImageView profilePicture;
    private TextView dob;
    private TextView gender;
    private TextView email;
    private TextView bio;
    private Button editProfile;
    private String userID;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.profile, container, false);

        profilePicture = (CircleImageView) getView().findViewById(R.id.profile_image);
        name = (TextView) getView().findViewById(R.id.name);
        dob = (TextView) getView().findViewById(R.id.dob);
        gender = (TextView) getView().findViewById(R.id.gender);
        email = (TextView) getView().findViewById(R.id.email);
        bio = (TextView) getView().findViewById(R.id.buskerBio);
        editProfile = (Button) getView().findViewById(R.id.changeDetails);

        //Declared database reference object to access database.
        //Only usable if user is signed in.
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        final FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();


        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {


                }
            }
        };

        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), editBuskerProfileActivity.class);
                startActivity(intent);
            }
        });

        //Called anytime there is a change made in the database or initially when the activity is started.
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
            showData(dataSnapshot);

            String username = dataSnapshot.child("users").child(userID).child("full name").getValue(String.class);
            String emailAddress = dataSnapshot.child("users").child(userID).child("email").getValue(String.class);
            String dateOfBirth = dataSnapshot.child("users").child(userID).child("age").getValue(String.class);
            String sex = dataSnapshot.child("users").child(userID).child("gender").getValue(String.class);
            String biography = dataSnapshot.child("users").child(userID).child("bio").getValue(String.class);

            name.setText(username);
            email.setText("Email: " + emailAddress);
            dob.setText("DOB: " + dateOfBirth);
            gender.setText("Gender: " + sex);
            bio.setText("Bio: " + biography);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        String url = "https://firebasestorage.googleapis.com/v0/b/don8-1eb66.appspot.com/o/" + userID + "?alt=media&token=<token>";
        // diskCacheStrategy() used to handle the disk cache and  memory cache is skipped using skipMemoryCache() method.
        // This allows the busker to be able to change their profile picture on their profile page and prevents so users can view the most recent busker profile picture.
        Glide.with(getActivity().getApplicationContext()).load(url).skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE).into(profilePicture);

        return getView();
    }

    //Take snapshot of entire database
    private void showData(DataSnapshot dataSnapshot) {
    }

    @Nullable
    public View getView() {
        return myView;
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}
