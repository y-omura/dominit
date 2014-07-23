package com.yfoggi.dominion;

import java.util.ArrayList;
import java.util.Arrays;

import com.yfoggi.dominion.db.entity.Card;
import com.yfoggi.dominion.utils.Base64Utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class RandomizedActivity extends Activity {
	
	private ListView cardList;
	private CardListAdapter cardListAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_randomized);
		
		findViews();
		prepareAdapters();
	}
	private void findViews(){
		cardList = (ListView)findViewById(R.id.card_list);
	}
	private void prepareAdapters(){
		ArrayList<Card> cardListData;
		
		Intent intent = getIntent();
		if(intent == null){
			cardListData  = new ArrayList<Card>();
		} else {
			String cardsString = intent.getStringExtra("cards");
			Card[] cardsArray = Base64Utils.from(((MyApplication)getApplication()).allCards, cardsString);
			cardListData = new ArrayList<Card>(Arrays.asList(cardsArray));
		}
		
		cardListAdapter = new CardListAdapter(cardListData);
		cardList.setAdapter(cardListAdapter);
	}
	
	private class CardListAdapter extends BaseAdapter {
		private ArrayList<Card> data;
		private LayoutInflater inflater;

		public CardListAdapter(ArrayList<Card> data) {
			this.data = new ArrayList<Card>(data);
			inflater = (LayoutInflater) RandomizedActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			final Card c = getItem(position);
			
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
			
			holder.cardCostText.setText(c.cost+"");
			holder.cardNumText.setText(c.num+"");
			holder.cardNameText.setText(c.name);
			holder.cardExpansionText.setText(c.expansion);
			
			holder.rerandomizeBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					//TODO
					notifyDataSetChanged();
				}
			});
			
			return convertView;
		}
		@Override
		public int getCount() {
			return data.size();
		}
		@Override
		public Card getItem(int position) {
			return data.get(position);
		}
		@Override
		public long getItemId(int position) {
			return getItem(position).id;
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
