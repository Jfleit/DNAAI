package com.dnaai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.neura.standalonesdk.events.NeuraEvent;
import com.neura.standalonesdk.events.NeuraEventCallBack;
import com.neura.standalonesdk.events.NeuraPushCommandFactory;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONObject;

import java.io.IOException;

import JSONWrappers.EventData;
import JSONWrappers.NeuraWrapper;

public class FirebaseMessageService extends FirebaseMessagingService {
//    private static final MediaType useTypeJSON = MediaType.parse("application/json; charset=utf-8");

    @Override
    public void onMessageReceived(RemoteMessage message) {
        boolean isNeuraPush = NeuraPushCommandFactory.getInstance().isNeuraPush(getApplicationContext(), message.getData(), new NeuraEventCallBack() {
            @Override
            public void neuraEventDetected(NeuraEvent event) {
//                OkHttpClient client = new OkHttpClient();
//                String url = "https://fa62d41c.ngrok.io/neura/webhook/";
//
//                ObjectMapper mapper = new ObjectMapper();
//                NeuraWrapper wrapper = new NeuraWrapper();
//                wrapper.setIdentifier(event.getIdentifier());
//                wrapper.setUserId(event.getUserId());
//                EventData neuraEvent = new EventData();
//                neuraEvent.setName(event.getEventName());
//                neuraEvent.setTimestamp(event.getEventTimestamp());
//                wrapper.setEventData(neuraEvent);
//
//                try {
//                    String parsedToString = mapper.writeValueAsString(wrapper);
//                    JSONObject responseJSON = new JSONObject(parsedToString);
//
//                    RequestBody requestBody = RequestBody.create(useTypeJSON, responseJSON.toString());
//
//                    Request httpRequest = new Request.Builder()
//                            .url(url)
//                            .post(requestBody)
//                            .build();
//
//                    Response response = client.newCall(httpRequest).execute();
//                    System.out.println(response.message());
//
//                }
//                catch (Exception e) {
//                    e.printStackTrace();
//                }
            }
        });

        if(!isNeuraPush) {
            //Handle non neura push here
        }
    }
}
