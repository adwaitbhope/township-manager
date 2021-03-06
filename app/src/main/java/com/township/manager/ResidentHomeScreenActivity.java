package com.township.manager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.room.Room;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.township.manager.R.color.white;

public class ResidentHomeScreenActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, NoticeBoardFragment.OnFragmentInteractionListener, MaintenanceFragment.OnFragmentInteractionListener, VisitorHistoryFragment.OnFragmentInteractionListener, AmenitiesFragment.OnFragmentInteractionListener {

    String username, password;

    NoticeBoardFragment noticeBoardFragment;
    VisitorHistoryFragment visitorHistoryFragment;
    MaintenanceFragment maintenanceFragment;
    AmenitiesFragment amenitiesFragment;

    AppDatabase appDatabase;
    NoticeDao noticeDao;
    NoticeWingDao noticeWingDao;
    CommentDao commentDao;
    VisitorDao visitorDao;
    MaintenanceDao maintenanceDao;

    Notice[] noticesArray;
    NoticeWing[] noticeWingArray;
    Notice.Comment[] commentsArray;
    Visitor[] visitorsArray;
    Maintenance[] maintenancesArray;
    Amenity[] amenitiesArray;

    DrawerLayout drawerLayout;

    DBManager dbManager;
    Cursor cursor;
    View headerView;

    Menu menu;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resident_home_screen);

        Toolbar toolbar = (Toolbar) findViewById(R.id.resident_home_screen_toolbar);
        setSupportActionBar(toolbar);

        dbManager = new DBManager(getApplicationContext());
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        appDatabase = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "app-database")
                .fallbackToDestructiveMigration()
                .build();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        headerView = navigationView.getHeaderView(0);

        ImageButton editProfile = headerView.findViewById(R.id.resident_home_nav_header_edit_profile_button);

        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ResidentHomeScreenActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });

        cursor = dbManager.getDataLogin();
        cursor.moveToFirst();

        updateUI();

        username = cursor.getString(cursor.getColumnIndexOrThrow("Username"));
        password = cursor.getString(cursor.getColumnIndexOrThrow("Password"));

        noticeBoardFragment = new NoticeBoardFragment();
        maintenanceFragment = new MaintenanceFragment();
        visitorHistoryFragment = new VisitorHistoryFragment();
        amenitiesFragment = new AmenitiesFragment();

        getNoticesFromServer();
        getAmenitiesFromServer();
        getMaintenanceFromServer();
        getVisitorHistoryFromServer();

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.resident_home_screen_fragment_area, noticeBoardFragment);
        transaction.commit();

        BottomNavigationView bottomNavigationView = findViewById(R.id.resident_bottom_nav);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                FragmentTransaction transaction;
                switch (item.getItemId()) {
                    case R.id.resident_notice_board:
                        transaction = getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.resident_home_screen_fragment_area, noticeBoardFragment);
                        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                        transaction.commit();
                        menu.findItem(R.id.action_booking_history_item).setVisible(false);
                        return true;

                    case R.id.resident_amenities:
                        transaction = getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.resident_home_screen_fragment_area, amenitiesFragment);
                        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                        transaction.commit();
                        menu.findItem(R.id.action_booking_history_item).setVisible(true);
                        return true;

                    case R.id.resident_maintenance:
                        transaction = getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.resident_home_screen_fragment_area, maintenanceFragment);
                        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                        transaction.commit();
                        menu.findItem(R.id.action_booking_history_item).setVisible(false);
                        return true;

                    case R.id.resident_visitor_history:
                        transaction = getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.resident_home_screen_fragment_area, visitorHistoryFragment);
                        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                        transaction.commit();
                        menu.findItem(R.id.action_booking_history_item).setVisible(false);
                        return true;
                }

                return false;
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
    }

    public void updateUI() {
        cursor = dbManager.getDataLogin();
        cursor.moveToFirst();

        int flatNoCol, firstNameCol, lastNameCol, wingNoCol;
        firstNameCol = cursor.getColumnIndexOrThrow("First_Name");
        lastNameCol = cursor.getColumnIndexOrThrow("Last_Name");
        flatNoCol = cursor.getColumnIndexOrThrow("Apartment");
        wingNoCol = cursor.getColumnIndexOrThrow("Wing");

        String townshipId, userId;
        townshipId = cursor.getString(cursor.getColumnIndexOrThrow("TownshipId"));
        userId = cursor.getString(cursor.getColumnIndexOrThrow("User_Id"));

        ImageView profilePic = ((ImageView) headerView.findViewById(R.id.resident_home_nav_header_profile_image));
        final String url = "https://township-manager.s3.ap-south-1.amazonaws.com/townships/" + townshipId + "/user_profile_pics/" + userId + ".png";
        Picasso.get()
                .load(url)
                .noFade()
                .placeholder(R.drawable.ic_man)
                .error(R.drawable.ic_man)
                .networkPolicy(NetworkPolicy.NO_CACHE)
                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                .into(profilePic);

        TextView residentFlatNo = headerView.findViewById(R.id.resident_home_nav_header_flat_no);
        TextView residentName = headerView.findViewById(R.id.resident_home_nav_header_name);

        residentName.setText(cursor.getString(firstNameCol) + " " + cursor.getString(lastNameCol));
        residentFlatNo.setText(cursor.getString(wingNoCol) + "/" + cursor.getString(flatNoCol));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        this.menu = menu;
        getMenuInflater().inflate(R.menu.amenities_action_bar_menu, menu);
        menu.findItem(R.id.action_booking_history_item).setVisible(false);
        menu.findItem(R.id.action_booking_list_item).setVisible(false);

        Drawable historyDrawable = menu.findItem(R.id.action_booking_history_item).getIcon();
        historyDrawable = DrawableCompat.wrap(historyDrawable);
        DrawableCompat.setTint(historyDrawable, ContextCompat.getColor(this, white));
        menu.findItem(R.id.action_booking_history_item).setIcon(historyDrawable);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_booking_history_item) {
            startActivity (new Intent(this, AmenitiesBookingHistoryActivity.class));
            return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_intercom_resident) {
            Intent intent=new Intent(ResidentHomeScreenActivity.this, IntercomActivity.class);
            startActivity(intent);
            // Handle the camera action
        } else if (id == R.id.nav_vendors_resident) {
            Intent intent=new Intent(ResidentHomeScreenActivity.this,ServiceVendorActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_admin_info_resident) {
            Intent intent=new Intent(ResidentHomeScreenActivity.this,AdminInfoActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_complaints_resident) {
            startActivity(new Intent(ResidentHomeScreenActivity.this, ComplaintsResidentContainerActivity.class));

        } else if (id == R.id.nav_security_list_resident) {
            Intent intent=new Intent(ResidentHomeScreenActivity.this,SecurityActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_logout_resident) {
            LogOutDialog logOutDialog = new LogOutDialog();
            logOutDialog.show(getSupportFragmentManager(), "Logout");
        }

        drawerLayout.closeDrawers();
        return true;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    public void getNoticesFromServer() {

        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(getString(R.string.server_addr))
                .addConverterFactory(GsonConverterFactory.create());
        Retrofit retrofit = builder.build();

        RetrofitServerAPI retrofitServerAPI = retrofit.create(RetrofitServerAPI.class);

        Call<JsonArray> call = retrofitServerAPI.getNotices(
                username,
                password,
                null
        );

        call.enqueue(new Callback<JsonArray>() {
            @Override
            public void onResponse(Call<JsonArray> call, Response<JsonArray> response) {
                assert response.body() != null;
                Log.d("response", response.body().toString());
                String responseString = response.body().getAsJsonArray().toString();
                try {
                    JSONArray jsonArray = new JSONArray(responseString);
                    JSONObject loginResponse = jsonArray.getJSONObject(0);

                    if (loginResponse.getInt("login_status") == 1) {
                        JSONArray jsonNotices = jsonArray.getJSONArray(1);

                        JSONObject jsonNotice;
                        ArrayList<Notice> notices = new ArrayList<>();
                        Notice notice;
                        Notice.Comment comment;
                        Wing wing;
                        Gson gson = new Gson();

                        for (int i = 0; i < jsonNotices.length(); i++) {
                            jsonNotice = jsonNotices.getJSONObject(i);
                            notice = gson.fromJson(jsonNotice.toString(), Notice.class);

                            ArrayList<Wing> wings = new ArrayList<>();
                            JSONArray jsonWings = jsonNotice.getJSONArray("wings");
                            for (int j = 0; j < jsonWings.length(); j++) {
                                wing = gson.fromJson(jsonWings.getJSONObject(j).toString(), Wing.class);
                                wings.add(wing);
                            }
                            notice.setWings(wings);

                            ArrayList<Notice.Comment> comments = new ArrayList<>();
                            JSONArray jsonComments = jsonNotice.getJSONArray("comments");

                            for (int j = 0; j < jsonComments.length(); j++) {
                                comment = gson.fromJson(jsonComments.getString(j), Notice.Comment.class);
                                comment.setNotice_id(notice.getNotice_id());
                                comments.add(comment);
                            }
                            notice.setComments(comments);

                            notices.add(notice);
                        }

                        addNoticesToDatabase(notices);

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(Call<JsonArray> call, Throwable t) {

            }
        });

    }

    public void getAmenitiesFromServer() {
        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(getString(R.string.server_addr))
                .addConverterFactory(GsonConverterFactory.create());
        Retrofit retrofit = builder.build();

        RetrofitServerAPI retrofitServerAPI = retrofit.create(RetrofitServerAPI.class);

        Call<JsonArray> call = retrofitServerAPI.getAmenities(
                username,
                password
        );

        call.enqueue(new Callback<JsonArray>() {
            @Override
            public void onResponse(Call<JsonArray> call, Response<JsonArray> response) {
                String responseString = response.body().toString();
                try {
                    JSONArray responseArray = new JSONArray(responseString);
                    JSONObject loginData = responseArray.getJSONObject(0);

                    if (loginData.getInt("login_status") == 1) {
                        if (loginData.getInt("request_status") == 1) {

                            JSONArray amenitiesResponseArray = responseArray.getJSONArray(1);
                            Gson gson = new Gson();
                            amenitiesArray = new Amenity[amenitiesResponseArray.length()];
                            for (int i = 0; i < amenitiesResponseArray.length(); i++) {
                                Amenity amenity = gson.fromJson(amenitiesResponseArray.getJSONObject(i).toString(), Amenity.class);
                                amenitiesArray[i] = amenity;
                            }
                            new AmenitiesAsyncTask().execute();
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<JsonArray> call, Throwable t) {

            }
        });
    }

    public void getVisitorHistoryFromServer() {
        RetrofitServerAPI retrofitServerAPI = new Retrofit.Builder()
                .baseUrl(getString(R.string.server_addr))
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(RetrofitServerAPI.class);

        Call<JsonArray> call = retrofitServerAPI.getVisitorHistory(
                username,
                password,
                null
        );

        call.enqueue(new Callback<JsonArray>() {
            @Override
            public void onResponse(Call<JsonArray> call, Response<JsonArray> response) {
                String responseString = response.body().toString();
                try {
                    JSONArray responseArray = new JSONArray(responseString);
                    JSONObject loginData = responseArray.getJSONObject(0);

                    if (loginData.getInt("login_status") == 1) {
                        if (loginData.getInt("request_status") == 1) {

                            JSONArray visitorsResponseArray = responseArray.getJSONArray(1);
                            Gson gson = new Gson();
                            visitorsArray = new Visitor[visitorsResponseArray.length()];
                            for (int i = 0; i < visitorsResponseArray.length(); i++) {
                                Visitor visitor = gson.fromJson(visitorsResponseArray.getJSONObject(i).toString(), Visitor.class);
                                visitorsArray[i] = visitor;
                            }
                            new AddVisitorsToDatabase().execute();
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<JsonArray> call, Throwable t) {

            }
        });
    }

    private void getMaintenanceFromServer() {

        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(getString(R.string.server_addr))
                .addConverterFactory(GsonConverterFactory.create());
        Retrofit retrofit = builder.build();

        RetrofitServerAPI retrofitServerAPI = retrofit.create(RetrofitServerAPI.class);

        Call<JsonArray> call = retrofitServerAPI.getMaintenance(
                username,
                password,
                null
        );

        call.enqueue(new Callback<JsonArray>() {
            @Override
            public void onResponse(Call<JsonArray> call, Response<JsonArray> response) {
                Log.d("maintenance", response.body().toString());
                String responseString = response.body().getAsJsonArray().toString();
                try {
                    JSONArray jsonArray = new JSONArray(responseString);
                    JSONObject loginResponse = jsonArray.getJSONObject(0);

                    if (loginResponse.getInt("login_status") == 1) {
                        JSONArray jsonMaintenanceArray = jsonArray.getJSONArray(1);

                        JSONObject jsonMaintenance;

                        ArrayList<Maintenance> maintenances = new ArrayList<>();
                        Maintenance maintenance;
                        Gson gson = new Gson();

                        for (int i = 0; i < jsonMaintenanceArray.length(); i++) {
                            jsonMaintenance = jsonMaintenanceArray.getJSONObject(i);
                            maintenance = gson.fromJson(jsonMaintenance.toString(), Maintenance.class);

                            maintenances.add(maintenance);
                        }

                        addMaintenanceToDatabase(maintenances);
                    }

                } catch (JSONException e) {
                    Log.d("maintenance", e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<JsonArray> call, Throwable t) {

            }
        });

    }

    private void addMaintenanceToDatabase(final ArrayList<Maintenance> maintenances) {
        new Thread() {
            @Override
            public void run() {
                maintenanceDao = appDatabase.maintenanceDao();
                maintenancesArray = new Maintenance[maintenances.size()];
                maintenances.toArray(maintenancesArray);
                MaintenanceAsyncTask maintenanceAsyncTask = new MaintenanceAsyncTask();
                maintenanceAsyncTask.execute();
            }
        }.start();
    }

    public void addNoticesToDatabase(final ArrayList<Notice> notices) {

        new Thread() {
            public void run() {
                noticeDao = appDatabase.noticeDao();
                noticeWingDao = appDatabase.noticeWingsDao();
                commentDao = appDatabase.commentDao();

                noticesArray = new Notice[notices.size()];

                int noticeWingsLength = 0, commentsLength = 0;
                for (Notice notice : notices) {
                    noticeWingsLength += notice.getWings().size();
                    commentsLength += notice.getComments().size();
                }

                noticeWingArray = new NoticeWing[noticeWingsLength];
                commentsArray = new Notice.Comment[commentsLength];

                ArrayList<NoticeWing> noticeWings = new ArrayList<>();
                ArrayList<Notice.Comment> comments = new ArrayList<>();
                NoticeWing noticeWing;

                for (Notice notice : notices) {
                    for (Wing wing : notice.getWings()) {
                        noticeWing = new NoticeWing();
                        noticeWing.setWing_id(wing.getWing_id());
                        noticeWing.setNotice_id(notice.getNotice_id());
                        noticeWings.add(noticeWing);
                    }
                    comments.addAll(notice.getComments());
                }

                notices.toArray(noticesArray);
                noticeWings.toArray(noticeWingArray);
                comments.toArray(commentsArray);

                new NoticesAsyncTask().execute();
            }
        }.start();

    }

    private class NoticesAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            noticeDao.insert(noticesArray);
            noticeWingDao.insert(noticeWingArray);
            commentDao.insert(commentsArray);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            noticeBoardFragment.updateRecyclerView();
            super.onPostExecute(aVoid);
        }
    }

    private class AmenitiesAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            appDatabase.amenityDao().deleteAll();
            appDatabase.amenityDao().insert(amenitiesArray);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (amenitiesFragment.getContext() != null) {
                amenitiesFragment.updateRecyclerView();
            }
            super.onPostExecute(aVoid);
        }
    }

    private class MaintenanceAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            appDatabase.maintenanceDao().deleteAll();
            appDatabase.maintenanceDao().insert(maintenancesArray);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (maintenanceFragment.getContext() != null) {
                maintenanceFragment.updateRecyclerView();
            }
            super.onPostExecute(aVoid);
        }
    }

    private class AddVisitorsToDatabase extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            visitorDao = appDatabase.visitorDao();
            visitorDao.deleteAll();
            visitorDao.insert(visitorsArray);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (visitorHistoryFragment.getContext() != null) {
                visitorHistoryFragment.updateRecyclerView();
            }
            super.onPostExecute(aVoid);
        }
    }

}
