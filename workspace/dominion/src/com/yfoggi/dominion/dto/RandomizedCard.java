package com.yfoggi.dominion.dto;

import com.yfoggi.dominion.db.entity.Card;

public class RandomizedCard {
	public Card card;
	public boolean special = false;
	
	public boolean youngWitch = false;
	public boolean plucolo = false;
	public boolean potion = false;
	public boolean shelter = false;
	
	public int num;
	public String name;
	public String cost;
	public String expansion;
	
	public RandomizedCard(Card c){
		card = c;
	}

	public int getNum(){
		if(special)
			return num;
		return card.num;
	}
	public String getName(){
		if(special)
			return name;
		return card.name;
	}
	public String getCost(){
		if(special)
			return cost;
		return card.cost;
	}
	public String getExpansion(){
		if(special)
			return expansion;
		return card.expansion;
	}
}
