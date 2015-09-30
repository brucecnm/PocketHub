/*
 * Copyright 2012 GitHub Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.pockethub.ui.issue;

import static android.app.Activity.RESULT_OK;
import static android.content.DialogInterface.BUTTON_NEGATIVE;
import static android.content.DialogInterface.BUTTON_NEUTRAL;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.github.kevinsawicki.wishlist.SingleTypeAdapter;
import com.github.pockethub.R;
import com.github.pockethub.ui.DialogFragmentActivity;
import com.github.pockethub.ui.SingleChoiceDialogFragment;
import com.github.pockethub.util.AvatarLoader;
import com.google.inject.Inject;

import java.util.ArrayList;

import com.alorma.github.sdk.bean.dto.response.User;

/**
 * Dialog fragment to select an issue assignee from a list of collaborators
 */
public class AssigneeDialogFragment extends SingleChoiceDialogFragment {

    private static class UserListAdapter extends SingleTypeAdapter<User> {

        private final int selected;

        private final AvatarLoader loader;

        public UserListAdapter(LayoutInflater inflater, User[] users,
                int selected, AvatarLoader loader) {
            super(inflater, R.layout.collaborator_item);

            this.selected = selected;
            this.loader = loader;
            setItems(users);
        }

        @Override
        public long getItemId(int position) {
            return getItem(position).id;
        }

        @Override
        protected int[] getChildViewIds() {
            return new int[] { R.id.tv_login, R.id.iv_avatar, R.id.rb_selected };
        }

        @Override
        protected void update(int position, User item) {
            setText(0, item.login);
            loader.bind(imageView(1), item);
            setChecked(2, selected == position);
        }
    }

    /**
     * Get selected user from results bundle
     *
     * @param arguments
     * @return user
     */
    public static User getSelected(Bundle arguments) {
        return (User) arguments.getParcelable(ARG_SELECTED);
    }

    /**
     * Confirm message and deliver callback to given activity
     *
     * @param activity
     * @param requestCode
     * @param title
     * @param message
     * @param choices
     * @param selectedChoice
     */
    public static void show(final DialogFragmentActivity activity,
            final int requestCode, final String title, final String message,
            ArrayList<User> choices, final int selectedChoice) {
        show(activity, requestCode, title, message, choices, selectedChoice,
                new AssigneeDialogFragment());
    }

    @Inject
    private AvatarLoader loader;

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        Activity activity = getActivity();
        Bundle arguments = getArguments();

        final AlertDialog dialog = createDialog();
        dialog.setButton(BUTTON_NEGATIVE, activity.getString(R.string.cancel),
                this);
        dialog.setButton(BUTTON_NEUTRAL, activity.getString(R.string.clear), this);

        LayoutInflater inflater = activity.getLayoutInflater();

        ListView view = (ListView) inflater.inflate(R.layout.dialog_list_view,
                null);
        view.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                onClick(dialog, position);
            }
        });

        ArrayList<User> choices = getChoices();
        int selected = arguments.getInt(ARG_SELECTED_CHOICE);
        UserListAdapter adapter = new UserListAdapter(inflater,
                choices.toArray(new User[choices.size()]), selected, loader);
        view.setAdapter(adapter);
        if (selected >= 0)
            view.setSelection(selected);
        dialog.setView(view);

        return dialog;
    }

    @SuppressWarnings("unchecked")
    private ArrayList<User> getChoices() {
        return getArguments().getParcelableArrayList(ARG_CHOICES);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        super.onClick(dialog, which);

        switch (which) {
        case BUTTON_NEGATIVE:
            break;
        case BUTTON_NEUTRAL:
            onResult(RESULT_OK);
            break;
        default:
            getArguments().putParcelable(ARG_SELECTED, getChoices().get(which));
            onResult(RESULT_OK);
        }
    }
}
