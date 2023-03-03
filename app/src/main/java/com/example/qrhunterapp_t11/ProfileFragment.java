package com.example.qrhunterapp_t11;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;


/*
    Creates a fragment for the main profile page
* */
public class ProfileFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        TextView loginUsernameTextView = getView().findViewById(R.id.profile_name);
        SharedPreferences prefs = this.getActivity().getSharedPreferences("prefs", Context.MODE_PRIVATE);

        // TODO: - add players calculated stats, and add collection of QR codes
        String username = prefs.getString("loginUsername", null);
        loginUsernameTextView.setText(username);

        return inflater.inflate(R.layout.fragment_profile, container, false);
    }
}