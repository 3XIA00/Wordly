package edu.fandm.yshen.wordly;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.HttpsURLConnection;

public class PlayActivity extends AppCompatActivity implements GridAdapter.WinCallback {

    GridAdapter gridAdapter;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the text values from the EditText fields in the GridView
        ArrayList<String> editTextValues = gridAdapter.getEditTextValues();

        outState.putStringArrayList("EditTextValues", editTextValues);
        System.out.println("saving..." + editTextValues.toString());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        View rootView = findViewById(android.R.id.content);
        rootView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.hide();
        }

        Intent i = getIntent();
        ArrayList<String> path = new ArrayList<>();
        if(i != null){
            path = i.getStringArrayListExtra("Path");
        }else{
            Toast.makeText(getApplicationContext(),"Error on getIntent",Toast.LENGTH_LONG).show();
        }

        CatImageExecutor cie = new CatImageExecutor();
        cie.updateImage();

        ArrayList<String> editTextValues;
        try{
            editTextValues = savedInstanceState.getStringArrayList("EditTextValues");
        } catch (NullPointerException npe){
            editTextValues = new ArrayList<>();
            for(int j = 0; j < path.size(); j++){
                editTextValues.add("");
            }
        }

        GridView gridView = findViewById(R.id.gridView);
        gridAdapter = new GridAdapter(this, path, this, editTextValues);
        gridView.setAdapter(gridAdapter);


        Button newGame = (Button) findViewById(R.id.newGame_bt);
        newGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(),WorldyActivity.class);
                startActivity(i);
            }
        });

        Button hint = (Button) findViewById(R.id.hint_bt);
        hint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gridAdapter.hint();
            }
        });
    }


    interface CatImageCallback{
        void onComplete(Bitmap image);
    }

    CatImageCallback cic = new CatImageCallback() {
        @Override
        public void onComplete(Bitmap image) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(image != null){
                        ImageView iv = (ImageView) findViewById(R.id.image_iv);
                        iv.setImageBitmap(image);
                    }else {
                        Toast.makeText(getApplicationContext(),"Failed to download cat image :(",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    };

    public class CatImageExecutor{
        public void updateImage(){
            ExecutorService es = Executors.newFixedThreadPool(1);
            es.execute(new Runnable() {
                @Override
                public void run() {
                    Bitmap image = updateHelper();
                    cic.onComplete(image);
                }
            });

        }

        public Bitmap updateHelper(){
            Bitmap image;
            try {
                URL url = new URL("https://syimg.3dmgame.com/uploadimg/upload/image/20200521/20200521135826_14897.jpg");
                HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.connect();

                InputStream in = new BufferedInputStream(url.openStream());
                ByteArrayOutputStream out = new ByteArrayOutputStream();

                byte[] buf = new byte[1024];
                int n = 0;
                while(-1!=(n=in.read(buf))){
                    out.write(buf,0,n);
                }
                out.close();
                in.close();

                byte[] response = out.toByteArray();
                image = BitmapFactory.decodeByteArray(response,0,response.length);
                Log.d("Nextworking", "done downloading image");
            } catch (IOException ioe) {
                ioe.printStackTrace();
                image = null;
            }
            return image;
        }
    }

    @Override
    public void onWin() {
        // Update ImageView or perform any other action when the user wins
        ImageView imageView = findViewById(R.id.image_iv);
        // Set the image resource or perform any other customization
        imageView.setImageResource(R.drawable.brain);
    }

}