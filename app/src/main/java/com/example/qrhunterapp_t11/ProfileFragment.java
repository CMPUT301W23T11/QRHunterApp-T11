package com.example.qrhunterapp_t11;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;


/*
    Creates a fragment for the main profile page
* */
public class ProfileFragment extends Fragment {
    FirebaseFirestore db;

    public ProfileFragment(FirebaseFirestore db) {
        this.db = db;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        TextView loginUsernameTextView = view.findViewById(R.id.profile_name);
        SharedPreferences prefs = this.getActivity().getSharedPreferences("prefs", Context.MODE_PRIVATE);

        String username = prefs.getString("loginUsername", null);
        loginUsernameTextView.setText(username);

        // TODO: - add players calculated stats, and add collection of QR codes


        return view;
    }
}