package com.as.atlas.googlemapfollowwe;

import android.util.Log;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;

/**
 * Created by atlas on 2016/8/10.
 */
public class UserPlaceSelectionListener implements PlaceSelectionListener {
    private static final String TAG = UserPlaceSelectionListener.class.getSimpleName();
    public String suggestedPlace;

    @Override
    public void onPlaceSelected(Place place) {
        // TODO: Get info about the selected place.
        Log.i(TAG, "Place: " + place.getName());//get place details here
        suggestedPlace = (String) place.getName();
        Utils.getLatLngFromGoogleMapAPI(suggestedPlace);
    }

    @Override
    public void onError(Status status) {
        // TODO: Handle the error.
        Log.i(TAG, "An error occurred: " + status);
    }

    public String getSuggestedPlace() {
        return suggestedPlace;
    }
}
