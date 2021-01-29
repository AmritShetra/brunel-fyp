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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import cz.msebera.android.httpclient.Header;

public class ChatbotFragment extends Fragment {

    ScrollView scrollView;
    LinearLayout linearLayout;
    int chatbotMessage = R.layout.chatbot_message;
    int userMessage = R.layout.user_message;

    final int REQUEST_IMAGE_CAPTURE = 1;

    AsyncHttpClient client = new AsyncHttpClient();

    // Add the message view to the linear layout and scroll down
    private View addView (int viewLayout){
        View newView = getLayoutInflater().inflate(viewLayout, linearLayout, false);
        linearLayout.addView(newView);
        scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));
        return newView;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View parentHolder = inflater.inflate(R.layout.fragment_chatbot, container, false);

        scrollView = parentHolder.findViewById(R.id.scrollView);
        linearLayout = parentHolder.findViewById(R.id.linearLayout);

        // Show the introduction message from the chatbot to kick things off
        addView(chatbotMessage);

        // Add the user message template to the linear layout and edit the button text
        View firstUserView = addView(userMessage);
        Button firstUserButton = firstUserView.findViewById(R.id.userButton);
        firstUserButton.setText(R.string.recycle_option);

        firstUserButton.setOnClickListener(view -> showRecycleInstructions());

        return parentHolder;
    }

    // Make the chatbot's response visible and edit the text appropriately
    private void showRecycleInstructions() {
        View chatbotResponse = addView(chatbotMessage);
        TextView chatbotMessage = chatbotResponse.findViewById(R.id.messageText);
        chatbotMessage.setText(R.string.recycle_instruction);

        showUserButton();
    }

    // Display the user's "take photo" button
    private void showUserButton() {
        View userResponse = addView(userMessage);
        TextView userButton = userResponse.findViewById(R.id.userButton);
        userButton.setText(R.string.take_photo);

        userButton.setOnClickListener(view -> launchCamera());
    }

    private void launchCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            // Get the photo
            Bitmap photo = (Bitmap) data.getExtras().get("data");

            // Display the photo
            ImageView imageView = new ImageView(getContext());
            imageView.setImageBitmap(photo);
            linearLayout.addView(imageView);

            sendPhoto(photo);
        }
    }

    private byte[] convertPhotoToBytes(Bitmap photo) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        photo.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    public void sendPhoto(Bitmap photo) {
        byte[] img = convertPhotoToBytes(photo);

        String url = Server.chatbotRoute();
        RequestParams params = new RequestParams();

        SharedPreferences user = getActivity().getSharedPreferences("User", 0);
        String username = user.getString("username","");

        params.put("photo", new ByteArrayInputStream(img), username + "_app_image.png");

        // Make the response view visible - we'll substitute the API reply text there
        View chatbotResponse = addView(chatbotMessage);
        TextView response = chatbotResponse.findViewById(R.id.messageText);

        client.post(url, params, new TextHttpResponseHandler(){
            @Override
            public void onStart(){
                response.setText(". . .");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                if (responseString == null) {
                    response.setText(R.string.try_again);
                }
                else {
                    response.setText(responseString);
                }
                showUserButton();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                // Show the response in the chatbot message view (once made visible)
                response.setText(responseString);
                // Let the user take a new photo
                showUserButton();
            }
        });
    }
}
