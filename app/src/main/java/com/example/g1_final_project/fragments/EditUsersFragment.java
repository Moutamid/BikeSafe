package com.example.g1_final_project.fragments;

import static android.view.LayoutInflater.from;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.g1_final_project.R;
import com.example.g1_final_project.databinding.FragmentEditUsersBinding;
import com.example.g1_final_project.models.UserModel;
import com.example.g1_final_project.models.UserModel2;
import com.example.g1_final_project.utils.Constants;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class EditUsersFragment extends Fragment {

    private FragmentEditUsersBinding b;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        b = FragmentEditUsersBinding.inflate(inflater, container, false);
        View root = b.getRoot();

        Constants.databaseReference().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()) {

                    usersArraylist.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        UserModel2 model2 = dataSnapshot.getValue(UserModel2.class);
                        model2.uid = dataSnapshot.getKey();
                        usersArraylist.add(model2);
                    }

                    initRecyclerView();

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return root;
    }

    private ArrayList<UserModel2> usersArraylist = new ArrayList<>();

    private RecyclerView conversationRecyclerView;
    private RecyclerViewAdapterMessages adapter;

    private void initRecyclerView() {

        conversationRecyclerView = b.ediusersRecyc;
        adapter = new RecyclerViewAdapterMessages();
        if (isAdded()) {
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext());
            linearLayoutManager.setReverseLayout(true);
            conversationRecyclerView.setLayoutManager(linearLayoutManager);
            conversationRecyclerView.setHasFixedSize(true);
            conversationRecyclerView.setNestedScrollingEnabled(false);

            conversationRecyclerView.setAdapter(adapter);
        }
    }


    private class RecyclerViewAdapterMessages extends RecyclerView.Adapter
            <RecyclerViewAdapterMessages.ViewHolderRightMessage> {

        @NonNull
        @Override
        public ViewHolderRightMessage onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = from(parent.getContext()).inflate(R.layout.layout_edit_users, parent, false);
            return new ViewHolderRightMessage(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final ViewHolderRightMessage holder, int position) {
            UserModel2 model = usersArraylist.get(position);

            holder.title.setText(model.name);
            holder.username.setText(model.username);
            holder.email.setText(model.email);
            holder.password.setText(model.password);

            holder.edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDialog(model);
                }
            });
            holder.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(requireContext())
                            .setTitle("Are you sure?")
                            .setMessage("Do you really want to delete this user?")
                            .setNegativeButton("No", (dialog, which) -> {
                                dialog.dismiss();
                            })
                            .setPositiveButton("Yes", (dialog, which) -> {
                                Constants.databaseReference().child(model.uid).removeValue();
                                dialog.dismiss();
                            })
                            .show();
                }
            });

        }

        @Override
        public int getItemCount() {
            if (usersArraylist == null)
                return 0;
            return usersArraylist.size();
        }

        public class ViewHolderRightMessage extends RecyclerView.ViewHolder {

            TextView title, username, email, password;
            ImageView profile, edit, delete;

            public ViewHolderRightMessage(@NonNull View v) {
                super(v);
                title = v.findViewById(R.id.title_name);
                username = v.findViewById(R.id.username);
                email = v.findViewById(R.id.email);
                password = v.findViewById(R.id.password);
                profile = v.findViewById(R.id.img_user);
                edit = v.findViewById(R.id.img_edit);
                delete = v.findViewById(R.id.img_delete);

            }
        }

    }

    private void showDialog(UserModel2 model2) {
        UserModel2 userModel2 = model2;

        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_edit);
        dialog.setCancelable(true);
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(dialog.getWindow().getAttributes());
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;

        TextInputLayout email = dialog.findViewById(R.id.email);
        TextInputLayout name = dialog.findViewById(R.id.name);
        TextInputLayout username = dialog.findViewById(R.id.username);
        TextInputLayout password = dialog.findViewById(R.id.password);

        email.getEditText().setText(model2.email);
        name.getEditText().setText(model2.name);
        username.getEditText().setText(model2.username);
        password.getEditText().setText(model2.password);

        dialog.findViewById(R.id.btnSubmit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // CODE HERE

                String emailStr = email.getEditText().getText().toString();
                String nameStr = name.getEditText().getText().toString();
                String usernameStr = username.getEditText().getText().toString();
                String passwordStr = password.getEditText().getText().toString();

                if (emailStr.isEmpty())
                    return;
                if (nameStr.isEmpty())
                    return;
                if (usernameStr.isEmpty())
                    return;
                if (passwordStr.isEmpty())
                    return;

                userModel2.email = emailStr;
                userModel2.name = nameStr;
                userModel2.username = usernameStr;
                userModel2.password = passwordStr;

                Constants.databaseReference().child(userModel2.uid).setValue(userModel2);

                dialog.dismiss();
            }
        });
        dialog.show();
        dialog.getWindow().setAttributes(layoutParams);

    }
}