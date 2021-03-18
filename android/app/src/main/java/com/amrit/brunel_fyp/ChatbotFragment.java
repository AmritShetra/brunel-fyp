package com.amrit.brunel_fyp;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.fragment.app.Fragment;
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
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class ChatbotFragment extends Fragment {
    Context context;

    LinearLayout linearLayout;

    int chatbotMessage = R.layout.chatbot_message;
    int userMessage = R.layout.user_message;

    final int REQUEST_IMAGE_CAPTURE = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View parentHolder = inflater.inflate(R.layout.fragment_chatbot, container, false);
        context = getContext();

        ScrollView scrollView = parentHolder.findViewById(R.id.scrollView);
        linearLayout = scrollView.findViewById(R.id.linearLayout);
        initialiseChatbot();

        return parentHolder;
    }

    // Display introduction message from chatbot + user's options to reply
    private void initialiseChatbot() {
        addMessageTemplate(chatbotMessage);

        View userRow = addMessageTemplate(userMessage);
        FlexboxLayout flowLayout = userRow.findViewById(R.id.buttonContainer);

        Button recycleButton = createButton(getString(R.string.recycle_option));
        Button otherButton = createButton(getString(R.string.other_option));
        flowLayout.addView(recycleButton);
        flowLayout.addView(otherButton);

        recycleButton.setOnClickListener(view -> {
            resetLayout();
            showRecycleInstructions();
            showTakePhotoButton();
        });
        otherButton.setOnClickListener(view -> {
            resetLayout();
            showOtherOptions();
        });
    }

    // Inflates a new user/chatbot message template
    private View addMessageTemplate (int viewLayout){
        View newView = getLayoutInflater().inflate(viewLayout, linearLayout, false);
        linearLayout.addView(newView);
        scrollToBottom();
        return newView;
    }

    private void scrollToBottom() {
        ScrollView scrollView = (ScrollView) linearLayout.getParent();
        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
    }

    // Clean up the screen (user might have pressed many buttons and scrolled up to select a new option)
    private void resetLayout() {
        linearLayout.removeAllViews();
        initialiseChatbot();
    }

    private Button createButton(String text) {
        Button button = new Button(getContext());
        button.setText(text);
        button.setBackgroundResource(R.drawable.user_message_shape);
        button.setAllCaps(false);
        return button;
    }

    private void showRecycleInstructions() {
        View chatbotResponse = addMessageTemplate(chatbotMessage);
        TextView chatbotMessage = chatbotResponse.findViewById(R.id.messageText);
        chatbotMessage.setText(R.string.recycle_instruction);
    }

    private void showTakePhotoButton() {
        View userResponse = addMessageTemplate(userMessage);
        FlexboxLayout flowLayout = userResponse.findViewById(R.id.buttonContainer);

        Button photoButton = createButton(getString(R.string.take_photo));
        flowLayout.addView(photoButton);

        photoButton.setOnClickListener(view -> launchCamera());
    }

    // Currently two options for the user
    private void showOtherOptions() {
        View chatbotResponse = addMessageTemplate(chatbotMessage);
        TextView chatbotMessage = chatbotResponse.findViewById(R.id.messageText);
        chatbotMessage.setText(R.string.other_option_response);

        View userRow = addMessageTemplate(userMessage);
        FlexboxLayout flowLayout = userRow.findViewById(R.id.buttonContainer);

        Button optionOneButton = createButton(getString(R.string.other_option_1));
        Button optionTwoButton = createButton(getString(R.string.other_option_2));

        flowLayout.addView(optionOneButton);
        flowLayout.addView(optionTwoButton);

        optionOneButton.setOnClickListener(view -> optionResponse(1));
        optionTwoButton.setOnClickListener(view -> optionResponse(2));
    }

    // Set chatbot's reply depending on user's option
    private void optionResponse(int option) {
        View chatbotResponse = addMessageTemplate(chatbotMessage);
        TextView chatbotMessage = chatbotResponse.findViewById(R.id.messageText);

        if (option == 1)
            chatbotMessage.setText(R.string.other_option_1_response);
        else
            chatbotMessage.setText(R.string.other_option_2_response);
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

            // Display the photo in user message
            ImageView imageView = new ImageView(getContext());
            imageView.setImageBitmap(photo);
            View userRow = addMessageTemplate(userMessage);
            FlexboxLayout flowLayout = userRow.findViewById(R.id.buttonContainer);
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
        params.put("photo", new ByteArrayInputStream(img), "app_image.png");

        // Make the response view visible - we'll substitute the API reply text there
        View chatbotResponse = addMessageTemplate(chatbotMessage);
        TextView responseMessage = chatbotResponse.findViewById(R.id.messageText);

        AsyncHttpClient client = new AsyncHttpClient();
        client.post(url, params, new JsonHttpResponseHandler(){
            @Override
            public void onStart(){
                // This is a placeholder to say that the chatbot is "typing"
                responseMessage.setText(". . .");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                responseMessage.setText(R.string.try_again);
                showTakePhotoButton();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    // Identification of resin code
                    responseMessage.setText(response.getString("sentence"));
                    // Create another message - description of resin code
                    View chatbotResponse = addMessageTemplate(chatbotMessage);
                    TextView responseMessage = chatbotResponse.findViewById(R.id.messageText);
                    responseMessage.setText(response.getString("desc"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                // Unlock a trophy if it's their first time using the chatbot to take a photo
                tryUnlockTrophy();
                showTakePhotoButton();
            }
        });
    }

    public void tryUnlockTrophy() {
        AsyncHttpClient client = new AsyncHttpClient();
        String token = User.retrieveToken(context);
        client.addHeader("Authorization", "Bearer " + token);

        JSONObject json = new JSONObject();
        StringEntity entity = null;
        try {
            json.put("trophy_name", "trophy_one");
            entity = new StringEntity(json.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        String url = Server.trophiesRoute();
        client.put(context, url, entity,"application/json", new JsonHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                // Do nothing, they already have the achievement - return to the chatbot
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    String responseString = response.getString("message");
                    View chatbotResponse = addMessageTemplate(chatbotMessage);
                    TextView responseMessage = chatbotResponse.findViewById(R.id.messageText);
                    responseMessage.setText(responseString);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                // Return to onSuccess() in sendPhoto() so the user can decide what to do next
            }
        });
    }
}
