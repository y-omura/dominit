package com.yfoggi.dominion.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

import com.yfoggi.dominion.db.entity.Card;
import com.yfoggi.dominion.db.entity.Card.Selection;

public class CardUtils {
	private CardUtils(){
	}
	
	private static Random r = new Random(System.currentTimeMillis());
	
	public static Card[] randomize(ArrayList<Card> allCards, ArrayList<Card> extraBan, int length) throws CardIsShort, CardIsTooMany{
		if(extraBan == null){
			extraBan = new ArrayList<Card>();
		}
		
		ArrayList<Card> randomizer = new ArrayList<Card>(allCards);
		randomizer.removeAll(extraBan);
		//remove 'ban'
		for(int i = randomizer.size()-1; i >= 0; i--){
			if(randomizer.get(i).selection == Selection.BAN){
				randomizer.remove(i);
			}
		}
		
		if(randomizer.size() < length){
			throw new CardIsShort();
		}
		
		int counter = 0;
		//find 'always'
		for(int i = 0; i < randomizer.size(); i++){
			if(randomizer.get(i).selection == Selection.ALWAYS){
				Collections.swap(randomizer, i, counter++);
				if(counter > length){
					throw new CardIsTooMany();
				}
			}
		}
		
		//randomize
		for(int i = counter; i < length; i++){
			int rest = randomizer.size() - i;
			int idx = r.nextInt(rest)+i;
			Collections.swap(randomizer, i, idx);
		}
		counter = length;
		
		//convert data
		Card[] cs = new Card[length];
		for(int i = 0; i < cs.length; i++){
			cs[i] = randomizer.get(i);
		}
		
		// sort
		Arrays.sort(cs, new Comparator<Card>() {
			@Override
			public int compare(Card a, Card b) {
				return a.num - b.num;
			}
		});
		
		return cs;
	}
	
	
	public static class CardIsShort extends Exception {
		private static final long serialVersionUID = 1L;
	}
	public static class CardIsTooMany extends Exception {
		private static final long serialVersionUID = 1L;
	}
	
}
