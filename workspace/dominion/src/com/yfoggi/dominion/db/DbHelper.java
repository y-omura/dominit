package com.yfoggi.dominion.db;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.yfoggi.dominion.db.entity.Card;

public class DbHelper extends SQLiteOpenHelper{
	static final String DB = "dominit.db";
	static final String CSV = "cardlist.csv";
	static final int DB_VERSION = 1;
	private static final String CREATE_TABLE = 
			"CREATE TABLE card" +
			"(" +
				"id INTEGER PRIMARY KEY AUTOINCREMENT," +
				"num INTEGER NOT NULL," +
				"name VARCHAR(45) NOT NULL," +
				"ruby VARCHAR(45) NOT NULL," +
				"english VARCHAR(255) NOT NULL," +
				"expansion VARCHAR(45) NOT NULL," +
				"cost VARCHAR(45) NOT NULL," +
				"card_type VARCHAR(45) NOT NULL," +
				"selection INTEGER NOT NULL" +
			");";
	private static final String DROP_TABLE = "DROP TABLE card;";
	
	private Context context;
	
    public DbHelper(Context c) {
        super(c, DB, null, DB_VERSION);
        this.context = c;
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
        
        db.beginTransaction();
        InputStream is = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        try {
        	AssetManager assetManager = context.getAssets();
        	is = assetManager.open(CSV);
        	isr = new InputStreamReader(is);
        	br = new BufferedReader(isr);
        	
        	String line;
        	while((line = br.readLine()) != null){
        		String[] data = line.split(",");
        		ContentValues cv = new ContentValues();
        		cv.put("num", data[0]);
        		cv.put("name", data[1]);
        		cv.put("ruby", data[2]);
        		cv.put("english", data[3]);
        		cv.put("expansion", data[4]);
        		cv.put("cost", data[5]);
        		cv.put("card_type", data[6]);
        		cv.put("selection", data[7]);
        		
        		db.insert(Card.TABLE, null, cv);
        	}

        	db.setTransactionSuccessful();
        } catch(IOException e){
        	throw new RuntimeException(e);
        } finally {
        	db.endTransaction();
        	try {
        		if(is != null){
        			is.close();
        		}
        	} catch (IOException e) {
        		e.printStackTrace();
        	}
        	try {
        		if(isr != null){
        			isr.close();
        		}
			} catch (IOException e) {
				e.printStackTrace();
			}
        	try {
        		if(br != null){
        			br.close();
        		}
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROP_TABLE);
        onCreate(db);
    }
	
}
