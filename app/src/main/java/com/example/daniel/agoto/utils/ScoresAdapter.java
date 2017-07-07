package com.example.daniel.agoto.utils;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.daniel.agoto.R;

import java.util.List;

/**
 * Created by Daniel on 6/28/2017.
 */

public class ScoresAdapter extends RecyclerView.Adapter<ScoresAdapter.ItemScoreViewHolder> {

    private List<User> usersList;
    private Context context;

    public ScoresAdapter(List<User> usersList, Context context) {
        this.usersList = usersList;
        this.context = context;
    }

    @Override
    public ItemScoreViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_score, parent, false);

        return new ItemScoreViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ItemScoreViewHolder holder, int position) {
        if(usersList.get(position).getMe()) {
            holder.llItemScoreView.setBackgroundColor(context.getColor(R.color.colorMe));
        }
        holder.tvNumber.setText(String.valueOf(position+1)+".");
        holder.tvScore.setText(String.valueOf(usersList.get(position).getScore()));
        holder.tvUser.setText(usersList.get(position).getEmail());
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    public class ItemScoreViewHolder extends RecyclerView.ViewHolder {
        TextView tvUser, tvScore,tvNumber;
        LinearLayout llItemScoreView;

        public ItemScoreViewHolder(View view) {
            super(view);
            tvUser = (TextView) view.findViewById(R.id.tvUser);
            tvScore = (TextView) view.findViewById(R.id.tvScore);
            llItemScoreView = (LinearLayout) view.findViewById(R.id.llItemScoreView);
            tvNumber= (TextView) view.findViewById(R.id.tvNumber);
        }
    }
}
