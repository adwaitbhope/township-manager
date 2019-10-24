package com.township.manager;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ProfileActivity extends AppCompatActivity {

    public FloatingActionButton editButton;
    private static final int EDIT_PROFILE_REQUEST_CODE = 0;
    public static final int PROFILE_EDITED = 1;
    public static final int PROFILE_NOT_EDITED = -1;

    Cursor cursor;
    String username, firstName, lastName, userInfo, phone, email, type;
    int profileUpdated;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        editButton = findViewById(R.id.edit_profile_fab);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
                startActivityForResult(intent, EDIT_PROFILE_REQUEST_CODE);
            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.profile_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Profile");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        updateUI();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == EDIT_PROFILE_REQUEST_CODE) {
            switch (resultCode) {
                case PROFILE_NOT_EDITED:
                    break;

                case PROFILE_EDITED:
                    updateUI();
                    break;
            }
        }

    }

    private void updateUI() {
        // get details from database
        DBManager dbManager = new DBManager(getApplicationContext());
        cursor = dbManager.getDataLogin();
        cursor.moveToFirst();

        username = cursor.getString(cursor.getColumnIndexOrThrow("Username"));
        firstName = cursor.getString(cursor.getColumnIndexOrThrow("First_Name"));
        lastName = cursor.getString(cursor.getColumnIndexOrThrow("Last_Name"));
        email = cursor.getString(cursor.getColumnIndexOrThrow("Email"));
        phone = cursor.getString(cursor.getColumnIndexOrThrow("Phone"));
        type = cursor.getString(cursor.getColumnIndexOrThrow("Type"));
        if (type.equals("resident")) {
            userInfo = cursor.getString(cursor.getColumnIndexOrThrow("Wing"));
            userInfo += "-" + cursor.getString(cursor.getColumnIndexOrThrow("Apartment"));
        } else {
            userInfo = cursor.getString(cursor.getColumnIndexOrThrow("Designation"));
        }

        ((TextView) findViewById(R.id.profile_name)).setText(firstName + " " + lastName);
        ((TextView) findViewById(R.id.profile_user_info)).setText(userInfo);
        ((TextView) findViewById(R.id.profile_username)).setText(username);
        ((TextView) findViewById(R.id.profile_email)).setText(email);
        ((TextView) findViewById(R.id.profile_phone)).setText(phone);

    }
}
