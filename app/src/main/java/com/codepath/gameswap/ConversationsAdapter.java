package com.codepath.gameswap;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.codepath.gameswap.fragments.ConversationFragment;
import com.codepath.gameswap.models.Conversation;
import com.parse.ParseUser;

import org.w3c.dom.Text;

import java.util.List;

public class ConversationsAdapter extends RecyclerView.Adapter<ConversationsAdapter.ViewHolder>{

    public static final String TAG = ConversationsAdapter.class.getSimpleName();

    private final Context context;
    private final List<Conversation> conversations;

    public ConversationsAdapter(Context context, List<Conversation> conversations) {
        this.context = context;
        this.conversations = conversations;
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
        holder.bind(conversation, position);
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

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private LinearLayout llPreview;
        private TextView tvUsername;

        private Conversation conversation;

        public ViewHolder(@NonNull View view) {
            super(view);
            llPreview = view.findViewById(R.id.llPreview);
            tvUsername = view.findViewById(R.id.tvUsername);

            llPreview.setOnClickListener(this);
        }

        public void bind(Conversation conversation, int position) {
            this.conversation = conversation;
            ParseUser userOne = conversation.getUserOne();
            ParseUser userTwo = conversation.getUserOne();
            if (userOne.equals(ParseUser.getCurrentUser())) {
                tvUsername.setText(userTwo.getUsername());
            } else {
                tvUsername.setText(userOne.getUsername());
            }
        }

        @Override
        public void onClick(View view) {
            FragmentManager fragmentManager = ((FragmentActivity) context).getSupportFragmentManager();
            Fragment fragment = new ConversationFragment();
            fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).addToBackStack(null).commit();
        }
    }
}
