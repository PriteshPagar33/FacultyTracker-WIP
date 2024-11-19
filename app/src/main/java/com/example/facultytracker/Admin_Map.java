package com.example.facultytracker;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.graphics.Color;
import android.os.Bundle;
import android.graphics.Color;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polygon;

import android.view.View;
import android.widget.ImageButton;


import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.views.MapView;

import java.util.ArrayList;

public class Admin_Map extends AppCompatActivity {

    private MapView mapView;
    private static final String TAG = "GeofenceActivity";

    private static final double COLLEGE_LATITUDE = 19.9528;
    private static final double COLLEGE_LONGITUDE = 73.8625;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_map);
        ImageButton backButton = findViewById(R.id.backB);



        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showbackAlert();
            }
        });

        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));

        // Find the map view
        mapView = findViewById(R.id.mapView);

        // Set the tile source to a standard map
        mapView.setTileSource(TileSourceFactory.MAPNIK);

        mapView.addOnFirstLayoutListener(new MapView.OnFirstLayoutListener() {
            @Override
            public void onFirstLayout(View v, int left, int top, int right, int bottom) {
                // Set the initial map center and zoom level after the map is loaded
                mapView.getController().setCenter(new GeoPoint(COLLEGE_LATITUDE, COLLEGE_LONGITUDE));
                mapView.getController().setZoom(15.0);  // Adjust the zoom level as needed

                // Add geofence circle to the map
                Admin_Map.this.addGeofenceToMap(new GeoPoint(COLLEGE_LATITUDE, COLLEGE_LONGITUDE), 0.25);
            }
        });






    }


    private void addGeofenceToMap(GeoPoint center, double radius) {
        // Calculate the vertices of the circle
        ArrayList<GeoPoint> circlePoints = new ArrayList<>();
        int numberOfPoints = 100;  // You can adjust this value for a smoother circle

        for (int i = 0; i < numberOfPoints; i++) {
            double theta = ((double) i / numberOfPoints) * (2 * Math.PI);
            double lat = center.getLatitude() + radius / 111.32 * Math.cos(theta);
            double lon = center.getLongitude() + radius / (111.32 * Math.cos(center.getLatitude() * Math.PI / 180)) * Math.sin(theta);
            circlePoints.add(new GeoPoint(lat, lon));
        }

        // Create a polygon using the circle points
        Polygon geofenceCircle = new Polygon();
        geofenceCircle.setPoints(circlePoints);
        geofenceCircle.setFillColor(Color.TRANSPARENT);  // Transparent fill color
        geofenceCircle.setStrokeColor(Color.BLUE);  // Circle stroke color
        geofenceCircle.setStrokeWidth(2f);         // Circle stroke width

        mapView.getOverlays().add(geofenceCircle);

        // Invalidate the map to redraw
        mapView.invalidate();
    }

    private void showbackAlert() {
        // Show an alert with "OK" and "Cancel" buttons
        new AlertDialog.Builder(this)
                .setTitle("Exit App")
                .setMessage("Are you sure you want to close the app?.")
                .setPositiveButton("OK", (dialog, which) -> {
                    // Log out the user or take any necessary actions here
                    // For now, simply close the app
                    finishAffinity();


                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    // Dismiss the dialog and do nothing
                    dialog.dismiss();
                })
                .setCancelable(false) // To prevent dismiss by tapping outside the dialog
                .show();

    }

}