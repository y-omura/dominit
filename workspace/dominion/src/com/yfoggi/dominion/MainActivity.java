package com.yfoggi.dominion;

import java.util.ArrayList;

import com.yfoggi.dominion.db.DbHelper;
import com.yfoggi.dominion.db.entity.Card;
import com.yfoggi.dominion.db.entity.Card.Selection;
import com.yfoggi.dominion.db.service.CardService;
import com.yfoggi.dominion.utils.Base64Utils;
import com.yfoggi.dominion.utils.CardUtils;
import com.yfoggi.dominion.utils.CardUtils.CardIsShort;
import com.yfoggi.dominion.utils.CardUtils.CardIsTooMany;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private Button randomizeBtn;
	private Button allBtn;
	private ListView cardList;
	private CardListAdapter cardListAdapter;
	
	private CardService cardService;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		cardService = new CardService(this);
		
		findViews();
		prepareAdapters();
		initListeners();
	}
	
	private void findViews(){
		randomizeBtn = (Button)findViewById(R.id.randomize_btn);
		allBtn = (Button)findViewById(R.id.all_btn);
		cardList = (ListView)findViewById(R.id.card_list);
	}
	
	private void prepareAdapters(){
		cardListAdapter = new CardListAdapter(((MyApplication)getApplication()).allCards);
		cardList.setAdapter(cardListAdapter);
	}
	
	private void initListeners(){
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
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private class CardListAdapter extends BaseAdapter {
		private ArrayList<Card> data;
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
}
