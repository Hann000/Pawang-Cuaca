package com.kelp.pawangcuaca.Model;

public class City {
    private String name;
    private String key;

    public City(String name, String key) {
        this.name = name;
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public String getKey() {
        return key;
    }
}

