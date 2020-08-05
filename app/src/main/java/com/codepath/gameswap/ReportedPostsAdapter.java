package com.codepath.gameswap;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;

import com.codepath.gameswap.models.Post;
import com.codepath.gameswap.models.PostReport;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ReportedPostsAdapter extends PostsAdapter {

    public static final String TAG = ReportedPostsAdapter.class.getSimpleName();

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view  = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
        return new ViewHolder(view);
    }

    public ReportedPostsAdapter(Context context, List<Post> posts) {
        super(context, posts);
    }

    public class ViewHolder extends PostsAdapter.ViewHolder {

        private ImageView ivClear;
        private LinearLayout llStatus;
        private TextView tvStatusText;

        private PostReport report;

        public ViewHolder(@NonNull View view) {
            super(view);
            CardView cvContent = view.findViewById(R.id.cvContent);
            ivClear = view.findViewById(R.id.ivClear);
            llStatus = view.findViewById(R.id.llStatus);
            tvStatusText = view.findViewById(R.id.tvStatusText);

            ViewGroup.LayoutParams cvParams = cvContent.getLayoutParams();
            cvParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            cvParams.height = 320;
            cvContent.setLayoutParams(cvParams);

            ivClear.setVisibility(View.VISIBLE);
            ivClear.setOnClickListener(this);
        }

        @Override
        public void bind(Post post) {
            super.bind(post);
            ParseRelation<PostReport> relation = post.getRelation("reports");
            final ParseQuery<PostReport> query = relation.getQuery();
            query.include("post");
            query.include("reportedBy");
            query.findInBackground(new FindCallback<PostReport>() {
                @Override
                public void done(List<PostReport> postReports, ParseException e) {
                    if (e != null) {
                        Log.e(TAG, "Error getting report", e);
                        return;
                    }
                    for (PostReport newReport : postReports) {
                        if (newReport.getReportedBy().getUsername().equals(ParseUser.getCurrentUser().getUsername())) {
                            report = newReport;
                            String status = report.getStatus();
                            if (status != null && !status.isEmpty()) {
                                llStatus.setVisibility(View.VISIBLE);
                                tvStatusText.setText(status);
                            }
                        }
                    }
                }
            });
        }

        @Override
        public void onClick(View view) {
            super.onClick(view);
            if (view == ivClear) {
                new AlertDialog.Builder(context)
                        .setTitle("Delete Report")
                        .setMessage("This action cannot be undone. Are you sure you want to delete the report?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                report.deleteInBackground(new DeleteCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        if (e != null) {
                                            Log.e(TAG, "Error deleting report", e);
                                            return;
                                        }
                                        int index = posts.indexOf(post);
                                        posts.remove(index);
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
        for (final Post post : posts) {
            ParseRelation<PostReport> relation = post.getRelation("reports");
            final ParseQuery<PostReport> query = relation.getQuery();
            query.include("post");
            query.include("reportedBy");
            query.findInBackground(new FindCallback<PostReport>() {
                @Override
                public void done(List<PostReport> postReports, ParseException e) {
                    if (e != null) {
                        Log.e(TAG, "Error getting report", e);
                        return;
                    }
                    for (PostReport report : postReports) {
                        if (report.getReportedBy().getUsername().equals(ParseUser.getCurrentUser().getUsername())) {
                            report.deleteInBackground(new DeleteCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e != null) {
                                        Log.e(TAG, "Error deleting report", e);
                                        return;
                                    }
                                    int index = posts.indexOf(post);
                                    posts.remove(index);
                                    notifyItemRemoved(index);
                                }
                            });
                        }
                    }
                }
            });
        }
    }
}
