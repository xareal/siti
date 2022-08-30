package com.xar.lore;

import android.content.Context;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.xar.naulo.databinding.ActivityPostDetailBinding;
import com.xar.lore.models.Comment;
import com.xar.lore.models.Post;
import com.xar.lore.models.User;

import java.util.ArrayList;
import java.util.List;

public class PostDetailActivity extends BaseActivity implements View.OnClickListener {

	private static final String TAG = "PostDetailActivity";
	public static final String EXTRA_POST_KEY = "post_key";

	private DatabaseReference mPostReference;
	private DatabaseReference mCommentsReference;
	private ValueEventListener mPostListener;
	private String mPostKey;
	private CommentAdapter mAdapter;
	private ActivityPostDetailBinding binding;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = ActivityPostDetailBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		// Get post key from intent
		mPostKey = getIntent().getStringExtra(EXTRA_POST_KEY);
		if (mPostKey == null) {
			throw new IllegalArgumentException("Must pass EXTRA_POST_KEY");
		}

		// Initialize Database
		mPostReference = FirebaseDatabase.getInstance().getReference().child("posts").child(mPostKey);
		mCommentsReference = FirebaseDatabase.getInstance().getReference().child("post-comments").child(mPostKey);

		binding.buttonPostComment.setOnClickListener(this);
		binding.recyclerPostComments.setLayoutManager(new LinearLayoutManager(this));
	}

	@Override
	public void onStart() {
		super.onStart();

		// Add value event listener to the post
		ValueEventListener postListener = new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot dataSnapShot) {
				// Get post object and use the value to update the UI
				Post post = dataSnapShot.getValue(Post.class);

				binding.postAuthorLayout.postAuthor.setText(post.author);
				binding.postTextLayout.postTitle.setText(post.title);
				binding.postTextLayout.postBody.setText(post.body);
			}

			@Override
			public void onCancelled(DatabaseError databaseError) {
				// Getting post failed, log a message
				Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
				Toast.makeText(PostDetailActivity.this, "Failed to load post.", Toast.LENGTH_SHORT).show();
			}
		};
		mPostReference.addValueEventListener(postListener);
		// [End post value event listener]

		// Keep copy of post listener so we can remove it when app stops
		mPostListener = postListener;

		// Listen for comments
		mAdapter = new CommentAdapter(this, mCommentsReference);
		binding.recyclerPostComments.setAdapter(mAdapter);
	}

	@Override
	public void onStop() {
		super.onStop();

		// Remove post value event listener
		if (mPostListener != null) {
			mPostReference.removeEventListener(mPostListener);
		}

		// Clean up comments listener
		mAdapter.cleanupListener();
	}

	@Override
	public void onClick(View v) {
		int i = v.getId();
		if (i == R.id.buttonPostComment) {
			postComment();
		}
	}

	private void postComment() {
		final String uid = getUid();
		FirebaseDatabase.getInstance().getReference().child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot dataSnapShot) {
				// Get user information
				User user = dataSnapShot.getValue(User.class);
				String authorName = user.username;

				// Create new comment object
				String commentText = binding.fieldCommentText.getText().toString();
				Comment comment = new Comment(uid, authorName, commentText);

				// Push the comment, it will appear in the list
				mCommentsReference.push().setValue(comment);

				// Clear the field
				binding.fieldCommentText.setText(null);
			}

			@Override
			public void onCancelled(DatabaseError databaseError) {}
		});
	}


	private static class CommentViewHolder extends RecyclerView.ViewHolder {

		public TextView authorView;
		public TextView bodyView;

		public CommentViewHolder(View itemView) {
			super(itemView);

			authorView = itemView.findViewById(R.id.commentAuthor);
			bodyView = itemView.findViewById(R.id.commentBody);
		}
	}

	private static class CommentAdapter extends RecyclerView.Adapter<CommentViewHolder> {

		private Context mContext;
		private DatabaseReference mDatabaseReference;
		private ChildEventListener mChildEventListener;

		private List<String> mCommentIds = new ArrayList<>();
		private List<Comment> mComments = new ArrayList<>();
		

		public CommentAdapter(final Context context, DatabaseReference ref) {

			mContext = context;
			mDatabaseReference = ref;

			// Create child event listener
			ChildEventListener childEventListener = new ChildEventListener() {
				@Override
				public void onChildAdded(DataSnapshot dataSnapShot, String previousChildName) {
					Log.d(TAG, "onChildAdded: " + dataSnapShot.getKey());

					// New Comment has been added, add it to the displayed list
					Comment comment = dataSnapShot.getValue(Comment.class);

					// [Start Exclude]
					// Update RecyclerView
					mCommentIds.add(dataSnapShot.getKey());
					mComments.add(comment);
					notifyItemInserted(mComments.size() - 1);
					// [End Exclude]
				}

				@Override
				public void onChildChanged(DataSnapshot dataSnapShot, String previousChildName) {
					Log.d(TAG, "onChildChanged: " + dataSnapShot.getKey());

					// A comment has changed, use the key to determine if we are displaying this
					// comment and if so displayed the changed comment.
					Comment newComment = dataSnapShot.getValue(Comment.class);
					String commentKey = dataSnapShot.getKey();

					// [Start Exclude]
					int commentIndex = mCommentIds.indexOf(commentKey);
					if (commentIndex > -1) {
						// Replace with the new data
						mComments.set(commentIndex, newComment);

						// Update the RecyclerView
						notifyItemChanged(commentIndex);
					} else {
						Log.w(TAG, "onChildChanged:unknown_child: " + commentKey);
					}
					// [End Exclude]
				}

				@Override
				public void onChildRemoved(DataSnapshot dataSnapShot) {
					Log.d(TAG, "onChildRemoved: " + dataSnapShot.getKey());

					// A comment has changed, use the key to determine if we are displaying this
					// comment and if so remove it.
					String commentKey = dataSnapShot.getKey();

					 // [START_EXCLUDE]
                    int commentIndex = mCommentIds.indexOf(commentKey);
                    if (commentIndex > -1) {
                        // Remove data from the list
                        mCommentIds.remove(commentIndex);
                        mComments.remove(commentIndex);

                        // Update the RecyclerView
                        notifyItemRemoved(commentIndex);
                    } else {
                        Log.w(TAG, "onChildRemoved:unknown_child:" + commentKey);
                    }
                    // [END_EXCLUDE]
				}

				@Override
                public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                    Log.d(TAG, "onChildMoved:" + dataSnapshot.getKey());

                    // A comment has changed position, use the key to determine if we are
                    // displaying this comment and if so move it.
                    Comment movedComment = dataSnapshot.getValue(Comment.class);
                    String commentKey = dataSnapshot.getKey();

                    // ...
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.w(TAG, "postComments:onCancelled", databaseError.toException());
                    Toast.makeText(mContext, "Failed to load comments.",
                            Toast.LENGTH_SHORT).show();
                }
			};
			ref.addChildEventListener(childEventListener);
            // [END child_event_listener_recycler]

            // Store reference to listener so it can be removed on app stop
            mChildEventListener = childEventListener;
		}

		@Override
        public CommentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            View view = inflater.inflate(R.layout.item_comment, parent, false);
            return new CommentViewHolder(view);
        }

        @Override
        public void onBindViewHolder(CommentViewHolder holder, int position) {
            Comment comment = mComments.get(position);
            holder.authorView.setText(comment.author);
            holder.bodyView.setText(comment.text);
        }

        @Override
        public int getItemCount() {
            return mComments.size();
        }

        public void cleanupListener() {
            if (mChildEventListener != null) {
                mDatabaseReference.removeEventListener(mChildEventListener);
            }
        }
    }
}
