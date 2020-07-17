package com.codepath.gameswap;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.codepath.gameswap.models.Message;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.List;

public class MessagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    public static final String TAG = MessagesAdapter.class.getSimpleName();
    public static final int SENT_MESSAGE = 0;
    public static final int RECEIVED_MESSAGE = 1;

    private final Context context;
    private final List<Message> messages;

    public MessagesAdapter(Context context, List<Message> messages) {
        this.context = context;
        this.messages = messages;
    }

    @Override
    public int getItemViewType(int position) {
        try {
            if (messages.get(position).getFrom().fetchIfNeeded().getUsername().equals(ParseUser.getCurrentUser().getUsername())) {
                return SENT_MESSAGE;
            } else {
                return RECEIVED_MESSAGE;
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return -1;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == SENT_MESSAGE) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_sent_message, parent, false);
            return new SentViewHolder(view);
        } else if (viewType == RECEIVED_MESSAGE) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_received_message, parent, false);
            return new ReceivedViewHolder(view);
        } else {
            return null;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);
        if (getItemViewType(position) == SENT_MESSAGE) {
            SentViewHolder sentViewHolder = (SentViewHolder) holder;
            sentViewHolder.bind(message);
        } else { // Is RECEIVED_MESSAGE
            ReceivedViewHolder receivedViewHolder = (ReceivedViewHolder) holder;
            receivedViewHolder.bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void clear() {
        messages.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<Message> list) {
        messages.addAll(list);
        notifyDataSetChanged();
    }

    public class SentViewHolder extends RecyclerView.ViewHolder {

        private TextView tvMessage;
        private Message message;

        public SentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
        }

        public void bind(Message message) {
            this.message = message;
            tvMessage.setText(message.getText());
        }


    }

    public class ReceivedViewHolder extends RecyclerView.ViewHolder {

        private TextView tvMessage;
        private Message message;

        public ReceivedViewHolder(@NonNull View view) {
            super(view);
            tvMessage = itemView.findViewById(R.id.tvMessage);
        }

        public void bind(Message message) {
            this.message = message;
            tvMessage.setText(message.getText());
        }


    }
}
