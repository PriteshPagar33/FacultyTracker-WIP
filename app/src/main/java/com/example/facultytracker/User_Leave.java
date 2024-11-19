package com.example.facultytracker;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;


public class User_Leave extends AppCompatActivity {

    private EditText editTextStartDate, editTextEndDate, editTextReason;
    private RadioGroup radioGroupLeaveType;
    private Button buttonApply;


    private DatabaseReference databaseReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_leave);


        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference();
        if (databaseReference != null) {
            // Your database operations here
            Log.d("LeaveApplicationError", "Database reference is not null");
        } else {
            Log.e("LeaveApplicationError", "Database reference is null");
            Toast.makeText(User_Leave.this, "Failed to submit leave application. Database reference is null", Toast.LENGTH_SHORT).show();
        }



        editTextStartDate = findViewById(R.id.editTextStartDate);
        editTextEndDate = findViewById(R.id.editTextEndDate);
        editTextReason = findViewById(R.id.editTextReason);
        radioGroupLeaveType = findViewById(R.id.radioGroupLeaveType);
        buttonApply = findViewById(R.id.buttonApply);

        buttonApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                SharedPreferences preference= getSharedPreferences("MyPrefs", MODE_PRIVATE);
                String username = preference.getString("username", "");

                String branch = preference.getString("selectedDepartment", "");


                    applyLeave(username,branch);

            }
        });
    }


    // ...

    private void applyLeave(String Username, String Branch)  {



        Log.d("LeaveApplication", "Applying leave...");
        String startDate = editTextStartDate.getText().toString().trim();
        String endDate = editTextEndDate.getText().toString().trim();
        String reason = editTextReason.getText().toString().trim();

        int selectedRadioButtonId = radioGroupLeaveType.getCheckedRadioButtonId();
        RadioButton selectedRadioButton = findViewById(selectedRadioButtonId);
        String leaveType = selectedRadioButton.getText().toString();



        // Check if any field is empty
        if (reason.isEmpty() || leaveType.isEmpty()) {
            Toast.makeText(this, "Please fill in all the fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get the current month and year
        SimpleDateFormat sdf = new SimpleDateFormat("MMM yyyy");
        String currentMonthYear = sdf.format(Calendar.getInstance().getTime());
        SimpleDateFormat currentDate = new SimpleDateFormat("yyyy-MM-dd");
        String applicationDate = currentDate.format(Calendar.getInstance().getTime());


        // Create a Map to store leave application data
        Map<String, Object> leaveApplication = new HashMap<>();
        leaveApplication.put("User Name", Username);
        //leaveApplication.put("startDate", startDate);
        //leaveApplication.put("endDate", endDate);
        leaveApplication.put("leaveType", leaveType);
        leaveApplication.put("reason", reason);
        //leaveApplication.put("applicationDate", applicationDate);
        leaveApplication.put("status", "applied");

        // Add the leave application to the Realtime Database
        if (databaseReference != null) {
            databaseReference.child(Branch) // Use the branch directly as the parent node
                    .child("LeaveData").child(currentMonthYear)
                    .child("Applications") // Add this to organize applications under each month
                    .push() // Push generates a unique ID for the application
                    .setValue(leaveApplication)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d("LeaveApplication", "Leave application submitted successfully");
                                Toast.makeText(User_Leave.this, "Leave application submitted successfully", Toast.LENGTH_SHORT).show();
                                // Clear the form after submission if needed
                                clearForm();
                            } else {
                                Toast.makeText(User_Leave.this, "Failed to submit leave application", Toast.LENGTH_SHORT).show();
                                Log.d("LeaveApplicationError", "Failed to submit leave application", task.getException());
                            }
                        }
                    });

        } else {
            Log.d("LeaveApplicationError", "Database reference is null");
            Toast.makeText(User_Leave.this, "Failed to submit leave application. Database reference is null", Toast.LENGTH_SHORT).show();
        }
    }
// ...


    private void clearForm() {
        editTextStartDate.setText("");
        editTextEndDate.setText("");
        editTextReason.setText("");
        radioGroupLeaveType.clearCheck();
    }
}
