package com.amsavarthan.covid19.fragments;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.amsavarthan.covid19.MainActivity;
import com.amsavarthan.covid19.R;
import com.amsavarthan.covid19.RecyclerAdapter;
import com.amsavarthan.covid19.databinding.FragmentStatsBinding;
import com.amsavarthan.covid19.models.Country;
import com.amsavarthan.covid19.models.Stat;
import com.amsavarthan.covid19.onItemClickListener;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONArrayRequestListener;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ocpsoft.prettytime.PrettyTime;

import java.io.IOException;
import java.util.AbstractSequentialList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static android.content.Context.CONNECTIVITY_SERVICE;
import static android.content.Context.LOCATION_SERVICE;
import static android.content.Context.MODE_PRIVATE;


public class StatsFragment extends Fragment implements onItemClickListener {

    private static final String TAG = StatsFragment.class.getSimpleName();
    private FragmentStatsBinding binding;
    private SharedPreferences dataSharedPref;
    private Stat worldStatToday, worldStatYesterday, countryStatToday, countryStatYesterday;
    private String[] countries;
    private List<Country> mCountries;
    private RecyclerAdapter mAdapter;

    public StatsFragment() {
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        AndroidNetworking.forceCancelAll();
    }



    private void hideCountryStats() {
        binding.flag.setVisibility(View.GONE);
        binding.countryName.setVisibility(View.GONE);
        binding.csPbar.setVisibility(View.GONE);
        binding.countryTab.setVisibility(View.GONE);
        binding.countryStatus.setVisibility(View.GONE);
        binding.cUpdated.setVisibility(View.GONE);
        binding.divider.setVisibility(View.GONE);
    }

    private void showCountryStats() {
        binding.flag.setVisibility(View.VISIBLE);
        binding.countryName.setVisibility(View.VISIBLE);
        binding.csPbar.setVisibility(View.VISIBLE);
        binding.countryTab.setVisibility(View.VISIBLE);
        binding.countryStatus.setVisibility(View.VISIBLE);
        binding.cUpdated.setVisibility(View.VISIBLE);
        binding.divider.setVisibility(View.VISIBLE);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mCountries=new ArrayList<>();
        mAdapter=new RecyclerAdapter(getContext(),this,mCountries);

        LinearLayoutManager layoutManager=new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        binding.recyclerView.setLayoutManager(layoutManager);
        binding.recyclerView.addItemDecoration(new DividerItemDecoration(getContext(),DividerItemDecoration.VERTICAL));
        binding.recyclerView.setItemAnimator(new DefaultItemAnimator());
        binding.recyclerView.setAdapter(mAdapter);

        binding.ctItem1.setOnClickListener(v -> updateCountryUI(countryStatToday,countryStatYesterday,"total"));
        binding.ctItem2.setOnClickListener(v -> updateCountryUI(countryStatToday,countryStatYesterday,"today"));
        binding.ctItem3.setOnClickListener(v -> updateCountryUI(countryStatToday,countryStatYesterday,"yesterday"));

        binding.wtItem1.setOnClickListener(v -> updateWorldUI(worldStatToday,worldStatYesterday,"total"));
        binding.wtItem2.setOnClickListener(v -> updateWorldUI(worldStatToday,worldStatYesterday,"today"));
        binding.wtItem3.setOnClickListener(v -> updateWorldUI(worldStatToday,worldStatYesterday,"yesterday"));

        countries=getContext().getResources().getStringArray(R.array.countries);
        ArrayAdapter<String> adapter=new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_1,countries);
        binding.search.setAdapter(adapter);

        binding.search.setOnEditorActionListener((v, actionId, event) -> {
            if(actionId==EditorInfo.IME_ACTION_SEARCH){

                String query=binding.search.getText().toString();

                if(!Arrays.asList(countries).contains(query)){
                    Toast.makeText(getContext(), "No country found!", Toast.LENGTH_SHORT).show();
                    return false;
                }


                SearchResultDialogFragment dialogFragment=SearchResultDialogFragment.newInstance(query);
                dialogFragment.show(getChildFragmentManager(),dialogFragment.getTag());
                return true;
            }
            return false;
        });

        initUI();
        binding.swipeRefreshLayout.setOnRefreshListener(this::initUI);

    }

    private void initUI() {

        binding.swipeRefreshLayout.setRefreshing(true);
        dataSharedPref = getContext().getSharedPreferences("data", MODE_PRIVATE);

        if (!isOnline())
            Toast.makeText(getContext(), "No internet connection", Toast.LENGTH_SHORT).show();

        worldStatToday = Stat.getInstance(dataSharedPref.getString("worldStatToday", ""));
        worldStatYesterday = Stat.getInstance(dataSharedPref.getString("worldStatYesterday", ""));
        updateWorldUI(worldStatToday, worldStatYesterday,"total");
        binding.countryName.setText(String.format("%s Status", dataSharedPref.getString("countryName","")));

        countryStatToday = Stat.getInstance(dataSharedPref.getString("countryStatToday", ""));
        countryStatYesterday = Stat.getInstance(dataSharedPref.getString("countryStatYesterday", ""));
        updateCountryUI(countryStatToday, countryStatYesterday,"total");

        updateData(dataSharedPref.getString("countryName",""));

        if(binding.csPbar.getVisibility()!=View.GONE){
            fadeIn(binding.csPbar);
        }
        fadeIn(binding.wsPbar);

        //get world stats
        AndroidNetworking.get("https://corona.lmao.ninja/v2/all")
                .addQueryParameter("yesterday", "false")
                .setPriority(Priority.IMMEDIATE)
                .setTag("worldReq2")
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            worldStatToday.setLastUpdated(response.getLong("updated"));
                            worldStatToday.setTotalCases(response.getInt("cases"));
                            worldStatToday.setTodayCases(response.getInt("todayCases"));
                            worldStatToday.setDeaths(response.getInt("deaths"));
                            worldStatToday.setTodayDeaths(response.getInt("todayDeaths"));
                            worldStatToday.setRecovered(response.getInt("recovered"));
                            worldStatToday.setTodayRecovered(response.getInt("todayRecovered"));
                            worldStatToday.setTodayActive(response.getInt("active"));
                            worldStatToday.setTodayCritical(response.getInt("critical"));
                            worldStatToday.setTests(response.getInt("tests"));
                            worldStatToday.setPopulation(response.getInt("population"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        AndroidNetworking.get("https://corona.lmao.ninja/v2/all")
                                .addQueryParameter("yesterday", "true")
                                .setPriority(Priority.IMMEDIATE)
                                .setTag("worldReq2")
                                .build()
                                .getAsJSONObject(new JSONObjectRequestListener() {
                                    @Override
                                    public void onResponse(JSONObject response) {

                                        try {
                                            worldStatYesterday.setLastUpdated(response.getLong("updated"));
                                            worldStatYesterday.setTotalCases(response.getInt("cases"));
                                            worldStatYesterday.setTodayCases(response.getInt("todayCases"));
                                            worldStatYesterday.setDeaths(response.getInt("deaths"));
                                            worldStatYesterday.setTodayDeaths(response.getInt("todayDeaths"));
                                            worldStatYesterday.setRecovered(response.getInt("recovered"));
                                            worldStatYesterday.setTodayRecovered(response.getInt("todayRecovered"));
                                            worldStatYesterday.setTodayActive(response.getInt("active"));
                                            worldStatYesterday.setTodayCritical(response.getInt("critical"));
                                            worldStatYesterday.setTests(response.getInt("tests"));
                                            worldStatYesterday.setPopulation(response.getInt("population"));
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }

                                        dataSharedPref.edit().putString("worldStatToday", worldStatToday.serialize()).apply();
                                        dataSharedPref.edit().putString("worldStatYesterday", worldStatYesterday.serialize()).apply();
                                        updateWorldUI(worldStatToday, worldStatYesterday,"total");

                                    }

                                    @Override
                                    public void onError(ANError anError) {
                                        Log.e(TAG, "onError: ", anError);
                                    }
                                });


                    }

                    @Override
                    public void onError(ANError anError) {
                        Log.e(TAG, "onError: ", anError);
                    }
                });

        binding.chipGroup.check(R.id.filter1);
        getMostAffected("cases");

        binding.chipGroup.setOnCheckedChangeListener((group, checkedId) -> {

            switch (checkedId){
                case R.id.filter1:
                    getMostAffected("cases");
                    break;
                case R.id.filter2:
                    getMostAffected("active");
                    break;
                case R.id.filter3:
                    getMostAffected("critical");
                    break;
                case R.id.filter4:
                    getMostAffected("recovered");
                    break;
                case R.id.filter5:
                    getMostAffected("deaths");
                    break;
            }
        });

        binding.swipeRefreshLayout.setRefreshing(false);

    }

    private void updateData(String country) {
        try {
            if(country.isEmpty()){
                hideCountryStats();
                return;
            }else{
                showCountryStats();
            }
            binding.countryName.setText(String.format("%s Status", country));
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

                                            dataSharedPref.edit().putString("countryStatToday", countryStatToday.serialize()).apply();
                                            dataSharedPref.edit().putString("countryStatYesterday", countryStatYesterday.serialize()).apply();
                                            updateCountryUI(countryStatToday, countryStatYesterday,"total");

                                        }

                                        @Override
                                        public void onError(ANError anError) {
                                            Log.e(TAG, "onError: ", anError);
                                        }
                                    });

                        }

                        @Override
                        public void onError(ANError anError) {
                            Log.e(TAG, "onError: ", anError);
                        }
                    });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getMostAffected(String sortedBy) {

        fadeIn(binding.maPbar);

        AndroidNetworking.get("https://corona.lmao.ninja/v2/countries")
                .addQueryParameter("yesterday","false")
                .addQueryParameter("sort",sortedBy)
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONArray(new JSONArrayRequestListener() {
                    @Override
                    public void onResponse(JSONArray response) {

                        try {
                            mCountries.clear();
                            for(int i=0;i<=4;i++){

                                if(mCountries.size()==5){
                                    mCountries.remove(i);
                                }

                                JSONObject object=response.getJSONObject(i);
                                String countryName=object.getString("country");
                                JSONObject countryInfo=object.getJSONObject("countryInfo");
                                String flag=countryInfo.getString("flag");

                                Country country=new Country(flag,countryName);
                                mCountries.add(country);
                                mAdapter.notifyDataSetChanged();

                            }
                            fadeOut(binding.maPbar);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onError(ANError anError) {
                        Log.e(TAG, "onError: ", anError);
                    }
                });

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

    private void updateWorldUI(@NotNull Stat todayStat, @NotNull Stat yesterdayStat, @NonNull String type) {

        fadeOut(binding.wsPbar);

        setSelection(binding.wtItem1,binding.wtItem2,binding.wtItem3,type);

        switch (type) {
            case "today":

                if(binding.worldInfectedCountChange.getVisibility()!=View.VISIBLE) {
                    fadeIn(binding.worldInfectedCountChange);
                    fadeIn(binding.worldCriticalCountChange);
                    fadeIn(binding.worldRecoveredCountChange);
                    fadeIn(binding.worldActiveCountChange);
                    fadeIn(binding.worldDeathCountChange);
                }

                binding.wUpdated.setText(String.format("Last Updated : %s", new PrettyTime().format(new Date(todayStat.getLastUpdated()))));
                binding.worldInfectedCount.setText(String.valueOf(todayStat.getTodayCases()));
                binding.worldActiveCount.setText(String.valueOf(todayStat.getTodayActive()));
                binding.worldCriticalCount.setText(String.valueOf(todayStat.getTodayCritical()));
                binding.worldRecoveredCount.setText(String.valueOf(todayStat.getTodayRecovered()));
                binding.worldDeathCount.setText(String.valueOf(todayStat.getTodayDeaths()));

                int ichange = todayStat.getTodayCases() - yesterdayStat.getTodayCases();
                int achange = todayStat.getTodayActive() - yesterdayStat.getTodayActive();
                int cchange = todayStat.getTodayCritical() - yesterdayStat.getTodayCritical();
                int rchange = todayStat.getTodayRecovered() - yesterdayStat.getTodayRecovered();
                int dchange = todayStat.getTodayDeaths() - yesterdayStat.getTodayDeaths();

                if (ichange == 0) {
                    binding.worldInfectedCountChange.setText("...");
                } else {
                    binding.worldInfectedCountChange.setText(ichange > 0 ? "+" + ichange : String.valueOf(ichange));
                    binding.worldInfectedCountChange.setTextColor(ichange > 0 ? getContext().getColor(R.color.green) : getContext().getColor(R.color.red));
                }

                if (achange == 0) {
                    binding.worldActiveCountChange.setText("...");
                } else {
                    binding.worldActiveCountChange.setText(achange > 0 ? "+" + achange : String.valueOf(achange));
                    binding.worldActiveCountChange.setTextColor(achange > 0 ? getContext().getColor(R.color.green) : getContext().getColor(R.color.red));
                }

                if (cchange == 0) {
                    binding.worldCriticalCountChange.setText("...");
                } else {
                    binding.worldCriticalCountChange.setText(cchange > 0 ? "+" + cchange : String.valueOf(cchange));
                    binding.worldCriticalCountChange.setTextColor(cchange > 0 ? getContext().getColor(R.color.green) : getContext().getColor(R.color.red));
                }

                if (rchange == 0) {
                    binding.worldRecoveredCountChange.setText("...");
                } else {
                    binding.worldRecoveredCountChange.setText(rchange > 0 ? "+" + rchange : String.valueOf(rchange));
                    binding.worldRecoveredCountChange.setTextColor(rchange > 0 ? getContext().getColor(R.color.green) : getContext().getColor(R.color.red));
                }

                if (dchange == 0) {
                    binding.worldDeathCountChange.setText("...");
                } else {
                    binding.worldDeathCountChange.setText(dchange > 0 ? "+" + dchange : String.valueOf(dchange));
                    binding.worldDeathCountChange.setTextColor(dchange > 0 ? getContext().getColor(R.color.green) : getContext().getColor(R.color.red));
                }
                break;

            case "total":

                if(binding.worldInfectedCountChange.getVisibility()!=View.VISIBLE) {
                    fadeIn(binding.worldInfectedCountChange);
                    fadeIn(binding.worldCriticalCountChange);
                    fadeIn(binding.worldRecoveredCountChange);
                    fadeIn(binding.worldActiveCountChange);
                    fadeIn(binding.worldDeathCountChange);
                }

                binding.wUpdated.setText(String.format("Last Updated : %s", new PrettyTime().format(new Date(todayStat.getLastUpdated()))));
                binding.worldInfectedCount.setText(String.valueOf(todayStat.getTotalCases()));
                binding.worldActiveCount.setText(String.valueOf(todayStat.getTodayActive()));
                binding.worldCriticalCount.setText(String.valueOf(todayStat.getTodayCritical()));
                binding.worldRecoveredCount.setText(String.valueOf(todayStat.getRecovered()));
                binding.worldDeathCount.setText(String.valueOf(todayStat.getDeaths()));

                ichange = todayStat.getTotalCases() - yesterdayStat.getTotalCases();
                achange = todayStat.getTodayActive() - yesterdayStat.getTodayActive();
                cchange = todayStat.getTodayCritical() - yesterdayStat.getTodayCritical();
                rchange = todayStat.getRecovered() - yesterdayStat.getRecovered();
                dchange = todayStat.getDeaths() - yesterdayStat.getDeaths();

                if (ichange == 0) {
                    binding.worldInfectedCountChange.setText("...");
                } else {
                    binding.worldInfectedCountChange.setText(ichange > 0 ? "+" + ichange : String.valueOf(ichange));
                    binding.worldInfectedCountChange.setTextColor(ichange > 0 ? getContext().getColor(R.color.green) : getContext().getColor(R.color.red));
                }

                if (achange == 0) {
                    binding.worldActiveCountChange.setText("...");
                } else {
                    binding.worldActiveCountChange.setText(achange > 0 ? "+" + achange : String.valueOf(achange));
                    binding.worldActiveCountChange.setTextColor(achange > 0 ? getContext().getColor(R.color.green) : getContext().getColor(R.color.red));
                }

                if (cchange == 0) {
                    binding.worldCriticalCountChange.setText("...");
                } else {
                    binding.worldCriticalCountChange.setText(cchange > 0 ? "+" + cchange : String.valueOf(cchange));
                    binding.worldCriticalCountChange.setTextColor(cchange > 0 ? getContext().getColor(R.color.green) : getContext().getColor(R.color.red));
                }

                if (rchange == 0) {
                    binding.worldRecoveredCountChange.setText("...");
                } else {
                    binding.worldRecoveredCountChange.setText(rchange > 0 ? "+" + rchange : String.valueOf(rchange));
                    binding.worldRecoveredCountChange.setTextColor(rchange > 0 ? getContext().getColor(R.color.green) : getContext().getColor(R.color.red));
                }

                if (dchange == 0) {
                    binding.worldDeathCountChange.setText("...");
                } else {
                    binding.worldDeathCountChange.setText(dchange > 0 ? "+" + dchange : String.valueOf(dchange));
                    binding.worldDeathCountChange.setTextColor(dchange > 0 ? getContext().getColor(R.color.green) : getContext().getColor(R.color.red));
                }

                break;
            case "yesterday":

                fadeOut(binding.worldInfectedCountChange);
                fadeOut(binding.worldCriticalCountChange);
                fadeOut(binding.worldRecoveredCountChange);
                fadeOut(binding.worldActiveCountChange);
                fadeOut(binding.worldDeathCountChange);

                binding.wUpdated.setText(String.format("Last Updated : %s", new PrettyTime().format(new Date(yesterdayStat.getLastUpdated()))));
                binding.worldInfectedCount.setText(String.valueOf(yesterdayStat.getTodayCases()));
                binding.worldActiveCount.setText(String.valueOf(yesterdayStat.getTodayActive()));
                binding.worldCriticalCount.setText(String.valueOf(yesterdayStat.getTodayCritical()));
                binding.worldRecoveredCount.setText(String.valueOf(yesterdayStat.getTodayRecovered()));
                binding.worldDeathCount.setText(String.valueOf(yesterdayStat.getTodayDeaths()));

                break;
        }

    }

    private void updateCountryUI(@NotNull Stat todayStat, @NotNull Stat yesterdayStat, @NonNull String type) {

        fadeOut(binding.csPbar);

        Glide.with(getContext()).applyDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.ic_placeholder)).load(todayStat.getFlag()).into(binding.flag);
        setSelection(binding.ctItem1,binding.ctItem2,binding.ctItem3,type);

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

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentStatsBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onItemClicked(Country country) {
        SearchResultDialogFragment dialogFragment=SearchResultDialogFragment.newInstance(country.getCountryName());
        dialogFragment.show(getChildFragmentManager(),dialogFragment.getTag());
    }

}
