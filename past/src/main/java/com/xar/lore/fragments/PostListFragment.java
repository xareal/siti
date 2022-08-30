package com.xar.lore.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.xar.naulo.PostDetailActivity;
import com.xar.naulo.PostViewHolder;
import com.xar.naulo.R;
import com.xar.naulo.models.Post;

public abstract class PostListFragment extends Fragment {

	private static final String TAG = "PostListFragment";

	private DatabaseReference mDatabase;

	private FirebaseRecyclerAdapter<Post, PostViewHolder> mAdapter;
	private RecyclerView mRecycler;

	public PostListFragment() {
		super(R.layout.fragment_all_posts);
	}



	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		// Initialize Database
		mDatabase = FirebaseDatabase.getInstance().getReference();
 
		mRecycler = view.findViewById(R.id.messagesList);
//TODOO: test		mRecycler.setHasFixedSize(true);

		// Set up Layout Manager, reverse layout
		LinearLayoutManager mManager = new LinearLayoutManager(getActivity());
		mManager.setReverseLayout(true);
		mManager.setStackFromEnd(true);
		mRecycler.setLayoutManager(mManager);

		// Set up FirebaseRecyclerAdapter with the Query
		Query postsQuery = getQuery(mDatabase);


		FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Post>()
				.setQuery(postsQuery, Post.class)
				.build();

		mAdapter = new FirebaseRecyclerAdapter<Post, PostViewHolder>(options) {

			@NonNull
			@Override
			public PostViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
				LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
				return new PostViewHolder(inflater.inflate(R.layout.item_post, viewGroup, false));
			}

			@Override
			protected void onBindViewHolder(@NonNull PostViewHolder viewHolder, int position, @NonNull final Post model) {
				final DatabaseReference postRef = getRef(position);

				// Set click listener for the whole post view
				final String postKey = postRef.getKey();
				viewHolder.itemView.setOnClickListener(v -> {
					// Launch PostDetailActivity
					Intent intent = new Intent(getActivity(), PostDetailActivity.class);
					intent.putExtra(PostDetailActivity.EXTRA_POST_KEY, postKey);
					startActivity(intent);
				});

				// Determine if the current user has liked this post and set UI accordingly
				if (model.stars.containsKey(getUid())) {
					viewHolder.starView.setImageResource(R.drawable.ic_toggle_star_24);
				} else {
					viewHolder.starView.setImageResource(R.drawable.ic_toggle_star_outline_24);
				}

				// Bind Post to ViewHolder, setting onClickListener for the star button
				viewHolder.bindToPost(model, starView -> {
					// Need to write to both places the post is stored
					DatabaseReference globalPostRef = mDatabase.child("post").child(postRef.getKey());
					DatabaseReference userPostRef = mDatabase.child("user-posts").child(model.uid).child(postRef.getKey());

					// Run two transactions
					onStarClicked(globalPostRef);
					onStarClicked(userPostRef);
				});
			}
		};
		mRecycler.setAdapter(mAdapter);
	}

	// [Start post stars transaction]
	private void onStarClicked(DatabaseReference postRef) {
		postRef.runTransaction(new Transaction.Handler() {
			@NonNull
			@Override
			public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
				Post p = mutableData.getValue(Post.class);
				if (p == null) {
					return Transaction.success(mutableData);
				}

				if (p.stars.containsKey(getUid())) {
					// Unstar the post and remove self from stars
					p.starCount = p.starCount - 1;
					p.stars.remove(getUid());
				} else {
					// Star the post and add self to stars
					p.starCount = p.starCount - 1;
					p.stars.put(getUid(), true); 
				}

				// Set value and report transaction success
				mutableData.setValue(p);
				return Transaction.success(mutableData);
			}

			@Override
			public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot currentData) {
				// Transaction completed
				Log.d(TAG, "postTransaction:onComplete: " + databaseError);
			}
		});
	}
	// [End post_stars transaction]

	@Override
	public void onStart() {
		if (mAdapter != null) {
			mAdapter.startListening();
		}
		super.onStart();
	}

	@Override
	public void onStop() {
		super.onStop();
		if (mAdapter != null) {
			mAdapter.stopListening();
		}
	}

	public String getUid() {
		return FirebaseAuth.getInstance().getCurrentUser().getUid();
	}

	public abstract Query getQuery(DatabaseReference databaseReference);


}
