package com.codepath.apps.restclienttemplate.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.codepath.apps.restclienttemplate.R;
import com.codepath.apps.restclienttemplate.TwitterApp;
import com.codepath.apps.restclienttemplate.TwitterClient;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONException;
import org.parceler.Parcels;

import okhttp3.Headers;

/*
    DetailActivity.java will allow the user to get a more enhanced view of a particular tweet,
    whilst also being able to reply, like, or retweet the tweet.
 */

public class DetailActivity extends AppCompatActivity {

    public static String TAG = "DetailActivity";

    ImageView ivDetailProfile, ivDetailEmbed;

    TextView tvDetailTweet, tvDetailName, tvDetailScreenName, tvDetailTime;

    EditText etDetail;

    Button btnBack, btnReplyDetail, btnReplyPublish, btnDetailLike, btnDetailRetweet;

    TwitterClient client;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reply);

        client = TwitterApp.getRestClient(this);

        // Unwrap the tweet passed by the intent in TweetsAdapter
        final Tweet tweet = Parcels.unwrap(getIntent().getParcelableExtra(Tweet.class.getSimpleName()));

        ivDetailProfile = findViewById(R.id.ivDetailProfile);
        ivDetailEmbed = findViewById(R.id.ivDetailEmbed);

        // Use glide to load images into correct locations
        assert tweet != null;
        Glide.with(this).load(tweet.user.profileImageUrl).transform(new RoundedCorners(200)).into(ivDetailProfile);

        // Check to see if there is an image to embed
        if (tweet.imageUrl != null) {
            Log.i(TAG, "" + ivDetailEmbed.toString());
            Glide.with(this).load(tweet.imageUrl).into(ivDetailEmbed);
        } else {
            ivDetailEmbed.setVisibility(View.GONE);
        }

        tvDetailTweet = findViewById(R.id.tvDetailTweet);
        tvDetailTweet.setText(tweet.body);

        tvDetailName = findViewById(R.id.tvDetailName);
        tvDetailName.setText(tweet.user.name);

        tvDetailScreenName = findViewById(R.id.tvDetailScreenName);
        tvDetailScreenName.setText("@" + tweet.user.screenName);

        tvDetailTime = findViewById(R.id.tvDetailTime);
        tvDetailName.setText(tweet.createdAtREG);

        etDetail = findViewById(R.id.etDetail);

        // Check to see if user wants to back out of the detailed view of the tweet.
        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        btnReplyPublish = findViewById(R.id.btnDetailReply2);
        // Check to see if the user wants to reply to the tweet directly from the window.
        btnReplyPublish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String tweetContent = etDetail.getText().toString();
                if (tweetContent.isEmpty()) {
                    Toast.makeText(DetailActivity.this, "Sorry, your tweet cannot be empty", Toast.LENGTH_LONG).show();
                    return;
                }
                if (tweetContent.length() > ComposeActivity.MAX_TWEET_LENGTH) {
                    Toast.makeText(DetailActivity.this, "Sorry, your tweet is too long", Toast.LENGTH_LONG).show();
                    return;
                }
                client.replyTweet(tweetContent, tweet.id, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Headers headers, JSON json) {
                        Log.i(TAG, "Replied to Tweet");
                        try {
                            Tweet tweet = Tweet.fromJson(json.jsonObject);
                            Intent intent = new Intent();
                            intent.putExtra("tweet", Parcels.wrap(tweet));
                            // set result code and bundle data for response
                            setResult(RESULT_OK, intent);
                            // closes the activity, pass data to parent

                            finish();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                        Log.e(TAG,"Failed to reply to tweet");
                    }
                });
            }
        });

        // Check to see if user wants to reply to the tweet through the reply activity.
        btnReplyDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DetailActivity.this, ReplyActivity.class);
                intent.putExtra(Tweet.class.getSimpleName(), Parcels.wrap(tweet));
                DetailActivity.this.startActivity(intent);
                finish();
            }
        });

        // Checks to see if the user wants to like the tweet
        btnDetailLike = findViewById(R.id.btnDetailLike);
        btnDetailLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final long id = tweet.id;
                // API call to like the tweet
                client.likeTweet(id, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Headers headers, JSON json) {
                        Log.i(TAG, "Liked tweet");
                        btnDetailLike.setBackgroundResource(R.drawable.ic_vector_heart);
                    }
                    // If the tweet cannot be liked than it must be the user wants to unlike it.
                    @Override
                    public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                        client.unlikeTweet(id, new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Headers headers, JSON json) {
                                Log.i(TAG, "Unliked tweet");
                                btnDetailLike.setBackgroundResource(R.drawable.ic_vector_heart_stroke);
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

        btnDetailRetweet = findViewById(R.id.btnDetailRetweet);
        // Check to see if the user wants to retweet the tweet
        btnDetailRetweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final long id = tweet.id;
                // API call to retweet the tweet
                client.retweet(id, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Headers headers, JSON json) {
                        Log.i(TAG, "Retweeted tweet");
                        btnDetailRetweet.setBackgroundResource(R.drawable.ic_vector_retweet);
                    }
                    // If the user cannot retweet the tweet than they are unretweeting the tweet.
                    @Override
                    public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                        Log.i(TAG, "Failed to retweet tweet");
                        client.unRetweet(id, new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Headers headers, JSON json) {
                                Log.i(TAG, "Unretweet");
                                btnDetailRetweet.setBackgroundResource(R.drawable.ic_vector_retweet_stroke);
                            }

                            @Override
                            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                                Log.i("Adapter", "Failed to Unretweet");
                            }
                        });
                    }
                });
            }
        });
    }
}
