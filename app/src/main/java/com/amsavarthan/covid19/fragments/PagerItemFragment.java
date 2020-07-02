package com.amsavarthan.covid19.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amsavarthan.covid19.databinding.FragmentPagerItemBinding;
import com.amsavarthan.covid19.models.Tip;

public class PagerItemFragment extends Fragment {

    FragmentPagerItemBinding binding;
    private static String mParamTitle="title";
    private static String mParamText="content";
    private static String mParamImage="image";

    private String title,content;
    private int image;

    public PagerItemFragment() {}

    public static PagerItemFragment newInstance(Tip tip) {
        Bundle bundle=new Bundle();
        bundle.putString(mParamTitle,tip.getTitle());
        bundle.putString(mParamText,tip.getContent());
        bundle.putInt(mParamImage,tip.getImage());
        PagerItemFragment fragment=new PagerItemFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments()!=null){
            title=getArguments().getString(mParamTitle);
            content=getArguments().getString(mParamText);
            image=getArguments().getInt(mParamImage);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.title.setText(title);
        binding.content.setText(content);
        binding.image.setImageResource(image);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding=FragmentPagerItemBinding.inflate(inflater);
        return binding.getRoot();
    }
}