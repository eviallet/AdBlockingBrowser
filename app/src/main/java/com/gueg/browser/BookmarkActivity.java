package com.gueg.browser;

import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

public class BookmarkActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmark);


        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        String title = extras.getString("com.gueg.browser.TITLE");
        String url = extras.getString("com.gueg.browser.URL");
        byte[] pic = extras.getByteArray("com.gueg.browser.PIC");

        final EditText textTitle = (EditText) findViewById(R.id.editTextTitle);
        final EditText textUrl = (EditText) findViewById(R.id.editTextUrl);
        final ImageButton btnPic = (ImageButton) findViewById(R.id.imageButtonPic);

        if (textTitle != null) {
            textTitle.setText(title);
        }
        if (textUrl != null) {
            textUrl.setText(url);
        }
        new DbBitmapUtility();
        if (btnPic != null) {
            btnPic.setImageBitmap(DbBitmapUtility.getImage(pic));
        }

        Button btnValider = (Button) findViewById(R.id.btn_bookmarkActivity_valider);
        if (btnValider != null) {
            btnValider.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent results = new Intent();
                    results.putExtra("com.gueg.browser.NEWTITLE", textTitle != null ? textTitle.getText().toString() : null);
                    results.putExtra("com.gueg.browser.NEWURL", textUrl != null ? textUrl.getText().toString() : null);
                    new DbBitmapUtility();
                    results.putExtra("com.gueg.browser.NEWPIC", DbBitmapUtility.getBytes((btnPic != null ? btnPic.getDrawable() : null) != null ? ((BitmapDrawable) btnPic.getDrawable()).getBitmap() : null));
                    setResult(BookmarkActivity.RESULT_OK,results);
                    finish();
                }
            });
        }
        Button btnAnnuler = (Button) findViewById(R.id.btn_bookmarkActivity_annuler);
        assert btnAnnuler != null;
        btnAnnuler.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent results = new Intent();
                setResult(BookmarkActivity.RESULT_CANCELED,results);
                finish();
            }
        });


    }
}

