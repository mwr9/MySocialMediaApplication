package com.example.owner.mysocialmediaapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by Mark on 31/08/2016.
 */

public class ShareTextActivity extends AppCompatActivity {

    private EditText textEntry;
    private Button shareButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_text);
        shareButton = (Button)findViewById(R.id.share_text_button);
        setupEvents();
    }

    private void setupEvents() {
        shareButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                textEntry = (EditText)findViewById(R.id.share_text_entry);
                String userEntry = textEntry.getText().toString();
                if( userEntry.length() == 0 ) {
                    textEntry.setError("Some text is required!");
                    return;
                }
                Intent textShareIntent = new Intent(Intent.ACTION_SEND);
                textShareIntent.putExtra(Intent.EXTRA_TEXT, userEntry);
                textShareIntent.setType("text/plain");
                startActivity(Intent.createChooser(textShareIntent, "Share text with..."));
            }
        });
    }
}