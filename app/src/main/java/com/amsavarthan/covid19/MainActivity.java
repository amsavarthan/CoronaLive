package com.amsavarthan.covid19;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.amsavarthan.covid19.databinding.ActivityMainBinding;
import com.amsavarthan.covid19.fragments.HomeFragment;
import com.amsavarthan.covid19.fragments.MoreFragment;
import com.amsavarthan.covid19.fragments.StatsFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import io.github.inflationx.viewpump.ViewPumpContextWrapper;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.location.LocationManager.PASSIVE_PROVIDER;

public class MainActivity extends AppCompatActivity implements LocationListener {

    ActivityMainBinding binding;
    int fragment;
    private LocationManager locationManager;
    private SharedPreferences dataSharedPref;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setFragment(new HomeFragment());
        fragment = 0;
        dataSharedPref = getSharedPreferences("data", MODE_PRIVATE);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 100);
        } else {
            getLocation();
        }

        binding.bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()){
                case R.id.action_home:
                    setFragment(new HomeFragment());
                    fragment=0;
                    break;
                case R.id.action_stat:
                    setFragment(new StatsFragment());
                    fragment=1;
                    break;
                case R.id.action_more:
                    setFragment(new MoreFragment());
                    fragment=2;
                    break;
                default:
                    return false;
            }
            return true;
        });
        binding.bottomNavigationView.setOnNavigationItemReselectedListener(item -> {});
    }

    @SuppressLint("MissingPermission")
    private void getLocation() {
        Location location = getLastKnownLocation( locationManager);
        if (location == null) {
            Criteria criteria = new Criteria();
            String bestProvider = locationManager.getBestProvider(criteria, true);
            locationManager.requestLocationUpdates(bestProvider == null ? PASSIVE_PROVIDER : bestProvider, 1, 2, this);
        }else{
           saveLocation(location);
        }
    }

    private Location getLastKnownLocation(LocationManager locationManager){
        Location l=null;
        List<String> providers=locationManager.getProviders(true);
        Location bestLocation=null;
        for(String provider:providers){
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)== PERMISSION_GRANTED)
                l=locationManager.getLastKnownLocation(provider);
            if(l==null)continue;
            if(bestLocation==null||l.getAccuracy()<bestLocation.getAccuracy())bestLocation=l;
        }
        return bestLocation;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("fragment",fragment);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        fragment=savedInstanceState.getInt("fragment",0);
        switch (fragment){
            case 0:
                setFragment(new HomeFragment());
                break;
            case 1:
                setFragment(new StatsFragment());
                break;
            case 2:
                setFragment(new MoreFragment());
                break;
            default:
        }
    }

    private void setFragment(Fragment fragment) {
          getSupportFragmentManager().beginTransaction()
                  .replace(binding.container.getId(),fragment,fragment.getTag())
                  .commit();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PERMISSION_GRANTED) {
                getLocation();
            } else {
                Toast.makeText(this, "Location permission has been denied", Toast.LENGTH_SHORT).show();
            }
        }

    }

    private void saveLocation(Location location){
        try{
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addressList;
            addressList=geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);

            String country=addressList.get(0).getCountryName();
            dataSharedPref.edit().putString("countryName",country).apply();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onLocationChanged(Location location) {

       saveLocation(location);

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
}