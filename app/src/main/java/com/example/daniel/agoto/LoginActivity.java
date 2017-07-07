package com.example.daniel.agoto;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.example.daniel.agoto.utils.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    EditText etEmail, etPass;
    Button bLogin, bSignUp;
    public String email;
    public String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setUpUI();
        SharedPreferences sharedPreferences = LoginActivity.this.getSharedPreferences(
                AppConstants.SHARERD_PREFERANCES_NAME, Context.MODE_PRIVATE);
        String jwt = sharedPreferences.getString(AppConstants.KEY_JWT,"nema");
        if (!jwt.equals("nema")) {
            DatabaseHelper databaseHelper = DatabaseHelper.getInstance(LoginActivity.this);
            databaseHelper.deleteAllTasks();
            databaseHelper.deleteAllUsers();
            sharedPreferences = this.getSharedPreferences(
                    AppConstants.SHARERD_PREFERANCES_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(AppConstants.KEY_JWT, "nema");
            editor.commit();
        }
        Log.e("Spremljenin jwt", jwt);
    }

    private void setUpUI() {
        etEmail = (EditText) findViewById(R.id.etEmail);
        etPass = (EditText) findViewById(R.id.etPassword);
        bLogin = (Button) findViewById(R.id.bLogin);
        bSignUp = (Button) findViewById(R.id.bSignUp);
        bLogin.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        bLogin.setEnabled(false);
        email = String.valueOf(etEmail.getText());
        password = String.valueOf(etPass.getText());
        Log.e("email", email);
        Log.e("password", password);
        if (!email.isEmpty() && !password.isEmpty()) {
            final JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put(AppConstants.KEY_EMAIL, email);
                jsonObject.put(AppConstants.KEY_PASSWORD, password);
                final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, AppConstants.BASE_URL + AppConstants.LOGIN_URL,
                        jsonObject, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e("Reposnse", String.valueOf(response));
                        if (!(String.valueOf(response).contains("jwt"))) {
                            Toast.makeText(LoginActivity.this, "You're not registred or you didn't put correct email or password, please click on Sign up button bellow", Toast.LENGTH_SHORT).show();
                            bLogin.setEnabled(true);
                        } else {
                            try {
                                String jwt = String.valueOf(response.get(AppConstants.KEY_JWT));
                                SharedPreferences sharedPreferences = LoginActivity.this.getSharedPreferences(
                                        AppConstants.SHARERD_PREFERANCES_NAME, Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString(AppConstants.KEY_JWT, jwt);
                                editor.commit();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            startSynchronization();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Reposnse", String.valueOf(error));
                        bLogin.setEnabled(true);
                        Toast.makeText(LoginActivity.this, "Server error occure, please try again later!", Toast.LENGTH_SHORT).show();
                    }
                }) {
                    @Override
                    public Request<?> setRetryPolicy(RetryPolicy retryPolicy) {
                        RetryPolicy retryPolicy1 = new DefaultRetryPolicy(10000, 2, 2);
                        return super.setRetryPolicy(retryPolicy1);
                    }
                };
                CustomVolleyRequest.getInstance(this).addToRequestQueue(jsonObjectRequest);
            } catch (JSONException e) {
                Log.e("Error", e.getMessage());
                bLogin.setEnabled(true);
                e.printStackTrace();
            }

        } else {
            Toast.makeText(this, "Please type into something", Toast.LENGTH_SHORT).show();
            bLogin.setEnabled(true);
        }
    }

    private void startSynchronization() {
        final DatabaseHelper databaseHelper = DatabaseHelper.getInstance(this);
        final JsonObjectRequest getTasksRequest = new JsonObjectRequest(Request.Method.GET, AppConstants.BASE_URL + AppConstants.TASK_SYN,
                null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.e("RESPONSE SA", String.valueOf(response));
                try {
                    JSONArray tasks = response.getJSONArray(AppConstants.KEY_TASKS);
                    for (int i = 0; i < tasks.length(); ++i) {
                        JSONObject jsonObject = (JSONObject) tasks.get(i);
                        databaseHelper.addTask(new Task(
                                jsonObject.getLong(AppConstants.KEY_SERVER_ID),
                                jsonObject.getString(AppConstants.KEY_NAME),
                                jsonObject.getString(AppConstants.KEY_BODY),
                                jsonObject.getString(AppConstants.KEY_SOLUTION),
                                jsonObject.getInt(AppConstants.KEY_COMPLETED) > 0,
                                jsonObject.getDouble(AppConstants.KEY_LONGITUDE),
                                jsonObject.getDouble(AppConstants.KEY_LATITUDE),
                                jsonObject.getInt(AppConstants.KEY_SCORE)
                        ));
                    }
                    JSONArray users = response.getJSONArray(AppConstants.KEY_USERS);

                    for (int i = 0; i < users.length(); ++i) {
                        JSONObject jsonObject = (JSONObject) users.get(i);
                        databaseHelper.addUser(new User(
                                jsonObject.getString(AppConstants.KEY_EMAIL),
                                jsonObject.getInt(AppConstants.KEY_SCORE),
                                jsonObject.getBoolean(AppConstants.KEY_IS_ME)
                        ));
                    }
                    goIntoMainActivity();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e("ERRA", e.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("RESPONSE SA", String.valueOf(error));
                bLogin.setEnabled(true);
                Toast.makeText(LoginActivity.this, "Server error occure, please try again later!", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                SharedPreferences sharedPreferences = LoginActivity.this.getSharedPreferences(
                        AppConstants.SHARERD_PREFERANCES_NAME, Context.MODE_PRIVATE);
                String jwt = "Bearer" + sharedPreferences.getString("jwt", "nema");
                params.put("Authorization", jwt);
                return params;
            }

            @Override
            public Request<?> setRetryPolicy(RetryPolicy retryPolicy) {
                RetryPolicy retryPolicy1 = new DefaultRetryPolicy(90000, 2, 2);
                return super.setRetryPolicy(retryPolicy1);
            }
        };
        CustomVolleyRequest.getInstance(this).addToRequestQueue(getTasksRequest);
    }

    private void goIntoMainActivity() {
        Intent mainActivity = new Intent();
        mainActivity.setClass(getApplicationContext(), MainActivity.class);
        Toast.makeText(LoginActivity.this, "Loging In", Toast.LENGTH_LONG).show();
        startActivity(mainActivity);
        this.finish();
    }


    public void SignUpActivity(View view) {
        bLogin.setEnabled(true);
        Intent SignUpActivity = new Intent(this, SignUpActivity.class);
        startActivity(SignUpActivity);
    }
}