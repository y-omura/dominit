package com.yfoggi.dominion;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;

public class SettingsActivity extends Activity {
	private SettingsFragment settingsFragment;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.settingsFragment = new SettingsFragment();
        getFragmentManager().beginTransaction().replace(android.R.id.content, settingsFragment).commit();
        
	}
	
	private class SettingsFragment extends PreferenceFragment {
		private SharedPreferences shared;
		private PreferenceScreen twitterPref;
		
		OnSharedPreferenceChangeListener listener = new OnSharedPreferenceChangeListener() {
			
			@Override
			public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
					String key) {
				if("twitter.screen_name".equals(key)){
					updateTwitterConfig();
				}
			}
		};
		
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.preference);
			
			shared = PreferenceManager.getDefaultSharedPreferences(getActivity());
			
			shared.registerOnSharedPreferenceChangeListener(listener);
			
			twitterPref = (PreferenceScreen)findPreference("twitter_pref");
			twitterPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					Intent intent = new Intent();
					intent.setClass(SettingsActivity.this, TwitterOAuthActivity.class);
					
					startActivity(intent);
					
					return true;
				}
			});

			updateTwitterConfig();
		}
		
		@Override
		public void onDestroy() {
			super.onDestroy();
			shared.unregisterOnSharedPreferenceChangeListener(listener);
		}
		
		public void updateTwitterConfig(){
			String screenName = shared.getString("twitter.screen_name", null);
			if(screenName != null){
				twitterPref.setSummary("@"+screenName);
			} else {
				twitterPref.setSummary(getString(R.string.config_twitter_unset));
			}
		}
	}
}
