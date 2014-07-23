package com.yfoggi.dominion;

import java.util.ArrayList;

import com.yfoggi.dominion.db.DbHelper;
import com.yfoggi.dominion.db.entity.Card;
import com.yfoggi.dominion.db.entity.Card.Selection;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
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
	private ArrayList<Card> allCards;
	
	private Button allBtn;
	private ListView cardList;
	private CardListAdapter cardListAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		readDb();
		findViews();
		prepareAdapters();
		initListeners();
	}
	
	private void readDb(){
		DbHelper helper = new DbHelper(this);
		SQLiteDatabase db = helper.getReadableDatabase();
		
		allCards = new ArrayList<Card>();
		
		Cursor c = db.query(Card.TABLE, Card.COLUMNS, null, null, null, null, "num asc");
		
		int[] idx = new int[Card.COLUMNS.length];
		for(int i = 0; i < idx.length; i++){
			idx[i] = c.getColumnIndex(Card.COLUMNS[i]);
		}
		
		while(c.moveToNext()){
			allCards.add(new Card(c, idx));
		}
		
		c.close();
	}
	
	private void findViews(){
		allBtn = (Button)findViewById(R.id.all_btn);
		cardList = (ListView)findViewById(R.id.card_list);
	}
	
	private void prepareAdapters(){
		cardListAdapter = new CardListAdapter(allCards);
		cardList.setAdapter(cardListAdapter);
		
	}
	private void initListeners(){
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
				holder.chooseBtn = (Button)convertView.findViewById(R.id.choose_btn);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder)convertView.getTag();
			}
			
			holder.cardCostText.setText(c.cost+"");
			holder.cardNumText.setText(c.num+"");
			holder.cardNameText.setText(c.name);
			holder.chooseBtn.setText(c.selection.icon);
			
			holder.chooseBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					c.selection = c.selection.next();
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
		public void setData(ArrayList<Card> data){
			this.data = new ArrayList<Card>(data);
			notifyDataSetChanged();
		}
	}
	private static class ViewHolder {
		TextView cardCostText;
		TextView cardNumText;
		TextView cardNameText;
		Button chooseBtn;
	}
}
