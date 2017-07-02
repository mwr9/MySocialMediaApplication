package com.example.owner.mysocialmediaapplication;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class MainActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private Button getPictureButton;
    private Button savePictureButton;
    private Button sharePictureButton;
    private Button shareTextButton;
    private Button db_button;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        databaseHelper = new DatabaseHelper(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupEvents();

        // For "receiving image" functionality
        //  get intent, action and MIME type
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        // handle the image that is sent
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if (type.startsWith("image/")) {
                handleSentImage(intent);
            }
        }
    }

    // Implement the "Search" functionality
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the options menu from XML
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(
                new ComponentName(getApplicationContext(), SearchableActivity.class)));
        searchView.setIconifiedByDefault(false);
        return true;
    }

    private void setupEvents() {
        // Access the network and get an image
        getPictureButton = (Button)findViewById(R.id.main_get_picture_button);
        if (getPictureButton != null) {
            getPictureButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String pictureURL = "";
                    EditText getPictureNW = (EditText)findViewById(R.id.get_picture_network);
                    if (getPictureNW != null) {
                        pictureURL = getPictureNW.getText().toString();
                        if( pictureURL.length() == 0 ) {
                            getPictureNW.setError("Image URL is required!");
                            return;
                        }
                    }
                    // Check connections
                    ConnectivityManager connMgr = (ConnectivityManager)
                            getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                    if (networkInfo != null && networkInfo.isConnected()) {
                            new DownloadImageTask((ImageView) findViewById(R.id.imageView)).execute("http://" + pictureURL);
                    } else {
                            makeToast("No network connection available.");
                    }
                }
            });
        }

        // Save the picture to the database
        savePictureButton = (Button)findViewById(R.id.main_save_picture_button);
        if (savePictureButton != null) {
                savePictureButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String pictureId = "";
                        Drawable dbDrawable = null;
                        imageView = (ImageView)findViewById(R.id.imageView);
                        if (imageView!=null) {
                            dbDrawable = imageView.getDrawable();
                            EditText getPictureId = (EditText) findViewById(R.id.save_picture_id);
                            if (getPictureId != null) {
                                 pictureId = getPictureId.getText().toString();
                                if( pictureId.length() == 0 ) {
                                    getPictureId.setError("Image name is required!");
                                    return;
                                }
                            }
                        }
                        makeToast("Inserting Image in database.");
                        databaseHelper.insertImage(dbDrawable, pictureId);
                    }
                });
        }

        // Share a picture functionality - go to share picture activity
        sharePictureButton = (Button)findViewById(R.id.main_share_picture_button);
        if (sharePictureButton != null) {
            sharePictureButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), SharePictureActivity.class);
                    startActivity(intent);
                }
            });
        }

        // Share text functionality - go to share text activity
        shareTextButton = (Button)findViewById(R.id.main_share_text_button);
        if (shareTextButton != null) {
            shareTextButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), ShareTextActivity.class);
                    startActivity(intent);
                }
            });
        }

        // Examine the database - very nice functionality
        db_button =(Button)findViewById(R.id.db_button);
        if (db_button != null) {
            db_button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent dbmanager = new Intent(getApplicationContext(), AndroidDatabaseManager.class);
                    startActivity(dbmanager);
                }
            });
        }
    }

    // Handle the downloading of the image - AsyncTask
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            try {
                  return downloadUrl(urls[0]);
            } catch (IOException e) {
                  makeToast("Error Downloading file.");
            }
            return null;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);

        }
    }

    // Get the data
    private Bitmap downloadUrl(String myurl) throws IOException {
        InputStream is = null;
        String DEBUG_TAG = "Download Task";
        Bitmap bitmap;
        try {
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.connect();
            int response = conn.getResponseCode();
            Log.d(DEBUG_TAG, "The response is: " + response);
            Log.d(DEBUG_TAG, "The url is: " + url);
            is = conn.getInputStream();
            bitmap = BitmapFactory.decodeStream(is);
            return bitmap;
         } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    void handleSentImage(Intent intent) {
        Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (imageUri != null) {
            // Update image view to reflect image being shared
            imageView = (ImageView)findViewById(R.id.imageView);
            if (imageView != null) {
                imageView.setImageURI(imageUri);
            }
        }
    }

    private void makeToast(String message) {
        Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG);
        toast.show();
    }

}
