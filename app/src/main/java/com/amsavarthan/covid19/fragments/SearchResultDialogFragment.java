package com.amsavarthan.covid19.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;

import com.amsavarthan.covid19.R;
import com.amsavarthan.covid19.databinding.FragmentSearchResultDialogBinding;
import com.amsavarthan.covid19.models.Stat;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import org.ocpsoft.prettytime.PrettyTime;

import java.util.Date;

public class SearchResultDialogFragment extends BottomSheetDialogFragment {

    FragmentSearchResultDialogBinding binding;
    private String country;

    public static SearchResultDialogFragment newInstance(String country) {
        Bundle args = new Bundle();
        args.putString("country",country);
        SearchResultDialogFragment fragment = new SearchResultDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        country=getArguments().getString("country");
    }

    @Nullable
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding=FragmentSearchResultDialogBinding.inflate(inflater);
        return binding.getRoot();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog=super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(dialogInterface -> {
            BottomSheetDialog bottomSheetDialog=(BottomSheetDialog)dialogInterface;
            setupFullHeight(bottomSheetDialog);
        });
        return dialog;
    }

    private void setupFullHeight(BottomSheetDialog bottomSheetDialog) {
        FrameLayout bottomSheet=bottomSheetDialog.findViewById(R.id.design_bottom_sheet);
//        BottomSheetBehavior behavior=BottomSheetBehavior.from(bottomSheet);

        ViewGroup.LayoutParams layoutParams=bottomSheet.getLayoutParams();
        int windowHeight=getWindowHeight();
        if(layoutParams!=null){
            layoutParams.height=windowHeight;
        }
        bottomSheet.setLayoutParams(layoutParams);
//        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);

    }

    private int getWindowHeight() {
        DisplayMetrics displayMetrics=new DisplayMetrics();
        ((Activity)getActivity()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.heightPixels;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        Stat countryStatToday=new Stat();
        Stat countryStatYesterday=new Stat();

        //get country stats
        AndroidNetworking.get("https://corona.lmao.ninja/v2/countries/"+country)
                .addQueryParameter("yesterday", "false")
                .addQueryParameter("strict", "true")
                .addQueryParameter("query", "")
                .setTag("countryReq1")
                .setPriority(Priority.IMMEDIATE)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            JSONObject countryInfo = response.getJSONObject("countryInfo");
                            countryStatToday.setFlag(countryInfo.getString("flag"));
                            countryStatToday.setCountry(response.getString("country"));
                            countryStatToday.setContinent(response.getString("continent"));
                            countryStatToday.setLastUpdated(response.getLong("updated"));
                            countryStatToday.setTotalCases(response.getInt("cases"));
                            countryStatToday.setTodayCases(response.getInt("todayCases"));
                            countryStatToday.setDeaths(response.getInt("deaths"));
                            countryStatToday.setTodayDeaths(response.getInt("todayDeaths"));
                            countryStatToday.setRecovered(response.getInt("recovered"));
                            countryStatToday.setTodayRecovered(response.getInt("todayRecovered"));
                            countryStatToday.setTodayActive(response.getInt("active"));
                            countryStatToday.setTodayCritical(response.getInt("critical"));
                            countryStatToday.setTests(response.getInt("tests"));
                            countryStatToday.setPopulation(response.getInt("population"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        AndroidNetworking.get("https://corona.lmao.ninja/v2/countries/"+country)
                                .addQueryParameter("yesterday", "true")
                                .addQueryParameter("strict", "true")
                                .addQueryParameter("query", "")
                                .setTag("countryReq2")
                                .setPriority(Priority.IMMEDIATE)
                                .build()
                                .getAsJSONObject(new JSONObjectRequestListener() {
                                    @Override
                                    public void onResponse(JSONObject response) {

                                        try {
                                            countryStatYesterday.setLastUpdated(response.getLong("updated"));
                                            countryStatYesterday.setTotalCases(response.getInt("cases"));
                                            countryStatYesterday.setTodayCases(response.getInt("todayCases"));
                                            countryStatYesterday.setDeaths(response.getInt("deaths"));
                                            countryStatYesterday.setTodayDeaths(response.getInt("todayDeaths"));
                                            countryStatYesterday.setRecovered(response.getInt("recovered"));
                                            countryStatYesterday.setTodayRecovered(response.getInt("todayRecovered"));
                                            countryStatYesterday.setTodayActive(response.getInt("active"));
                                            countryStatYesterday.setTodayCritical(response.getInt("critical"));
                                            countryStatYesterday.setTests(response.getInt("tests"));
                                            countryStatYesterday.setPopulation(response.getInt("population"));
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }

                                        updateCountryUI(countryStatToday, countryStatYesterday,"total");

                                    }

                                    @Override
                                    public void onError(ANError anError) {

                                    }
                                });

                    }

                    @Override
                    public void onError(ANError anError) {

                    }
                });

        binding.ctItem1.setOnClickListener(v -> updateCountryUI(countryStatToday,countryStatYesterday,"total"));
        binding.ctItem2.setOnClickListener(v -> updateCountryUI(countryStatToday,countryStatYesterday,"today"));
        binding.ctItem3.setOnClickListener(v -> updateCountryUI(countryStatToday,countryStatYesterday,"yesterday"));

    }

    private void setSelection(@NotNull TextView item1,@NotNull TextView item2,@NotNull TextView item3,@NotNull String type) {

        if (type.equals("total")){
            item1.setTextColor(getContext().getColor(R.color.selected));
            item2.setTextColor(getContext().getColor(R.color.unselected));
            item3.setTextColor(getContext().getColor(R.color.unselected));
        }else if (type.equals("today")){
            item2.setTextColor(getContext().getColor(R.color.selected));
            item1.setTextColor(getContext().getColor(R.color.unselected));
            item3.setTextColor(getContext().getColor(R.color.unselected));
        }else if(type.equals("yesterday")){
            item3.setTextColor(getContext().getColor(R.color.selected));
            item2.setTextColor(getContext().getColor(R.color.unselected));
            item1.setTextColor(getContext().getColor(R.color.unselected));
        }

    }

    private void updateCountryUI(@NotNull Stat todayStat, @NotNull Stat yesterdayStat, @NonNull String type) {

        Glide.with(getContext()).applyDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.ic_placeholder)).load(todayStat.getFlag()).into(binding.flag);
        setSelection(binding.ctItem1,binding.ctItem2,binding.ctItem3,type);
        binding.countryName.setText(String.format("%s Status",todayStat.getCountry()));
        switch (type) {
            case "today":
                if(binding.countryInfectedCountChange.getVisibility()!=View.VISIBLE){
                    fadeIn(binding.countryInfectedCountChange);
                    fadeIn(binding.countryCriticalCountChange);
                    fadeIn(binding.countryRecoveredCountChange);
                    fadeIn(binding.countryActiveCountChange);
                    fadeIn(binding.countryDeathCountChange);
                }

                binding.cUpdated.setText(String.format("Last Updated : %s", new PrettyTime().format(new Date(todayStat.getLastUpdated()))));
                binding.countryInfectedCount.setText(String.valueOf(todayStat.getTodayCases()));
                binding.countryActiveCount.setText(String.valueOf(todayStat.getTodayActive()));
                binding.countryCriticalCount.setText(String.valueOf(todayStat.getTodayCritical()));
                binding.countryRecoveredCount.setText(String.valueOf(todayStat.getTodayRecovered()));
                binding.countryDeathCount.setText(String.valueOf(todayStat.getTodayDeaths()));

                int ichange = todayStat.getTodayCases() - yesterdayStat.getTodayCases();
                int achange = todayStat.getTodayActive() - yesterdayStat.getTodayActive();
                int cchange = todayStat.getTodayCritical() - yesterdayStat.getTodayCritical();
                int rchange = todayStat.getTodayRecovered() - yesterdayStat.getTodayRecovered();
                int dchange = todayStat.getTodayDeaths() - yesterdayStat.getTodayDeaths();

                if (ichange == 0) {
                    binding.countryInfectedCountChange.setText("...");
                } else {
                    binding.countryInfectedCountChange.setText(ichange > 0 ? "+" + ichange : String.valueOf(ichange));
                    binding.countryInfectedCountChange.setTextColor(ichange > 0 ? getContext().getColor(R.color.green) : getContext().getColor(R.color.red));
                }

                if (achange == 0) {
                    binding.countryActiveCountChange.setText("...");
                } else {
                    binding.countryActiveCountChange.setText(achange > 0 ? "+" + achange : String.valueOf(achange));
                    binding.countryActiveCountChange.setTextColor(achange > 0 ? getContext().getColor(R.color.green) : getContext().getColor(R.color.red));
                }

                if (cchange == 0) {
                    binding.countryCriticalCountChange.setText("...");
                } else {
                    binding.countryCriticalCountChange.setText(cchange > 0 ? "+" + cchange : String.valueOf(cchange));
                    binding.countryCriticalCountChange.setTextColor(cchange > 0 ? getContext().getColor(R.color.green) : getContext().getColor(R.color.red));
                }

                if (rchange == 0) {
                    binding.countryRecoveredCountChange.setText("...");
                } else {
                    binding.countryRecoveredCountChange.setText(rchange > 0 ? "+" + rchange : String.valueOf(rchange));
                    binding.countryRecoveredCountChange.setTextColor(rchange > 0 ? getContext().getColor(R.color.green) : getContext().getColor(R.color.red));
                }

                if (dchange == 0) {
                    binding.countryDeathCountChange.setText("...");
                } else {
                    binding.countryDeathCountChange.setText(dchange > 0 ? "+" + dchange : String.valueOf(dchange));
                    binding.countryDeathCountChange.setTextColor(dchange > 0 ? getContext().getColor(R.color.green) : getContext().getColor(R.color.red));
                }
                break;
            case "total":

                if(binding.countryInfectedCountChange.getVisibility()!=View.VISIBLE){
                    fadeIn(binding.countryInfectedCountChange);
                    fadeIn(binding.countryCriticalCountChange);
                    fadeIn(binding.countryRecoveredCountChange);
                    fadeIn(binding.countryActiveCountChange);
                    fadeIn(binding.countryDeathCountChange);
                }

                binding.cUpdated.setText(String.format("Last Updated : %s", new PrettyTime().format(new Date(todayStat.getLastUpdated()))));
                binding.countryInfectedCount.setText(String.valueOf(todayStat.getTotalCases()));
                binding.countryActiveCount.setText(String.valueOf(todayStat.getTodayActive()));
                binding.countryCriticalCount.setText(String.valueOf(todayStat.getTodayCritical()));
                binding.countryRecoveredCount.setText(String.valueOf(todayStat.getRecovered()));
                binding.countryDeathCount.setText(String.valueOf(todayStat.getDeaths()));

                ichange = todayStat.getTotalCases() - yesterdayStat.getTotalCases();
                achange = todayStat.getTodayActive() - yesterdayStat.getTodayActive();
                cchange = todayStat.getTodayCritical() - yesterdayStat.getTodayCritical();
                rchange = todayStat.getRecovered() - yesterdayStat.getRecovered();
                dchange = todayStat.getDeaths() - yesterdayStat.getDeaths();

                if (ichange == 0) {
                    binding.countryInfectedCountChange.setText("...");
                } else {
                    binding.countryInfectedCountChange.setText(ichange > 0 ? "+" + ichange : String.valueOf(ichange));
                    binding.countryInfectedCountChange.setTextColor(ichange > 0 ? getContext().getColor(R.color.green) : getContext().getColor(R.color.red));
                }

                if (achange == 0) {
                    binding.countryActiveCountChange.setText("...");
                } else {
                    binding.countryActiveCountChange.setText(achange > 0 ? "+" + achange : String.valueOf(achange));
                    binding.countryActiveCountChange.setTextColor(achange > 0 ? getContext().getColor(R.color.green) : getContext().getColor(R.color.red));
                }

                if (cchange == 0) {
                    binding.countryCriticalCountChange.setText("...");
                } else {
                    binding.countryCriticalCountChange.setText(cchange > 0 ? "+" + cchange : String.valueOf(cchange));
                    binding.countryCriticalCountChange.setTextColor(cchange > 0 ? getContext().getColor(R.color.green) : getContext().getColor(R.color.red));
                }

                if (rchange == 0) {
                    binding.countryRecoveredCountChange.setText("...");
                } else {
                    binding.countryRecoveredCountChange.setText(rchange > 0 ? "+" + rchange : String.valueOf(rchange));
                    binding.countryRecoveredCountChange.setTextColor(rchange > 0 ? getContext().getColor(R.color.green) : getContext().getColor(R.color.red));
                }

                if (dchange == 0) {
                    binding.countryDeathCountChange.setText("...");
                } else {
                    binding.countryDeathCountChange.setText(dchange > 0 ? "+" + dchange : String.valueOf(dchange));
                    binding.countryDeathCountChange.setTextColor(dchange > 0 ? getContext().getColor(R.color.green) : getContext().getColor(R.color.red));
                }
                break;
            case "yesterday":

                fadeOut(binding.countryInfectedCountChange);
                fadeOut(binding.countryCriticalCountChange);
                fadeOut(binding.countryRecoveredCountChange);
                fadeOut(binding.countryActiveCountChange);
                fadeOut(binding.countryDeathCountChange);

                binding.cUpdated.setText(String.format("Last Updated : %s", new PrettyTime().format(new Date(yesterdayStat.getLastUpdated()))));
                binding.countryInfectedCount.setText(String.valueOf(yesterdayStat.getTodayCases()));
                binding.countryActiveCount.setText(String.valueOf(yesterdayStat.getTodayActive()));
                binding.countryCriticalCount.setText(String.valueOf(yesterdayStat.getTodayCritical()));
                binding.countryRecoveredCount.setText(String.valueOf(yesterdayStat.getTodayRecovered()));
                binding.countryDeathCount.setText(String.valueOf(yesterdayStat.getTodayDeaths()));

                break;
        }

    }

    private void fadeOut(View view) {
        view.animate().alpha(0.0f).setDuration(300).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                view.setVisibility(View.INVISIBLE);
            }
        }).start();
    }

    private void fadeIn(View view) {
        view.setVisibility(View.VISIBLE);
        view.setAlpha(0.0f);
        view.animate().alpha(1.0f).setDuration(300).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                view.setVisibility(View.VISIBLE);
            }
        }).start();
    }

}