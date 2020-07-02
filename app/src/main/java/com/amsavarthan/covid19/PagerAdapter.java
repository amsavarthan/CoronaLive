package com.amsavarthan.covid19;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.amsavarthan.covid19.fragments.PagerItemFragment;
import com.amsavarthan.covid19.models.Tip;

import java.util.List;

public class PagerAdapter extends FragmentStatePagerAdapter {

    List<Tip> mTips;

    public PagerAdapter(@NonNull List<Tip> mTips, @NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
        this.mTips=mTips;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return PagerItemFragment.newInstance(mTips.get(position));
    }


    @Override
    public int getCount() {
        return mTips.size();
    }
}
