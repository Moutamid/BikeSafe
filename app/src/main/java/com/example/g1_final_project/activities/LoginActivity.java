package com.example.g1_final_project.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.g1_final_project.databinding.ActivityLoginBinding;
import com.example.g1_final_project.models.UserModel;
import com.example.g1_final_project.utils.Constants;
import com.fxn.stash.Stash;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.database.DataSnapshot;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding b;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading...");

        b.btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
            }
        });

        b.btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emailStr = b.email.getEditText().getText().toString();
                String passwordStr = b.password.getEditText().getText().toString();

                if (emailStr.isEmpty())
                    return;
                if (passwordStr.isEmpty())
                    return;

                progressDialog.show();
                Constants.auth().signInWithEmailAndPassword(emailStr, passwordStr)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    getUserModel();
                                } else {
                                    Constants.auth().signOut();
                                    progressDialog.dismiss();
                                    Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }

                            private void getUserModel() {
                                Constants.databaseReference().child(Constants.auth().getUid())
                                        .get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                                            @Override
                                            public void onSuccess(DataSnapshot dataSnapshot) {
                                                UserModel userModel;
                                                if (dataSnapshot.exists()) {
                                                    userModel = dataSnapshot.getValue(UserModel.class);
                                                } else {
                                                    userModel = new UserModel();
                                                    userModel.email = emailStr;
                                                    userModel.name = "nameStr";
                                                    userModel.username = "usernameStr";
                                                    userModel.password = passwordStr;
                                                    Constants.databaseReference().child(Constants.auth().getUid())
                                                            .setValue(userModel);
                                                }
                                                progressDialog.dismiss();
                                                Toast.makeText(LoginActivity.this, "Success", Toast.LENGTH_SHORT).show();
                                                Stash.put(Constants.USER_MODEL, userModel);
                                                Stash.put(Constants.IS_LOGGED_IN, true);
                                                Intent intent = new Intent(LoginActivity.this, NavigationDrawerActivity.class);
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