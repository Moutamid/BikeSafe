package com.example.g1_final_project.fragments;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.service.controls.Control;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.example.g1_final_project.R;
import com.example.g1_final_project.activities.LoginActivity;
import com.example.g1_final_project.activities.MainActivity;
import com.example.g1_final_project.databinding.FragmentHomeBinding;
import com.example.g1_final_project.models.HistoryItemModel;
import com.example.g1_final_project.utils.Constants;
import com.example.g1_final_project.utils.Controller;
import com.fxn.stash.Stash;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.BuildConfig;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import timerx.Stopwatch;
import timerx.StopwatchBuilder;
import timerx.TimeTickListener;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";

    private FragmentHomeBinding b;
    private double currentMileagesDouble = 0;
    private double totalMileagesDouble = 0;
    private double finalDistancee = 0;

    private double finalDistancec = 0;

    private LocationManager locationManager;
    private LocationListener locationListener;

    private YoYo.YoYoString gpsAnimation;

    private int LOCATION_REQUEST_CODE = 10001;

    private FusedLocationProviderClient fusedLocationProviderClient;

    private LocationRequest locationRequest;

    private boolean locationRequested = true;
    private Location startLocation;

    boolean isStarted = false;
    private ProgressDialog progressDialog;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        b = FragmentHomeBinding.inflate(inflater, container, false);
        View root = b.getRoot();

        if (!Stash.getBoolean(Constants.IS_LOGGED_IN, false)) {
            return b.getRoot();
        }

        progressDialog = new ProgressDialog(requireContext());
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading...");

        b.textView5.setText("HELLO, " + Constants.userModel().name + "!");

        b.dashDuration.setText(Stash.getString(Constants.CURRENT_TIME, "0 hrs 0 mins"));

        Constants.databaseReference().child(Constants.auth().getUid())
                .child(Constants.CURRENT_MILEAGES)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists() && isAdded()) {
                            currentMileagesDouble = snapshot
                                    .getValue(Double.class);

                            String value = String.valueOf(currentMileagesDouble);

                            b.dashKilometer.setText(value + " mi");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        b.btnStartJourney.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dexter.withContext(requireContext())
                        .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                        .withListener(new PermissionListener() {
                            @Override
                            public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                                Log.d(TAG, "onPermissionGranted: isStarted: " + isStarted);
                                if (isStarted) {
                                    stopLocationUpdates();
                                    isStarted = false;
                                    b.btnStartJourney.setText("START\nJOURNEY");
                                    b.btnStartJourney.setIcon(getResources().getDrawable(
                                            R.drawable.ic_play_icon));
                                    Controller.stopAnimation();
//                                    Controller.stopStopWatch();
//                                    handler.removeCallbacks(runnable);
                                    timer.cancel();
                                    HistoryItemModel model = new HistoryItemModel();
                                    model.title = "Journey on " + new Date();
                                    model.distance = finalDistancec + "";
                                    model.time = Stash.getString(Constants.CURRENT_TIME);

                                    Constants.databaseReference().child(Constants.auth().getUid())
                                            .child(Constants.HISTORY)
                                            .push()
                                            .setValue(model);

                                } else {
//                                    Controller.startStopWatch(b.dashDuration);
//                                    handler.postDelayed(runnable, 60000);// RUN AT EVERY MINUTE
                                    timer.schedule(new TimerTask() {
                                        @Override
                                        public void run() {
                                            runTimer();
                                        }
                                    }, 60000, 60000);
                                    progressDialog.show();
                                    isStarted = true;
                                    getLastLocation();

                                }
                            }

                            @Override
                            public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                                if (permissionDeniedResponse.isPermanentlyDenied()) {
                                    // open device settings when the permission is
                                    // denied permanently
                                    Toast.makeText(requireContext(), "You need to provide permission!", Toast.LENGTH_SHORT).show();

                                    Intent intent = new Intent();
                                    intent.setAction(
                                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    Uri uri = Uri.fromParts("package",
                                            BuildConfig.APPLICATION_ID, null);
                                    intent.setData(uri);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                }
                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                                permissionToken.continuePermissionRequest();

                            }
                        }).check();
            }
        });
//        handler = new Handler();
        timer = new Timer();

        return root;
    }

    private void runTimer() {
        // THIS WILL RUN AFTER EVERY MINUTE

        if (lastMinutes == 60) {
            lastMinutes = 0;
            Stash.put(Constants.MINUTES, 0);
            lastHours++;
            Stash.put(Constants.HOURS, lastHours);
        } else {
            lastMinutes++;
            Stash.put(Constants.MINUTES, lastMinutes);
        }

           /* new Handler().post(() -> {
            });*/
        requireActivity().runOnUiThread(() -> {
            b.dashDuration.setText(lastHours + " hrs " + lastMinutes + " mins");
        });

        Stash.put(Constants.CURRENT_TIME, lastHours + " hrs " + lastMinutes + " mins");
        Log.d(TAG, "run: triggerAt: "+lastMinutes);
    }

    int lastMinutes = Stash.getInt(Constants.MINUTES, 0);
    int lastHours = Stash.getInt(Constants.HOURS, 0);

    Timer timer;

    /*TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {

        }
    };*/

    /*Handler handler1;
    Runnable runnable1 = () -> {


        handler.postDelayed(runnable, 60000);// RUN AT EVERY MINUTE

    };
*/
    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (locationResult == null && requireActivity() == null
                    && locationResult.getLastLocation() == null) {
                return;
            }

            Location currentLocation = locationResult.getLastLocation();

            if (currentLocation == null)
                return;

            Log.d(TAG, "onLocationResult: currentLocation " + currentLocation.getLatitude());
            Log.d(TAG, "onLocationResult: currentLocation " + currentLocation.getLongitude());

            double distance = (double) startLocation.distanceTo(currentLocation);
            Log.d(TAG, "onLocationResult: distance: " + distance);

            double distanceInMiles = distance / 1609;
            Log.d(TAG, "onLocationResult: finalDistance " + distanceInMiles);

            double currentLocationDistance = currentMileagesDouble + distanceInMiles;

            if (currentLocationDistance < finalDistancee) {
                startLocation = currentLocation;
                if (finalDistancec == 0)
                    Toast.makeText(requireActivity(), "finalDistancec == 0", Toast.LENGTH_SHORT).show();

                currentMileagesDouble = finalDistancec;
                finalDistancee = 0;
                return;
            }

            finalDistancee = 0;
            finalDistancee = currentMileagesDouble + distanceInMiles;

            DecimalFormat df = new DecimalFormat("#.##");
            df.setRoundingMode(RoundingMode.CEILING);

            b.dashKilometer.setText(df.format(finalDistancee));

            finalDistancec = Double.parseDouble(df.format(finalDistancee));

            /*databaseReference
                    .child("cars")
                    .child(currentCarKey)
                    .child("booking")
                    .child("currentMileages")
                    .setValue(finalDistancec);*/
            Constants.databaseReference()
                    .child(Constants.auth().getUid())
                    .child(Constants.CURRENT_MILEAGES)
                    .setValue(finalDistancec);

            Log.d(TAG, "onLocationResult: textview " + b.dashKilometer.getText().toString());
            Log.d(TAG, "--------------------------------------------------------------\n\n\n");
        }
    };

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void stopLocationUpdates() {
        if (fusedLocationProviderClient != null
                && locationCallback != null)
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    private void getLastLocation() {
        String TAG = "TrackerFragment";

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        locationRequest = LocationRequest.create();
        locationRequest.setInterval(4000);
        locationRequest.setFastestInterval(2000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        Task<Location> locationTask = fusedLocationProviderClient.getLastLocation();

        locationTask.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    //We have a location
                    progressDialog.dismiss();
                    Log.d(TAG, "onSuccess: startLocation: " + location.getLatitude());
                    Log.d(TAG, "onSuccess: startLocation: " + location.getLongitude());

                    b.btnStartJourney.setText("STOP\nJOURNEY");
                    b.btnStartJourney.setIcon(getResources().getDrawable(
                            R.drawable.ic_baseline_stop_circle_24));

                    startLocation = location;

                    Controller.startAnimation(b.imgBike);

                    startLocationChecker();
                } else {
                    progressDialog.dismiss();
                    isStarted = false;
                    Controller.stopAnimation();
                    b.btnStartJourney.setText("START\nJOURNEY");
                    b.btnStartJourney.setIcon(getResources().getDrawable(
                            R.drawable.ic_play_icon));
                    Toast.makeText(requireContext(), "Location is null", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onSuccess: Location was null...");
//                    Controller.stopStopWatch();
//                    handler.removeCallbacks(runnable);
                    timer.cancel();
                }
            }
        });
        locationTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                isStarted = false;
                Controller.stopAnimation();
                b.btnStartJourney.setText("START\nJOURNEY");
                b.btnStartJourney.setIcon(getResources().getDrawable(
                        R.drawable.ic_play_icon));
                Toast.makeText(requireContext(), "Location is null", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onSuccess: Location was null...");

                Log.e(TAG, "onFailure: " + e.getLocalizedMessage());
                Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
//                Controller.stopStopWatch();
//                handler.removeCallbacks(runnable);
                timer.cancel();
            }
        });


    }

    private void startLocationChecker() {

        checkSettingsAndStartLocationUpdates();

    }

    @Override
    public void onStop() {
        super.onStop();
        if (requireActivity() != null)
            stopLocationUpdates();
//        Controller.stopStopWatch();
//        handler.removeCallbacks(runnable);
        timer.cancel();
    }

    private void checkSettingsAndStartLocationUpdates() {
        LocationSettingsRequest request = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest).build();
        SettingsClient client = LocationServices.getSettingsClient(requireActivity());

        Task<LocationSettingsResponse> locationSettingsResponseTask = client.checkLocationSettings(request);
        locationSettingsResponseTask.addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                //Settings of device are satisfied and we can start location updates
                startLocationUpdates();
            }
        });
        locationSettingsResponseTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    ResolvableApiException apiException = (ResolvableApiException) e;
                    try {
                        apiException.startResolutionForResult(requireActivity(), 1001);
                    } catch (IntentSender.SendIntentException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            }
        }
    }


}