package com.yfoggi.dominion.utils;

import android.content.Context;
import android.widget.Toast;

public class MessageUtils {
	private MessageUtils(){
	}
	public static void error(Context ctx, String text){
		Toast.makeText(ctx, text, Toast.LENGTH_SHORT).show();
	}
}
