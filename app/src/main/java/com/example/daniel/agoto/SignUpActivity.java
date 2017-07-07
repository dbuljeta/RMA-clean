package com.example.daniel.agoto;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.daniel.agoto.utils.AppConstants;
import com.example.daniel.agoto.utils.CustomVolleyRequest;

import org.json.JSONException;
import org.json.JSONObject;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {
    EditText etEmail, etPass;
    Button bSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle("Sign up");
        setContentView(R.layout.activity_sign_up);
        setUpUI();
    }

    private void setUpUI() {
        etEmail = (EditText) findViewById(R.id.etASUEmail);
        etPass = (EditText) findViewById(R.id.etASUPassword);
        bSignUp = (Button) findViewById(R.id.bASUSignUp);
        bSignUp.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        bSignUp.setEnabled(false);
        String email = String.valueOf(etEmail.getText());
        String password = String.valueOf(etPass.getText());
        Log.e("email", email);
        Log.e("password", password);
        if (!email.isEmpty() && !password.isEmpty()) {
        final JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(AppConstants.KEY_EMAIL, email);
            jsonObject.put(AppConstants.KEY_PASSWORD, password);
            Log.e("pass", password);

            final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, AppConstants.BASE_URL + AppConstants.REGISTER_URL,
                    jsonObject, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.e("Reposnsereg0", String.valueOf(response));
                    try {
                        if (response.getInt("status") == 1) {
                            Toast.makeText(SignUpActivity.this, "Registred, now you can click on Login button", Toast.LENGTH_SHORT).show();
                        } else
                            Toast.makeText(SignUpActivity.this, "Error during registration, please try again!", Toast.LENGTH_SHORT).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    finish();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(SignUpActivity.this, "Server error occure, please try again later!", Toast.LENGTH_SHORT).show();
                    bSignUp.setEnabled(true);
                }
            }) {
                @Override
                public Request<?> setRetryPolicy(RetryPolicy retryPolicy) {
                    RetryPolicy retryPolicy1 = new DefaultRetryPolicy(90000, 2, 2);
                    return super.setRetryPolicy(retryPolicy1);
                }
            };
            CustomVolleyRequest.getInstance(this).addToRequestQueue(jsonObjectRequest);
        } catch (JSONException e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }

    }
    else Toast.makeText(this,"Please type into something", Toast.LENGTH_SHORT).show();
        bSignUp.setEnabled(true);
    }
}
