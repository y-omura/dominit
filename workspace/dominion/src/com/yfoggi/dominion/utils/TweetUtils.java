package com.yfoggi.dominion.utils;

import twitter4j.AsyncTwitter;
import twitter4j.AsyncTwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationContext;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.yfoggi.dominion.db.entity.Card;
import com.yfoggi.dominion.dto.RandomizedCard;

public class TweetUtils {
	private TweetUtils(){
	}
	
	public static boolean isConfigured(Context ctx){

		SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(ctx);
		
		String access = shared.getString("twitter.access", null);
		String accessSecret = shared.getString("twitter.access_secret", null);
		return !(access == null || accessSecret == null);
		
	}
	
	public static AsyncTwitter getTwitter(Context ctx){

		SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(ctx);
		
		String access = shared.getString("twitter.access", null);
		String accessSecret = shared.getString("twitter.access_secret", null);
		
		if(access == null || accessSecret == null){
			return null;
		}
		
		return new AsyncTwitterFactory(ConfigurationContext.getInstance()).getInstance(
				new AccessToken(access, accessSecret));
	}
	
	public static String shareCards(Context ctx, RandomizedCard[] randomizedCards){
		StringBuilder sb = new StringBuilder();
		for(RandomizedCard c : randomizedCards){
			sb.append(c.getName()).append(" ");
		}
		
		return String.format("%s#dominit", sb.toString());
	}
}
