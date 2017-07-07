package com.example.daniel.agoto.utils;

import android.app.Application;
import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by daniel on 16.5.2017..
 * https://developer.android.com/training/volley/requestqueue.html
 */

public class CustomVolleyRequest extends Application {

    private static CustomVolleyRequest customVolleyRequest;
    private RequestQueue requestQueue;
    private static Context context;

    private CustomVolleyRequest(Context c) {
        context = c;
        requestQueue = getRequestQueue();
    }

    public static synchronized CustomVolleyRequest getInstance(Context c) {
        if (customVolleyRequest == null) {
            customVolleyRequest = new CustomVolleyRequest(c);
        }
        return customVolleyRequest;
    }


    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(context.getApplicationContext());
        }
        return requestQueue;
    }

    public <T> void addToRequestQueue(Request<T> request) {
        getRequestQueue().add(request);
    }

}
