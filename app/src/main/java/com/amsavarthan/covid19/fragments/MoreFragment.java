package com.amsavarthan.covid19.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amsavarthan.covid19.R;
import com.amsavarthan.covid19.databinding.FragmentMoreBinding;

public class MoreFragment extends Fragment {

    FragmentMoreBinding binding;
    private SharedPreferences themePreferences;

    public MoreFragment() {
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        themePreferences=view.getContext().getSharedPreferences("ui", Context.MODE_PRIVATE);

        String theme=themePreferences.getString("theme","light");
        binding.nightSwitch.setChecked(theme.equals("dark"));

        binding.nightSwitchLayout.setOnClickListener(v -> {
            binding.nightSwitch.setChecked(!binding.nightSwitch.isChecked());
        });

        binding.learnMore.setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.who_resource_url)))));
        binding.otherApps.setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.developer_play_url)))));

        binding.nightSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked){
                themePreferences.edit().putString("theme","dark").apply();
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            }else{
                themePreferences.edit().putString("theme","light").apply();
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding=FragmentMoreBinding.inflate(inflater);
        return binding.getRoot();
    }
}