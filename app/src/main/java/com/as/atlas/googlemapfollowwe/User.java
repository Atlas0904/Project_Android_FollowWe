package com.as.atlas.googlemapfollowwe;

/**
 * Created by atlas on 2016/7/13.
 */
public class User {

    public String name;
    public double lat;
    public double lng;

    ArriveMethod arriveMethod;

    enum ArriveMethod{
        WALKING, BICYCLE, CAR
    }

    public User() {
      /*Blank default constructor essential for Firebase*/
    }

    public User(String name, double lat, double lng) {
        this.name = name;
        this.lat = lat;
        this.lng = lng;
        arriveMethod = ArriveMethod.WALKING;
    }

    //Getters and setters
    public String getName() { return name; }
    public double getLat() { return lat; }
    public double getLng() { return lng; }

}
