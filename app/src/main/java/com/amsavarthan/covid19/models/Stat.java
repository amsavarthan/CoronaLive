package com.amsavarthan.covid19.models;

import com.google.gson.Gson;

public class Stat {

    //this is exclusively for country query
    private String flag,continent,country;
    private int totalCases,todayCases,deaths,todayDeaths,recovered,todayRecovered,population,todayActive,todayCritical,tests;
    private long lastUpdated;

    public Stat() {
    }

    public String serialize(){
        Gson gson=new Gson();
        return gson.toJson(this);
    }

    public static Stat getInstance(String serializedData){
        if (serializedData.equals("")){
            return new Stat();
        }else{
            Gson gson=new Gson();
            return gson.fromJson(serializedData, Stat.class);
        }

    }

    public int getTodayCases() {
        return todayCases;
    }

    public void setTodayCases(int todayCases) {
        this.todayCases = todayCases;
    }

    public int getDeaths() {
        return deaths;
    }

    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }

    public int getTodayDeaths() {
        return todayDeaths;
    }

    public void setTodayDeaths(int todayDeaths) {
        this.todayDeaths = todayDeaths;
    }

    public int getRecovered() {
        return recovered;
    }

    public void setRecovered(int recovered) {
        this.recovered = recovered;
    }

    public int getTodayRecovered() {
        return todayRecovered;
    }

    public void setTodayRecovered(int todayRecovered) {
        this.todayRecovered = todayRecovered;
    }

    public int getPopulation() {
        return population;
    }

    public void setPopulation(int population) {
        this.population = population;
    }

    public int getTodayActive() {
        return todayActive;
    }

    public void setTodayActive(int todayActive) {
        this.todayActive = todayActive;
    }

    public int getTodayCritical() {
        return todayCritical;
    }

    public void setTodayCritical(int todayCritical) {
        this.todayCritical = todayCritical;
    }

    public int getTests() {
        return tests;
    }

    public void setTests(int tests) {
        this.tests = tests;
    }

    public int getTotalCases() {
        return totalCases;
    }

    public String getContinent() {
        return continent;
    }

    public void setContinent(String continent) {
        this.continent = continent;
    }

    public void setTotalCases(int totalCases) {
        this.totalCases = totalCases;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
