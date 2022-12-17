package com.example.g1_final_project.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.g1_final_project.R;
import com.example.g1_final_project.databinding.ActivitySignUpBinding;
import com.example.g1_final_project.models.UserModel;
import com.example.g1_final_project.utils.Constants;
import com.fxn.stash.Stash;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;

public class SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding b;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading...");

        b.btnBackLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        b.btnSignup.setOnClickListener(new View.OnClickListener() {
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
                progressDialog.show();
                Constants.auth().createUserWithEmailAndPassword(emailStr, passwordStr)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    uploadUserModel();
                                } else {
                                    Constants.auth().signOut();
                                    progressDialog.dismiss();
                                    Toast.makeText(SignUpActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }

                            }

                            private void uploadUserModel() {
                                Constants.databaseReference().child(Constants.auth().getUid())
                                        .setValue(userModel)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                progressDialog.dismiss();
                                                Toast.makeText(SignUpActivity.this, "Success", Toast.LENGTH_SHORT).show();
                                                Stash.put(Constants.USER_MODEL, userModel);
                                                Stash.put(Constants.IS_LOGGED_IN, true);
                                                Intent intent = new Intent(SignUpActivity.this, NavigationDrawerActivity.class);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                finish();
                                                startActivity(intent);
                                            }
                                        });
                            }
                        });

            }
        });

    }
}