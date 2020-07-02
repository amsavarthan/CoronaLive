package com.amsavarthan.covid19.models;

public class Country {

    private String flag,countryName;

    public Country(String flag, String countryName) {
        this.flag = flag;
        this.countryName = countryName;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }
}
