package org.codecyprus.th.model;

import java.io.Serializable;

public class Location implements Serializable {

    private String uuid; // PK
    private String sessionUuid; // FK
    private long timestamp;
    private double latitude;
    private double longitude;

    public Location(String sessionUuid, long timestamp, double latitude, double longitude) {
        this(null, sessionUuid, timestamp, latitude, longitude);
    }

    public Location(String uuid, String sessionUuid, long timestamp, double latitude, double longitude) {
        this.uuid = uuid;
        this.sessionUuid = sessionUuid;
        this.timestamp = timestamp;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getUuid() {
        return uuid;
    }

    public String getSessionUuid() {
        return sessionUuid;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double distanceTo(final double latitude, final double longitude) {
        final double R = 6371f; // earth diameter, in Km
        final double dLat = toRad(this.latitude -latitude);
        final double dLng = toRad(this.longitude - longitude);
        final double latRadian1 = toRad(latitude);
        final double latRadian2 = toRad(this.latitude);

        final double a = Math.sin(dLat/2d) * Math.sin(dLat/2d) + Math.sin(dLng/2d) * Math.sin(dLng/2d) * Math.cos(latRadian1) * Math.cos(latRadian2);
        final double c = 2d * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        return (float) (R * c);
    }

    private double toRad(final double n)
    {
        return n * Math.PI / 180d;
    }
}