package com.example.qrhunterapp_t11;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Handles settings screen.
 * Users can toggle geolocation on and off, or log out.
 *
 * @author Afra, Kristina
 * @reference <a href="https://www.tutlane.com/tutorial/android/android-switch-on-off-button-with-examples">For handling switch events</a>
 */
public class SettingsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        SharedPreferences prefs = this.getActivity().getSharedPreferences("prefs", Context.MODE_PRIVATE);

        Button logoutButton = view.findViewById(R.id.logout_button);
        Switch geolocationSwitch = view.findViewById(R.id.geolocation_switch);

        geolocationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    prefs.edit().putBoolean("geolocationOn", true).commit();
                } else {
                    prefs.edit().putBoolean("geolocationOn", false).commit();
                }
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prefs.edit().clear().commit();
                Intent intent = new Intent(getActivity(), LoginRegisterActivity.class);

                startActivity(intent);
            }
        });

        return view;
    }
}