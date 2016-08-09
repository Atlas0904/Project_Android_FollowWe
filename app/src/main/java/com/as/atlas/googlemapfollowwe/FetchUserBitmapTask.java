package com.as.atlas.googlemapfollowwe;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;

import java.lang.ref.WeakReference;

/**
 * Created by atlas on 2016/8/9.
 */
public class FetchUserBitmapTask extends AsyncTask<String, Void, Bitmap> {

    WeakReference<FetchUserBitmapResponse> fetchUserBitmapResponseWeakReference;

    LatLng latLng;
    String title;
    String snippet;

    public FetchUserBitmapTask(LatLng latLng, String title, String snippet, FetchUserBitmapResponse fetchUserBitmapResponse) {
        this.fetchUserBitmapResponseWeakReference = new WeakReference<FetchUserBitmapResponse>(fetchUserBitmapResponse);

        this.latLng = latLng;
        this.title = title;
        this.snippet = snippet;
    }

    interface FetchUserBitmapResponse {
        void responseWithFetchUserBitmapResult(LatLng latLng, String title, String snippet, Bitmap bitmap);
    }

    @Override
    protected Bitmap doInBackground(String... url) {
        return Utils.getBitmapFromURL(url[0]);
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        if (fetchUserBitmapResponseWeakReference.get() != null) {
            FetchUserBitmapResponse response = fetchUserBitmapResponseWeakReference.get();
            response.responseWithFetchUserBitmapResult(latLng, title, snippet, bitmap);
        }
    }


}