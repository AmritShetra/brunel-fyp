package com.example.android.brunel_fyp;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableRow;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import cz.msebera.android.httpclient.Header;

public class Chatbot extends Fragment {

    final int REQUEST_IMAGE_CAPTURE = 1;

    private View parentHolder;

    AsyncHttpClient client = new AsyncHttpClient();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        parentHolder = inflater.inflate(R.layout.fragment_chatbot, container, false);

        // Second row of the screen is the first button that the user can click
        View firstUserView = parentHolder.findViewById(R.id.userOptionView1);
        Button firstUserButton = firstUserView.findViewById(R.id.userButton);
        firstUserButton.setText(R.string.recycle_option);

        firstUserButton.setOnClickListener(view -> showRecycleInstructions(parentHolder));

        return parentHolder;
    }

    private void showRecycleInstructions(View parentHolder) {
        // Make the row visible
        TableRow chatbotRow = parentHolder.findViewById(R.id.ChatbotRow2);
        chatbotRow.setVisibility(View.VISIBLE);

        // Show the chatbot's instructions
        View chatbotView = chatbotRow.findViewById(R.id.ChatbotView2);
        TextView chatbotMessage = chatbotView.findViewById(R.id.chatbotMessage);
        chatbotMessage.setText(R.string.recycle_response);

        showUserButton(parentHolder);
    }

    private void showUserButton(View parentHolder) {
        // Make the next row (user button) visible
        TableRow userRow = parentHolder.findViewById(R.id.UserRow4);
        userRow.setVisibility(View.VISIBLE);

        // Change its text and make the button do something
        View userView = userRow.findViewById(R.id.UserOptionView2);
        Button takePhotoButton = userView.findViewById(R.id.userButton);

        takePhotoButton.setText(R.string.take_photo);
        takePhotoButton.setOnClickListener(view -> launchCamera(parentHolder));
    }

    // Launching the camera
    private void launchCamera(View parentHolder) {
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
            sendPhoto(photo);
        }
    }

    public void sendPhoto(Bitmap photo) {
        // Send the photo to the API
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        photo.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] img = stream.toByteArray();

        String url = Server.chatbotRoute();
        RequestParams params = new RequestParams();
        SharedPreferences user = getActivity().getSharedPreferences("User", 0);
        String username = user.getString("username","");
        params.put("photo", new ByteArrayInputStream(img), username + "_app_image.png");

        // Make the final row visible and obtain the view where we'll substitute the response into
        TableRow chatbotRow = parentHolder.findViewById(R.id.ChatbotRow5);
        chatbotRow.setVisibility(View.VISIBLE);

        View chatbotResponseView = parentHolder.findViewById(R.id.chatbotReplyView);
        TextView response = chatbotResponseView.findViewById(R.id.chatbotMessage);

        client.post(url, params, new TextHttpResponseHandler(){
            @Override
            public void onStart(){
                response.setText(". . .");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                response.setText(responseString);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                // Show the response in the chatbot message view (once made visible)
                response.setText(responseString);
            }
        });
    }

}
