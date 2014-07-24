package com.yfoggi.dominion;

import twitter4j.AsyncTwitter;
import twitter4j.Status;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.yfoggi.dominion.utils.MessageUtils;
import com.yfoggi.dominion.utils.TweetUtils;

public class TweetActivity extends Activity {
	private EditText tweetEdit;
	private Button tweetBtn;
	
	private Handler handler;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tweet);
		
		handler = new Handler();
		
		findViews();
		initListeners();
	}
	
	private void findViews(){
		tweetEdit = (EditText)findViewById(R.id.tweet_edit);
		
		Intent intent = getIntent();
		String defaultTweet;
		if(intent != null){
			defaultTweet = intent.getStringExtra("tweet");
		} else {
			defaultTweet = "";
		}
		tweetEdit.setText(defaultTweet);
		
		tweetBtn = (Button)findViewById(R.id.tweet_btn);
	}
	
	private void initListeners(){
		tweetBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(!TweetUtils.isConfigured(TweetActivity.this)){
					MessageUtils.error(TweetActivity.this, "Twitterの設定がまだみたい");
				}
				AsyncTwitter twitter = TweetUtils.getTwitter(TweetActivity.this);
				tweetBtn.setEnabled(false);
				tweetBtn.setText("送信中...");
				
				twitter.addListener(new TwitterAdapter(){
					@Override
					public void updatedStatus(Status status) {
						handler.post(new Runnable(){
							@Override
							public void run() {
								tweetBtn.setEnabled(true);
								tweetBtn.setText(getString(R.string.tweet));
								setResult(RESULT_OK);
								finish();
							}
						});
					}
					@Override
					public void onException(TwitterException te,
							TwitterMethod method) {
						handler.post(new Runnable(){
							@Override
							public void run() {
								MessageUtils.error(TweetActivity.this, "なんかおかしいっぽい！");
								tweetBtn.setEnabled(true);
								tweetBtn.setText(getString(R.string.tweet));
							}
						});
					}
				});
				twitter.updateStatus(tweetEdit.getText().toString());
			}
		});
	}

}
