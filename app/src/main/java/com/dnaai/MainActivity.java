package com.dnaai;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.iid.FirebaseInstanceId;
import com.neura.resources.authentication.AuthenticateCallback;
import com.neura.resources.authentication.AuthenticateData;
import com.neura.sdk.object.AuthenticationRequest;
import com.neura.standalonesdk.service.NeuraApiClient;
import com.neura.standalonesdk.util.Builder;
import com.neura.sdk.service.SubscriptionRequestCallbacks;
import com.neura.sdk.object.EventDefinition;
import com.neura.standalonesdk.util.SDKUtils;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONObject;

import java.util.ArrayList;

import JSONWrappers.EventData;
import JSONWrappers.NeuraWrapper;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    NeuraApiClient mNeuraApiClient;

    static final int REQUEST_IMAGE_CAPTURE = 1;
    private ImageView mImageView;

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");

            mImageView = (ImageView) findViewById(R.id.imagething);
            mImageView.setImageBitmap(imageBitmap);
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connectToNeura();
        AuthenticationRequest request = new AuthenticationRequest();
        authenticateNeuraClient(request);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        final Button button = (Button) findViewById(R.id.button_id);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    dispatchTakePictureIntent();
                }

        });

        mNeuraApiClient.simulateAnEvent();
        new asynchHTTPRequest().execute();

    }

    private class asynchHTTPRequest extends AsyncTask<Object, Object, Object> {
        @Override
        public Object doInBackground(Object[] params){
            sendNeuraEventToServer();
            return null;
        }
        private void sendNeuraEventToServer() {
            OkHttpClient client = new OkHttpClient();
            String url = "https://fa62d41c.ngrok.io/neura/webhook/";
            MediaType useTypeJSON = MediaType.parse("application/json; charset=utf-8");

            ObjectMapper mapper = new ObjectMapper();
            NeuraWrapper wrapper = new NeuraWrapper();
            wrapper.setIdentifier("event.getIdentifier()");
            wrapper.setUserId("event.getUserId()");
            wrapper.setFirebaseToken(FirebaseInstanceId.getInstance().getToken());
            EventData neuraEvent = new EventData();
            neuraEvent.setName("event.getEventName()");
            long itslong = 33242334;
            neuraEvent.setTimestamp(itslong);
            wrapper.setEventData(neuraEvent);

            try {
                String parsedToString = mapper.writeValueAsString(wrapper);
                JSONObject responseJSON = new JSONObject(parsedToString);

                System.out.println(responseJSON.toString());
                RequestBody requestBody = RequestBody.create(useTypeJSON, responseJSON.toString());

                Request httpRequest = new Request.Builder()
                        .url(url)
                        .post(requestBody)
                        .build();

                Response response = client.newCall(httpRequest).execute();
                System.out.println(response.message());

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void connectToNeura() {
        Builder builder = new Builder(getApplicationContext());
        mNeuraApiClient = builder.build();
        mNeuraApiClient.setAppUid("122f4359d6e00d1e6606e335ec35dc4d0854024933e295e74f6a0bfede5f2640");
        mNeuraApiClient.setAppSecret("481e8da5d4695c94f87420f4ba30c92775d58d8a54c6811fc4a92e965c2473bb");
        mNeuraApiClient.connect();
    }

    private void subscribeToEvents(ArrayList<EventDefinition> events) {
        String webhookId = "neurawebhook";
        //Subscribe to the events you wish Neura to alert you
        for (int i = 0; i < events.size(); i++) {
            mNeuraApiClient.subscribeToEvent(events.get(i).getName(),
                    "YourEventIdentifier_" + events.get(i).getName(), webhookId,
                    new SubscriptionRequestCallbacks() {
                        @Override
                        public void onSuccess(String eventName, Bundle bundle, String s1) {
                            Log.i(getClass().getSimpleName(), "Successfully subscribed to event!!! " + eventName);
                        }

                        @Override
                        public void onFailure(String eventName, Bundle bundle, int i) {
                            Log.e(getClass().getSimpleName(), "Failed to subscribe to event???? " + eventName);
                        }
                    });
        }
    }

    private void authenticateNeuraClient(AuthenticationRequest request) {
        mNeuraApiClient.authenticate(request, new AuthenticateCallback() {
            @Override
            public void onSuccess(AuthenticateData authenticateData) {
                Log.i(getClass().getSimpleName(), "Successfully authenticate with neura. " +
                        "NeuraUserId = " + authenticateData.getNeuraUserId() + " " +
                        "AccessToken = " + authenticateData.getAccessToken());
                Log.i(getClass().getSimpleName(), "Token: " + FirebaseInstanceId.getInstance().getToken());

                mNeuraApiClient.registerFirebaseToken(MainActivity.this,
                        FirebaseInstanceId.getInstance().getToken());

                ArrayList<EventDefinition> events = authenticateData.getEvents();
                subscribeToEvents(events);
            }

            @Override
            public void onFailure(int i) {
                Log.e(getClass().getSimpleName(), "Failed to authenticate with neura. "
                        + "Reason : " + SDKUtils.errorCodeToString(i));
            }
        });
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.status) {

        }

        else if (id == R.id.configuration){
            Intent intent = new Intent(this, Configuration.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
