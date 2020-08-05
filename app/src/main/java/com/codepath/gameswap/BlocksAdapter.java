package com.codepath.gameswap;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codepath.gameswap.fragments.ProfileFragment;
import com.codepath.gameswap.models.Block;
import com.codepath.gameswap.models.Post;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.DeleteCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.util.List;

public class BlocksAdapter extends RecyclerView.Adapter<BlocksAdapter.ViewHolder> {

    public static final String TAG = BlocksAdapter.class.getSimpleName();

    protected final Context context;
    protected final List<Block> blocks;

    public BlocksAdapter(Context context, List<Block> blocks) {
        this.context = context;
        this.blocks = blocks;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view  = LayoutInflater.from(context).inflate(R.layout.item_user_report, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Block block = blocks.get(position);
        holder.bind(block);
    }

    @Override
    public int getItemCount() {
        return blocks.size();
    }

    public void clear() {
        blocks.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<Block> list) {
        blocks.addAll(list);
        notifyDataSetChanged();
    }

    public List<Block> getAll() {
        return blocks;
    }

    public class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        protected RelativeLayout rlProfile;
        protected ImageView ivProfile;
        protected TextView tvName;
        protected TextView tvUsername;
        protected TextView tvBio;
        protected ImageView ivClear;

        protected Block block;
        protected ParseUser user;

        public ViewHolder(@NonNull View view) {
            super(view);

            rlProfile = view.findViewById(R.id.rlProfile);
            ivProfile = view.findViewById(R.id.ivProfile);
            tvName = view.findViewById(R.id.tvName);
            tvUsername = view.findViewById(R.id.tvUsername);
            tvBio = view.findViewById(R.id.tvBio);
            ivClear = view.findViewById(R.id.ivClear);

            rlProfile.setOnClickListener(this);
            ivClear.setOnClickListener(this);
        }

        public void bind(Block block) {
            this.block = block;
            user = block.getUser();
            String firstName = null;
            String lastName = null;
            try {
                firstName = user.fetchIfNeeded().getString("firstName");
                lastName = user.getString("lastName");
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (lastName != null) {
                tvName.setText(String.format("%s %s.", firstName, lastName.charAt(0)));
            } else {
                tvName.setText(String.format("%s", firstName));
            }
            tvUsername.setText(String.format("@%s", user.getUsername()));
            String bio = user.getString("bio");
            if (bio != null && !bio.isEmpty()) {
                tvBio.setText(bio);
            }
            ParseFile profileImage = (ParseFile) user.get("image");
            if (profileImage != null) {
                Glide.with(context)
                        .load(profileImage.getUrl())
                        .placeholder(R.drawable.ic_profile)
                        .into(ivProfile);
            }
        }

        @Override
        public void onClick(View view) {
            if (view == rlProfile) {
                FragmentActivity activity = (FragmentActivity) context;
                if (user.getUsername().equals(ParseUser.getCurrentUser().getUsername())) {
                    ((BottomNavigationView) activity.findViewById(R.id.bottomNavigation)).setSelectedItemId(R.id.actionProfile);
                }
                FragmentManager fragmentManager = activity.getSupportFragmentManager();
                Fragment fragment = new ProfileFragment();
                Bundle bundle = new Bundle();
                bundle.putParcelable(Post.KEY_USER, user);
                fragment.setArguments(bundle);
                fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).addToBackStack(null).commit();
            } else if (view == ivClear) {
                new AlertDialog.Builder(context)
                        .setTitle("Unblock Account")
                        .setMessage("This action cannot be undone. Are you sure you want to unblock @"
                                + block.getUser().getUsername() + "?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                block.deleteInBackground(new DeleteCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        if (e != null) {
                                            Log.e(TAG, "Error deleting report", e);
                                            return;
                                        }
                                        int index = blocks.indexOf(block);
                                        blocks.remove(index);
                                        notifyItemRemoved(index);
                                    }
                                });
                            }
                        })
                        // A null listener allows the button to dismiss the dialog and take no further action.
                        .setNegativeButton(android.R.string.no, null)
                        .setIcon(R.drawable.ic_caution)
                        .show();
            }
        }
    }


    public void deleteAll() {
        for (final Block block : blocks) {
            block.deleteInBackground(new DeleteCallback() {
                @Override
                public void done(ParseException e) {
                    if (e != null) {
                        Log.e(TAG, "Error deleting report", e);
                        return;
                    }
                    int index = blocks.indexOf(block);
                    blocks.remove(index);
                    notifyItemRemoved(index);
                }
            });
        }
    }
}
