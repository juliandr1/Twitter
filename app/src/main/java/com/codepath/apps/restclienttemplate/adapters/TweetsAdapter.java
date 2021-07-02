package com.codepath.apps.restclienttemplate.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.codepath.apps.restclienttemplate.R;
import com.codepath.apps.restclienttemplate.TwitterApp;
import com.codepath.apps.restclienttemplate.TwitterClient;
import com.codepath.apps.restclienttemplate.activities.DetailActivity;
import com.codepath.apps.restclienttemplate.activities.ReplyActivity;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.jetbrains.annotations.NotNull;
import org.parceler.Parcels;

import java.util.List;

import okhttp3.Headers;
/*
       TweetsAdapter.java is the adapter that binds Tweet "items" (item_tweet.xml) to the Recycler
       View. It also helps with recognizing likes, retweets, and replies by the user on his or her
       timeline.
 */

public class TweetsAdapter extends RecyclerView.Adapter<TweetsAdapter.ViewHolder> {

    public static String TAG = "TweetsAdapter";

    Context context;
    List<Tweet> tweets;

    // Pass in the context and list of tweets
    public TweetsAdapter(Context context, List<Tweet> tweets) {
        this.context = context;
        this.tweets = tweets;
    }

    // For each row, inflate the layout
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_tweet, parent, false);
        return new ViewHolder(view);
    }

    // Bind values based on the position of the element
    @Override
    public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {
        // Get the data at position
        Tweet tweet = tweets.get(position);
        // Bind the tweet with view holder
        holder.bind(tweet);
    }

    @Override
    public int getItemCount() {
        return tweets.size();
    }

    // Define a viewholder
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView ivProfileImage;
        ImageView ivEmbed;
        TextView tvBody;
        TextView tvScreenName;
        TextView tvCreatedAt;
        TextView tvName;
        Button btnLike, btnRetweet, btnReply;
        TwitterClient client;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            client = TwitterApp.getRestClient(context);
            ivProfileImage = itemView.findViewById(R.id.ivProfileImage);
            tvBody = itemView.findViewById(R.id.tvBody);
            tvScreenName = itemView.findViewById(R.id.tvScreenName);
            tvCreatedAt = itemView.findViewById(R.id.tvCreatedAt);
            ivEmbed = itemView.findViewById(R.id.ivEmbed);
            tvName = itemView.findViewById(R.id.tvName);
            btnLike = itemView.findViewById(R.id.btnLike);
            btnRetweet = itemView.findViewById(R.id.btnRetweet);
            btnReply = itemView.findViewById(R.id.btnDetailReply2);

            // Set onClickListener to see when users want to go to the detailed view.
            itemView.setOnClickListener(this);

        }

        public void bind(Tweet tweet) {
            tvBody.setText(tweet.body);
            tvScreenName.setText("@" + tweet.user.screenName);
            tvName.setText(tweet.user.name);
            tvCreatedAt.setText(tweet.createdAt);
            // Load profile picture
            Glide.with(context).load(tweet.user.profileImageUrl).transform(new CircleCrop()).into(ivProfileImage);
            // Check to see if there is an image to embed
            if (tweet.imageUrl != null) {
                Glide.with(context).load(tweet.imageUrl).into(ivEmbed);
            } else {
                ivEmbed.setVisibility(View.GONE);
            }

            // Checks to see if a user has liked the image.
            btnLike.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final int position = getAdapterPosition();
                    final long id = tweets.get(position).id;
                    // API call to like the tweet
                    client.likeTweet(id, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Headers headers, JSON json) {
                            Log.i(TAG, "Liked tweet");
                            btnLike.setBackgroundResource(R.drawable.ic_vector_heart);
                        }

                        @Override
                        public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                            // If liking the tweet fails, it means that the user has already liked it.
                            // so we must now unlike the tweet
                            client.unlikeTweet(id, new JsonHttpResponseHandler() {
                                @Override
                                public void onSuccess(int statusCode, Headers headers, JSON json) {
                                    Log.i(TAG, "Unliked tweet");
                                    btnLike.setBackgroundResource(R.drawable.ic_vector_heart_stroke);
                                }

                                @Override
                                public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                                    Log.i(TAG, "Failed to  unlike tweet");
                                }
                            });
                        }
                    });
                }
            });

            // Check to see if the user has retweeted a tweet
            btnRetweet.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final int position = getAdapterPosition();
                    final long id = tweets.get(position).id;
                    // API call to retweet a particular tweet
                    client.retweet(id, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Headers headers, JSON json) {
                            Log.i(TAG, "Retweeted tweet:");
                            btnRetweet.setBackgroundResource(R.drawable.ic_vector_retweet);
                        }

                        @Override
                        public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                            // If it fails to retweet a tweet it is because the user has already
                            // retweeted it, so we must unretweet it.
                            client.unRetweet(id, new JsonHttpResponseHandler() {
                                @Override
                                public void onSuccess(int statusCode, Headers headers, JSON json) {
                                    Log.i(TAG, "Unretweet");
                                    btnRetweet.setBackgroundResource(R.drawable.ic_vector_retweet_stroke);
                                }
                                @Override
                                public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                                    Log.i(TAG, "Failed to Unretweet");
                                }
                            });
                        }
                    });
                }
            });

            // Decide what color the buttons must be dependent on if they are liked or retweeted
            // Note: I have a bug where likes and retweets are not remembered if the user logs
            // in or out. So, if a user logs back in a "liked" tweet may appear "unliked".
            int position = getAdapterPosition();
            if (position >= 0) {
                long id = tweets.get(position).id;
                if (client.likedTweets.contains(id)) {
                    btnLike.setBackgroundResource(R.drawable.ic_vector_heart);
                } else {
                    btnLike.setBackgroundResource(R.drawable.ic_vector_heart_stroke);
                }

                if (client.retweetedTweets.contains(id)) {
                    btnRetweet.setBackgroundResource(R.drawable.ic_vector_retweet);
                } else {
                    btnRetweet.setBackgroundResource(R.drawable.ic_vector_retweet_stroke);
                }
            }

            // Check to see if the user wants to reply to a tweet. If so, pass an intent to the
            // reply activity
            btnReply.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, ReplyActivity.class);
                    intent.putExtra(Tweet.class.getSimpleName(), Parcels.wrap(tweets.get(getAdapterPosition())));
                    context.startActivity(intent);
                }
            });

        }

        // Check to see if the user wants to go to the detail of a tweet. If yes, pass an intent
        // to the details activity.
        @Override
        public void onClick(View view) {
            Log.i(TAG,"CLICKED");
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                // New intent
                Intent intent = new Intent(context, DetailActivity.class);
                // Serialize the tweets using parceler, using the tweets shorter name
                intent.putExtra(Tweet.class.getSimpleName(), Parcels.wrap(tweets.get(position)));
                // show the activity
                context.startActivity(intent);
            }
        }
    }

    // Clean all elements of the recycler
    public void clear() {
        tweets.clear();
        notifyDataSetChanged();
    }

    // Add a list of items -- change to type used
    public void addAll(List<Tweet> list) {
        tweets.addAll(list);
        notifyDataSetChanged();
    }


}
