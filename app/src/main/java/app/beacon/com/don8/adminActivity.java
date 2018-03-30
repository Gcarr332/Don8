package app.beacon.com.don8;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;



public class adminActivity extends AppCompatActivity {

    private EditText assignBusker1;
    private Button mSaveChanges;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        assignBusker1 = (EditText) findViewById(R.id.assignBusker1);
        mSaveChanges = (Button) findViewById(R.id.saveChanges);

        mSaveChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                setUUID(adminActivity.this, assignBusker1.getText().toString());
                Intent a = new Intent(adminActivity.this, loginActivity.class );
                startActivity(a);
            }
        });
    }

    public static void setUUID(Context context, String UUID) {
        SharedPreferences prefs = context.getSharedPreferences("myAppPackage", 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("UUID", UUID);
        editor.commit();
    }
}
