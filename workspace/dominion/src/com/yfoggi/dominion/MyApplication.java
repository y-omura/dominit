package com.yfoggi.dominion;

import java.util.ArrayList;

import com.yfoggi.dominion.db.entity.Card;
import com.yfoggi.dominion.db.service.CardService;

import android.app.Application;

public class MyApplication extends Application{
	
	public ArrayList<Card> allCards;
	
	@Override
	public void onCreate() {
		CardService cardService = new CardService(this);
		allCards = cardService.findAll();
	}
}
