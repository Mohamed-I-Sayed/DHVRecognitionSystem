package com.qalex.veinrec;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Mohamed I. Sayed, Mohamed Taha, and Hala H. Zayed 
 * @date 01/10/2021
 * @version 1.0
 */

public class MenuActivity extends AppCompatActivity {

    ImageView ver , id;
    String DB_PATH;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        ver = (ImageView)findViewById(R.id.verify);
        id = (ImageView)findViewById(R.id.identify);

        try {
            if (android.os.Build.VERSION.SDK_INT >= 17)
                DB_PATH = this.getApplicationInfo().dataDir + "/databases/"+"myDB";
            else
                DB_PATH = "/data/data/" + this.getPackageName() + "/databases/"+"myDB";

            File f = new File(DB_PATH);
            if (f.exists()) {
                CopyDB( getBaseContext().getAssets().open("MyDB.db"),
                        new FileOutputStream(DB_PATH));


            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
         
		 
        } catch (IOException e) {
            e.printStackTrace();

        }


        ver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            Intent i = new Intent(MenuActivity.this,VerifyActivity.class);
                startActivity(i);

            }
        });

        id.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MenuActivity.this,MainActivity.class);

                startActivity(i);
            }
        });

    }

    public void CopyDB(InputStream inputStream,
                       OutputStream outputStream)
            throws IOException {
//---copy 1K bytes at a time---
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, length);
        }
        inputStream.close();
        outputStream.close();
    }

}
