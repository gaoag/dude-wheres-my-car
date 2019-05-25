package com.example.parkour;
import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
//import com.abhiandroid.GoogleMaps.googlemaps.R;

import java.io.IOException;
import java.text.BreakIterator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    //current loc of user
    static public double posLng;
    static public double posLat;
    //loc of parked car
    static public double carLng;
    static public double carLat;
    MarkerOptions posMarkerOptions = new MarkerOptions();
    static MarkerOptions carMarkerOptions = new MarkerOptions();
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    static Marker mCarMarker;
    LocationRequest mLocationRequest;
    static private GoogleMap mMap;

    private FilterZoneRowView filterButtons;

    private TimerPickupView timerView;

    //Parking zone validation related vars
    public static final Double BIG_DOUBLE_FOR_HORI = 10000.0;
    private ArrayList<ParkingZone> allZones;
    private ParkingZone currZone;
    private String currPermit;
    private int currTimeLimit;
    private ArrayList<Polygon> allZonePolygons;


    static TextView timer;
    static TextView walking;
    static Button pickup;
    static Button park;

    public static boolean isParking;

    private FrameLayout frameForTimers;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        park = findViewById(R.id.button);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager()
                        .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);

        //initialize Zones, add them manually
        allZones = new ArrayList<ParkingZone>();
        addExampleZones();


        //set current permit manually
        Intent getPermitIntent = getIntent();
        currPermit = getPermitIntent.getExtras().getString("permit");
        Log.d("permit", currPermit);
        //currPermit = "C";

        currTimeLimit = Integer.MIN_VALUE;

        filterButtons = new FilterZoneRowView(this);
        FrameLayout frameForButtons = findViewById(R.id.frameForButtons);
        frameForButtons.addView(filterButtons);

        isParking = false;

        // set marker for car and position
        Bitmap.Config conf = Bitmap.Config.ARGB_8888;
        Bitmap bmp = Bitmap.createBitmap(300, 300, conf);
        Paint p = new Paint();
        Canvas canvas = new Canvas(bmp);
        canvas.drawBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.mapdot), 0, 0, p);
        bmp = Bitmap.createScaledBitmap(bmp, 150, 150, true);
        posMarkerOptions.icon(BitmapDescriptorFactory.fromBitmap(bmp));
        //posMarkerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

        Bitmap bmp2 = Bitmap.createBitmap(300, 300, conf);
        Canvas canvas2 = new Canvas(bmp2);
        canvas2.drawBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.car), 0, 0, p);
        bmp2 = Bitmap.createScaledBitmap(bmp2, 150, 150, true);
        carMarkerOptions.icon(BitmapDescriptorFactory.fromBitmap(bmp2));
        //carMarkerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(false);
            }
        } else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(false);
        }

        // turn off the blue dot
        mMap.setMyLocationEnabled(false);
        // turn off the function to move to current location
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(37.868308, -122.262346), 17));
        posLat = 37.868308;
        posLng = -122.262346;


        LatLng latLng = new LatLng(posLat, posLng);
        posMarkerOptions.position(latLng);
        mCurrLocationMarker = mMap.addMarker(posMarkerOptions);

        drawZones();

    }

    private void findCurrentZone() {
        //HOW TO GET CURRENT LOCATION

        //TESTING FOR LARGE ZONE
        Double currLat = 37.34999;
        Double currLng = -122.1;

        //TESTING FOR BERKELEY ZONE
        currLat = 37.867097;
        currLng = -122.257432;

        //USING THE LOCAL FAKELAT/FAKELNG
        currLat = posLat;
        currLng = posLng;

        currZone = null;
        //GETS CURRENT POLYGON CONTAINING POINT (or null if no valid polygon)
        Side horiRay = new Side(currLat, currLng, currLat, BIG_DOUBLE_FOR_HORI);
        for (ParkingZone zone : allZones) {
            int count = 0;
            for (Side polygonSide : zone.getSides()) {
                if (lineSegmentsIntersecting(horiRay, polygonSide)) {
                    count++;
                }
            }
            if (count%2 == 1) {
                currZone = zone;
            }
        }


    }

    private Boolean lineSegmentsIntersecting(Side horiRay, Side polySide) {
        int o1 = orientationChecker(horiRay.getLat1(), horiRay.getLng1(), horiRay.getLat2(), horiRay.getLng2(), polySide.getLat1(), polySide.getLng1());
        int o2 = orientationChecker(horiRay.getLat1(), horiRay.getLng1(), horiRay.getLat2(), horiRay.getLng2(), polySide.getLat2(), polySide.getLng2());
        int o3 = orientationChecker(polySide.getLat1(), polySide.getLng1(), polySide.getLat2(), polySide.getLng2(), horiRay.getLat1(), horiRay.getLng1());
        int o4 = orientationChecker(polySide.getLat1(), polySide.getLng1(), polySide.getLat2(), polySide.getLng2(), horiRay.getLat2(), horiRay.getLng2());

        if (o1 != o2 && o3 != o4) {
            return true;
        } else {
            return false;
        }
    }

    private int orientationChecker(Double p1lat, Double p1lng, Double p2lat, Double p2lng, Double p3lat, Double p3lng) {
        Double val = (p2lat - p1lat) * (p3lng - p2lng) - (p2lng - p1lng) * (p3lat - p2lat);
        return (val > 0)? 1:2;
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }
    @Override
    public void onConnected(Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(100);
        mLocationRequest.setFastestInterval(100);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                    mLocationRequest, this);
        }
    }
    @Override
    public void onConnectionSuspended(int i) {
    }
    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }
        //Showing Current Location Marker on Map

        LocationManager locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);
        //String provider = locationManager.getBestProvider(new Criteria(), true);
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Location locations = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        double lat = locations.getLatitude();
        double lng = locations.getLongitude();
        posLat = lat;
        posLng = lng;

        posLat = 37.868308;
        posLng = -122.262346;

        LatLng latLng = new LatLng(posLat, posLng);
        posMarkerOptions.position(latLng);
        mCurrLocationMarker = mMap.addMarker(posMarkerOptions);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(37.868308, -122.262346), 17));

        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,
                    this);
        }


    }
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }
    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(false);
                    }
                } else {
                    Toast.makeText(this, "permission denied",
                            Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    public void drawMarkers(){
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }
        LatLng latLng = new LatLng(posLat, posLng);
        posMarkerOptions.position(latLng);
        mCurrLocationMarker = mMap.addMarker(posMarkerOptions);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

        findCurrentZone();

    }
    public void moveUp(View view){
        posLat += 0.0005;

        if (isParking) {
            setWalkingTime(timerView.getWalkingTime());
        }
        drawMarkers();
    }
    public void moveDown(View view){
        posLat -= 0.0005;
        if (isParking) {
            setWalkingTime(timerView.getWalkingTime());
        }
        drawMarkers();
    }
    public void moveRight(View view){
        posLng += 0.0005;
        if (isParking) {
            setWalkingTime(timerView.getWalkingTime());
        }
        drawMarkers();
    }
    public void moveLeft(View view){
        posLng -= 0.0005;
        if (isParking) {
            setWalkingTime(timerView.getWalkingTime());
        }
        drawMarkers();
    }


    public void onParkButtonClick(View view) {
        if (currZone != null && currZone.getValidPermit().equals(currPermit) && currZone.getTimeLimit() >= currTimeLimit) {
                DialogFragment validParkFragment = newValidFragment(currZone.getTimeLimit(), currZone.getValidPermit());
                ((ValidParkingDialogFragment) validParkFragment).setContext(this);
                validParkFragment.show(getSupportFragmentManager(), "validPark");
                
                park.setVisibility(View.INVISIBLE);


        } else if (currZone == null){
            DialogFragment invalidParkFragment = newInvalidFragment(1);
            invalidParkFragment.show(getSupportFragmentManager(), "invalidPark");
        } else if (!currZone.getValidPermit().equals(currPermit)){
            DialogFragment invalidParkFragment = newInvalidFragment(currZone.getTimeLimit(), currZone.getValidPermit(), currPermit, 2);
            invalidParkFragment.show(getSupportFragmentManager(), "invalidPark");
        } else if (!(currZone.getTimeLimit() >= currTimeLimit)) {
            DialogFragment invalidParkFragment = newInvalidFragment(currZone.getTimeLimit(), currZone.getValidPermit(), currPermit, 3);
            invalidParkFragment.show(getSupportFragmentManager(), "invalidPark");
        }
        else {
            DialogFragment invalidParkFragment = new InvalidParkingDialogFragment();
            invalidParkFragment.show(getSupportFragmentManager(), "invalidPark");
        }

    }

    public static InvalidParkingDialogFragment newInvalidFragment(int time, String permit, String currPermit, int violation) {
        InvalidParkingDialogFragment invalidDialog = new InvalidParkingDialogFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putInt("time", time);
        args.putString("permit", permit);
        args.putInt("violation", violation);
        args.putString("currPermit", currPermit);
        invalidDialog.setArguments(args);

        return invalidDialog;
    }

    public static InvalidParkingDialogFragment newInvalidFragment(int violation) {
        InvalidParkingDialogFragment invalidDialog = new InvalidParkingDialogFragment();

        Bundle args = new Bundle();
        args.putInt("time", 1);
        args.putString("permit", "asdf");
        args.putInt("violation", violation);
        args.putString("currPermit", "asdf");
        invalidDialog.setArguments(args);

        return invalidDialog;
    }

    public static ValidParkingDialogFragment newValidFragment(int time, String currPermit) {
        ValidParkingDialogFragment validDialog = new ValidParkingDialogFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putInt("time", time);
        args.putString("currPermit", currPermit);
        validDialog.setArguments(args);

        return validDialog;
    }




    public static class InvalidParkingDialogFragment extends DialogFragment {
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Bundle args = getArguments();
            String zonePermit = args.getString("permit");
            String currPermit = args.getString("currPermit");
            Integer violation = args.getInt("violation");
            Integer time = args.getInt("time");
            String msg;
            //null zone case
            if (violation == 1) {
                msg = "Not in a valid parking zone for any permit. Find somewhere else!";

            } else if (violation == 2) { //wrong permit case
                msg = "This zone requires permit " + zonePermit + ", but you own permit " + currPermit + ".";
            } else if (violation == 3) { //wrong time case
                msg = "You are only able to park in this zone for " + time.toString() + " hours.";
            } else {
                msg = "asdf";
            }


            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setMessage(msg)
                    .setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    });
            // Create the AlertDialog object and return it
            return builder.create();
        }
    }


    public static class ValidParkingDialogFragment extends DialogFragment {

        public Context mContext;



        public void setContext(Context ctx) {
            mContext = ctx;
        }
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Bundle args = getArguments();
            Integer time = args.getInt("time");
            String permit = args.getString("currPermit");
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("This is a valid parking spot for permit " + permit + "! You have up to " + time.toString() + " hours, and will be fined $200 for going overtime. Park here?")
                    .setPositiveButton("Park here!", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                            isParking = true;

                            carLat = posLat;
                            carLng = posLng;
                            LatLng latLng = new LatLng(carLat, carLng);
                            carMarkerOptions.position(latLng);
                            mCarMarker = mMap.addMarker(carMarkerOptions);

                            MapsActivity cont = (MapsActivity) mContext;
                            //display timer here
                            cont.setTimer();
                        }

                    })
                    .setNegativeButton("Don't park", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            park.setVisibility(View.VISIBLE);
                        }
                    });
            // Create the AlertDialog object and return it
            return builder.create();
        }
    }

    static SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("HH:mm:ss");
    private static final String FORMAT = "%02d:%02d:%02d";

    /*
    calculation of distance code taken from:
    https://stackoverflow.com/questions/837872/calculate-distance-in-meters-when-you-know-longitude-and-latitude-in-java

 */
    public static int walkingTime(double lat1, double lat2, double lng1, double lng2) {
        double earthRadius = 6371000; //meters
        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng/2) * Math.sin(dLng/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        float dist = (float) (earthRadius * c);

        return (int) Math.ceil((dist/1609.34)*20);
    }

    public static void setWalkingTime(TextView walkTime) {
        int time = walkingTime(posLat, carLat, posLng, carLng);
        if (time < 1) {
            walkTime.setText("Distance to car: " + "< 1 min");

        } else if (time == 1) {
            walkTime.setText("Distance to car: " + Integer.toString(time) + " min");
        } else {
            walkTime.setText("Distance to car: " + Integer.toString(time) + " min");
        }
    }


    public void setTimer() {


        timerView = new TimerPickupView(this);
        timerView.startTimer(currZone.getTimeLimit());


        frameForTimers = findViewById(R.id.frameForTimers);
        frameForTimers.setVisibility(View.VISIBLE);
        frameForTimers.addView(timerView);



        setWalkingTime(timerView.getWalkingTime());





    }

    public void pickupButtonOnClick(View v) {
        park.setVisibility(View.VISIBLE);
        mCarMarker.remove();
        frameForTimers.removeView(timerView);
        frameForTimers.setVisibility(View.INVISIBLE);
        isParking = false;
    }



    public void filterButtonOnClick(View v) {
        Button b = (Button) v;
        int buttonClicked = (Integer) b.getTag();
        filterButtons.styleAllButtonsExcept(buttonClicked);
        if (buttonClicked == 0) {
            currTimeLimit = Integer.MIN_VALUE;
        } else {
            currTimeLimit = buttonClicked;
        }
        drawZones();

    }

    public void drawZones() {
        //first, clear existing zones
        if (allZonePolygons != null) {
            for (Polygon toRemove : allZonePolygons) {
                toRemove.remove();
            }
        } else {
            allZonePolygons = new ArrayList<Polygon>();
        }

        for (ParkingZone Zone : allZones) {
            //DRAW only if ParkingZone's valid permit matches the user selected matching permit.
            //Even if not drawn, info is still stored with allZones to test for invalid Zone location
            if (Zone.getValidPermit().equals(currPermit) && (Zone.getTimeLimit() >= currTimeLimit)) {
                PolygonOptions zonePolygonOptions = new PolygonOptions();
                for (Side side : Zone.getSides()) {
                    zonePolygonOptions = zonePolygonOptions.add(new LatLng(side.getLat1(), side.getLng1()));
                }
                if (Zone.getValidPermit().equals("A")) {
                    zonePolygonOptions.fillColor(Color.argb(100, 255,127,80));
                    zonePolygonOptions.strokeColor(Color.argb(175, 255,127,80));
                } else {
                    zonePolygonOptions.fillColor(Color.argb(100, 160, 59, 237));
                    zonePolygonOptions.strokeColor(Color.argb(175, 160, 59, 237));
                }
                Polygon zonePolygon = mMap.addPolygon(zonePolygonOptions);
                allZonePolygons.add(zonePolygon);
                zonePolygon.setTag(Zone);
            }
        }
    }

    public void addExampleZones() {
        //NORMAL SIZED PARKING ZONE IN BERKELEY
        ParkingZone berkeleyZone = new ParkingZone();
        berkeleyZone.addSide(37.867834, -122.258988, 37.868325, -122.254450);
        berkeleyZone.addSide(37.868325, -122.254450, 37.866597, -122.254117);
        berkeleyZone.addSide(37.866597, -122.254117, 37.866055, -122.258612);
        berkeleyZone.addSide(37.866055, -122.258612, 37.867834, -122.258988);
        berkeleyZone.setTimeLimit(4);
        allZones.add(berkeleyZone);

        ParkingZone berkeleyZone2 = new ParkingZone();
        berkeleyZone2.addSide(37.866016, -122.258633, 37.866583, -122.254127);
        berkeleyZone2.addSide(37.866583, -122.254127, 37.863610, -122.253709);
        berkeleyZone2.addSide(37.863610, -122.253709, 37.863305, -122.258687);
        berkeleyZone2.addSide(37.863305, -122.258687, 37.865151, -122.258429);
        berkeleyZone2.addSide(37.865151, -122.258429, 37.866016, -122.258633);
        berkeleyZone2.setTimeLimit(6);
        allZones.add(berkeleyZone2);

        ParkingZone berkeleyZone3 = new ParkingZone();
        berkeleyZone3.addSide(37.868125, -122.263742, 37.864559, -122.262994);
        berkeleyZone3.addSide(37.864559, -122.262994, 37.864864, -122.260762);
        berkeleyZone3.addSide(37.864864, -122.260762, 37.868421, -122.261492);
        berkeleyZone3.addSide(37.868421, -122.261492, 37.868125, -122.263742);
        berkeleyZone3.setTimeLimit(2);
        allZones.add(berkeleyZone3);

        ParkingZone berkeleyZone4 = new ParkingZone();
        berkeleyZone4.addSide(37.868424, -122.261453, 37.863054, -122.260369);
        berkeleyZone4.addSide(37.863054, -122.260369, 37.863283, -122.258706);
        berkeleyZone4.addSide(37.863283, -122.258706, 37.865146, -122.258427);
        berkeleyZone4.addSide(37.865146, -122.258427, 37.868720, -122.259210);
        berkeleyZone4.addSide(37.868720, -122.259210, 37.868424, -122.261453);
        berkeleyZone4.setTimeLimit(5);
        berkeleyZone4.setValidPermit("A");
        allZones.add(berkeleyZone4);

        ParkingZone berkeleyZone5 = new ParkingZone();
        berkeleyZone5.addSide(37.864840, -122.260717, 37.864220, -122.265253);
        berkeleyZone5.addSide(37.864220, -122.265253, 37.861569, -122.264727);
        berkeleyZone5.addSide(37.861569, -122.264727, 37.862182, -122.260207);
        berkeleyZone5.addSide(37.862182, -122.260207, 37.864840, -122.260717);
        berkeleyZone5.setTimeLimit(3);
        berkeleyZone5.setValidPermit("A");
        allZones.add(berkeleyZone5);



    }


    public void onBackPressed() {
        if (isParking == true) {
            AlertDialog diaBox = AskOption(this);
            diaBox.show();
        } else {
            finish();
            super.onBackPressed();

        }

    }

    private AlertDialog AskOption(Context context)
    {
        final Context mCtx = context;
        AlertDialog myQuittingDialogBox =new AlertDialog.Builder(this)
                .setTitle("Reselect Permit")
                .setMessage("Are you sure you want to select a new permit? This will erase your current parked location and timer!")

                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        finish();
                        MapsActivity.super.onBackPressed();


                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
        return myQuittingDialogBox;

    }
}
