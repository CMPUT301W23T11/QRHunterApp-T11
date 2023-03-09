package com.example.qrhunterapp_t11;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;


/**
 * Handles player profile screen.
 *
 * @author Afra, Kristina
 */
public class ProfileFragment extends Fragment {
    private final FirebaseFirestore db;
    private final CollectionReference usersReference;
    private final CollectionReference QRCodesReference;

    /**
     * Constructor for registration fragment.
     * Also instantiates a reference to the Users and QRCodes collections for ease of access.
     *
     * @param db Firestore database instance
     */
    public ProfileFragment(FirebaseFirestore db) {
        this.db = db;
        this.usersReference = db.collection("Users");
        this.QRCodesReference = db.collection("QRCodes");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        TextView loginUsernameTextView = view.findViewById(R.id.profile_name);
        SharedPreferences prefs = this.getActivity().getSharedPreferences("prefs", Context.MODE_PRIVATE);

        String username = prefs.getString("currentUser", null);
        loginUsernameTextView.setText(username);

        // TODO: - add players calculated stats, and add collection of QR codes


        return view;
    }
}