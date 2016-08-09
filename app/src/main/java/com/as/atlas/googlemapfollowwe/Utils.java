package com.as.atlas.googlemapfollowwe;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

/**
 * Created by atlas on 2016/7/13.
 */
public class Utils {

    private static final String ENCODE_UTF8 = "utf-8";
    private static final String PREFIX_GOOGLE_MAP_API_FOR_ADDRESS = "http://maps.google.com.tw/maps/api/geocode/json?address=";
    private static final String RESPONSE_STATUS = "status";
    private static final String RESPONSE_STATUS_OK = "OK";
    private static final String RESPONSE_RESULTS = "results";
    private static final String RESPONSE_GEOMETRY = "geometry";
    private static final String RESPONSE_LOCATION = "location";
    private static final String RESPONSE_LOCATION_LAT = "lat";
    private static final String RESPONSE_LOCATION_LNG = "lng";
    private static final int OUTSTREAM_BUFFER_SIZE = 1024;

    public static double[] getLatLngFromGoogleMapAPI(String addr) {

        try {
            addr = URLEncoder.encode(addr, ENCODE_UTF8);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String apiUrl = PREFIX_GOOGLE_MAP_API_FOR_ADDRESS + addr;
        byte[] bytes = Utils.urlToByte(apiUrl);

        if (bytes == null)  return null;

        try {
            JSONObject obj = new JSONObject(new String(bytes));

            if (obj.getString(RESPONSE_STATUS).equals(RESPONSE_STATUS_OK)) {
                JSONObject location = obj.getJSONArray(RESPONSE_RESULTS)
                        .getJSONObject(0)
                        .getJSONObject(RESPONSE_GEOMETRY)
                        .getJSONObject(RESPONSE_LOCATION);

                double lat = location.getDouble(RESPONSE_LOCATION_LAT);
                double lng = location.getDouble(RESPONSE_LOCATION_LNG);

                return new double[] {lat, lng};
            }

        } catch (JSONException e) {
            Log.d("Atlas", "getLatLngFromGoogleMapAPI() e:" + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    public static byte[] urlToByte(String urlString) {
        try {
            URL url = new URL(urlString);
            URLConnection connection = url.openConnection();
            InputStream inputStream = connection.getInputStream();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            byte[] buffer = new byte[OUTSTREAM_BUFFER_SIZE];
            int len = 0;
            while ((len = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, len);
            }

            return byteArrayOutputStream.toByteArray();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
