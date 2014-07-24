package com.yfoggi.dominion;

import com.yfoggi.dominion.utils.TweetUtils;

import twitter4j.AsyncTwitter;
import twitter4j.AsyncTwitterFactory;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationContext;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class TwitterOAuthActivity extends Activity {
	private Button authBrowserBtn;
	private EditText pinEdit;
	private Button authBtn;
	private TextView deleteText;
	private Button deleteBtn;
	
	private AsyncTwitter twitter;
	private RequestToken requestToken;

	
	private Handler handler;
	private SharedPreferences shared;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_twitter_oauth);
		
		handler  = new Handler();
		shared = PreferenceManager.getDefaultSharedPreferences(TwitterOAuthActivity.this);

		findViews();
		initListeners();
		
		launchOAuth();
	}
	
	private void findViews(){
		authBrowserBtn = (Button)findViewById(R.id.auth_browser_btn);
		pinEdit = (EditText)findViewById(R.id.pin_edit);
		authBtn = (Button)findViewById(R.id.auth_btn);
		deleteText = (TextView)findViewById(R.id.delete_text);
		deleteBtn = (Button)findViewById(R.id.delete_btn);
		if(TweetUtils.isConfigured(this)){
			deleteText.setText("@"+shared.getString("twitter.screen_name", ""));
			deleteBtn.setEnabled(true);
		}
	}
	
	private void initListeners(){
		authBrowserBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				authBrowserBtn.setEnabled(false);
				authBtn.setEnabled(true);
				Intent intent = new Intent(Intent.ACTION_VIEW,
						Uri.parse(requestToken.getAuthorizationURL()));
				startActivity(intent);
			}
		});
		authBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				twitter.getOAuthAccessTokenAsync(requestToken, pinEdit.getText().toString());
			}
		});
		deleteBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Editor editor = shared.edit();
				editor.remove("twitter.screen_name");
				editor.remove("twitter.access");
				editor.remove("twitter.access_secret");
				editor.commit();
				
				setResult(RESULT_OK);
				finish();
			}
		});
	}

	private void launchOAuth() {
		Configuration conf = ConfigurationContext.getInstance();
		AsyncTwitterFactory factory = new AsyncTwitterFactory(conf);
		twitter = factory.getInstance();
		
		twitter.addListener(new TwitterAdapter(){
			@Override
			public void gotOAuthRequestToken(final RequestToken token) {
				handler.post(new Runnable(){
					@Override
					public void run(){
						requestToken = token;
						authBrowserBtn.setText(getString(R.string.prepared_auth));
						authBrowserBtn.setEnabled(true);
					}
				});
			}
			
			@Override
			public void gotOAuthAccessToken(final AccessToken token) {
				handler.post(new Runnable(){
					@Override
					public void run() {
						Editor editor = shared.edit();
						editor.putString("twitter.screen_name", token.getScreenName());
						editor.putString("twitter.access", token.getToken());
						editor.putString("twitter.access_secret", token.getTokenSecret());
						editor.commit();
						
						setResult(RESULT_OK);
						finish();
					}
				});
			}
		});
		twitter.getOAuthRequestTokenAsync();
	}
}
