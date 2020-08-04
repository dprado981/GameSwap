package com.codepath.gameswap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.codepath.gameswap.fragments.ConversationFragment;
import com.codepath.gameswap.fragments.ProfileFragment;
import com.codepath.gameswap.models.Conversation;
import com.codepath.gameswap.models.Post;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.util.List;

public class ConversationsAdapter extends RecyclerView.Adapter<ConversationsAdapter.ViewHolder>{

    public static final String TAG = ConversationsAdapter.class.getSimpleName();

    private final Context context;
    private FragmentManager fragmentManager;
    private final List<Conversation> conversations;

    public ConversationsAdapter(Context context, List<Conversation> conversations) {
        this.context = context;
        this.conversations = conversations;
        fragmentManager = ((FragmentActivity) context).getSupportFragmentManager();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view  = LayoutInflater.from(context).inflate(R.layout.item_conversation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Conversation conversation = conversations.get(position);
        holder.bind(conversation);
    }

    @Override
    public int getItemCount() {
        return conversations.size();
    }

    public void clear() {
        conversations.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<Conversation> list) {
        conversations.addAll(list);
        notifyDataSetChanged();
    }

    public boolean isEmpty() {
        return conversations.isEmpty();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView ivProfile;
        private LinearLayout llPreview;
        private TextView tvName;
        private TextView tvPreview;

        private Conversation conversation;
        private ParseUser currentUser;
        private ParseUser otherUser;

        public ViewHolder(@NonNull View view) {
            super(view);
            ivProfile = view.findViewById(R.id.ivProfile);
            llPreview = view.findViewById(R.id.llPreview);
            tvName = view.findViewById(R.id.tvName);
            tvPreview = view.findViewById(R.id.tvPreview);
            ivProfile.setOnClickListener(this);
            llPreview.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            Fragment fragment;
            Bundle bundle = new Bundle();
            if (view == llPreview) {
                fragment = new ConversationFragment();
                bundle.putParcelable(Conversation.TAG, conversation);

            } else if (view == ivProfile) {
                fragment = new ProfileFragment();
                bundle.putParcelable(Post.KEY_USER, otherUser);
            } else {
                Log.e(TAG, "Not yet implemented");
                return;
            }
            fragment.setArguments(bundle);
            fragmentManager.beginTransaction()
                    .replace(R.id.flContainer, fragment)
                    .addToBackStack(null)
                    .commit();
        }

        public void bind(Conversation conversation) {
            this.conversation = conversation;
            currentUser = ParseUser.getCurrentUser();
            otherUser = getOtherUser(conversation);
            String firstName = otherUser.getString("firstName");
            String lastName = otherUser.getString("lastName");
            if (lastName != null) {
                tvName.setText(String.format("%s %s.", firstName, lastName.charAt(0)));
            } else {
                tvName.setText(String.format("%s", firstName));
            }
            if (conversation.getLastMessage() != null) {
                tvPreview.setText(conversation.getLastMessage().getText());
            }
            ParseFile image = (ParseFile) otherUser.get("image");
            ivProfile.setImageDrawable(context.getDrawable(R.drawable.ic_profile));
            if (image != null) {
                setProfileImage(image);
            }
        }

        private void setProfileImage(ParseFile image) {
            Glide.with(context)
                    .asBitmap()
                    .load(image.getUrl())
                    .circleCrop()
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            ivProfile.setImageBitmap(resource);
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                        }
                    });
        }

        private ParseUser getOtherUser(Conversation conversation) {
            ParseUser userOne = conversation.getUserOne();
            ParseUser userTwo = conversation.getUserTwo();
            if (userOne.getUsername().equals(ParseUser.getCurrentUser().getUsername())) {
                return userTwo;
            } else {
                return userOne;
            }
        }
    }
}
