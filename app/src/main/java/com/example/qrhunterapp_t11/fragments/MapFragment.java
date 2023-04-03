package com.example.qrhunterapp_t11.fragments;

import static android.app.Activity.RESULT_OK;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.VectorDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.example.qrhunterapp_t11.R;
import com.example.qrhunterapp_t11.adapters.QRCodeAdapterMap;
import com.example.qrhunterapp_t11.interfaces.QueryCallbackWithArrayList;
import com.example.qrhunterapp_t11.objectclasses.QRCode;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.Status;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.LocationBias;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Everything to do with google maps
 *
 * @author Daniel, Afra
 */
public class MapFragment extends Fragment implements OnMapReadyCallback, OnMapsSdkInitializedCallback {

    public static final int errorDialogRequest = 9001;
    public static final int permissionsRequestEnableGPS = 9002;
    public static final int permissionsRequestAccessFineLocation = 9003;
    public static final int permissionsRequestAccessCoarseLocation = 9004;
    public static final int AUTOCOMPLETE_REQUEST_CODE = 1;
    private static final String TAG = "MapFragment";
    private final CollectionReference qrCodesReference;
    private final CollectionReference usersReference;
    private GoogleMap mMap;
    private boolean mLocationPermissionGranted = false;
    private FloatingActionButton searchButton;
    private LatLng currentLocation;
    private QRCodeAdapterMap qrCodeAdapterMap;
    private ListView nearbyQRListView;
    private Button nearbyQRCloseButton;
    private RelativeLayout nearbyQRRelativeLayout;
    private FloatingActionButton nearbyCodesButton;
    private RectangularBounds rectangularBounds;

    public MapFragment(@NonNull FirebaseFirestore db) {
        this.qrCodesReference = db.collection("QRCodes");
        this.usersReference = db.collection("Users");
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
     * @sources <a href="https://developers.google.com/maps/documentation/places/android-sdk/autocomplete">For autocomplete</a>
     */
    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        MapsInitializer.initialize(getActivity().getApplicationContext(), MapsInitializer.Renderer.LATEST, this);
        Places.initialize(getActivity().getApplicationContext(), getResources().getString(R.string.google_map_api_key));

        searchButton = view.findViewById(R.id.map_search_button);
        nearbyCodesButton = view.findViewById(R.id.map_nearby_codes_button);
        nearbyQRListView = view.findViewById(R.id.nearby_codes_listview);
        nearbyQRRelativeLayout = view.findViewById(R.id.nearby_codes_relative_layout);
        nearbyQRCloseButton = view.findViewById(R.id.close_nearby_button);

        return view;
    }

    /**
     * Checks if GPS is enabled and requests permission to use the device's location if necessary.
     */
    private void checkMapWorking() {
        Log.d(TAG, "checkMapWorking");
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
     * Checks if the app has permission to access fine location.
     *
     * @return true if the app has permission to access fine location, false otherwise.
     */
    public boolean isLocationEnabled() {
        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            getLocationPermission();
            Log.d(TAG, "isLocationEnabled: No");
            return false;
        } else {
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
                    Log.d(TAG, "onRequestPermissionsResult: GPS permission granted");
                    displayMap();
                } else {
                    Log.d(TAG, "onRequestPermissionsResult: GPS permission denied");
                }
                break;
            case permissionsRequestAccessCoarseLocation:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                    Log.d(TAG, "onRequestPermissionsResult: Coarse Location permission granted");
                    displayMap();
                } else {
                    Log.d(TAG, "onRequestPermissionsResult: Coarse Location permission denied");
                }
                break;
            case permissionsRequestAccessFineLocation:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "onRequestPermissionsResult: Fine Location permission granted");
                    mLocationPermissionGranted = true;
                    displayMap();
                } else {
                    Log.d(TAG, "onRequestPermissionsResult: Fine Location permission denied");
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
        Log.d(TAG, "onActivityResult");
        if (requestCode == permissionsRequestEnableGPS) {
            if (mLocationPermissionGranted) {
                displayMap();
            } else {
                getLocationPermission();
            }
        }
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                assert data != null;
                Place place = Autocomplete.getPlaceFromIntent(data);
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getLatLng());
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 15f);
                mMap.animateCamera(cameraUpdate);
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                Status status = Autocomplete.getStatusFromIntent(data);
                Log.i(TAG, status.getStatusMessage());
            }

            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Called when the map is ready to be used. Sets the map to the GoogleMap object passed in as a parameter and sets the current location if permission is granted.
     * Loads map markers and autocomplete searchbar
     *
     * @param googleMap the GoogleMap object to set the map to
     */
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        Log.d(TAG, "onMapReady");
        mMap = googleMap;
        if (mLocationPermissionGranted) {
            Log.d(TAG, "myLocationPermissionGranted");
            try {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);

                FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                if (location != null) {
                                    // Create LatLng object with the current location
                                    currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                                    rectangularBounds = RectangularBounds.newInstance(currentLocation, currentLocation);
                                    // Create CameraUpdate object and move the camera to the current location
                                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(currentLocation, 15);
                                    mMap.animateCamera(cameraUpdate);
                                }
                            }
                        });

            } catch (SecurityException e) {
                Log.e(TAG, "SecurityException: " + e.getMessage());
            }
        }

        VectorDrawable vectorDrawable = (VectorDrawable) ResourcesCompat.getDrawable(getResources(), R.drawable.ic_qr, null);

        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        vectorDrawable.draw(canvas);

        BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(bitmap);

        // Retrieve all User documents that reference any QRCode document
        usersReference.get().addOnSuccessListener(users -> {
            Set<String> referencedQRCodeIds = new HashSet<>();
            for (QueryDocumentSnapshot user : users) {
                CollectionReference userQRCodeRef = user.getReference().collection("User QR Codes");
                userQRCodeRef.get().addOnSuccessListener(qrCodeRefs -> {
                    for (QueryDocumentSnapshot qrCodeRef : qrCodeRefs) {
                        String qrCodeId = qrCodeRef.getId();
                        referencedQRCodeIds.add(qrCodeId);
                    }
                    // Add markers for each QRCode that is still being referenced by at least one user
                    qrCodesReference.get().addOnSuccessListener(qrCodes -> {
                        for (QueryDocumentSnapshot qrCode : qrCodes) {
                            String qrCodeId = qrCode.getId();
                            if (referencedQRCodeIds.contains(qrCodeId)) {
                                Double latitude = qrCode.getDouble("latitude");
                                Double longitude = qrCode.getDouble("longitude");
                                if (latitude != null && longitude != null) {
                                    LatLng location = new LatLng(latitude, longitude);
                                    Marker marker = mMap.addMarker(new MarkerOptions()
                                            .position(location)
                                            .title(qrCodeId)
                                            .icon(icon));  // Use the custom icon
                                    marker.setTag(qrCode.toObject(QRCode.class)); // Set QRCode object as the marker's tag
                                }
                            }
                        }
                    });
                });
            }
        });

        // OnMarkerClickListener to show the QRCodeView dialog fragment when a marker is clicked
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {
                QRCode qrCode = (QRCode) marker.getTag();
                if (qrCode != null) {
                    new QRCodeView(qrCode, null).show(getActivity().getSupportFragmentManager(), "Show QR");
                }
                return true;
            }
        });

        // Show nearby codes when user clicks on button
        nearbyCodesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                final EditText radiusEditText = new EditText(getActivity().getApplicationContext());
                radiusEditText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                InputFilter[] filterArray = new InputFilter[1];
                filterArray[0] = new InputFilter.LengthFilter(5);
                radiusEditText.setFilters(filterArray);

                builder
                        .setTitle("Nearby QR Codes")
                        .setMessage("Enter a radius (km)")
                        .setView(radiusEditText)
                        .setNegativeButton("Cancel", null)
                        .setPositiveButton("Set", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String radiusString = radiusEditText.getText().toString();
                                double radius = Double.parseDouble(radiusString);

                                // Convert km to m
                                radius *= 1000;
                                findNearbyQRCodes(currentLocation, radius, new QueryCallbackWithArrayList() {
                                    @SuppressLint("ClickableViewAccessibility")
                                    @Override
                                    public void setArrayList(@NonNull ArrayList<List<?>> arrayList) {
                                        if (arrayList.isEmpty()) {
                                            builder
                                                    .setMessage("No QR Codes found within " + radiusString + " km.")
                                                    .setPositiveButton("Dismiss", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                            dialogInterface.dismiss();
                                                        }
                                                    })
                                                    .create();
                                            builder.show();
                                        } else {
                                            qrCodeAdapterMap = new QRCodeAdapterMap(getActivity().getApplicationContext(), arrayList);
                                            nearbyQRListView.setAdapter(qrCodeAdapterMap);

                                            nearbyQRRelativeLayout.setVisibility(View.VISIBLE);
                                            nearbyQRListView.setOnTouchListener(new ListView.OnTouchListener() {
                                                @Override
                                                public boolean onTouch(View v, MotionEvent event) {
                                                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                                                        v.getParent().requestDisallowInterceptTouchEvent(true);
                                                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                                                        v.getParent().requestDisallowInterceptTouchEvent(false);
                                                    }

                                                    v.onTouchEvent(event);
                                                    return true;
                                                }
                                            });

                                            // If user clicks on QR Code in list, take them to its map marker
                                            nearbyQRListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                                @Override
                                                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                                                    int index = (int) adapterView.getItemAtPosition(i);
                                                    QRCode qrcode = (QRCode) arrayList.get(index).get(0);

                                                    LatLng qrCodeLocation = new LatLng(qrcode.getLatitude(), qrcode.getLongitude());

                                                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(qrCodeLocation, 15);
                                                    mMap.animateCamera(cameraUpdate);
                                                }
                                            });

                                            // Close nearby QR Codes list
                                            nearbyQRCloseButton.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    nearbyQRRelativeLayout.setVisibility(View.GONE);
                                                }
                                            });
                                        }
                                    }
                                });
                            }
                        })
                        .create();
                builder.show();
            }
        });

        // Launch autocomplete when user clicks on search
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                List<Place.Field> fields = Arrays.asList(Place.Field.NAME, Place.Field.LAT_LNG);

                LocationBias locationBias = rectangularBounds;
                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                        .setHint("Search...")
                        .setLocationBias(locationBias)
                        .build(getActivity().getApplicationContext());
                startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
            }
        });
    }

    /**
     * Displays the map on the screen.
     */
    private void displayMap() {
        Log.d(TAG, "displayMap");
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.google_map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
    }

    /**
     * Gets all QR Codes within a given radius of the current user
     *
     * @param currentLocationLatLng Current user's location
     * @param radius                Some radius in metres
     */
    private void findNearbyQRCodes(LatLng currentLocationLatLng, double radius, QueryCallbackWithArrayList nearbyCodes) {

        if (mLocationPermissionGranted) {
            Location currentLocationLocation = new Location("");
            currentLocationLocation.setLatitude(currentLocationLatLng.latitude);
            currentLocationLocation.setLongitude(currentLocationLatLng.longitude);
            ArrayList<List<?>> nearbyQRCodes = new ArrayList<>();

            // Get all QR Codes with location
            qrCodesReference
                    .whereNotEqualTo("latitude", null)
                    .get()
                    .addOnSuccessListener(qrCodes -> {

                        // For each QR Code, create Location object
                        for (QueryDocumentSnapshot qrCodeDocument : qrCodes) {
                            Location qrCodeLocation = new Location("");
                            qrCodeLocation.setLatitude(qrCodeDocument.getDouble("latitude"));
                            qrCodeLocation.setLongitude(qrCodeDocument.getDouble("longitude"));
                            double distance = currentLocationLocation.distanceTo(qrCodeLocation);

                            // Check if within radius
                            if (distance <= radius) {
                                QRCode qrCode = qrCodeDocument.toObject(QRCode.class);

                                List<Object> qrCodeDistance = new ArrayList<>();
                                qrCodeDistance.add(qrCode);

                                // Convert from m back to km
                                distance /= 1000;
                                distance = Double.parseDouble(String.format("%.2f", distance));

                                qrCodeDistance.add(distance);

                                nearbyQRCodes.add(qrCodeDistance);
                                nearbyCodes.setArrayList(nearbyQRCodes);
                            }
                        }
                    });
        }
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
                //Log.d(TAG, "Latest Renderer");
                break;
            case LEGACY:
                //Log.d(TAG, "Legacy Renderer");
                break;
        }
    }
}