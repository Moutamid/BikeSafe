package com.example.g1_final_project.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.g1_final_project.databinding.FragmentSettingsBinding;
import com.example.g1_final_project.models.UserModel;
import com.example.g1_final_project.utils.Constants;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding b;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        b = FragmentSettingsBinding.inflate(inflater, container, false);
        View root = b.getRoot();

        b.email.getEditText().setText(Constants.userModel().email);
        b.name.getEditText().setText(Constants.userModel().name);
        b.username.getEditText().setText(Constants.userModel().username);
        b.password.getEditText().setText(Constants.userModel().password);

        b.btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emailStr = b.email.getEditText().getText().toString();
                String nameStr = b.name.getEditText().getText().toString();
                String usernameStr = b.username.getEditText().getText().toString();
                String passwordStr = b.password.getEditText().getText().toString();

                if (emailStr.isEmpty())
                    return;
                if (nameStr.isEmpty())
                    return;
                if (usernameStr.isEmpty())
                    return;
                if (passwordStr.isEmpty())
                    return;

                UserModel userModel = new UserModel();
                userModel.email = emailStr;
                userModel.name = nameStr;
                userModel.username = usernameStr;
                userModel.password = passwordStr;

                Constants.databaseReference().child(Constants.auth().getUid())
                        .setValue(userModel);
                Toast.makeText(requireContext(), "Success", Toast.LENGTH_SHORT).show();

            }
        });

        return root;
    }

}