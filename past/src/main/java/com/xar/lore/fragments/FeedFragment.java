package com.xar.lore.fragments;

import androidx.fragment.app.Fragment;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

public class FollowingFragment extends PostListFragment {

    public FollowingFragment() {
        // Required empty public constructor
    }

    @Override
    public Query getQuery(DatabaseReference databaseReference) {
        // TODO: Posts from following 
        //return databaseReference.child
        return null;
    }
 }

