package com.example.facultytracker;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class Admin_Faculty_Info extends AppCompatActivity {

    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_faculty_info);

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance();
        SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String branch = preferences.getString("Branch", "DefaultBranch");
        Log.d("SharedPreferences Info Page", "Branch: " + branch);


        // Fetch faculty information
        fetchFacultyInformation(branch);
    }

    private void fetchFacultyInformation(String branch) {
        firestore.collection(branch)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            // Clear existing rows except for the header
                            TableLayout tableLayout = findViewById(R.id.facultyTable);
                            int childCount = tableLayout.getChildCount();
                            for (int i = 1; i < childCount; i++) {
                                View child = tableLayout.getChildAt(i);
                                if (child instanceof TableRow) {
                                    tableLayout.removeView(child);
                                }
                            }

                            // Add data rows
                            for (DocumentSnapshot document : task.getResult()) {
                                String name = document.getString("firstName");
                                String designation = document.getString("designation");
                                String mobile = document.getString("mobile");
                                String email = document.getString("email");

                                addTableRow(name, designation, mobile, email);
                            }
                        } else {
                            // Handle errors
                            // e.g., task.getException().getMessage()
                        }
                    }
                });
    }


    private void addTableRow(String name, String designation, String mobile, String email) {
        TableLayout tableLayout = findViewById(R.id.facultyTable);

        TableRow tableRow = new TableRow(this);

        // Create TextViews for each column
        TextView nameTextView = createTextView(name);
        TextView designationTextView = createTextView(designation);
        TextView mobileTextView = createTextView(mobile);
        TextView emailTextView = createTextView(email);

        // Add TextViews to the TableRow
        tableRow.addView(nameTextView);
        tableRow.addView(designationTextView);
        tableRow.addView(mobileTextView);
        tableRow.addView(emailTextView);

        // Add TableRow to the TableLayout
        tableLayout.addView(tableRow);
    }

    private TextView createTextView(String text) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setPadding(8, 8, 8, 8);
        return textView;
    }
}
