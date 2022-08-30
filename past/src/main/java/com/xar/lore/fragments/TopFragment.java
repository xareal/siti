package com.xar.lore.fragments;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

public class GlobalFragment extends PostListFragment {

    public GlobalFragment() {
        // Required empty public constructor
    }

    @Override
    public Query getQuery(DatabaseReference databaseReference) {
        // [Start top post query]
        Query topPostQuery = databaseReference.child("posts").orderByChild("starCount").limitToFirst(1000);
        // [End top post query]

        return topPostQuery;
    }
}

