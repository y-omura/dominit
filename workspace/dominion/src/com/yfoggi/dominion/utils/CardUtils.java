package com.yfoggi.dominion.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.yfoggi.dominion.db.entity.Card;
import com.yfoggi.dominion.db.entity.Card.Selection;
import com.yfoggi.dominion.dto.RandomizedCard;

public class CardUtils {
	private CardUtils(){
	}
	
	private static Random r = new Random(System.currentTimeMillis());

	public static RandomizedCard[] randomize(ArrayList<Card> allCards, ArrayList<RandomizedCard> extraBan, int length) throws CardIsShort, CardIsTooMany{
		return randomize(allCards, extraBan, length, -1, -1, true);
	}
	
	public static RandomizedCard[] randomize(ArrayList<Card> allCards, ArrayList<RandomizedCard> extraBan, int length, int costFrom, int costTo, boolean pot) throws CardIsShort, CardIsTooMany{
		if(extraBan == null){
			extraBan = new ArrayList<RandomizedCard>();
		}
		if(costFrom == -1){
			costFrom = 0;
		}
		if(costTo == -1){
			costTo = Integer.MAX_VALUE;
		}
		
		ArrayList<Card> randomizer = new ArrayList<Card>(allCards);
		randomizer.removeAll(Arrays.asList(toCard(extraBan.toArray(new RandomizedCard[0]))));
		//remove 'ban'
		for(int i = randomizer.size()-1; i >= 0; i--){
			if(randomizer.get(i).selection == Selection.BAN  ||
					!costRange(randomizer.get(i), costFrom, costTo, pot)){
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
		ArrayList<RandomizedCard> cs = new ArrayList<RandomizedCard>();
		for(int i = 0; i < length; i++){
			cs.add(new RandomizedCard(randomizer.get(i)));
		}
		
		// sort
		Collections.sort(cs, new Comparator<RandomizedCard>() {
			@Override
			public int compare(RandomizedCard a, RandomizedCard b) {
				return a.card.num - b.card.num;
			}
		});
		Collections.sort(cs, new Comparator<RandomizedCard>() {
			@Override
			public int compare(RandomizedCard a, RandomizedCard b) {
				return a.card.expansion.compareTo(b.card.expansion);
			}
		});
		
		return cs.toArray(new RandomizedCard[0]);
	}
	
	public static ArrayList<RandomizedCard> configSpecials(ArrayList<RandomizedCard> cs){
		//remove specials
		for(int i = cs.size() -1; i >= 0; i--){
			if(cs.get(i).special){
				cs.remove(i);
			}
		}
		int len = cs.size();
		
		// プラコロ
		int prosperity = 0;
		for(RandomizedCard c : cs){
			if(!c.special && c.card.expansion.equals("繁栄")){
				prosperity++;
			}
		}
		if(r.nextInt(len) < prosperity){
			RandomizedCard plucolo = new RandomizedCard(null);
			plucolo.special = true;
			plucolo.plucolo = true;
			plucolo.cost = "9/11";
			plucolo.name = "*白金/植民地";
			plucolo.expansion = "繁栄";
			plucolo.num = -1;
			cs.add(plucolo);
		}
		
		//POT
		boolean pot = false;
		for(RandomizedCard c : cs){
			if(!c.special && c.card.cost.indexOf('p') >= 0){
				pot = true;
				break;
			}
		}
		if(pot){
			RandomizedCard potCard = new RandomizedCard(null);
			potCard.special = true;
			potCard.plucolo = true;
			potCard.cost = "4";
			potCard.name = "*ポーション";
			potCard.expansion = "錬金術";
			potCard.num = -1;
			cs.add(potCard);
		}
		
		return cs;
	}
	
	public static boolean costRange(Card c, int costFrom, int costTo, boolean potionEnable){
		Pattern numeric = Pattern.compile("[0-9]+");
		Matcher numericMatcher = numeric.matcher(c.cost);
		if(!numericMatcher.find()){
			throw new IllegalArgumentException();
		}
		int cost = Integer.parseInt(numericMatcher.group());
		
		Pattern potion = Pattern.compile("p");
		Matcher potionMatcher = potion.matcher(c.cost);
		boolean pot = potionMatcher.find();
		
		if(potionEnable || (!potionEnable && !pot)){
			if(costFrom <= cost && cost <= costTo){
				return true;
			}
		}
		return false;
	}
	
	public static RandomizedCard[] toRandomzied(Card[] cs){
		RandomizedCard[] rcs = new RandomizedCard[cs.length];
		for(int i = 0; i < rcs.length; i++){
			rcs[i] = new RandomizedCard(cs[i]);
		}
		return rcs;
	}
	
	public static Card[] toCard(RandomizedCard[] cs){
		Card[] rcs = new Card[cs.length];
		for(int i = 0; i < rcs.length; i++){
			rcs[i] = cs[i].card;
		}
		return rcs;
	}
	
	public static class CardIsShort extends Exception {
		private static final long serialVersionUID = 1L;
	}
	public static class CardIsTooMany extends Exception {
		private static final long serialVersionUID = 1L;
	}
	
}
