package com.example.mapsdk_direction_routing_nearbylocation_autoplacesugestion;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.JsonObject;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class MainActivity2 extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, RoutingListener {
    private EditText et_origin, et_destination;
    private Button btn_findDistance, btn_dropPin, btn_route;
    private TextView tv_Distance;
    private ImageView iv_pin, iv_searchNearbyLocations;
    private Double originLat, destinationLat, originLng, destinationLng;
    private String originAddress, destinationAddress;
    private String distance;
    private Boolean isCameraIdol = false;
    private Boolean isOriginMarkerPlacing = false;
    private Boolean isDestinationMarkerPlacing = false;
    private Boolean isMapReady = false;

    private LocationRequest locationRequest;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private GoogleMap gMap;
    private LatLng originLatLng = null, destinationLatLng = null, testLatLng;
    private Marker originLocationMarker, destinationMarker, fixedTestMarker;
    private MarkerOptions fixedTestMarkerOptions;
    private List<Polyline> polylines = null;
    private List<M_Map> listPlaceDetails = new ArrayList<>();
    private Spinner spinner;

    private static final int AUTOCOMPLETE_REQUEST_CODE_ORIGIN = 1;
    private static final int AUTOCOMPLETE_REQUEST_CODE_DESTINATION = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        //******************************View Binding*************************************//
        et_origin = findViewById(R.id.et_origin_distanceMatrix);
        et_destination = findViewById(R.id.et_destination_distanceMatrix);
        btn_findDistance = findViewById(R.id.btn_distance_DistanceMatrix);
        tv_Distance = findViewById(R.id.tv_distance_DistanceMatrix);
        iv_pin = findViewById(R.id.iv_pin_DistanceMatrix);
        iv_searchNearbyLocations = findViewById(R.id.iv_searchNearbyLocations_DistanceMatrix);
        btn_dropPin = findViewById(R.id.btn_dropPin_DistanceMatrix);
        btn_route = findViewById(R.id.btn_Route_DistanceMatrix);
        spinner = findViewById(R.id.spinner_DistanceMatrix);


        //********************************Initializations*****************************//
        Places.initialize(MainActivity2.this, getString(R.string.google_maps_key));
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity2.this);
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(500).setFastestInterval(500).setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_DstanceMatrix);
        supportMapFragment.getMapAsync(this);
        listPlaceDetails = new ArrayList<>();
        et_origin.setFocusable(false);
        et_destination.setFocusable(false);
        fixedTestMarkerOptions = new MarkerOptions();


        initSpinner();


        //*****************************OnCLick Listeners******************************
        et_origin.setOnClickListener(v -> {
            Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields).setCountry("BD")
                    .build(this);
            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE_ORIGIN);
        });

        et_destination.setOnClickListener(v -> {
            Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields).setCountry("BD")
                    .build(this);
            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE_DESTINATION);
        });


        btn_findDistance.setOnClickListener(v -> {
            fetchDistance();
        });


        btn_dropPin.setOnClickListener(v -> {
            if (isOriginMarkerPlacing) {
                dropOriginPin();
            } else if (isDestinationMarkerPlacing) {
                dropDestinationPin();
            }
        });


        btn_route.setOnClickListener(v -> {
            Findroutes(originLatLng, destinationLatLng);
        });

        iv_searchNearbyLocations.setOnClickListener(v -> fecthNearbyLocation());

    }


    private void dropOriginPin() {
        gMap.clear();

        if (destinationLatLng != null) {
            placeMarkerInDestination();
        }

        placeMarkerInOrigin();
        fetchDistance();


    }

    private void dropDestinationPin() {
        gMap.clear();

        if (originLatLng != null) {
            placeMarkerInOrigin();
        }

        placeMarkerInDestination();
        fetchDistance();
    }


    private void placeMarkerInOrigin() {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.title("My place");
        markerOptions.position(originLatLng);
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_green));
        originLocationMarker = gMap.addMarker(markerOptions);
        isOriginMarkerPlacing = false;
        iv_pin.setVisibility(View.GONE);
    }


    private void placeMarkerInDestination() {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.title("My place");
        markerOptions.position(destinationLatLng);
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker));
        destinationMarker = gMap.addMarker(markerOptions);
        isDestinationMarkerPlacing = false;
        iv_pin.setVisibility(View.GONE);
    }


    private void fetchDistance() {
        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(EndPoints.BASE_URL_MAP)
                .build();
        CallBackDistanceMatrix callBackDistanceMatrix = retrofit.create(CallBackDistanceMatrix.class);
        String originLatStr = String.valueOf(originLat);
        String originLngStr = String.valueOf(originLng);
        String originLatLngStr = originLatStr + "," + originLngStr;

        String destinationLatStr = String.valueOf(destinationLat);
        String destinationLngStr = String.valueOf(destinationLng);
        String destinationLatLngStr = destinationLatStr + "," + destinationLngStr;

        Call<JsonObject> call = callBackDistanceMatrix.callDistance(originLatLngStr,
                destinationLatLngStr, getString(R.string.google_maps_key));

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                //Toast.makeText(DistanceMatrixx.this, "Success Code : "+response.code(), Toast.LENGTH_SHORT).show();
                JsonObject jsonObject = response.body();
                try {
                    JSONObject resultJSONObject = new JSONObject(String.valueOf(jsonObject));
                    JSONArray rowJSONArray = resultJSONObject.getJSONArray("rows");
                    JSONObject jsonObject1 = rowJSONArray.getJSONObject(0);
                    JSONArray elementJsonArray = jsonObject1.getJSONArray("elements");
                    JSONObject jsonObject2 = elementJsonArray.getJSONObject(0);
                    JSONObject distanceJSONObject = jsonObject2.getJSONObject("distance");
                    JSONObject durationJSONObject = jsonObject2.getJSONObject("duration");

                    distance = distanceJSONObject.getString("text");
                    String duration = durationJSONObject.getString("text");

                   /* Toast.makeText(DistanceMatrixx.this, "Distance : "+distance, Toast.LENGTH_SHORT).show();
                    Log.d("tag","duration : "+duration);*/
                    String message = originAddress + " vs " + destinationAddress + "\nDistance is : " + distance + "\nDuration is : " + duration;
                    tv_Distance.setText(message);

                    //*************************************Setting Info adapter to the destination******************************//
                    destinationMarker.setSnippet("Distance : " + distance);
                    gMap.setInfoWindowAdapter(new AdapterCustomInfoWindow(MainActivity2.this));


                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(MainActivity2.this, "Failed Code : " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE_DESTINATION) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                et_destination.setText(place.getAddress());
                destinationLat = place.getLatLng().latitude;
                destinationLng = place.getLatLng().longitude;
                destinationLatLng = new LatLng(destinationLat, destinationLng);
                Location location = new Location(LocationManager.GPS_PROVIDER);
                location.setLatitude(destinationLat);
                location.setLongitude(destinationLng);
                setDestinationMarker(location);

            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                // TODO: Handle the error.
                Status status = Autocomplete.getStatusFromIntent(data);
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
            return;
        } else if (requestCode == AUTOCOMPLETE_REQUEST_CODE_ORIGIN) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                et_origin.setText(place.getAddress());
                originLat = place.getLatLng().latitude;
                originLng = place.getLatLng().longitude;
                originLatLng = new LatLng(place.getLatLng().latitude, place.getLatLng().longitude);
                Location location = new Location(LocationManager.GPS_PROVIDER);
                location.setLatitude(originLat);
                location.setLongitude(originLng);
                setMyMarker(location);
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                // TODO: Handle the error.
                Status status = Autocomplete.getStatusFromIntent(data);
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
            return;
        }


        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;
        startLocationUpdates();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        gMap.setMyLocationEnabled(true);

        gMap.setOnCameraMoveListener(() -> {

        });


        gMap.setOnCameraIdleListener(() -> {
            LatLng latLng = gMap.getCameraPosition().target;
            fixedTestMarkerOptions = new MarkerOptions();
            fixedTestMarkerOptions.title("test Position");
            fixedTestMarkerOptions.position(latLng);

            //if(isMapReady){
            if (isOriginMarkerPlacing) {
                et_origin.setText(getAddressFromLatlng(latLng));
                originLatLng = latLng;
            } else if (isDestinationMarkerPlacing) {
                et_destination.setText(getAddressFromLatlng(latLng));
                destinationLatLng = latLng;
            }

            //}


        });


    }


    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(MainActivity2.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        } else {
            ActivityCompat.requestPermissions(MainActivity2.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
        }

    }


    private void setMyAddressToOriginEditText(Location location) {
        Geocoder geocoder = new Geocoder(MainActivity2.this, Locale.getDefault());
        try {
            List<Address> addressList = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addressList.size() > 0) {
                String myAddress = addressList.get(0).getAddressLine(0);
                et_origin.setText(myAddress);
                setMyMarker(location);
            } else {
                Toast.makeText(this, "No Address Found", Toast.LENGTH_SHORT).show();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private void setMyMarker(Location location) {
        gMap.clear();
        if (destinationLatLng != null) {
            placeMarkerInDestination();
        }
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        originLat = location.getLatitude();
        originLng = location.getLongitude();
        originLatLng = latLng;
        originAddress = getAddressFromLatlng(latLng);
        isOriginMarkerPlacing = true;
        isDestinationMarkerPlacing = false;
        iv_pin.setImageResource(R.drawable.marker_green);
        iv_pin.setVisibility(View.VISIBLE);
        gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));

    }


    private void setDestinationMarker(Location location) {
        gMap.clear();
        if (originLatLng != null) {
            placeMarkerInOrigin();
        }

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        destinationLat = location.getLatitude();
        destinationLng = location.getLongitude();
        destinationLatLng = latLng;
        destinationAddress = getAddressFromLatlng(latLng);
        testLatLng = latLng;
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 12);
        gMap.animateCamera(cameraUpdate);
        iv_pin.setImageResource(R.drawable.marker);
        iv_pin.setVisibility(View.VISIBLE);
        isCameraIdol = true;
        isOriginMarkerPlacing = false;
        isDestinationMarkerPlacing = true;

    }


    private String getAddressFromLatlng(LatLng latLng) {
        Geocoder geocoder = new Geocoder(MainActivity2.this, Locale.getDefault());
        List<Address> addressList = null;
        try {
            addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return addressList.get(0).getAddressLine(0);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == RESULT_OK) {
                startLocationUpdates();
            } else {

            }

        }
    }


    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            super.onLocationResult(locationResult);
            if (gMap != null) {
                setMyMarker(locationResult.getLastLocation());
                setMyAddressToOriginEditText(locationResult.getLastLocation());
                fusedLocationProviderClient.removeLocationUpdates(locationCallback);
                fusedLocationProviderClient = null;
            } else {
                Toast.makeText(MainActivity2.this, "Location is Null", Toast.LENGTH_SHORT).show();
            }
        }
    };


    //*********************************************Routing Methods********************************************//
    public void Findroutes(LatLng Start, LatLng End) {
        if (Start == null || End == null) {
            Toast.makeText(MainActivity2.this, "Unable to get location", Toast.LENGTH_LONG).show();
        } else {

            Routing routing = new Routing.Builder()
                    .travelMode(AbstractRouting.TravelMode.DRIVING)
                    .withListener(this)
                    .alternativeRoutes(true)
                    .waypoints(Start, End)
                    .key(getString(R.string.google_maps_key))  //also define your api key here.
                    .build();
            routing.execute();
        }
    }

    //Routing call back functions.
    @Override
    public void onRoutingFailure(RouteException e) {
        View parentLayout = findViewById(android.R.id.content);
        Snackbar snackbar = Snackbar.make(parentLayout, e.toString(), Snackbar.LENGTH_LONG);
        snackbar.show();
//    Findroutes(start,end);
    }

    @Override
    public void onRoutingStart() {
        Toast.makeText(MainActivity2.this, "Finding Route...", Toast.LENGTH_LONG).show();
    }

    //If Route finding success..
    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {

        CameraUpdate center = CameraUpdateFactory.newLatLng(originLatLng);
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(16);
        if (polylines != null) {
            polylines.clear();
        }
        PolylineOptions polyOptions = new PolylineOptions();
        LatLng polylineStartLatLng = null;
        LatLng polylineEndLatLng = null;


        polylines = new ArrayList<>();
        //add route(s) to the map using polyline
        for (int i = 0; i < route.size(); i++) {

            if (i == shortestRouteIndex) {
                polyOptions.color(getResources().getColor(R.color.black));
                polyOptions.width(14);
                polyOptions.addAll(route.get(shortestRouteIndex).getPoints());
                Polyline polyline = gMap.addPolyline(polyOptions);
                polylineStartLatLng = polyline.getPoints().get(0);
                int k = polyline.getPoints().size();
                polylineEndLatLng = polyline.getPoints().get(k - 1);
                polylines.add(polyline);

            } else {

            }

        }

    }

    @Override
    public void onRoutingCancelled() {
        Findroutes(originLatLng, destinationLatLng);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Findroutes(originLatLng, destinationLatLng);

    }


    //********************************************Fetch Nearby Locations************************************

    private void initSpinner() {
        spinner = findViewById(R.id.spinner_DistanceMatrix);
        ArrayAdapter spinnerAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.types));
        spinner.setAdapter(spinnerAdapter);
    }


    private void fecthNearbyLocation() {
        iv_pin.setVisibility(View.GONE);
        int index = spinner.getSelectedItemPosition();
        String text = getResources().getStringArray(R.array.types)[index];
        String myLatStr = String.valueOf(originLat);
        String myLngStr = String.valueOf(originLng);
        String myLocationStr = myLatStr + "," + myLngStr;
        Retrofit retrofit = new Retrofit
                .Builder()
                .baseUrl(EndPoints.BASE_URL_MAP)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        CallBackMap callBackMap = retrofit.create(CallBackMap.class);
        Call<JsonObject> call = callBackMap.callNearbyPlaces(text, myLocationStr,getString(R.string.google_maps_key));
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.code() == 200) {
                    gMap.clear();
                    listPlaceDetails.clear();
                    JsonObject jsonObject = response.body();
                    try {
                        JSONObject object = new JSONObject(String.valueOf(jsonObject));
                        JSONArray resultArray = object.getJSONArray("results");
                        for (int i = 0; i < resultArray.length(); i++) {
                            JSONObject object1 = resultArray.getJSONObject(i);

                            JSONObject geometry = object1.getJSONObject("geometry");
                            JSONObject location = geometry.getJSONObject("location");
                            String lat1 = location.getString("lat");
                            String lng1 = location.getString("lng");
                            LatLng latLng1 = new LatLng(Double.parseDouble(lat1), Double.parseDouble(lng1));
                            String placeName = object1.getString("name");

                            Log.d("tag", "Place name : " + placeName);

                            M_Map m_map = new M_Map(lat1, lng1, placeName);
                            listPlaceDetails.add(m_map);

                            MarkerOptions markerOptions1 = new MarkerOptions();
                            markerOptions1.title(placeName);
                            markerOptions1.position(latLng1);
                            gMap.addMarker(markerOptions1);
                            //Toast.makeText(MapForRetrofit.this, "done", Toast.LENGTH_SHORT).show();

                        }
                        Log.d("tag", "passed : ");
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.d("tag", "error : " + e.getMessage());
                    }


                } else {
                    Toast.makeText(MainActivity2.this, "failed" + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(MainActivity2.this, "failed2 : " + t.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });

    }


    private void checkFineLocaionPermission(){
        Dexter.withContext(MainActivity2.this).withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        startLocationUpdates();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package",getPackageName(),"");
                        intent.setData(uri);
                        startActivity(intent);
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).onSameThread().check();
    }


    @Override
    protected void onStart() {
        super.onStart();
        checkFineLocaionPermission();
    }
}