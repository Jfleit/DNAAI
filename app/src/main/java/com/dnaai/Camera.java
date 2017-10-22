package com.dnaai;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Camera extends AppCompatActivity {
    private static final String CLOUD_VISION_API_KEY = "AIzaSyDJ0l7Bh5BnJ_WlyE9aVNamEAacbMrP9Z4";
    public static final String FILE_NAME = "temp.jpg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        final Button button = (Button) findViewById(R.id.button_id);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dispatchTakePictureIntent();
            }

        });
    }
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private ImageView mImageView;

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    public File getCameraFile() {
        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return new File(dir, FILE_NAME);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");

            mImageView = (ImageView) findViewById(R.id.imagething);
            mImageView.setImageBitmap(imageBitmap);
            String result = uploadImage(imageBitmap);
            //pass result to the food API HERE
            TextView tv1 = (TextView)findViewById(R.id.textView2);
            tv1.setText(result);
            setContentView(R.layout.activity_camera);
        }

    }

    public String uploadImage(final Bitmap bitmap) {
        new AsyncTask<Object, Void, String>() {
            @Override
            protected String doInBackground(Object... params) {
                try {
                    HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
                    JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
                    //Log.d("first","two lines in");
                    VisionRequestInitializer requestInitializer = new VisionRequestInitializer(CLOUD_VISION_API_KEY) {
                    };

                    Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, null);
                    builder.setVisionRequestInitializer(requestInitializer);
                    //Log.d("second","five lines in");
                    Vision vision = builder.build();

                    BatchAnnotateImagesRequest batchAnnotateImagesRequest =
                            new BatchAnnotateImagesRequest();
                    batchAnnotateImagesRequest.setRequests(new ArrayList<AnnotateImageRequest>() {{
                        AnnotateImageRequest annotateImageRequest = new AnnotateImageRequest();
                    //Log.d("third","eight lines in");
                        // Add the image
                        Image base64EncodedImage = new Image();
                        // Convert the bitmap to a JPEG
                        // Just in case it's a format that Android understands but Cloud Vision
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
                        byte[] imageBytes = byteArrayOutputStream.toByteArray();
                    //Log.d("fourth","converted");
                        // Base64 encode the JPEG
                        base64EncodedImage.encodeContent(imageBytes);
                        annotateImageRequest.setImage(base64EncodedImage);
                    //Log.d("fifth","encoded");
                        // add the features we want
                        annotateImageRequest.setFeatures(new ArrayList<Feature>() {{
                            Feature labelDetection = new Feature();
                            labelDetection.setType("LABEL_DETECTION");
                            labelDetection.setMaxResults(10);
                            add(labelDetection);
                        }});

                        // Add the list of one thing to the request
                        add(annotateImageRequest);
                        //Log.d("sixth","annotated");
                    }});
                    Log.d("seventh","afterFirst");
                        Vision.Images.Annotate annotateRequest = vision.images().annotate(batchAnnotateImagesRequest);
                    Log.d("eigth","vision.images.annotated");
                    annotateRequest.setDisableGZipContent(true);
                    Log.d("ninth","gZipped");
                    BatchAnnotateImagesResponse response = annotateRequest.execute();
                    Log.d("tenth","imageResponse");
                    Log.d("answer",convertResponseToString(response));

                    return convertResponseToString(response);

                } catch (GoogleJsonResponseException e) {
                    Log.d("JRE", "failed to make API request because " + e.getContent());
                } catch (IOException e) {
                    Log.d("IOE", "failed to make API request because of other IOException " +
                            e.getMessage());
                }
                 finally {
                    Log.d("finally","over");

                    //return "Cloud Vision API request failed. Check logs for details.";
                }
                return "Cloud Vision API request failed. Check logs for details.";
            }
            //this oesn't work
            protected void onPostExecute(String result) {
                //THIS IS WHERE WE PARSE/PASS THE STRING TO API #2--Nutrition
                Log.println(Log.DEBUG,"editing",result);
                TextView tv1 = (TextView)findViewById(R.id.textView2);
                tv1.setText(result);
                setContentView(R.layout.activity_camera);
            }
        }.execute();
    return " ";
    }

    //should just give us the description
    private String convertResponseToString(BatchAnnotateImagesResponse response) {
        String message = "";

        List<EntityAnnotation> labels = response.getResponses().get(0).getLabelAnnotations();

        if (labels != null) {
            for (EntityAnnotation label : labels) {
                //message += String.format(Locale.US, "%.3f: %s", label.getScore(), label.getDescription());
                message += label.getDescription();
                //message += "\n";
                return message;
            }
        } else {
            message += "nothing";
        }

        return message;
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
}
