package com.example.facultytracker;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;


import com.google.android.gms.maps.model.LatLng;

import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;


import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.GeoPoint;

public class BackgroundService extends Service {

    private LocationRequest mLocationRequest;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private static final String TAG = "BackgroundService";

    private static final double COLLEGE_LATITUDE = 19.9528;
    private static final double COLLEGE_LONGITUDE = 73.8625;
    private long UPDATE_INTERVAL = 10 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 2000; /* 2 sec */

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");

        startLocationUpdates();
        getLastLocation();
        createNotificationChannel();
        super.onCreate();
        // Initialization code if needed
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                // Check whether the user has exited the geofence
                double geofenceLatitude = COLLEGE_LATITUDE;
                double geofenceLongitude = COLLEGE_LONGITUDE;
                float geofenceRadius = 0.25f;

                float distance = distanceBetween(
                        (float) location.getLatitude(), (float) location.getLongitude(),
                        (float) geofenceLatitude, (float) geofenceLongitude);

                if (distance > geofenceRadius) {
                    Log.d(TAG, "Notification Sent!!");
                    // User has exited the geofence, perform actions
                    showNotification("Geofence Alert", "You have exited the geofence.");
                }

                // Display the updated location
                String msg = "Updated Location: " +
                        Double.toString(location.getLatitude()) + "," +
                        Double.toString(location.getLongitude());
                Toast.makeText(BackgroundService.this, msg, Toast.LENGTH_SHORT).show();

                // You can now create a GeoPoint Object for use with maps
                GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                // Use the GeoPoint as needed (e.g., for updating map markers)
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        };

        // Register for location updates
        if (locationManager != null) {
            try {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListener);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");

        startLocationUpdates();
        getLastLocation();

        // Return START_STICKY to restart the service if it gets terminated
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
        stopLocationUpdates();
        if (locationManager != null && locationListener != null) {
            locationManager.removeUpdates(locationListener);
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        // Not used for a started service
        Log.d(TAG, "onBind");
        return null;
    }


    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Log.d(TAG, "onLocationResult");
            // do work here
            onLocationChanged(locationResult.getLastLocation());
        }
    };

    protected void startLocationUpdates() {
        Log.d(TAG, "startLocationUpdates");

        // Create the location request to start receiving updates
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

        // Create LocationSettingsRequest object using location request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();


        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(locationSettingsRequest);

        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions

            return;
        }
        getFusedLocationProviderClient(this).requestLocationUpdates(mLocationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        // do work here
                        onLocationChanged(locationResult.getLastLocation());
                    }
                },
                Looper.myLooper());


    }

    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged");
        // New location has now been determined
        String msg = "Updated Location: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());

        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        // You can now create a LatLng Object for use with maps
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
    }

    public void getLastLocation() {
        Log.d(TAG, "getLastLocation");
        // Get last known recent location using new Google Play Services SDK (v11+)
        FusedLocationProviderClient locationClient = getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        Log.d(TAG, "getLastLocation onSuccess");

                        // GPS location can be null if GPS is switched off
                        if (location != null) {
                            onLocationChanged(location);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "getLastLocation onFailure");
                        Log.d("MapDemoActivity", "Error trying to get last GPS location");
                        e.printStackTrace();
                    }
                });


    }

    public void stopLocationUpdates() {
        Log.d(TAG, "stopLocationUpdates");
        getFusedLocationProviderClient(this).removeLocationUpdates(locationCallback);
    }

    private float distanceBetween(float lat1, float lon1, float lat2, float lon2) {
        Log.d(TAG, "distanceBetween");
        float[] result = new float[1];
        Location.distanceBetween(lat1, lon1, lat2, lon2, result);
        return result[0];
    }

    private void showNotification(String title, String message) {
        Log.d(TAG, "Pop Notification code");

        Context context = getApplicationContext(); // Get your application context here

        // Check if the notification channel exists
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            NotificationChannel channel = notificationManager.getNotificationChannel("Pranav 123");
            if (channel == null) {
                Log.e(TAG, "Notification channel does not exist");
                return;
            }
        }

        Intent intent = new Intent(context, User_Home.class); // Create an Intent for the action you want when notification is clicked
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "channelId")
                .setSmallIcon(R.drawable.ic_logout) // Customize with your notification icon
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true); // Removes the notification when the user taps on it

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId("Pranav 123");
        }

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        int notificationId = 12;
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Missing notification permission");
            // Handle missing permission, request it, or log an error
            return;
        }
        notificationManager.notify(notificationId, builder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = getSystemService(NotificationManager.class);

            // Check if the notification channel already exists
            NotificationChannel channel = notificationManager.getNotificationChannel("Pranav 123");
            if (channel == null) {
                // Create the notification channel if it doesn't exist
                channel = new NotificationChannel("Pranav 123", "Your Channel Name", NotificationManager.IMPORTANCE_HIGH);
                channel.setDescription("Your Channel Description");
                notificationManager.createNotificationChannel(channel);
            }
        }
    }






}
