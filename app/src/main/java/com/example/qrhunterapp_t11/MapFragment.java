package com.example.qrhunterapp_t11;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
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
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.MapsInitializer.Renderer;
import com.google.android.gms.maps.OnMapsSdkInitializedCallback;

import java.io.IOException;
import java.util.List;

/**
 * Everything to do with google maps
 * Asks for location permissions and zooms in on current location.
 *
 * @author Daniel Guo
 */
public class MapFragment extends Fragment implements OnMapReadyCallback, OnMapsSdkInitializedCallback {

    private static final String TAG = "MapFragment";
    public static final int errorDialogRequest = 9001;
    public static final int permissionsRequestEnableGPS = 9002;
    public static final int permissionsRequestAccessFineLocation = 9003;
    private GoogleMap mMap;
    private boolean mLocationPermissionGranted = false;
    private SearchView searchView;

    /**
     *Called when the fragment is created.
     *Initializes the map fragment, checks if GPS is enabled, and calls getMapAsync to notify when the map is ready to use.
     *
     *@param savedInstanceState Bundle object containing the instance state of the fragment.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.google_map);
        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
            getChildFragmentManager().beginTransaction().replace(R.id.google_map, mapFragment).commit();
        }
        checkMapWorking();
        mapFragment.getMapAsync(this);
    }

    /**
     *Called when the fragment's UI is being created.
     *Initializes the layout of the fragment and initializes the Google Maps API.
     *
     *@param inflater LayoutInflater object used to inflate any views in the fragment.
     *@param container ViewGroup object that will contain the inflated views.
     *@param savedInstanceState Bundle object containing the instance state of the fragment.
     *
     *@return View object representing the fragment's UI.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        MapsInitializer.initialize(getActivity().getApplicationContext(), MapsInitializer.Renderer.LATEST, this);
        searchView = view.findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchLocation(query);
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return view;
    }

    private void searchLocation(String location) {
        Geocoder geocoder = new Geocoder(getContext());
        try {
            List<Address> addresses = geocoder.getFromLocationName(location, 1);
            if (addresses.size() > 0) {
                Address address = addresses.get(0);
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f));
            } else {
                Toast.makeText(getContext(), "Location not found", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *Checks if GPS is enabled and requests permission to use the device's location if necessary.
     */
    private void checkMapWorking() {
        Log.d(TAG, "checkMapWorking");
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!isGPSEnabled) {
            buildAlertMessageNoGps();
        }
        if (isServicesOK()) {
            if (isLocationEnabled()) {
                mLocationPermissionGranted = true;
                SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.google_map);
                if (mapFragment == null) {
                    mapFragment = SupportMapFragment.newInstance();
                    getChildFragmentManager().beginTransaction().replace(R.id.google_map, mapFragment).commit();
                }
                mapFragment.getMapAsync(this);
            }
        }
    }

    /**
     *Displays a dialog prompting the user to enable GPS.
     */
    private void buildAlertMessageNoGps() {
        Log.d(TAG, "buildAlertMessageNoGps");
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("This application requires GPS to work properly, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", (dialog, id) -> {
                    if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        Intent gpsOptionsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(gpsOptionsIntent);
                        getLocationPermission();
                    } else {
                        Intent gpsOptionsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(gpsOptionsIntent);
                        getLocationPermission();
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

    /**
     *Checks if the app has permission to access fine location.
     *
     *@return true if the app has permission to access fine location, false otherwise.
     */
    public boolean isLocationEnabled() {
        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            getLocationPermission();
            Log.d(TAG, "isLocationEnabled: No");
            return false;
        }
        else {
            Log.d(TAG, "isLocationEnabled: Yes");
            return true;
        }
    }

    /**
     * Request location permission, so that we can get the location of the
     * device. The result of the permission request is handled by a callback,
     * onRequestPermissionsResult.
     */
    private void getLocationPermission() {
        Log.d(TAG, "getLocationPermission");
        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
            displayMap();
        } else {
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, permissionsRequestAccessFineLocation);
        }
    }

    /**
     *Handles the result of a permission request by the user for enabling GPS or accessing fine location.
     *
     *@param requestCode the code used for the request
     *@param permissions the requested permissions
     *@param grantResults the results of the request
     */
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

    /**
     *Handles the result of an activity launched for a permission request by the user for enabling GPS.
     *
     *@param requestCode the code used for the request
     *@param resultCode the result of the request
     *@param data the data returned from the request
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.d(TAG, "onActivityResult");
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

    /**
     *Called when the map is ready to be used. Sets the map to the GoogleMap object passed in as a parameter and sets the current location if permission is granted.
     *
     *@param googleMap the GoogleMap object to set the map to
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady");
        mMap = googleMap;
        if (mLocationPermissionGranted) {
            Log.d(TAG, "myLocationPermissionGranted");
            try {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                //mMap.setPadding(0, 0, 0, 0);

                // Add marker at user's current location
                LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
                Criteria criteria = new Criteria();
                Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
                if (location != null) {
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    MarkerOptions markerOptions = new MarkerOptions()
                            .position(latLng)
                            .title("Current location");
                    mMap.addMarker(markerOptions);
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f));
                }
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

    /**
     * Displays the map on the screen.
     */
    private void displayMap() {
        Log.d(TAG, "displayMap");
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.google_map);
        mapFragment.getMapAsync((OnMapReadyCallback) this);
    }

    /**
     * Checks if Google Play Services is available on the device.
     *
     * @return True if Google Play Services is available, False otherwise.
     */
    private boolean isServicesOK() {
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getActivity());
        if (available == ConnectionResult.SUCCESS) {
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            Log.d(TAG, "isServicesOK: an error occurred but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(getActivity(), available, errorDialogRequest);
            dialog.show();
        } else {
            Toast.makeText(getActivity(), "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    /**
     * Called when the Maps SDK is initialized, checks which version of renderer is used.
     *
     * @params renderer, the renderer used by the Maps SDK.
     * Can be either LATEST or LEGACY.
     */
    @Override
    public void onMapsSdkInitialized(MapsInitializer.Renderer renderer) {
        switch (renderer) {
            case LATEST:
                //Log.d(TAG, "Latest Renderer");
                break;
            case LEGACY:
                //Log.d(TAG, "Legacy Renderer");
                break;
        }
    }

}
