package com.yfoggi.dominion.db.entity;

import android.database.Cursor;

public class Card {
	public static final String TABLE = "card";
	public static final String[] COLUMNS = new String[]{"id", "num", "name", "ruby", "english",
		"expansion", "cost", "card_type", "selection"};
	
	public int id;
	public int num;
	public String name;
	public String ruby;
	public String english;
	public String expansion;
	public String cost;
	public String cardType;
	public Selection selection;
	
	public Card(Cursor c, int[] idx){
		id = c.getInt(idx[0]);
		num = c.getInt(idx[1]);
		name = c.getString(idx[2]);
		ruby = c.getString(idx[3]);
		english = c.getString(idx[4]);
		expansion = c.getString(idx[5]);
		cost = c.getString(idx[6]);
		cardType = c.getString(idx[7]);
		selection = Selection.parseFromDb(c.getInt(idx[8]));
	}

	public Card(int id, int num, String name, String ruby, String english,
			String expansion, String cost, String card_type, Selection selection) {
		this.id = id;
		this.num = num;
		this.name = name;
		this.ruby = ruby;
		this.english = english;
		this.expansion = expansion;
		this.cost = cost;
		this.cardType = card_type;
		this.selection = selection;
	}

	@Override
	public String toString() {
		return "Card ["+"name=" + name + "]";
	}
	
	public enum Selection {
		ALLOW(0, "？"), BAN(1, "✕"), ALWAYS(2, "＋");
		public final int dbInt;
		public final String icon;
		private Selection(int dbInt, String icon){
			this.dbInt = dbInt;
			this.icon = icon;
		}
		public Selection next(){
			Selection[] vs = values();
			return vs[(ordinal()+1)%vs.length];
		}
		public static Selection parseFromDb(int i){
			for(Selection s : values()){
				if(s.dbInt == i){
					return s;
				}
			}
			return null;
		}
	}
	
}
