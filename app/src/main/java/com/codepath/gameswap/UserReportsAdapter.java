package com.codepath.gameswap;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codepath.gameswap.fragments.ProfileFragment;
import com.codepath.gameswap.models.Post;
import com.codepath.gameswap.models.Report;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.DeleteCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.util.List;

public class UserReportsAdapter extends RecyclerView.Adapter<UserReportsAdapter.ViewHolder> {

    public static final String TAG = UserReportsAdapter.class.getSimpleName();

    protected final Context context;
    protected final List<Report> reports;

    public UserReportsAdapter(Context context, List<Report> reports) {
        this.context = context;
        this.reports = reports;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view  = LayoutInflater.from(context).inflate(R.layout.item_user_report, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Report report = reports.get(position);
        holder.bind(report);
    }

    @Override
    public int getItemCount() {
        return reports.size();
    }

    public void clear() {
        reports.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<Report> list) {
        reports.addAll(list);
        notifyDataSetChanged();
    }

    public List<Report> getAll() {
        return reports;
    }

    public class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        protected RelativeLayout rlProfile;
        protected ImageView ivProfile;
        protected TextView tvName;
        protected TextView tvUsername;
        protected TextView tvBio;
        protected LinearLayout llStatus;
        protected TextView tvStatusText;
        protected ImageView ivClear;

        protected Report report;
        protected ParseUser user;

        public ViewHolder(@NonNull View view) {
            super(view);

            rlProfile = view.findViewById(R.id.rlProfile);
            ivProfile = view.findViewById(R.id.ivProfile);
            tvName = view.findViewById(R.id.tvName);
            tvUsername = view.findViewById(R.id.tvUsername);
            tvBio = view.findViewById(R.id.tvBio);
            llStatus = view.findViewById(R.id.llStatus);
            tvStatusText = view.findViewById(R.id.tvStatusText);
            ivClear = view.findViewById(R.id.ivClear);

            rlProfile.setOnClickListener(this);
            ivClear.setOnClickListener(this);
        }

        public void bind(Report report) {
            this.report = report;
            user = report.getUser();
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
            String status = report.getStatus();
            if (status != null && !status.isEmpty()) {
                llStatus.setVisibility(View.VISIBLE);
                tvStatusText.setText(status);
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
                report.deleteInBackground(new DeleteCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null) {
                            Log.e(TAG, "Error deleting report", e);
                            return;
                        }
                        int index = reports.indexOf(report);
                        reports.remove(index);
                        notifyItemRemoved(index);
                    }
                });
            }
        }
    }


    public void deleteAll() {
        for (final Report report : reports) {
            report.deleteInBackground(new DeleteCallback() {
                @Override
                public void done(ParseException e) {
                    if (e != null) {
                        Log.e(TAG, "Error deleting report", e);
                        return;
                    }
                    int index = reports.indexOf(report);
                    reports.remove(index);
                    notifyItemRemoved(index);
                }
            });
        }
    }
}
