package com.example.qrhunterapp_t11;

import com.example.qrhunterapp_t11.ViewQR;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
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
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.OnMapsSdkInitializedCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.VectorDrawable;
import androidx.core.content.res.ResourcesCompat;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.List;

/**
 * Everything to do with google maps
 * Asks for location permissions and zooms in on current location.
 *
 * @author Daniel Guo
 */
public class MapFragment extends Fragment implements OnMapReadyCallback, OnMapsSdkInitializedCallback {

    public static final int errorDialogRequest = 9001;
    public static final int permissionsRequestEnableGPS = 9002;
    public static final int permissionsRequestAccessFineLocation = 9003;
    public static final int permissionsRequestAccessCoarseLocation = 9004;
    private static final String tag = "MapFragment";
    private GoogleMap mMap;
    private boolean mLocationPermissionGranted = false;
    private SearchView searchView;
    private final CollectionReference qrCodeRef;

    public MapFragment(FirebaseFirestore db) {
        this.qrCodeRef = db.collection("QRCodes");
    }

    /**
     * Called when the fragment is created.
     * Initializes the map fragment, checks if GPS is enabled, and calls getMapAsync to notify when the map is ready to use.
     *
     * @param savedInstanceState Bundle object containing the instance state of the fragment.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
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
     * Called when the fragment's UI is being created.
     * Initializes the layout of the fragment and initializes the Google Maps API.
     *
     * @param inflater           LayoutInflater object used to inflate any views in the fragment.
     * @param container          ViewGroup object that will contain the inflated views.
     * @param savedInstanceState Bundle object containing the instance state of the fragment.
     * @return View object representing the fragment's UI.
     */
    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
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

    @Override
    public void onResume() {
        super.onResume();
        if (mMap != null) {
            isServicesOK();
            isLocationEnabled();
        }
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
     * Checks if GPS is enabled and requests permission to use the device's location if necessary.
     */
    private void checkMapWorking() {
        Log.d(tag, "checkMapWorking");
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!isGPSEnabled) {
            // Request the user to enable GPS
            Intent gpsOptionsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(gpsOptionsIntent);
        } else {
            // GPS is enabled, check for location permissions
            if (isServicesOK() && isLocationEnabled()) {
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
     * Displays a dialog prompting the user to enable GPS.
     */
    private void buildAlertMessageNoGps() {
        Log.d(tag, "buildAlertMessageNoGps");
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
     * Checks if the app has permission to access fine location.
     *
     * @return true if the app has permission to access fine location, false otherwise.
     */
    public boolean isLocationEnabled() {
        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            getLocationPermission();
            Log.d(tag, "isLocationEnabled: No");
            return false;
        } else {
            Log.d(tag, "isLocationEnabled: Yes");
            return true;
        }
    }

    /**
     * Request location permission, so that we can get the location of the
     * device. The result of the permission request is handled by a callback,
     * onRequestPermissionsResult.
     */
    private void getLocationPermission() {
        Log.d(tag, "getLocationPermission");
        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
            displayMap();
        } else {
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION}, permissionsRequestAccessFineLocation);
        }
    }

    /**
     * Handles the result of a permission request by the user for enabling GPS or accessing fine/coarse location.
     *
     * @param requestCode  the code used for the request
     * @param permissions  the requested permissions
     * @param grantResults the results of the request
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case permissionsRequestEnableGPS:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(tag, "onRequestPermissionsResult: GPS permission granted");
                    displayMap();
                } else {
                    Log.d(tag, "onRequestPermissionsResult: GPS permission denied");
                }
                break;
            case permissionsRequestAccessCoarseLocation:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                    Log.d(tag, "onRequestPermissionsResult: Coarse Location permission granted");
                    displayMap();
                } else {
                    Log.d(tag, "onRequestPermissionsResult: Coarse Location permission denied");
                }
                break;
            case permissionsRequestAccessFineLocation:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(tag, "onRequestPermissionsResult: Fine Location permission granted");
                    mLocationPermissionGranted = true;
                    displayMap();
                } else {
                    Log.d(tag, "onRequestPermissionsResult: Fine Location permission denied");
                }
                break;
        }
    }

    /**
     * Handles the result of an activity launched for a permission request by the user for enabling GPS.
     *
     * @param requestCode the code used for the request
     * @param resultCode  the result of the request
     * @param data        the data returned from the request
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.d(tag, "onActivityResult");
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == permissionsRequestEnableGPS) {
            if (mLocationPermissionGranted) {
                displayMap();
            } else {
                getLocationPermission();
            }
        }
    }

    /**
     * Called when the map is ready to be used. Sets the map to the GoogleMap object passed in as a parameter and sets the current location if permission is granted.
     *
     * @param googleMap the GoogleMap object to set the map to
     */
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        Log.d(tag, "onMapReady");
        mMap = googleMap;
        if (mLocationPermissionGranted) {
            Log.d(tag, "myLocationPermissionGranted");
            try {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                //mMap.setPadding(0, 0, 0, 0);

                FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                if (location != null) {
                                    // Create LatLng object with the current location
                                    LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());

                                    // Create CameraUpdate object and move the camera to the current location
                                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(currentLocation, 15);
                                    mMap.animateCamera(cameraUpdate);
                                }
                            }
                        });
                VectorDrawable vectorDrawable = (VectorDrawable) ResourcesCompat.getDrawable(getResources(), R.drawable.ic_qr, null);

                Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                vectorDrawable.draw(canvas);

                BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(bitmap);

                // Add markers for each QRCode
                qrCodeRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Double latitude = document.getDouble("latitude");
                                Double longitude = document.getDouble("longitude");
                                if (latitude != null && longitude != null) {
                                    LatLng location = new LatLng(latitude, longitude);
                                    Marker marker = mMap.addMarker(new MarkerOptions()
                                            .position(location)
                                            .title(document.getId())
                                            .icon(icon));  // Use the custom icon
                                    marker.setTag(document.toObject(QRCode.class)); // Set QRCode object as the marker's tag
                                }
                            }
                        } else {
                            Log.d(tag, "Error getting documents: ", task.getException());
                        }
                    }
                });

                // OnMarkerClickListener to show the ViewQR dialog fragment when a marker is clicked
                mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        QRCode qrCode = (QRCode) marker.getTag();
                        if (qrCode != null) {
                            new ViewQR(qrCode).show(getActivity().getSupportFragmentManager(), "Show QR");
                        }
                        return true;
                    }
                });

            } catch (SecurityException e) {
                Log.e(tag, "SecurityException: " + e.getMessage());
            }
        }
    }

    /**
     * Displays the map on the screen.
     */
    private void displayMap() {
        Log.d(tag, "displayMap");
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.google_map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
    }

    /**
     * Checks if Google Play Services is available on the device.
     *
     * @return True if Google Play Services is available, False otherwise.
     */
    private boolean isServicesOK() {
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getActivity());
        if (available == ConnectionResult.SUCCESS) {
            Log.d(tag, "isServicesOK: Google Play Services is working");
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            Log.d(tag, "isServicesOK: an error occurred but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(getActivity(), available, errorDialogRequest);
            assert dialog != null;
            dialog.show();
        } else {
            Toast.makeText(getActivity(), "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    /**
     * Called when the Maps SDK is initialized, checks which version of renderer is used.
     *
     * @param renderer, the renderer used by the Maps SDK.
     *                  Can be either LATEST or LEGACY.
     */
    @Override
    public void onMapsSdkInitialized(@NonNull MapsInitializer.Renderer renderer) {
        switch (renderer) {
            case LATEST:
                //Log.d(tag, "Latest Renderer");
                break;
            case LEGACY:
                //Log.d(tag, "Legacy Renderer");
                break;
        }
    }
}