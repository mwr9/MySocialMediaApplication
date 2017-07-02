package com.example.owner.mysocialmediaapplication;


import android.app.SearchManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * Created by Mark on 7/09/2016.
 */

public class SearchableActivity extends AppCompatActivity {

    ImageView searchView;
    DatabaseHelper databaseHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);

        searchView = (ImageView) findViewById(R.id.picture_search);
        databaseHelper = new DatabaseHelper(this);
        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            // Show query text along with image
            Toast.makeText(getApplicationContext(), query, Toast.LENGTH_LONG).show();
            doSearch(query);
        }
    }

    private void doSearch(String queryStr) {
        ImageHelper dbImage = databaseHelper.getImage(queryStr);
        byte[] byteArray = dbImage.getImageByteArray();
        if (byteArray != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
            searchView.setImageBitmap(bitmap);
        } else {
            Toast.makeText(getApplicationContext(), "Unable to retrieve image. Please check image name", Toast.LENGTH_LONG).show();
        }

    }
}

