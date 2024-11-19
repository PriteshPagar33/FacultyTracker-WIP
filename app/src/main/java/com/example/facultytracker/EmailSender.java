package com.example.facultytracker;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class EmailSender {

    // Replace with your Sendinblue API key
    private static final String API_KEY = "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";

    // API endpoint for sending a transactional email
    private static final String API_ENDPOINT = "https://api.sendinblue.com/v3/smtp/email";

    public void sendEmailInBackground(String toEmail, String subject, String messageBody) {
        new SendEmailTask().execute(toEmail, subject, messageBody);
    }

    private class SendEmailTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            sendEmail(params[0], params[1], params[2]);
            return null;
        }
    }

    private void sendEmail(String toEmail, String subject, String messageBody) {
        try {
            // Create JSON payload for the email
            JSONObject emailData = new JSONObject();
            emailData.put("sender", new JSONObject().put("email", "educatedeagless@email.com"));
            emailData.put("to", new JSONArray().put(new JSONObject().put("email", toEmail)));
            emailData.put("subject", subject);
            emailData.put("htmlContent", messageBody);

            // Create the HTTP connection
            URL url = new URL(API_ENDPOINT);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("api-key", API_KEY);
            connection.setDoOutput(true);

            // Write the email data to the request body
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = emailData.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // Get the HTTP response code
            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                Log.d("Sendinblue", "Email sent successfully");
            } else {
                Log.e("Sendinblue", "Error sending email. Response code: " + responseCode);
            }

            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Sendinblue", "Error sending email: " + e.getMessage());
        }
    }
}
