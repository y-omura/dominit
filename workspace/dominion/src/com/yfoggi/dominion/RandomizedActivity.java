package com.yfoggi.dominion;

import java.util.ArrayList;
import java.util.Arrays;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.yfoggi.dominion.db.entity.Card;
import com.yfoggi.dominion.dto.RandomizedCard;
import com.yfoggi.dominion.utils.Base64Utils;
import com.yfoggi.dominion.utils.CardUtils;
import com.yfoggi.dominion.utils.CardUtils.CardIsShort;
import com.yfoggi.dominion.utils.CardUtils.CardIsTooMany;
import com.yfoggi.dominion.utils.MessageUtils;
import com.yfoggi.dominion.utils.TweetUtils;

public class RandomizedActivity extends Activity {
	
	private ListView cardList;
	private CardListAdapter cardListAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_randomized);
		
		findViews();
		prepareAdapters();
		initListeners();
	}
	private void findViews(){
		cardList = (ListView)findViewById(R.id.card_list);
	}
	private void prepareAdapters(){
		ArrayList<RandomizedCard> cardListData;
		
		Intent intent = getIntent();
		if(intent == null){
			cardListData  = new ArrayList<RandomizedCard>();
		} else {
			String cardsString = intent.getStringExtra("cards");
			Card[] cardsArray = Base64Utils.from(((MyApplication)getApplication()).allCards, cardsString);
			cardListData = new ArrayList<RandomizedCard>(Arrays.asList(CardUtils.toRandomzied(cardsArray)));
		}
		cardListData = CardUtils.configSpecials(cardListData);
		
		cardListAdapter = new CardListAdapter(cardListData);
		cardList.setAdapter(cardListAdapter);
	}
	private void initListeners(){
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.ramdomized, menu);
		menu.findItem(R.id.action_tweet).setEnabled(TweetUtils.isConfigured(this));
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(R.id.action_tweet).setEnabled(TweetUtils.isConfigured(this));
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if(id == R.id.action_tweet){
			Intent intent = new Intent(RandomizedActivity.this, TweetActivity.class);
			intent.putExtra("tweet", TweetUtils.shareCards(RandomizedActivity.this, cardListAdapter.data.toArray(new Card[0])));
			startActivity(intent);
		}
		if (id == R.id.action_settings) {
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
			
			return true;
		}
		return false;
	}
	
	
	private class CardListAdapter extends BaseAdapter {
		private ArrayList<RandomizedCard> data;
		private LayoutInflater inflater;

		public CardListAdapter(ArrayList<RandomizedCard> data) {
			this.data = new ArrayList<RandomizedCard>(data);
			inflater = (LayoutInflater) RandomizedActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			final RandomizedCard c = getItem(position);
			
			final ViewHolder holder;
			if(convertView == null){
				convertView = inflater.inflate(R.layout.randomized_card_list_item, null);
				holder = new ViewHolder();
				holder.cardCostText = (TextView)convertView.findViewById(R.id.card_cost_text);
				holder.cardNumText = (TextView)convertView.findViewById(R.id.card_num_text);
				holder.cardNameText = (TextView)convertView.findViewById(R.id.card_name_text);
				holder.cardExpansionText = (TextView)convertView.findViewById(R.id.card_expansion_text);
				holder.rerandomizeBtn = (Button)convertView.findViewById(R.id.rerandomize_btn);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder)convertView.getTag();
			}
			if(c.special){
				holder.rerandomizeBtn.setEnabled(false);
			} else {
				holder.rerandomizeBtn.setEnabled(true);
			}
			
			holder.cardCostText.setText(c.getCost());
			holder.cardNumText.setText(c.getNum()+"");
			holder.cardNameText.setText(c.getName());
			holder.cardExpansionText.setText(c.getExpansion());
			
			holder.rerandomizeBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					new AlertDialog.Builder(RandomizedActivity.this)
						.setTitle("編集")
						.setItems(
								new String[]{
										getString(R.string.rerandomize_this_card),
										getString(R.string.delete_this_card),
								},
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										if(which == 0){
											RandomizedCard[] cards;
											try {
												cards = CardUtils.randomize(((MyApplication)getApplication()).allCards, data, 1);
											} catch (CardIsShort e) {
												MessageUtils.error(RandomizedActivity.this, "ランダム対象が少なすぎるっぽい！");
												return;
											} catch (CardIsTooMany e) {
												MessageUtils.error(RandomizedActivity.this, "強制が多すぎるっぽい！");
												return;
											}
											data.remove(position);
											data.add(cards[0]);
											notifyDataSetChanged();
										} else if(which == 1){
											data.remove(position);
											notifyDataSetChanged();
										}
									}
								})
						.show();
				}
			});
			
			return convertView;
		}
		@Override
		public int getCount() {
			return data.size();
		}
		@Override
		public RandomizedCard getItem(int position) {
			return data.get(position);
		}
		@Override
		public long getItemId(int position) {
			return position;
		}
	}
	private static class ViewHolder {
		TextView cardCostText;
		TextView cardNumText;
		TextView cardNameText;
		TextView cardExpansionText;
		Button rerandomizeBtn;
	}
}
