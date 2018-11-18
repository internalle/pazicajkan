/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package mk.com.pazicajkan.pazicajkan.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import mk.com.pazicajkan.pazicajkan.MainActivity;
import mk.com.pazicajkan.pazicajkan.R;

/**
 * These utilities will be used to communicate with the network.
 */
public class NetworkUtils {

    String API_BASE_URL = "";
    public static final String TAG = NetworkUtils.class.getSimpleName();
    final static String PARAM_QUERY = "q";

    /*
     * The sort field. One of stars, forks, or updated.
     * Default: results are sorted by best match if no field is specified.
     */
    final static String PARAM_SORT = "sort";
    final static String sortBy = "stars";

    public NetworkUtils(String urlString) {
        API_BASE_URL = urlString;
    }

    /**
     * Builds the URL used to query GitHub.
     *
     * @param githubSearchQuery The keyword that will be queried for.
     * @return The URL to use to query the GitHub.
     */
    public URL buildUrl(String githubSearchQuery) {
        Uri builtUri = Uri.parse(API_BASE_URL).buildUpon()
                .appendQueryParameter(PARAM_QUERY, githubSearchQuery)
                .appendQueryParameter(PARAM_SORT, sortBy)
                .build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    /**
     * This method returns the entire result from the HTTP response.
     *
     * @param url The URL to fetch the HTTP response from.
     * @return The contents of the HTTP response.
     * @throws IOException Related to network and stream reading
     */
    public String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }

    public void post(String url, Map map, Context context, final MainActivity activity) {
        RequestQueue requestQueue = Volley.newRequestQueue(context);

        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);

        try {
            JSONObject jsonBody = new JSONObject();

            jsonBody.put("email", map.get("email"));
            jsonBody.put("name", map.get("name"));
            jsonBody.put("platform_id", map.get("platform_id"));
            jsonBody.put("platform", map.get("platform"));

            JsonObjectRequest stringRequest = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    jsonBody,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                String success = response.getString("success");
                                Integer user_id = response.getInt("user_id");

                                if (success == "true") {
                                    activity.openLocationInMap();
                                    activity.setUserId(user_id);
                                }
                            } catch (Exception e) {
                                //handle exceptions
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                           // Log.e("NEW", error.toString());
                        }
                    }
            );

            requestQueue.add(stringRequest);
        } catch (Exception e) {

        }
    }
}