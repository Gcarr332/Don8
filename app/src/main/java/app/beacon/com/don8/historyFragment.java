package app.beacon.com.don8;


import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class historyFragment extends Fragment {

    View myView;

    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;
    private String userID;
    private ImageView profilePicture1;
    private String Busker_1_Image;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
     myView = inflater.inflate(R.layout.history, container, false);

      profilePicture1 = (ImageView)  myView.getRootView().findViewById(R.id.buskerHistory);

        //Declared database reference object to access database.
        //Only usable if user is signed in.
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        final FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();

        Busker_1_Image = getImageUrl(getActivity()).toString();
        Glide.with(getActivity().getApplicationContext()).load(Busker_1_Image).skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE).into(profilePicture1);

        return myView;
    }

    public String getImageUrl(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("myAppPackage", 0);
        return prefs.getString(userID.toString(), "");

    }
}
