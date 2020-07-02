package com.amsavarthan.covid19;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.os.Bundle;

import com.amsavarthan.covid19.databinding.ActivityMainBinding;
import com.amsavarthan.covid19.fragments.HomeFragment;
import com.amsavarthan.covid19.fragments.MoreFragment;
import com.amsavarthan.covid19.fragments.StatsFragment;

import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    int fragment;

      @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setFragment(new HomeFragment());
        fragment=0;

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
                  .commitNow();
    }
}