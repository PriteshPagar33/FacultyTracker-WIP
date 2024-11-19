package com.example.facultytracker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;


import org.springframework.security.crypto.bcrypt.BCrypt;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Login_Page extends BaseActivity_reg_login {

    private Spinner spinnerDepartment;
    private EditText editTextUsername;
    private EditText editTextPassword;
    private Button buttonLogin;

    private FirebaseFirestore db;

    private static final int MAX_LOGIN_ATTEMPTS = 3;
    private static final long LOCKOUT_DURATION_HOURS = 24;
    private static final String LOCKOUT_COLLECTION = "lockout";

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);

        mAuth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // User is already signed in, navigate to the home activity
            startActivity(new Intent(Login_Page.this, User_Home.class));
            finish();  // Finish the login activity so the user can't go back to it
        }

        db = FirebaseFirestore.getInstance();

        spinnerDepartment = findViewById(R.id.spinnerDepartment);
        editTextUsername = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.cirLoginButton);

        populateDepartmentSpinner();

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String selectedDepartment = spinnerDepartment.getSelectedItem().toString();
                String username = editTextUsername.getText().toString().trim();
                String password = editTextPassword.getText().toString();

                loginUser(selectedDepartment, username, password);
            }
        });
    }

    private void loginUser(String selectedDepartment, String username, String password) {
        // Check if the device is currently locked out
        checkLockoutStatus(username, new OnSuccessListener<Boolean>() {
            @Override
            public void onSuccess(Boolean isLockedOut) {
                if (isLockedOut) {
                    // Device is locked out, show an alert
                    showLockoutAlert();
                } else {
                    // Device is not locked out, proceed with login attempt
                    performLogin(selectedDepartment, username, password);
                }
            }
        });
    }

    private void checkLockoutStatus(String username, OnSuccessListener<Boolean> successListener) {
        CollectionReference lockoutCollection = db.collection(LOCKOUT_COLLECTION);

        lockoutCollection.document(username)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Check if the lockout duration has elapsed
                        Date lastAttemptTimestamp = documentSnapshot.getDate("timestamp");
                        if (lastAttemptTimestamp != null) {
                            long elapsedTime = Calendar.getInstance().getTime().getTime() - lastAttemptTimestamp.getTime();
                            long lockoutDurationMillis = TimeUnit.HOURS.toMillis(LOCKOUT_DURATION_HOURS);
                            boolean isLockedOut = elapsedTime < lockoutDurationMillis;
                            successListener.onSuccess(isLockedOut);
                        } else {
                            successListener.onSuccess(false);
                        }
                    } else {
                        successListener.onSuccess(false);
                    }
                })
                .addOnFailureListener(e -> {
                    // Assume not locked out in case of an error
                    successListener.onSuccess(false);
                });
    }

    private void showLockoutAlert() {
        new AlertDialog.Builder(this)
                .setTitle("Login Restricted")
                .setMessage("You have exceeded the maximum number of login attempts. Please try again later.")
                .setPositiveButton("OK", null)
                .show();
    }

    private void performLogin(String selectedDepartment, String username, String password) {
        CollectionReference departmentCollection = db.collection(selectedDepartment);

        departmentCollection
                .whereEqualTo("username", username)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot userDocument = queryDocumentSnapshots.getDocuments().get(0);
                        String storedHashedPassword = userDocument.getString("password");

                        if (BCrypt.checkpw(password, storedHashedPassword)) {
                            // Login successful, reset login attempts
                            resetLoginAttempts(username);

                            onLoginSuccess(username,selectedDepartment);
                            setLoggedInFlag(username,selectedDepartment);

                            // Navigate to the home or dashboard activity
                            Toast.makeText(Login_Page.this, "Login successful", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(Login_Page.this, User_Home.class);
                            startActivity(intent);
                            finish();
                            // Add your navigation logic here
                        } else {
                            handleFailedLogin(username);
                        }
                    } else {
                        handleFailedLogin(username);
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle the failure, show an error message
                    Toast.makeText(Login_Page.this, "Error during login: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void handleFailedLogin(String username) {
        // Increment login attempts and check if the user is locked out
        incrementLoginAttempts(username, new OnSuccessListener<Integer>() {
            @Override
            public void onSuccess(Integer loginAttempts) {
                int attemptsLeft = MAX_LOGIN_ATTEMPTS - loginAttempts;
                if (attemptsLeft > 0) {
                    // Show an alert indicating login attempts left
                    showAttemptsLeftAlert(attemptsLeft);
                } else {
                    // Lock the device out and show the lockout alert
                    lockDevice(username);
                    showLockoutAlert();
                }
            }
        });
    }

    private void incrementLoginAttempts(String username, OnSuccessListener<Integer> successListener) {
        CollectionReference lockoutCollection = db.collection(LOCKOUT_COLLECTION);

        lockoutCollection.document(username)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    int loginAttempts = documentSnapshot.contains("loginAttempts") ?
                            documentSnapshot.getLong("loginAttempts").intValue() : 0;
                    loginAttempts++;

                    int finalLoginAttempts = loginAttempts;
                    int finalLoginAttempts1 = loginAttempts;
                    lockoutCollection.document(username)
                            .set(new LockoutData(username, loginAttempts), SetOptions.merge())
                            .addOnSuccessListener(aVoid -> {
                                successListener.onSuccess(finalLoginAttempts);
                            })
                            .addOnFailureListener(e -> {
                                successListener.onSuccess(finalLoginAttempts1);
                            });
                })
                .addOnFailureListener(e -> {
                    successListener.onSuccess(0);
                });
    }

    private void resetLoginAttempts(String username) {
        CollectionReference lockoutCollection = db.collection(LOCKOUT_COLLECTION);

        lockoutCollection.document(username)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Lockout data deleted successfully
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle failure to delete lockout data
                    }
                });
    }

    private void lockDevice(String username) {
        CollectionReference lockoutCollection = db.collection(LOCKOUT_COLLECTION);

        lockoutCollection.document(username)
                .set(new LockoutData(username, MAX_LOGIN_ATTEMPTS), SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Device locked out successfully
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle failure to lock the device
                    }
                });
    }

    private void showAttemptsLeftAlert(int attemptsLeft) {
        new AlertDialog.Builder(this)
                .setTitle("Login Attempt Failed")
                .setMessage("Invalid username or password. " + attemptsLeft + " login attempt(s) left.")
                .setPositiveButton("OK", null)
                .show();
    }

    private void populateDepartmentSpinner() {
        // Create a list of department options, including "Select Department" as the default
        List<String> departmentOptions = new ArrayList<>();
        departmentOptions.add("Select Department");
        departmentOptions.add("Civil Engineering");
        departmentOptions.add("Mechanical Engineering");
        departmentOptions.add("Electrical Engineering");
        departmentOptions.add("Mechatronic Engineering");
        departmentOptions.add("Computer Technology");
        departmentOptions.add("Information Technology");
        departmentOptions.add("Polymer Engineering");
        departmentOptions.add("Electronic & Telecommunication");
        departmentOptions.add("Automobile Engineering");
        departmentOptions.add("Dress Design & Garment Manu.");
        departmentOptions.add("Interior Design");
        departmentOptions.add("Other Department");
        // Add more options as needed

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, departmentOptions);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        spinnerDepartment.setAdapter(adapter);

        // Set the default selection to "Select Department" and disable it
        spinnerDepartment.setSelection(0);

    }

    // Add this class to your project
    public class LockoutData {
        private String username;
        private int loginAttempts;

        public LockoutData(String username, int loginAttempts) {
            this.username = username;
            this.loginAttempts = loginAttempts;
        }

        public String getUsername() {
            return username;
        }

        public int getLoginAttempts() {
            return loginAttempts;
        }
    }




    private void setLoggedInFlag(String username,String selectedDepartment) {
        // After a successful login, set the flag in shared preferences
        SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("isLoggedIn", true);
        editor.apply();

        editor.putString("username", username);
        editor.putString("selectedDepartment", selectedDepartment);
        editor.apply();

        Log.d("Value taken", "setLoggedInFlag: Username = " + username + ", Department = " + selectedDepartment);
    }

    private void onLoginSuccess(String username, String selectedDepartment) {
        // Your existing login logic...

        // Store the username and selected department in SharedPreferences
        SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("username", username);
        editor.putString("selectedDepartment", selectedDepartment);
        editor.apply();
    }





}
