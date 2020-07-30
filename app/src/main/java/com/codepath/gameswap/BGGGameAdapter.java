package com.codepath.gameswap;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codepath.gameswap.fragments.ComposeFragment;
import com.codepath.gameswap.fragments.ComposeGameFragment;
import com.codepath.gameswap.models.BGGGame;

import java.util.List;

public class BGGGameAdapter extends RecyclerView.Adapter<BGGGameAdapter.ViewHolder>{

    public static final String TAG = BGGGameAdapter.class.getSimpleName();

    private final Context context;
    private final List<BGGGame> games;

    public BGGGameAdapter(Context context, List<BGGGame> games) {
        this.context = context;
        this.games = games;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view  = LayoutInflater.from(context).inflate(R.layout.item_bgg_game, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BGGGame game = games.get(position);
        holder.bind(game);
    }

    @Override
    public int getItemCount() {
        return games.size();
    }

    public void add(BGGGame game) {
        games.add(game);
        notifyItemInserted(games.size() - 1);
    }

    public void clear() {
        games.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<BGGGame> list) {
        games.addAll(list);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView ivImage;
        private TextView tvTitle;

        private BGGGame game;

        public ViewHolder(@NonNull View view) {
            super(view);
            ivImage = view.findViewById(R.id.ivImage);
            tvTitle = view.findViewById(R.id.tvTitle);
            view.setOnClickListener(this);
        }

        public void bind(BGGGame game) {
            this.game = game;
            tvTitle.setText(game.getTitle());
            Glide.with(context)
                    .load(game.getImageUrl())
                    .placeholder(R.drawable.ic_image)
                    .into(ivImage);
        }

        @Override
        public void onClick(View view) {
            FragmentManager fragmentManager = ((FragmentActivity)context).getSupportFragmentManager();
            ComposeFragment fragment = new ComposeGameFragment();
            Bundle bundle = new Bundle();
            bundle.putParcelable(BGGGame.TAG, game);
            fragment.setArguments(bundle);
            fragmentManager.beginTransaction()
                    .replace(R.id.flContainer, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }
}
