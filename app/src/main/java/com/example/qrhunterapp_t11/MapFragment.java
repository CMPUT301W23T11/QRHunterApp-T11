package com.example.qrhunterapp_t11;//package com.example.qrhunterapp_t11;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.OnMapsSdkInitializedCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.MapsInitializer.Renderer;
import com.google.android.gms.maps.OnMapsSdkInitializedCallback;

public class MapFragment extends Fragment implements OnMapReadyCallback, OnMapsSdkInitializedCallback {

    private static final String TAG = "MapFragment";
    public static final int errorDialogRequest = 9001;
    public static final int permissionsRequestEnableGPS = 9002;
    public static final int permissionsRequestAccessFineLocation = 9003;
    private GoogleMap mMap;
    private boolean mLocationPermissionGranted = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.google_map);
        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
            getChildFragmentManager().beginTransaction().replace(R.id.google_map, mapFragment).commit();
        }
        checkMapServices();
        mapFragment.getMapAsync(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        MapsInitializer.initialize(getActivity().getApplicationContext(), MapsInitializer.Renderer.LATEST, this);
        return view;
    }

    private boolean checkMapServices() {
        Log.d(TAG, "checkMapServices");
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!isGPSEnabled) {
            buildAlertMessageNoGps();
            return false;
        }
        if (isServicesOK()) {
            if (isMapsEnabled()) {
                Log.d(TAG, "checkMapServices: True");
                mLocationPermissionGranted = true;
                SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.google_map);
                if (mapFragment == null) {
                    mapFragment = SupportMapFragment.newInstance();
                    getChildFragmentManager().beginTransaction().replace(R.id.google_map, mapFragment).commit();
                }
                mapFragment.getMapAsync(this);
                return true;
            }
        }
        return false;
    }

    @Override
    public void onMapsSdkInitialized(MapsInitializer.Renderer renderer) {
        switch (renderer) {
            case LATEST:
                Log.d(TAG, "The latest version of the renderer is used.");
                break;
            case LEGACY:
                Log.d(TAG, "The legacy version of the renderer is used.");
                break;
        }
    }

    //Device GPS turn on
    private void buildAlertMessageNoGps() {
        Log.d(TAG, "buildAlertMessageNoGps");
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("This application requires GPS to work properly, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            Intent gpsOptionsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(gpsOptionsIntent);
                            //ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, permissionsRequestAccessFineLocation);
                            getLocationPermission();
                        } else {
                            Intent gpsOptionsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(gpsOptionsIntent);
                            getLocationPermission();
                            mLocationPermissionGranted = true;
                            displayMap();
                        }
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    //checks if the current app allows gps location
    public boolean isMapsEnabled() {
        final LocationManager manager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        boolean isGpsEnabled = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!isGpsEnabled) {
            buildAlertMessageNoGps();
            Log.d(TAG, "isMapEnabled: False");
            return false;
        }
        else if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, permissionsRequestAccessFineLocation);
            getLocationPermission();
            Log.d(TAG, "isMapEnabled: Location permission not granted");
            return false;
        }
        else {
            Log.d(TAG, "isMapEnabled: True");
            return true;
        }
    }

    private void getLocationPermission() {
        Log.d(TAG, "getLocationPermission");
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
            displayMap();
        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, permissionsRequestAccessFineLocation);
        }
    }

    //checks if google services is available on device
    private boolean isServicesOK() {
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getActivity());
        if (available == ConnectionResult.SUCCESS) {
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            //an error occurred but we can resolve it
            Log.d(TAG, "isServicesOK: an error occurred but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(getActivity(), available, errorDialogRequest);
            dialog.show();
        } else {
            Toast.makeText(getActivity(), "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case permissionsRequestEnableGPS:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "onRequestPermissionsResult: GPS permission granted");
                    displayMap();
                } else {
                    Log.d(TAG, "onRequestPermissionsResult: GPS permission denied");
                }
                break;
            case permissionsRequestAccessFineLocation:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "onRequestPermissionsResult: Location permission granted");
                    mLocationPermissionGranted = true;
                    displayMap();
                } else {
                    Log.d(TAG, "onRequestPermissionsResult: Location permission denied");
                }
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.d(TAG, "onActivityResult: called.");
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case permissionsRequestEnableGPS:
                if (mLocationPermissionGranted) {
                    displayMap();
                } else {
                    getLocationPermission();
                }
                break;
        }
    }

    private void displayMap() {
        Log.d(TAG, "displayMap");
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.google_map);
        mapFragment.getMapAsync((OnMapReadyCallback) this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady");
        mMap = googleMap;
//        while (checkMapServices() == false) {
//            Log.d(TAG, "looping");
//        }
        if (mLocationPermissionGranted) {
            Log.d(TAG, "myLocationPermissionGranted");
            try {
                mMap.setMyLocationEnabled(true);
                FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                if (location != null) {
                                    // Create a LatLng object with the current location
                                    LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());

                                    // Create a CameraUpdate object with zoom level 15 and move the camera to the current location
                                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(currentLocation, 15);
                                    mMap.animateCamera(cameraUpdate);
                                }
                            }
                        });
            } catch (SecurityException e) {
                Log.e(TAG, "SecurityException: " + e.getMessage());
            }
        }
    }
}
