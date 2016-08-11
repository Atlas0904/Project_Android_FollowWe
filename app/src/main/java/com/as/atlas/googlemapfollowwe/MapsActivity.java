package com.as.atlas.googlemapfollowwe;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.IndoorBuilding;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


import com.firebase.client.Firebase;

import java.io.IOException;
import java.util.Locale;

import static android.widget.Toast.LENGTH_LONG;

public class MapsActivity extends AppCompatActivity
        implements
        GoogleMap.OnMapClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        OnMapReadyCallback,
        LocationListener {

    private static final String TAG = MapsActivity.class.getSimpleName();
    private static boolean mLockedOnUserView = false;

    public static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    public final String URL_FIREBASE = "https://followwe-7f0e8.firebaseio.com/";


    private Button buttonShow;
    private CheckBox checkBox;
    private GoogleApiClient googleApiClient;

    // Location請求物件
    private LocationRequest locationRequest;
    private GoogleMap googleMap;
    private Location currentLocation;


    // Firebase section
    private Firebase mFirebaseUserInfo;
    // Firebase chat room section
    private Firebase mFirebaseOnline;
    private Firebase mFirebaseUser;

    private ValueEventListener mOnlineChangeListener;
    private ValueEventListener mUserChangeListener;

    //Local variable
    private Handler mHandler;
    private UserPlaceSelectionListener userPlaceSelectionListener;
    private UserInfoValueEventListener userInfoValueEventListener;
    private GoogleMapEventHandler googleMapEventHandler;
    private OnMapReadyCallback onMapReadyCallback;

    // Note: rember to add package in Google console
    // https://console.developers.google.com/apis/credentials?project=at-shareyourlocation

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.abs_layout);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);

        checkBox = (CheckBox) findViewById(R.id.checkBoxSyncPlace);
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLockedOnUserView = ((CheckBox) v).isChecked();
            }
        });

        buttonShow = (Button) findViewById(R.id.buttonShow);


        configGoogleApiClient();
        configLocationRequest();

        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        userPlaceSelectionListener = new UserPlaceSelectionListener();
        autocompleteFragment.setOnPlaceSelectedListener(userPlaceSelectionListener);


        // Firebase section
        Firebase.setAndroidContext(this);
        mFirebaseUserInfo = new Firebase(URL_FIREBASE).child("userinfo");

        // Firebase chat room section
        String userid = "0904";
        mFirebaseOnline = new Firebase(URL_FIREBASE).child(".info/connected");
        mFirebaseUser = new Firebase(URL_FIREBASE).child("presence").child(userid);
        log("mFirebaseOnline: " + mFirebaseOnline + " mFirebaseUser:" + mFirebaseUser);


        mFirebaseUserInfo.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "onChildAdded()");
                Log.d(TAG, "dataSnapshot:" + dataSnapshot.getValue());
                Log.d(TAG, "dataSnapshot.child(name):" + dataSnapshot.child("name").getValue());
                Log.d(TAG, "dataSnapshot.child(lat):" + dataSnapshot.child("lat").getValue());
                Log.d(TAG, "dataSnapshot.child(lng):" + dataSnapshot.child("lng").getValue());
                Log.d(TAG, "dataSnapshot.getChildrenCount():" + dataSnapshot.getChildrenCount());

                int i = 0;
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    if (child.getValue() != null)
                        Log.d(TAG, "child index:" + (i++) + " " + String.valueOf(child.getValue()));
                }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });


        setPeople();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {  // Note: init may wait for google map ready
        Log.d(TAG, "onMapReady: map=" + googleMap);
        this.googleMap = googleMap;
        googleMapEventHandler = new GoogleMapEventHandler(googleMap);
        userInfoValueEventListener = new UserInfoValueEventListener(googleMapEventHandler);
        mFirebaseUserInfo.addValueEventListener(userInfoValueEventListener);
        googleMap.setOnMapClickListener(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.d(TAG, "Permission check for setMyLocationEnable");
            return;
        }
        googleMap.setMyLocationEnabled(true);
    }


    private void setPeople() {
        User atlas = new User("Atlas", 25.033408, 121.564099);
        User sandy = new User("Sandy", 25.043408, 121.564099);
        User warhol = new User("Warhol", 25.043408, 121.574099);

        mFirebaseUserInfo.push().setValue(atlas);    // 如果本身就是 class, 就不要再用  child("Person")
        mFirebaseUserInfo.push().setValue(sandy);
        mFirebaseUserInfo.push().setValue(warhol);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Firebase chatroom section
        // Finally, a little indication of connection status
        mOnlineChangeListener = mFirebaseOnline.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean connected = (Boolean) dataSnapshot.getValue();
                log("mOnlineChangeListener: connected" + connected);
                if (connected) {
                    mFirebaseUser.onDisconnect().removeValue();
                    mFirebaseUser.setValue(true);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                // No-op
            }
        });

    }

    @Override
    protected void onStop() {
        super.onStop();
        //disableLocationUpdate();
    }

    private void configGoogleApiClient() {
        log("configGoogleApiClient");

        googleApiClient = new GoogleApiClient.Builder(this).
                addConnectionCallbacks(this).
                addOnConnectionFailedListener(this).
                addApi(LocationServices.API).
                build();
        googleApiClient.connect();
    }

    private void configLocationRequest() {
        log("configLocationRequest");
        locationRequest = new LocationRequest();
        // 設定讀取位置資訊的間隔時間為一秒（1000ms）
        locationRequest.setInterval(1000);
        // 設定讀取位置資訊最快的間隔時間為一秒（1000ms）
        locationRequest.setFastestInterval(1000);
        // 設定優先讀取高精確度的位置資訊（GPS）
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        log("onConnected Bundle: " + bundle);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
            }

            return;
        }

//        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
//            @Override
//            public boolean onMarkerClick(Marker marker) {
//
//                Toast.makeText(MapsActivity.this, "On marker click", LENGTH_LONG).show();
//
//                // Save current place
//                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MapsActivity.this);
//
//                alertDialog.setTitle(R.string.title_current_location)
//                        .setMessage(R.string.message_current_location)
//                        .setCancelable(true);
//
//                alertDialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        Intent result = new Intent();
//                        result.putExtra("lat", currentLocation.getLatitude());
//                        result.putExtra("lng", currentLocation.getLongitude());
//                        setResult(Activity.RESULT_OK, result);
//                        finish();
//                    }
//                });
//                alertDialog.setNegativeButton(android.R.string.cancel, null);
//                alertDialog.show();
//
//                return true;
//            }
//        });

        googleMap.setMyLocationEnabled(true);
        createLocationRequest();
        LocationServices.FusedLocationApi.requestLocationUpdates(
                googleApiClient, locationRequest, MapsActivity.this);

        currentLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        LatLng start = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());   // 有可能 Geany 一開始給錯  導致沒有路線圖

        if (currentLocation != null) {
            googleMap.addMarker(new MarkerOptions()
                    .position(start)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET))
                    .title(start.toString())
            );
        }
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(start, 17));

    }

    private void createLocationRequest() {    // 一直 update
        if (locationRequest == null) {
            locationRequest = new LocationRequest();
            locationRequest.setInterval(1000);
            //locationRequest.setFastestInterval();  其他 app 拿
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        // Google Services連線中斷
        // int參數是連線中斷的代號

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        log("onConnectionFailed connectionResult: " + connectionResult);

        int errorCode = connectionResult.getErrorCode();
        if (errorCode == ConnectionResult.SERVICE_MISSING) {
            Toast.makeText(this, R.string.google_play_service_missing,
                    LENGTH_LONG).show();
        }

    }

    @Override
    public void onLocationChanged(Location location) {
        log("onLocationChanged location: " + location);
        if (mLockedOnUserView) {
            LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));

            log("onLocationChanged currentLocation: " + currentLocation);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                Log.i(TAG, "Place: " + place.getName());   // fragment return
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.
                Log.i(TAG, status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }

    private void log(String s) {
        Log.d(TAG, s);
    }

    @Override
    public void onMapClick(LatLng latLng) {
        Log.d(TAG, "onMapClick latLng:" + latLng);
        IndoorBuilding building = googleMap.getFocusedBuilding();  // null?
        Log.d(TAG, "building: " + building);

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            Log.d(TAG, "Address:" + geocoder.getFromLocation(25.033408, 121.564099, 1).toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
