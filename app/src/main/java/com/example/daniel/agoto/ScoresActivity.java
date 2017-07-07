package com.example.daniel.agoto;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.example.daniel.agoto.utils.DatabaseHelper;
import com.example.daniel.agoto.utils.ScoresAdapter;
import com.example.daniel.agoto.utils.User;

import java.util.List;

public class ScoresActivity extends AppCompatActivity {

    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle("Scores");
        setContentView(R.layout.activity_scores);
        setUpUI();
    }

    private void setUpUI() {
        recyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        List<User> userList = DatabaseHelper.getInstance(this).getAllUsers();
        ScoresAdapter scoresAdapter = new ScoresAdapter(userList, this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                LinearLayoutManager.VERTICAL);
        recyclerView.addItemDecoration(mDividerItemDecoration);
        recyclerView.setAdapter(scoresAdapter);
    }
}
