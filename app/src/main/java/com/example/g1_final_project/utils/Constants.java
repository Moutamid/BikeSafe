package com.example.g1_final_project.utils;

import com.example.g1_final_project.models.UserModel;
import com.fxn.stash.Stash;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Constants {

    public static final String USER_MODEL = "USER_MODEL";
    public static final String IS_LOGGED_IN = "IS_LOGGED_IN";
    public static final String CURRENT_MILEAGES = "CURRENT_MILEAGES";
    public static final String HOURS = "HOURS";
    public static final String MINUTES = "MINUTES";
    public static final String CURRENT_TIME = "CURRENT_TIME";
    public static final String HISTORY = "HISTORY";

    public static FirebaseAuth auth() {
        return FirebaseAuth.getInstance();
    }

    public static DatabaseReference databaseReference() {
        DatabaseReference db = FirebaseDatabase.getInstance().getReference().child("BikeJourneyApp");
        db.keepSynced(true);
        return db;
    }

    public static UserModel userModel(){
        return (UserModel) Stash.getObject(USER_MODEL, UserModel.class);
    }

}
