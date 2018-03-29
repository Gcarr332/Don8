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
//                String UUID = assignBusker1.getText().toString();
//                Bundle basket = new Bundle();
//                basket.putString("abc", UUID);
                setUsername(adminActivity.this, assignBusker1.getText().toString());
                Intent a = new Intent(adminActivity.this, loginActivity.class );
//                a.putExtras(basket);
                startActivity(a);

            }
        });
    }

    public static void setUsername(Context context, String username) {
        SharedPreferences prefs = context.getSharedPreferences("myAppPackage", 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("username", username);
        editor.commit();
    }
}
