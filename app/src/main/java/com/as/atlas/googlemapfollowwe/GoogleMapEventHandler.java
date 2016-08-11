package com.as.atlas.googlemapfollowwe;

import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by atlas on 2016/8/10.
 */
public class GoogleMapEventHandler extends Handler implements FetchUserBitmapTask.FetchUserBitmapResponse{
    public final static String TAG =GoogleMapEventHandler.class.getSimpleName();
    private GoogleMap googleMap;

    private static final int LETTER_ON_ICON = 1;
    private static final int ICON_SIZE = 128;

    public GoogleMapEventHandler(GoogleMap googleMap) {
        Log.d(TAG, "GoogleMapEventHandler ctr: googleMap=" + googleMap);
        this.googleMap = googleMap;
    }

    public void showOnlineUserOnMap(String name, double lat, double lng) {
        /*
         * Ref: http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=A|FF0000|000000  // Will show A on above example
         */


        Log.d(TAG, "showOnlineUserOnMap: name=" + name + " lat= " + lat + " lng=" + lng);
        LatLng latLng = new LatLng(lat, lng);
        String url = "http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=" + name.substring(0, LETTER_ON_ICON) + "|FF0000|000000";
        (new FetchUserBitmapTask(latLng, name, latLng.toString(), this)).execute(url);
    }

    // 在地圖加入指定位置與標題的標記
    public void addMarker(LatLng place, String title, String snippet, Bitmap bitmap) {

        BitmapDescriptor icon = getMarkedIcon(bitmap);
        Log.d(TAG,"title: " + title + " snippet:" + snippet);

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(place)
                .title(title.toString())
                .snippet(snippet.toString())
                .icon(icon);

        googleMap.addMarker(markerOptions);
    }

    private BitmapDescriptor getMarkedIcon(Bitmap bitmap) {
        BitmapDescriptor icon = (bitmap!= null) ? BitmapDescriptorFactory.fromBitmap(bitmap) : BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher);
        return icon;
    }

    public void moveCamera(LatLng latLng, int scale) {

        CameraPosition cameraPosition =
                new CameraPosition.Builder()
                        .target(latLng)
                        .zoom(scale)
                        .build();

        // 使用動畫的效果移動地圖
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
//        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, scale));
    }

    @Override
    public void responseWithFetchUserBitmapResult(LatLng latLng, String title, String snippet, Bitmap bitmap) {
        Bitmap scaledBitmap = Utils.scaleBitmap(bitmap, ICON_SIZE, ICON_SIZE);
        addMarker(latLng, title, snippet, scaledBitmap);
        moveCamera(latLng, 16);
    }

}
