package com.example.facultytracker;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.springframework.security.crypto.bcrypt.BCrypt;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import android.os.AsyncTask;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class Admin_login extends AppCompatActivity {

    private static final String TAG = "AdminLogin";
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private EmailSender emailSender;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        emailSender = new EmailSender();

        if (emailSender != null) {
            Log.d(TAG, "EmailSender initialized successfully");
        } else {
            Log.e(TAG, "EmailSender is null");
        }




        // Assume you have an EditText for entering the username, an EditText for entering the password, and a login button
        EditText usernameEditText = findViewById(R.id.username);
        EditText passwordEditText = findViewById(R.id.password);
        Button loginButton = findViewById(R.id.loginButton);

        loginButton.setOnClickListener(v -> {
            // Get the entered username and password
            String enteredUsername = usernameEditText.getText().toString();
            String enteredPassword = passwordEditText.getText().toString();

            // Retrieve the hashed password and other details from Firestore based on the entered username
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("Admin_Login")
                    .document(enteredUsername)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    // Get the hashed password and other details from Firestore
                                    String hashedPassword = document.getString("password");

                                    // Verify the entered password with the hashed password
                                    if (BCrypt.checkpw(enteredPassword, hashedPassword)) {
                                        // Passwords match, login successful
                                        Log.d(TAG, "Admin login successful");
                                        showToast("Admin login successful");
                                        sendOTP(enteredUsername);


                                    } else {
                                        // Passwords do not match, login failed
                                        Log.d(TAG, "Incorrect password");
                                        showToast("Incorrect password");
                                    }
                                } else {
                                    // Document does not exist
                                    Log.d(TAG, "Admin document not found");
                                    showToast("Admin document not found");
                                }
                            } else {
                                // Error getting document
                                Log.e(TAG, "Error getting document", task.getException());
                                showToast("Error getting admin document");
                            }
                        }
                    });
        });
    }


    private void sendOTP(String username) {


        // Retrieve email from the database based on the entered username
        // For simplicity, we assume the email is stored in a collection called "Admin_Login"
        // Replace "emailField" with the actual field name where the email is stored
        DocumentReference docRef = db.collection("Admin_Login").document(username);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String email = document.getString("educatedeagless@gmail.com");
                        if (email != null && !email.isEmpty()) {
                            // Email found, generate and send OTP
                            String generatedOTP = generateRandomOTP();

                            // Save the generated OTP in the database
                            saveOTPinDatabase(username, generatedOTP);

                            // Send OTP to the user's email
                            //sendOTPByEmail(email, generatedOTP);

                            // Display an alert dialog to enter OTP
                            showOTPDialog(username);
                        } else {
                            Toast.makeText(Admin_login.this, "Email not found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(Admin_login.this, "Document not found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(Admin_login.this, "Error getting document", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private String generateRandomOTP() {
        // Generate a random 6-digit OTP
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    private void saveOTPinDatabase(String username, String generatedOTP) {
        // Save the OTP in the database
        // Replace "otpField" with the actual field name where the OTP should be stored
        DocumentReference docRef = db.collection("Admin_Login").document(username);
        Map<String, Object> data = new HashMap<>();
        data.put("otpField", generatedOTP);
        docRef.update(data);
    }

    private void sendOTPByEmail(String email, String generatedOTP) {
        // Send OTP to the user's email
        // Replace "YourEmail@gmail.com", "YourEmailPassword" with your actual email and password
        // Note: For security reasons, it's recommended to use a dedicated server for email sending in production
        String subject = "Your Login OTP for Faculty Tracker";
        String message = "Dear Admin,<br><br>" +
                "Thank you for choosing Faculty Tracker for your needs. Your security is our top priority.<br><br>" +
                "Your One-Time Password (OTP) for login is:<br>" + generatedOTP + "<br><br>" +
                "Please enter this code within the next [time limit, e.g., 5 minutes] to complete your login securely. If you didn't request this OTP, please contact our support team immediately at [support email or phone number].<br><br>" +
                "Best Regards,<br><br>" +
                "Government Polytechnic Nashik<br>" +
                "<a href='mailto:office.gpnashik@dtemaharashtra.gov.in'>office.gpnashik@dtemaharashtra.gov.in</a>";




        emailSender.sendEmailInBackground(email, subject, message);

    }

    private void showOTPDialog(String username) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter OTP");

        final EditText input = new EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String enteredOTP = input.getText().toString();
                verifyOTPAndLogin(enteredOTP,username);
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }


    private void verifyOTPAndLogin(String enteredOTP,String username) {


        // Retrieve the stored OTP from the database
        DocumentReference docRef = db.collection("Admin_Login").document(username);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String storedOTP = document.getString("otpField");

                        if (enteredOTP.equals(storedOTP)) {
                            // OTP is valid, proceed with login
                            onLoginSuccess(username);
                            setLoggedInFlag(username);
                            Toast.makeText(Admin_login.this, "Login successful", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(Admin_login.this, dashboard_admin.class);
                            startActivity(intent);
                            finish();
                            // You can navigate to the admin dashboard or perform other actions
                        } else {
                            Toast.makeText(Admin_login.this, "Invalid OTP", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(Admin_login.this, "Document not found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(Admin_login.this, "Error getting document", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setLoggedInFlag(String username) {
        // After a successful login, set the flag in shared preferences
        SharedPreferences preferences = getSharedPreferences("MyPrefAdmin", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("isLoggedIn", true);
        editor.apply();

        editor.putString("username", username);

        editor.apply();

        Log.d("Value taken", "setLoggedInFlag: Username = " + username);
    }

    private void onLoginSuccess(String username) {
        // Your existing login logic...

        // Store the username and selected department in SharedPreferences
        SharedPreferences preferences = getSharedPreferences("MyPrefAdmin", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("username", username);

        editor.apply();
    }


    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}

