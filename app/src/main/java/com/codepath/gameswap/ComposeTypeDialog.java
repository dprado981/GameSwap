package com.codepath.gameswap;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.codepath.gameswap.fragments.ChatsFragment;
import com.codepath.gameswap.fragments.ComposeFragment;
import com.codepath.gameswap.fragments.ConversationFragment;
import com.codepath.gameswap.fragments.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.jetbrains.annotations.NotNull;

public class ComposeTypeDialog extends DialogFragment {

    private Fragment oldFragment;
    private BottomNavigationView bottomNavigation;

    public interface ComposeTypeDialogListener {
        void onClick(DialogInterface dialog, int pos);
    }

    public static final String TAG = ComposeTypeDialog.class.getSimpleName();
    private Context context;
    private ComposeTypeDialogListener listener;

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.choose_type)
                .setItems(R.array.type_array, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int pos) {
                        listener.onClick(dialog, pos);
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        this.context = context;
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = (ComposeTypeDialogListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(context.toString()
                    + " must implement NoticeDialogListener");
        }
    }

    public void setBottomNav(BottomNavigationView bottomNavigation) {
        this.bottomNavigation = bottomNavigation;
    }

    public void setOldFragment(Fragment oldFragment) {
        this.oldFragment = oldFragment;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (oldFragment instanceof ProfileFragment) {
            bottomNavigation.setSelectedItemId(R.id.actionProfile);
        } else if (oldFragment instanceof ChatsFragment || oldFragment instanceof ConversationFragment) {
            bottomNavigation.setSelectedItemId(R.id.actionChat);
        } else if (oldFragment instanceof ComposeFragment) {
            bottomNavigation.setSelectedItemId(R.id.actionCompose);
        } else {
            bottomNavigation.setSelectedItemId(R.id.actionHome);
        }
    }
}
