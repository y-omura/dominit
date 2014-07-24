package com.yfoggi.dominion.utils;

import java.util.ArrayList;

import com.yfoggi.dominion.db.entity.Card;
import com.yfoggi.dominion.dto.RandomizedCard;

public class Base64Utils {
	private Base64Utils(){
	}
	
	public static final String SERIALIZE = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_=";
	
	public static String to(int d, int order){
		StringBuilder sb = new StringBuilder(order);
		for(int i = 0; i < order; i++){
			sb.append(SERIALIZE.charAt(d%64));
			d /= 64;
		}
		return sb.reverse().toString();
	}
	
	public static int from(String s){
		int d = 0;
		int order = 1;
		for(int i = s.length()-1; i >= 0; i--){
			char c = s.charAt(i);
			d += SERIALIZE.indexOf(c) * order;
			order *= 64;
		}
		return d;
	}
	
	public static String to(RandomizedCard[] cs){
		StringBuilder sb = new StringBuilder();
		for(RandomizedCard c : cs){
			if(!c.special){
				sb.append(to(c.card.num, 2));
			}
		}
		return sb.toString();
	}
	
	public static Card[] from(ArrayList<Card> all, String base64){
		Card[] cs = new Card[base64.length()/2];
		
		i:for(int i = 0; i < cs.length; i++){
			int num = from(base64.substring(i*2, i*2+2));
			for(Card c : all){
				if(c.num == num){
					cs[i] = c;
					continue i;
				}
			}
			cs[i] = null;
		}
		return cs;
	}
}
