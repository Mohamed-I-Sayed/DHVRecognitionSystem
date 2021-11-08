package com.qalex.veinrec;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

/**
 * @author Mohamed I. Sayed, Mohamed Taha, and Hala H. Zayed 
 * @date 01/10/2021
 * @version 1.0
 */

public class VerifyActivity extends AppCompatActivity {

    Button btn;
    EditText id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify);


        btn = (Button) findViewById(R.id.btn_verify);
        id = (EditText) findViewById(R.id.editTextID);

       

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String get_id = id.getText().toString();
                int id_num = Integer.parseInt(get_id);
                Intent i = new Intent(VerifyActivity.this,VerifyCameraActivity.class);
                i.putExtra("UserID", id_num+1);

                Toast.makeText(getBaseContext(),"Verify user "+id_num+"", Toast.LENGTH_SHORT).show();
                startActivity(i);

            }
        });
    }


}
