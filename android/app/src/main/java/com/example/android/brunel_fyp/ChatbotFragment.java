package com.example.android.brunel_fyp;


import android.app.Activity;
import android.content.Context;
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
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.flexbox.FlexboxLayout;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class ChatbotFragment extends Fragment {

    private String username;
    private String password;

    ScrollView scrollView;

    int chatbotMessage = R.layout.chatbot_message;
    int userMessage = R.layout.user_message;

    final int REQUEST_IMAGE_CAPTURE = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View parentHolder = inflater.inflate(R.layout.fragment_chatbot, container, false);

        Context context = getContext();
        username = User.getUsername(context);
        password = User.getPassword(context);

        scrollView = parentHolder.findViewById(R.id.scrollView);
        initialiseChatbot();

        return parentHolder;
    }

    // Remove all of the message rows from the layout
    private void clearRows() {
        LinearLayout ll = scrollView.findViewById(R.id.linearLayout);
        ll.removeAllViews();
    }

    private void initialiseChatbot() {
        // Show the introduction message from the chatbot to kick things off
        addView(chatbotMessage);

        // Add the user message to the screen, show the two buttons
        View userOptions = addView(userMessage);
        FlexboxLayout flowLayout = userOptions.findViewById(R.id.buttonContainer);

        Button recycleButton = createButton(
                getString(R.string.recycle_option)
        );
        flowLayout.addView(recycleButton);

        Button otherButton = createButton(
                getString(R.string.other_option)
        );
        flowLayout.addView(otherButton);

        recycleButton.setOnClickListener(view -> {
            // Clean up the screen (the user might have pressed many buttons and scrolled up to do something else)
            clearRows();
            initialiseChatbot();
            showRecycleInstructions();
        });
        otherButton.setOnClickListener(view -> {
            clearRows();
            initialiseChatbot();
            showOtherOptions();
        });

    }

    // Add the message view to the linear layout and scroll down
    private View addView (int viewLayout){
        LinearLayout linearLayout = scrollView.findViewById(R.id.linearLayout);
        View newView = getLayoutInflater().inflate(viewLayout, linearLayout, false);
        linearLayout.addView(newView);
        scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));
        return newView;
    }

    // Create buttons that the user can press
    private Button createButton(String text) {
        Button button = new Button(getContext());
        button.setText(text);
        button.setBackgroundResource(R.drawable.user_message_shape);
        button.setPadding(30, -0, 30, -0);
        button.setAllCaps(false);
        return button;
    }

    // Shows dialogue, followed by two choices for the user
    private void showOtherOptions() {
        View chatbotResponse = addView(chatbotMessage);
        TextView chatbotMessage = chatbotResponse.findViewById(R.id.messageText);
        chatbotMessage.setText(R.string.other_option_response);

        View userOptions = addView(userMessage);
        FlexboxLayout flowLayout = userOptions.findViewById(R.id.buttonContainer);

        Button optionOneButton = createButton(
                getString(R.string.other_option_1)
        );
        flowLayout.addView(optionOneButton);

        Button optionTwoButton = createButton(
                getString(R.string.other_option_2)
        );
        flowLayout.addView(optionTwoButton);

        optionOneButton.setOnClickListener(view -> optionResponse(1));
        optionTwoButton.setOnClickListener(view -> optionResponse(2));

    }

    // Depending on option, return a response from the chatbot
    private void optionResponse(int option) {
        View chatbotResponse = addView(chatbotMessage);
        TextView chatbotMessage = chatbotResponse.findViewById(R.id.messageText);

        if (option == 1)
            chatbotMessage.setText(R.string.other_option_1_response);
        else
            chatbotMessage.setText(R.string.other_option_2_response);
    }

    // If the user wants to recycle an item, show them appropriate dialogue + a choice
    private void showRecycleInstructions() {
        View chatbotResponse = addView(chatbotMessage);
        TextView chatbotMessage = chatbotResponse.findViewById(R.id.messageText);
        chatbotMessage.setText(R.string.recycle_instruction);

        showUserButton();
    }

    // Display the user's "take photo" button
    private void showUserButton() {
        View userResponse = addView(userMessage);
        FlexboxLayout flowLayout = userResponse.findViewById(R.id.buttonContainer);

        Button photoButton = createButton(
                getString(R.string.take_photo)
        );
        flowLayout.addView(photoButton);

        photoButton.setOnClickListener(view -> launchCamera());
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

            // Display the photo in
            ImageView imageView = new ImageView(getContext());
            imageView.setImageBitmap(photo);

            View userResponse = addView(userMessage);
            FlexboxLayout flowLayout = userResponse.findViewById(R.id.buttonContainer);
            flowLayout.addView(imageView);

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
        params.put("photo", new ByteArrayInputStream(img), username + "_app_image.png");

        // Make the response view visible - we'll substitute the API reply text there
        View chatbotResponse = addView(chatbotMessage);
        TextView response = chatbotResponse.findViewById(R.id.messageText);

        AsyncHttpClient client = new AsyncHttpClient();
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

                // Unlock a trophy if it's their first time using the chatbot to take a photo
                tryUnlockTrophy();

                // Let the user take a new photo
                showUserButton();
            }
        });
    }

    public void tryUnlockTrophy() {
        AsyncHttpClient client = new AsyncHttpClient();
        client.setBasicAuth(username, password);
        String url = Server.trophiesRoute();
        JSONObject json = new JSONObject();
        StringEntity entity = null;
        try {
            json.put("trophy_name", "trophy_one");
            entity = new StringEntity(json.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        client.put(getContext(), url, entity,"application/json", new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                // Do nothing, they already have the achievement - return to the chatbot
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                View chatbotResponse = addView(chatbotMessage);
                TextView response = chatbotResponse.findViewById(R.id.messageText);
                response.setText(responseString);

                // Return to onSuccess() in sendPhoto() so the user can decide what to do next
            }
        });
    }
}
