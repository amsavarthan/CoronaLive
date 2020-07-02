package com.amsavarthan.covid19.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amsavarthan.covid19.PagerAdapter;
import com.amsavarthan.covid19.R;
import com.amsavarthan.covid19.databinding.FragmentHomeBinding;
import com.amsavarthan.covid19.models.Tip;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    FragmentHomeBinding binding;


    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        List<Tip> mTips=new ArrayList<>();
        mTips.add(new Tip("Prepare Yourself","Being strong minded is the most powerful weapon in defeating the virus.", R.drawable.prepare_fight));
        mTips.add(new Tip("Be Hygiene","Wash your hands often using hand wash or alcohol based sanitizers.",R.drawable.wash_hands));
        mTips.add(new Tip("Stay at Home","Enjoy your me time by staying at home to slow the spread of virus.",R.drawable.stay_home));
        mTips.add(new Tip("Social Distancing","Keep yourself 1m apart from other person while talking.",R.drawable.social_distance));

        PagerAdapter adapter=new PagerAdapter(mTips,getChildFragmentManager(), FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        binding.viewPager.setAdapter(adapter,getChildFragmentManager());
        binding.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        binding.piv.setSelected(0);
        binding.chip.setOnClickListener(v -> {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.who_resource_url))));
        });

    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding=FragmentHomeBinding.inflate(inflater);
        return binding.getRoot();
    }
}