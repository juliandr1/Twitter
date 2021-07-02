package com.codepath.apps.restclienttemplate.activities;

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
    ReplyActivity.java allows users to reply to tweets from other users. Note: The usage of
    a constraint layout was done on purpose, as I originally intended to use a fragment, but was
    stuck. Thus, I converted the fragment formatting into this activity, though if I had more time
    I would continue to try with the fragment.
 */

public class ReplyActivity extends AppCompatActivity {

    public static String TAG = "ReplyActivity";

    ImageView ivReplyProfile, ivReplyOwnProfile, ivReplyEmbed;
    TextView tvReplyTweet, tvReplyName, tvReplyScreenName, tvReplyTime, tvReplyStatement;

    EditText etReply;

    Button btnExit, btnReply;
    TwitterClient client;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reply);

        client = TwitterApp.getRestClient(this);

        // Unwrap the tweet passed by the intent in TweetsAdapter
        final Tweet tweet = Parcels.unwrap(getIntent().getParcelableExtra(Tweet.class.getSimpleName()));

        ivReplyProfile = findViewById(R.id.ivReplyProfile);
        // I intended to load one's own profile picture like in Twitter desktop, but ran out of
        // time.
        ivReplyOwnProfile = findViewById(R.id.ivReplyOwnProfile);
        ivReplyEmbed = findViewById(R.id.ivEmbed);

        // Load the images into their appropriate locale
        assert tweet != null;
        Glide.with(this).load(tweet.user.profileImageUrl).transform(new RoundedCorners(200)).into(ivReplyProfile);
        if (tweet.imageUrl != null) {
            Glide.with(this).load(tweet.imageUrl).into(ivReplyEmbed);
        } else {
            ivReplyEmbed.setVisibility(View.GONE);
        }

        tvReplyTweet = findViewById(R.id.tvReplyTweet);
        tvReplyTweet.setText(tweet.body);

        tvReplyName = findViewById(R.id.tvReplyName);
        tvReplyName.setText(tweet.user.name);

        tvReplyScreenName = findViewById(R.id.tvReplyScreenName);
        tvReplyScreenName.setText("@" + tweet.user.screenName);

        tvReplyTime = findViewById(R.id.tvReplyTime);
        tvReplyTime.setText(tweet.createdAt);

        tvReplyStatement = findViewById(R.id.tvReplyStatement);
        tvReplyStatement.setText("Replying to @" + tweet.user.screenName);

        etReply = findViewById(R.id.etDetail);

        // Check to see if user wants to back out.
        btnExit = findViewById(R.id.btnBack);
        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // Check to see if user is ready to reply
        btnReply = findViewById(R.id.btnReply);
        btnReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String tweetContent = etReply.getText().toString();

                // Check to see if tweet meets Twitter requirements
                if (tweetContent.isEmpty()) {
                    Toast.makeText(ReplyActivity.this, "Sorry, your tweet cannot be empty", Toast.LENGTH_LONG).show();
                    return;
                }
                if (tweetContent.length() > ComposeActivity.MAX_TWEET_LENGTH) {
                    Toast.makeText(ReplyActivity.this, "Sorry, your tweet is too long", Toast.LENGTH_LONG).show();
                    return;
                }

                // Api call to reply to the tweet
                client.replyTweet(tweetContent, tweet.id, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Headers headers, JSON json) {
                        Log.i(TAG, "Replied to Tweet");
                        try {
                            Tweet tweet = Tweet.fromJson(json.jsonObject);
                            // Pass a new intent with the tweet
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
                        Log.e(TAG, "Failed to reply to tweet");
                    };
            });

        }
        });
    }
}

