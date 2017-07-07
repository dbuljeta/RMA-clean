package com.example.daniel.agoto;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.daniel.agoto.utils.AppConstants;
import com.example.daniel.agoto.utils.CustomVolleyRequest;
import com.example.daniel.agoto.utils.DatabaseHelper;
import com.example.daniel.agoto.utils.Task;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends Activity implements OnMapReadyCallback, View.OnClickListener {
    private static final int REQUEST_LOCATION_PERMISSION = 50;
    private TextView tvTasksDone, tvTimeElapsed;
    private ProgressBar progressBar;
    private MapFragment mMapFragment;
    private GoogleMap mGoogleMap;
    private Button bScores, bSignOut;
    private LocationListener mLocationListener;
    private LocationManager mLocationManager;
    private MarkerOptions taskMarker, currentMarker;
    private boolean markerExist = false, fCalled = false;
    private ArrayList<LatLng> locations;
    private String Result = "";
    private List<Task> taskList;
    private Integer countOfSolvedTasks = 0;
    private Handler mHandler;
    private boolean mStarted;
    private long seconds = 0;

    //https://stackoverflow.com/questions/21981817/is-there-already-a-stopwatch-class-for-android-and-why-doesnt-my-implementation
    private final Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            if (mStarted) {
                ++seconds;
                tvTimeElapsed.setText(String.format("%02d:%02d", seconds / 60, seconds % 60));
                mHandler.postDelayed(mRunnable, 1000L);
            }
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DatabaseHelper databaseHelper = DatabaseHelper.getInstance(this);
        taskList = databaseHelper.getAllUnfinishedTasks();
        setUpUI();
        this.mLocationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        mHandler = new Handler();
    }

    private void setUpUI() {
        countOfSolvedTasks = DatabaseHelper.getInstance(this).getNumberOfSolvedTasks();
        tvTasksDone = (TextView) findViewById(R.id.tvTasksDone);
        tvTasksDone.setText(String.valueOf(countOfSolvedTasks) + "/" + String.valueOf(taskList.size() + countOfSolvedTasks));
        progressBar = (ProgressBar) findViewById(R.id.progress);
        progressBar.setProgress((int) ((float) countOfSolvedTasks / (taskList.size() + countOfSolvedTasks) * 100));
        tvTimeElapsed = (TextView) findViewById(R.id.tvTimeElapsed);
        bScores = (Button) findViewById(R.id.bScores);
        bSignOut = (Button) findViewById(R.id.bSignOut);

        this.mMapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.fGoogleMap);
        Log.e("aloo", "asyncMap");
        this.mMapFragment.getMapAsync(this);
        locations = new ArrayList();
        currentMarker = new MarkerOptions();
        mLocationListener = new SimpleLocationListener();
        bScores.setOnClickListener(this);
        bSignOut.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mStarted = true;
        mHandler.postDelayed(mRunnable, 1000L);
        if (hasLocationPermission() == false) {
            requestPermission();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopTracking();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (this.hasLocationPermission()) {
            startTracking();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mStarted = false;
        mHandler.removeCallbacks(mRunnable);
    }

    private void stopTracking() {
        Log.e("Tracking", "Tracking stopped.");
        this.mLocationManager.removeUpdates(this.mLocationListener);
    }

    private void startTracking() {
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        String locationProvider = this.mLocationManager.getBestProvider(criteria, true);
        long minTime = 1000;
        float minDistance = 1;
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        this.mLocationManager.requestLocationUpdates(locationProvider, minTime, minDistance,
                this.mLocationListener);
    }

    private boolean hasLocationPermission() {
        String LocationPermission = android.Manifest.permission.ACCESS_FINE_LOCATION;
        int status = ContextCompat.checkSelfPermission(this, LocationPermission);
        if (status == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }

    private void requestPermission() {
        String[] permissions = new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION};
        ActivityCompat.requestPermissions(MainActivity.this,
                permissions, REQUEST_LOCATION_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION_PERMISSION:
                if (grantResults.length > 0) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        Log.e("Permission", "Permission granted. User pressed allow.");
                    } else {
                        Log.e("Permission", "Permission not granted. User pressed deny.");
                        askForPermission();
                    }
                }
        }

    }

    private void askForPermission() {
        boolean shouldExplain = ActivityCompat.shouldShowRequestPermissionRationale(
                MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION);
        if (shouldExplain) {
            this.displayDialog();
        } else {

            Toast.makeText(this, "Sorry, we really need that permission", Toast.LENGTH_SHORT).show();
        }
    }

    private void displayDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle("Location permission")
                .setMessage("To solve tasks, application needs your permission")
                .setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.e("Permission", "User declined and won't be asked again.");
                        dialog.cancel();
                    }
                })
                .setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.e("Permission", "Permission requested because of the explanation.");
                        requestPermission();
                        dialog.cancel();
                    }
                })
                .show();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.mGoogleMap = googleMap;
        UiSettings uiSettings = this.mGoogleMap.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);
        uiSettings.setMyLocationButtonEnabled(true);
        uiSettings.setZoomGesturesEnabled(true);
        createLocations();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                !=
                PackageManager.PERMISSION_GRANTED) {
            return;
        }
        this.mGoogleMap.setMyLocationEnabled(true);
    }

    private void createLocations() {
        for (int i = 0; i < taskList.size(); i++) {
            Log.e("lok", "LOKACIJA" + String.valueOf(i));
            LatLng latl = new LatLng(taskList.get(i).getLatitude(), taskList.get(i).getLongitude());
            Log.e("for", String.valueOf(i));
            locations.add(latl);
            setTaskMarkers(latl, taskList.get(i).getName());
        }
    }

    private void setTaskMarkers(LatLng lat, String Name) {
        taskMarker = new MarkerOptions();
        taskMarker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        taskMarker.position(lat);
        taskMarker.title(Name);
        mGoogleMap.addMarker(taskMarker);
    }

    private void managerTasks(Location mlocation) {
        float distance;
        for (int i = 0; i < locations.size(); i++) {
            Location locationTaskMarker = new Location("");
            locationTaskMarker.setLatitude(locations.get(i).latitude);
            locationTaskMarker.setLongitude(locations.get(i).longitude);
            distance = locationTaskMarker.distanceTo(mlocation);
            Log.e("distance", Float.toString(distance));
            if (distance < 50) {
                if (!fCalled) {
                    fCalled = true;
                    taskBuilder(i);
                }
            }
        }
    }

    private void taskBuilder(Integer i) {
        final Task task = taskList.get(i);
        String Task = task.getBody();
        final String Solution = task.getSolution();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final EditText input = new EditText(this);
        builder.setTitle(Task);
        builder.setView(input);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (Result.equals(Solution)) {
                    Toast.makeText(MainActivity.this, "Correct Answer!!:)", Toast.LENGTH_SHORT).show();
                    final JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put(AppConstants.KEY_TASK_ID, task.getServer_id());
                        Log.e("server_id", String.valueOf(task.getServer_id()));

                        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, AppConstants.BASE_URL + AppConstants.SOLVE_TASK,
                                jsonObject, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Log.e("Reposnsereg0", String.valueOf(response));
                                try {
                                    if (response.getInt("status") == 1) {
                                        Toast.makeText(MainActivity.this, "Bravissimo", Toast.LENGTH_SHORT).show();
                                    } else
                                        Toast.makeText(MainActivity.this, "Server error, please try again!", Toast.LENGTH_SHORT).show();

                                    DatabaseHelper databaseHelper = DatabaseHelper.getInstance(MainActivity.this);
                                    task.setCompleted(true);
                                    databaseHelper.solveTask(task);
                                    Log.e("BEFORE SEND", String.valueOf(task.getScore()));
                                    databaseHelper.updateUserScore(task.getScore());
                                    taskList.clear();
                                    taskList = databaseHelper.getAllUnfinishedTasks();
                                    locations.clear();
                                    mGoogleMap.clear();
                                    currentMarker.visible(true);
                                    mGoogleMap.addMarker(currentMarker);
                                    createLocations();
                                    tvTasksDone.setText(String.valueOf(++countOfSolvedTasks) + "/" + String.valueOf(taskList.size() + countOfSolvedTasks));
                                    progressBar.setProgress((int) ((float) countOfSolvedTasks / (taskList.size() + countOfSolvedTasks) * 100));
                                    fCalled = false;

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.e("ReposnseReger", String.valueOf(error));
                            }
                        }) {
                            @Override
                            public Map<String, String> getHeaders() throws AuthFailureError {
                                Map<String, String> params = new HashMap<String, String>();
                                SharedPreferences sharedPreferences = MainActivity.this.getSharedPreferences(
                                        AppConstants.SHARERD_PREFERANCES_NAME, Context.MODE_PRIVATE);
                                String jwt = "Bearer" + sharedPreferences.getString(AppConstants.KEY_JWT,"nema");
                                params.put("Authorization", jwt);
                                return params;
                            }

                            @Override
                            public Request<?> setRetryPolicy(RetryPolicy retryPolicy) {
                                RetryPolicy retryPolicy1 = new DefaultRetryPolicy(10000, 2, 2);
                                return super.setRetryPolicy(retryPolicy1);
                            }
                        };
                        CustomVolleyRequest.getInstance(MainActivity.this).addToRequestQueue(jsonObjectRequest);
                    } catch (JSONException e) {
                        Log.e("Error", e.getMessage());
                        e.printStackTrace();
                    }
                } else if (!Result.equals(Solution)) {
                    Toast.makeText(MainActivity.this, "Wrong answer, please think about it!", Toast.LENGTH_SHORT).show();
                    fCalled = false;
                }
                dialog.dismiss();
            }
        });
        Log.e("dialog", "negative button");
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                fCalled = false;
                dialog.dismiss();
            }
        });

        final AlertDialog dialog1 = builder.create();
        dialog1.show();
        dialog1.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);

        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Check if edittext is empty
                if (TextUtils.isEmpty(s)) {
                    // Disable ok button
                    dialog1.getButton(
                            AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                } else {
                    // Something into edit text. Enable the button.
                    dialog1.getButton(
                            AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                    Result = input.getText().toString();
                }
            }
        });
    }

    private void showLocation(Location location) {
        if (markerExist) {
            Log.e("postoji", "da");
            currentMarker.visible(false);
        }
        LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
        Log.e("currentLocation", currentLocation.toString());
        currentMarker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        currentMarker.position(currentLocation)
                .title("You are here").snippet("Do some tasks! :)");
        mGoogleMap.addMarker(currentMarker);
        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLng(currentLocation));
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(currentLocation)
                .build();
        mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 12));
        markerExist = true;
        managerTasks(location);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bSignOut:
                DatabaseHelper databaseHelper = DatabaseHelper.getInstance(MainActivity.this);
                databaseHelper.deleteAllTasks();
                databaseHelper.deleteAllUsers();
                Intent loginActivity = new Intent();
                loginActivity.setClass(this, LoginActivity.class);

                SharedPreferences sharedPreferences = MainActivity.this.getSharedPreferences(
                        AppConstants.SHARERD_PREFERANCES_NAME, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(AppConstants.KEY_JWT, "nema");
                editor.commit();
                startActivity(loginActivity);
                this.finish();
                break;
            case R.id.bScores:
                Intent scoresIntent = new Intent();
                scoresIntent.setClass(this, ScoresActivity.class);
                startActivity(scoresIntent);
                break;
        }

    }

    private class SimpleLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            showLocation(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    }
}
