package com.example.facultytracker;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import org.springframework.security.crypto.bcrypt.BCrypt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class Register_page extends BaseActivity_reg_login {

    private EditText editTextFirstName;

    // Firestore instance
    //private int facultyIdCounter = 0; // Counter for faculty IDs
    private EditText editTextLastName;
    private EditText editTextUsername;
    private RadioGroup radioGroupGender;

    private EditText editTextPassword;
    private EditText editTextConfirmPassword;
    private Spinner spinnerDepartment;
    private EditText editTextDesignation;
    private EditText editTextMobile;
    private EditText editTextEmail;
    private Button buttonRegister;

    private FirebaseFirestore db; // Firestore instance
    private int facultyIdCounter = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_page);
        db = FirebaseFirestore.getInstance(); // Initialize Firestore





            // Initialize views
            editTextFirstName = findViewById(R.id.editTextFirstName);
            editTextLastName = findViewById(R.id.editTextLastName);
            editTextUsername = findViewById(R.id.editTextUsername);
            radioGroupGender = findViewById(R.id.radioGroupGender);
            editTextPassword = findViewById(R.id.editTextPassword);
            editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
            spinnerDepartment = findViewById(R.id.spinnerDepartment);
            editTextDesignation = findViewById(R.id.editTextDesignation);
            editTextMobile = findViewById(R.id.editTextMobile);
            editTextEmail = findViewById(R.id.editTextEmail);
            buttonRegister = findViewById(R.id.buttonRegister);

            // Populate the department spinner
            populateDepartmentSpinner();

            // Set onClickListener for the register button
            buttonRegister.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Perform registration and validation
                    validateAndRegister();
                }
            });


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



    private void validateAndRegister() {
        // Retrieve user inputs
        String firstName = editTextFirstName.getText().toString().trim();
        String lastName = editTextLastName.getText().toString().trim();
        String username = editTextUsername.getText().toString().trim();
        String selectedGender = getSelectedGender();
        String password = editTextPassword.getText().toString();
        String confirmPassword = editTextConfirmPassword.getText().toString();

        String selectedDepartment = spinnerDepartment.getSelectedItem().toString();
        String designation = editTextDesignation.getText().toString().trim();
        String mobile = editTextMobile.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        String hashedConfirmpassword = BCrypt.hashpw(confirmPassword, BCrypt.gensalt());




        // Perform validations
        if (TextUtils.isEmpty(firstName)) {
            editTextFirstName.setError("Please enter your first name");
            return;
        }

        if (!isValidAlphabeticName(firstName)) {
            editTextFirstName.setError("First name should contain only alphabets");
            return;
        }


        if (TextUtils.isEmpty(lastName)) {
            editTextLastName.setError("Please enter your last name");
            return;
        }

        if (!isValidAlphabeticName(lastName)) {
            editTextLastName.setError("Last name should contain only alphabets");
            return;
        }

        if (TextUtils.isEmpty(username)) {
            editTextUsername.setError("Please enter a username");
            return;
        }

        if (TextUtils.isEmpty(selectedGender)) {
            Toast.makeText(this, "Please select your gender", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError("Please enter a password");
            return;
        }

        if (!isValidPassword(password)) {
            editTextPassword.setError("Password must be at least 8 characters and contain at least one uppercase letter, one lowercase letter, and one special character");
            return;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            editTextConfirmPassword.setError("Please confirm your password");
            return;
        }

        if (!password.equals(confirmPassword)) {
            editTextConfirmPassword.setError("Passwords do not match");
            return;
        }


        if (selectedDepartment.equals("Select Department")) {
            // User must select a department other than the default
            Toast.makeText(this, "Please select a department", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(designation)) {
            editTextDesignation.setError("Please enter your designation");
            return;
        }

        if (TextUtils.isEmpty(mobile)) {
            editTextMobile.setError("Please enter your mobile number");
            return;
        }

        if (!isValidMobileNumber(mobile)) {
            editTextMobile.setError("Please enter a valid 10-digit mobile number");
            return;
        }

        if (TextUtils.isEmpty(email)) {
            editTextEmail.setError("Please enter your email address");
            return;
        }

        if (!isValidEmail(email)) {
            editTextEmail.setError("Please enter a valid email address");
            return;
        }

        checkUsernameAvailability( hashedPassword,
                 hashedConfirmpassword,
                firstName,
                 lastName,
                 username,
                selectedDepartment,
                 designation,
                 selectedGender,
               mobile,
                 email);

        // Add more validations as needed

        // If all validations pass, you can proceed with the registration logic here

        // Display a success message

        //Toast.makeText(this, "Registration successful on the form", Toast.LENGTH_SHORT).show();


    }



    private void checkUsernameAvailability(String hashedPassword,
                                           String hashedConfirmpassword,
                                           String firstName,
                                           String lastName,
                                           String username,
                                           String selectedDepartment,
                                           String designation,
                                           String selectedGender,
                                           String mobile,
                                           String email) {
        // Reference to the users collection
        CollectionReference usersCollection = db.collection(selectedDepartment);

        // Query to check if the username already exists
        usersCollection.whereEqualTo("username", username).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Check if any documents match the query
                        if (!task.getResult().isEmpty()) {
                            // Username is taken, show an error message
                            editTextUsername.setError("Username is already taken. Please choose another.");
                        } else {
                            // Username is available, proceed with user registration
                            saveDataToFirestore(
                                    hashedPassword,
                                    hashedConfirmpassword,
                                    firstName,
                                    lastName,
                                    username,
                                    selectedDepartment,
                                    designation,
                                    selectedGender,
                                    mobile,
                                    email
                            );
                        }
                    } else {
                        // Error occurred while checking username availability
                        Toast.makeText(Register_page.this, "Error checking username availability.", Toast.LENGTH_SHORT).show();
                    }
                });
    }





    private String getSelectedGender() {
        int selectedRadioButtonId = radioGroupGender.getCheckedRadioButtonId();

        if (selectedRadioButtonId != -1) {
            RadioButton selectedRadioButton = findViewById(selectedRadioButtonId);
            return selectedRadioButton.getText().toString();
        }

        return "";
    }

    private boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isValidMobileNumber(String mobile) {
        // Check if the mobile number is exactly 10 digits
        return TextUtils.isDigitsOnly(mobile) && mobile.length() == 10;
    }

    private boolean isValidPassword(String password) {
        // Password must be at least 8 characters
        if (password.length() < 8) {
            return false;
        }

        // Check for at least one uppercase letter
        if (!containsUppercase(password)) {
            return false;
        }

        // Check for at least one lowercase letter
        if (!containsLowercase(password)) {
            return false;
        }

        // Check for at least one special character
        if (!containsSpecialCharacter(password)) {
            return false;
        }

        return true;
    }

    private boolean containsUppercase(String password) {
        return !password.equals(password.toLowerCase());
    }

    private boolean containsLowercase(String password) {
        return !password.equals(password.toUpperCase());
    }

    private boolean containsSpecialCharacter(String password) {
        Pattern specialCharPattern = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]");
        Matcher matcher = specialCharPattern.matcher(password);
        return matcher.find();
    }

    private boolean isValidAlphabeticName(String name) {
        // Check if the name contains only alphabets
        return name.matches("[a-zA-Z]+");
    }

    private void saveDataToFirestore(
            String hashedPassword,
            String hashedConfirmpassword,
            String firstName,
            String lastName,
            String username,
            String selectedDepartment,
            String designation,
            String selectedGender,
            String mobile,
            String email
    ) {
        // Reference to the department collection for the specified department
        CollectionReference departmentCollection = db.collection(selectedDepartment);

        getNextDocumentId(selectedDepartment, new OnSuccessListener<String>() {
            @Override
            public void onSuccess(String nextDocumentId) {
                Map<String, Object> user = new HashMap<>();
                user.put("firstName", firstName);
                user.put("lastName", lastName);
                user.put("username", username);
                user.put("designation", designation);
                user.put("gender", selectedGender);
                user.put("mobile", mobile);
                user.put("email", email);
                user.put("password", hashedPassword);
                user.put("Confirm Password", hashedConfirmpassword);
                user.put("timestamp", FieldValue.serverTimestamp());

                // Add a new document with the specified ID to the department collection
                departmentCollection.document(nextDocumentId)
                        .set(user)
                        .addOnSuccessListener(aVoid -> {
                            // Document added successfully
                            Toast.makeText(Register_page.this, "User registration successful", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(Register_page.this, Login_Page.class);
                            startActivity(intent);
                        })
                        .addOnFailureListener(e -> {
                            // Error adding document
                            Toast.makeText(Register_page.this, "Error adding user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        });
    }

    // ... (your existing code)

    private void getNextDocumentId(String originalDepartment, OnSuccessListener<String> successListener) {
        // Reference to the department collection for the specified department
        CollectionReference departmentCollection = db.collection(originalDepartment);
        String department = originalDepartment;


        if(originalDepartment.equals("Computer Technology"))
        {
            department="cm";
        }

        if(originalDepartment.equals("Mechanical Engineering"))
        {
            department="me";
        }

        if(originalDepartment.equals("Civil Engineering"))
        {
            department="ce";
        }

        if(originalDepartment.equals("Electrical Engineering"))
        {
            department="ee";
        }

        if(originalDepartment.equals("Mechatronic Engineering"))
        {
            department="mk";
        }

        if(originalDepartment.equals("Information Technology"))
        {
            department="if";
        }

        if(originalDepartment.equals("Polymer Engineering"))
        {
            department="pe";
        }

        if(originalDepartment.equals("Electronic & Telecommunication"))
        {
            department="entc";
        }

        if(originalDepartment.equals("Automobile Engineering"))
        {
            department="ae";
        }

        if(originalDepartment.equals("Dress Design & Garment Manu."))
        {
            department="ddgm";
        }

        if(originalDepartment.equals("Interior Design"))
        {
            department="idd";
        }

        if(originalDepartment.equals("Other Department"))
        {
            department="ot";
        }

        // Query to get the last document in the department collection
        String finalDepartment = department;
        String finalDepartment1 = department;
        departmentCollection
                .orderBy("timestamp", Query.Direction.DESCENDING).limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // If there are no documents, set the next document ID to 1
                    String nextDocumentId = finalDepartment.toLowerCase() + "1";

                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Get the last document
                        DocumentSnapshot lastDocument = queryDocumentSnapshots.getDocuments().get(0);

                        // Extract the last document ID
                        String lastDocumentId = lastDocument.getId();

                        // Parse the last document ID to get the number part
                        String[] parts = lastDocumentId.split(finalDepartment.toLowerCase());
                        if (parts.length == 2) {
                            // Extract the number part and increment it
                            int lastNumber = Integer.parseInt(parts[1]);
                            int nextNumber = lastNumber + 1;

                            // Set the next document ID
                            nextDocumentId = finalDepartment.toLowerCase() + nextNumber;
                        }
                    }

                    // Log the next document ID for debugging
                    Log.d("Next Document ID", nextDocumentId);

                    // Invoke the success listener with the next document ID
                    successListener.onSuccess(nextDocumentId);
                })
                .addOnFailureListener(e -> {
                    // Log the error for debugging
                    Log.e("Firestore Error", "Error getting documents.", e);

                    // Handle the failure
                    // For simplicity, you might want to set a default next document ID
                    successListener.onSuccess(finalDepartment1.toLowerCase() + "1");
                });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }


}