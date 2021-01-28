package com.example.android.brunel_fyp;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import cz.msebera.android.httpclient.Header;

public class Chatbot extends Fragment {

    final int REQUEST_IMAGE_CAPTURE = 1;

    Button button;
    ImageView imageView;

    AsyncHttpClient client = new AsyncHttpClient();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View parentHolder = inflater.inflate(R.layout.fragment_chatbot, container, false);

        button = parentHolder.findViewById(R.id.button);
        imageView = parentHolder.findViewById(R.id.imageView);

        button.setOnClickListener(view -> launchCamera());

        return parentHolder;
    }

    // Launching the camera
    private void launchCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
    }

    // Return the image taken
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            // Get the photo
            Bundle extras = data.getExtras();
            Bitmap photo = (Bitmap) extras.get("data");
            imageView.setImageBitmap(photo);

            // Send the photo to the API
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            photo.compress(Bitmap.CompressFormat.PNG, 90, stream);
            byte[] img = stream.toByteArray();

            String url = Server.chatbotRoute();
            RequestParams params = new RequestParams();
            params.put("photo", new ByteArrayInputStream(img), "app_image.png");

            client.post(url, params, new TextHttpResponseHandler(){
                @Override
                public void onStart(){
                    System.out.println("Starting.");
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    System.out.println(responseString);
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, String responseString) {
                    System.out.println(responseString);
                }
            });
        }
    }

}
