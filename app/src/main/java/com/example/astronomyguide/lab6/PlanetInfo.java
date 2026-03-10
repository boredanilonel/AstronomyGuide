package com.example.astronomyguide.lab6;

public class PlanetInfo {
    private String name;
    private String russianName;
    private String description;
    private int imageResourceId;
    private String[] characteristics;

    public PlanetInfo(String name, String russianName, String description, int imageResourceId, String[] characteristics) {
        this.name = name;
        this.russianName = russianName;
        this.description = description;
        this.imageResourceId = imageResourceId;
        this.characteristics = characteristics;
    }

    public String getName() {
        return name;
    }

    public String getRussianName() {
        return russianName;
    }

    public String getDescription() {
        return description;
    }

    public int getImageResourceId() {
        return imageResourceId;
    }

    public String[] getCharacteristics() {
        return characteristics;
    }
}