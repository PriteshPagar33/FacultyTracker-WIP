package com.example.facultytracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

// AboutUsActivity.java
public class About_Us extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);

        // Find card views and set names and designations
        CardView cardView1 = findViewById(R.id.cardView1);
        CardView cardView2 = findViewById(R.id.cardView2);
        CardView cardView3 = findViewById(R.id.cardView3);
        CardView cardView4 = findViewById(R.id.cardView4);

        setTeamMemberData(cardView1, "Pritesh Pagar", "Software Engineer");
        setTeamMemberData(cardView2, "Mrunal Thakare", "UI/UX Designer");
        setTeamMemberData(cardView3, "Pranav Mahale", "Marketing Manager");
        setTeamMemberData(cardView4, "Shruti Sardesai", "Product Manager");
    }

    private void setTeamMemberData(CardView cardView, String name, String designation) {
        TextView textName1 = cardView.findViewById(R.id.textName1);
        TextView textDesignation1 = cardView.findViewById(R.id.textDesignation1);

       TextView textName2 = cardView.findViewById(R.id.textName2);
        TextView textDesignation2= cardView.findViewById(R.id.textDesignation2);

        TextView textName3 = cardView.findViewById(R.id.textName3);
        TextView textDesignation3= cardView.findViewById(R.id.textDesignation3);

        TextView textName4 = cardView.findViewById(R.id.textName4);
        TextView textDesignation4= cardView.findViewById(R.id.textDesignation4);



        if (textName1 != null && textDesignation1 != null) {
            textName1.setText(name);
            textDesignation1.setText(designation);
        } else {
            Log.e("About_Us", "TextView not found in CardView");
        }

        if (textName2 != null && textDesignation2 != null) {
            textName2.setText(name);
            textDesignation2.setText(designation);
        } else {
            Log.e("About_Us", "TextView not found in CardView");
        }

        if (textName3 != null && textDesignation3 != null) {
            textName3.setText(name);
            textDesignation3.setText(designation);
        } else {
            Log.e("About_Us", "TextView not found in CardView");
        }

        if (textName4 != null && textDesignation4 != null) {
            textName4.setText(name);
            textDesignation4.setText(designation);
        } else {
            Log.e("About_Us", "TextView not found in CardView");
        }


    }


}


