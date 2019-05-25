package com.example.parkour;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
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

import java.util.ArrayList;

//import com.abhiandroid.GoogleMaps.googlemaps.R;


public class MapsActivityDrawing extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public static final Double BIG_DOUBLE_FOR_HORI = 10000.0;
    private ArrayList<ParkingZone> allZones;

    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    LocationRequest mLocationRequest;
    private GoogleMap mMap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager()
                        .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);
        allZones = new ArrayList<ParkingZone>();
        //VERY LARGE PARKING ZONE
//        ParkingZone zoneToAdd = new ParkingZone();
//        zoneToAdd.addSide(37.35, -122.0, 37.45, -122.0);
//        zoneToAdd.addSide(37.45, -122.0, 37.45, -122.2);
//        zoneToAdd.addSide(37.45, -122.2, 37.35, -122.2);
//        zoneToAdd.addSide(37.35, -122.2, 37.35, -122.0);
//        allZones.add(zoneToAdd);


        //NORMAL SIZED PARKING ZONE IN BERKELEY
        ParkingZone berkeleyZone = new ParkingZone();
        berkeleyZone.addSide(37.867834, -122.258988, 37.868325, -122.254450);
        berkeleyZone.addSide(37.868325, -122.254450, 37.866597, -122.254117);
        berkeleyZone.addSide(37.866597, -122.254117, 37.866055, -122.258612);
        berkeleyZone.addSide(37.866055, -122.258612, 37.867834, -122.258988);
        allZones.add(berkeleyZone);

        //create
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        //Initialize Google Play Services
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        } else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }


        for (ParkingZone Zone : allZones) {
            PolygonOptions zonePolygonOptions = new PolygonOptions();
            for (Side side : Zone.getSides()) {
                zonePolygonOptions = zonePolygonOptions.add(new LatLng(side.getLat1(), side.getLng1()));
            }

            zonePolygonOptions.fillColor(Color.argb(100, 160, 59, 237));
            zonePolygonOptions.strokeColor(Color.argb(175, 160, 59, 237));
            Polygon zonePolygon = mMap.addPolygon(zonePolygonOptions);
            zonePolygon.setTag(Zone);
        }

        Boolean valid = checkIfCurrLocInZone();
        System.out.println(valid);
    }

    private Boolean checkIfCurrLocInZone() {
        //HOW TO GET CURRENT LOCATION~~

        //TESTING FOR LARGE ZONE
        Double currLat = 37.34999;
        Double currLng = -122.1;

        //TESTING FOR BERKELEY ZONE
        currLat = 37.867097;
        currLng = -122.257432;

        LatLng latLng = new LatLng(currLat, currLng);
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        mMap.addMarker(markerOptions);


        Side horiRay = new Side(currLat, currLng, currLat, BIG_DOUBLE_FOR_HORI);
        for (ParkingZone zone : allZones) {
            int count = 0;
            for (Side polygonSide : zone.getSides()) {
                if (lineSegmentsIntersecting(horiRay, polygonSide)) {
                    count++;
                }
            }
            if (count%2 == 1) {
                return true;
            }
        }

        return false;
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
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
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
        //Location locations = locationManager.getLastKnownLocation(provider);
        Location locations = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        /*
        List<String> providerList = locationManager.getAllProviders();
        if (null != locations && null != providerList && providerList.size() > 0) {
            double longitude = locations.getLongitude();
            double latitude = locations.getLatitude();
            Geocoder geocoder = new Geocoder(getApplicationContext(),
                    Locale.getDefault());
            try {
                List<Address> listAddresses = geocoder.getFromLocation(latitude,
                        longitude, 1);
                if (null != listAddresses && listAddresses.size() > 0) {
                    String state = listAddresses.get(0).getAdminArea();
                    String country = listAddresses.get(0).getCountryName();
                    String subLocality = listAddresses.get(0).getSubLocality();
                    markerOptions.title("" + latLng + "," + subLocality + "," + state
                            + "," + country);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        */
        LatLng latLng = new LatLng(locations.getLatitude(), locations.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        mCurrLocationMarker = mMap.addMarker(markerOptions);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
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
                        mMap.setMyLocationEnabled(true);
                    }
                } else {
                    Toast.makeText(this, "permission denied",
                            Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }
    public void ButtonPressCallback(View view){

        Log.d("button", " click button 1");
        onLocationChanged(mLastLocation);


    }
}