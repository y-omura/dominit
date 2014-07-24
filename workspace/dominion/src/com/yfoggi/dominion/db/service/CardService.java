package com.yfoggi.dominion.db.service;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.yfoggi.dominion.db.DbHelper;
import com.yfoggi.dominion.db.entity.Card;

public class CardService {
	private SQLiteDatabase db;
	
	public CardService(Context c){
		DbHelper helper = new DbHelper(c);
		db = helper.getWritableDatabase();
	}
	
	public void updateSelection(Card[] c){
		if(c.length == 0){
			return;
		}
		ContentValues cv = new ContentValues();
		cv.put("selection", c[0].selection.dbInt);
		
		StringBuilder sb = new StringBuilder();
		sb.append(c[0].id);
		for(int i = 1; i < c.length; i++){
			sb.append(',');
			sb.append(c[i].id);
		}
		
		db.update(Card.TABLE, cv, String.format("id IN (%s)", sb.toString()), null);
	}
	
	public ArrayList<Card> findAll(){
		
		ArrayList<Card> allCards = new ArrayList<Card>();
		
		Cursor c = db.query(Card.TABLE, Card.COLUMNS, null, null, null, null, "num asc");
		
		int[] idx = new int[Card.COLUMNS.length];
		for(int i = 0; i < idx.length; i++){
			idx[i] = c.getColumnIndex(Card.COLUMNS[i]);
		}
		
		while(c.moveToNext()){
			allCards.add(new Card(c, idx));
		}
		
		c.close();
		
		return allCards;
	}
	
}
