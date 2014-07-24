package com.yfoggi.dominion;

import java.util.ArrayList;
import java.util.Arrays;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.yfoggi.dominion.db.entity.Card;
import com.yfoggi.dominion.db.entity.Card.Selection;
import com.yfoggi.dominion.db.service.CardService;
import com.yfoggi.dominion.utils.Base64Utils;
import com.yfoggi.dominion.utils.CardUtils;
import com.yfoggi.dominion.utils.CardUtils.CardIsShort;
import com.yfoggi.dominion.utils.CardUtils.CardIsTooMany;

public class MainActivity extends Activity {
	private Button searchBtn;
	private TextView searchConditionText;
	private Button randomizeBtn;
	private Button allBtn;
	private ListView cardList;
	private CardListAdapter cardListAdapter;
	
	private CardService cardService;
	
	//Search Query
	private String[] expansions;
	private boolean[] expansionsFlag;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		cardService = new CardService(this);
		expansions = cardService.findExpansions();
		expansionsFlag = new boolean[expansions.length];
		for(int i = 0; i < expansionsFlag.length; i++){
			expansionsFlag[i] = true;
		}
		
		findViews();
		prepareAdapters();
		initListeners();
	}
	
	private void findViews(){
		searchBtn = (Button)findViewById(R.id.search_btn);
		searchConditionText = (TextView)findViewById(R.id.search_condition_text);
		randomizeBtn = (Button)findViewById(R.id.randomize_btn);
		allBtn = (Button)findViewById(R.id.all_btn);
		cardList = (ListView)findViewById(R.id.card_list);
	}
	
	private void prepareAdapters(){
		cardListAdapter = new CardListAdapter(new ArrayList<Card>());
		cardList.setAdapter(cardListAdapter);
		search();
	}
	
	private void initListeners(){
		searchBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final boolean[] newQuery = Arrays.copyOf(expansionsFlag, expansionsFlag.length);
				
				new AlertDialog.Builder(MainActivity.this)
					.setTitle("")
					.setMultiChoiceItems(
							expansions, 
							newQuery,
							new OnMultiChoiceClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which, boolean isChecked) {
									newQuery[which] = isChecked;
								}
							})
					.setPositiveButton("絞り込み", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							expansionsFlag = newQuery;
							search();
						}
					})
					.setNeutralButton("全選択", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							for(int i = 0; i < expansionsFlag.length; i++){
								expansionsFlag[i] = true;
							}
							search();
						}
					})
					.setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
						}
					})
					.show();
			}
		});
		randomizeBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(MainActivity.this, RandomizedActivity.class);
				
				Card[] randomized;
				try {
					randomized = CardUtils.randomize(((MyApplication)getApplication()).allCards, 10);
				} catch (CardIsShort e) {
					Toast.makeText(MainActivity.this, "ランダム対象が少なすぎるっぽい！", Toast.LENGTH_SHORT).show();
					return;
				} catch (CardIsTooMany e) {
					Toast.makeText(MainActivity.this, "強制が多すぎるっぽい！", Toast.LENGTH_SHORT).show();
					return;
				}
				String encoded = Base64Utils.to(randomized);
				
				intent.putExtra("cards", encoded);
				
				startActivity(intent);
			}
		});
		allBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Builder alert = new Builder(MainActivity.this);
				alert.setTitle(getString(R.string.change_visible_card_state));
				alert.setItems(new String[]{
						getString(R.string.random_visible_card_state),
						getString(R.string.delete_visible_card_state),
						getString(R.string.force_visible_card_state),
						}, 
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								Card.Selection s;
								switch(which){
								case 0:
									s = Selection.ALLOW;
									break;
								case 1:
									s = Selection.BAN;
									break;
								case 2:
									s = Selection.ALWAYS;
									break;
								default: 
									s = null;
									break;
								}
								if(s != null){
									for(Card c : cardListAdapter.data){
										c.selection = s;
									}
									cardService.updateSelection(cardListAdapter.data.toArray(new Card[0]));
									cardListAdapter.notifyDataSetChanged();
								}
							}
						});
				alert.show();
			}
		});
		cardList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				Toast.makeText(MainActivity.this, position+"", Toast.LENGTH_SHORT).show();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
			
			return true;
		}
		return false;
	}
	
	private class CardListAdapter extends BaseAdapter {
		public ArrayList<Card> data;
		private LayoutInflater inflater;

		public CardListAdapter(ArrayList<Card> data) {
			this.data = new ArrayList<Card>(data);
			inflater = (LayoutInflater) MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			final Card c = getItem(position);
			
			final ViewHolder holder;
			if(convertView == null){
				convertView = inflater.inflate(R.layout.card_list_item, null);
				holder = new ViewHolder();
				holder.cardCostText = (TextView)convertView.findViewById(R.id.card_cost_text);
				holder.cardNumText = (TextView)convertView.findViewById(R.id.card_num_text);
				holder.cardNameText = (TextView)convertView.findViewById(R.id.card_name_text);
				holder.cardExpansionText = (TextView)convertView.findViewById(R.id.card_expansion_text);
				holder.chooseBtn = (Button)convertView.findViewById(R.id.choose_btn);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder)convertView.getTag();
			}
			
			holder.cardCostText.setText(c.cost+"");
			holder.cardNumText.setText(c.num+"");
			holder.cardNameText.setText(c.name);
			holder.cardExpansionText.setText(c.expansion);
			holder.chooseBtn.setText(c.selection.icon);
			
			holder.chooseBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					c.selection = c.selection.next();
					cardService.updateSelection(new Card[]{c});
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
		Button chooseBtn;
	}
	private void search(){
		ArrayList<Card> searched = new ArrayList<Card>();
		for(Card c : ((MyApplication)getApplication()).allCards){
			for(int i = 0; i < expansions.length; i++){
				if(expansionsFlag[i] && c.expansion.equals(expansions[i])){
					searched.add(c);
				}
			}
		}
		cardListAdapter.data = searched;
		cardListAdapter.notifyDataSetChanged();

		boolean allflag = true;
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < expansions.length; i++){
			if(expansionsFlag[i]){
				sb.append(expansions[i]).append(" ");
			} else {
				allflag = false;
			}
		}
		if(sb.toString().equals("")){
			searchConditionText.setText(getString(R.string.search_condition_nothing));
		} else if(allflag){
			searchConditionText.setText(getString(R.string.search_condition_all));
		} else {
			searchConditionText.setText(sb.toString());
		}
		
	}
}
