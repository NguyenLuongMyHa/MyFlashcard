package com.myha.myflashcard;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyDatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "SQLite";

    // Database Version
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String DATABASE_NAME = "db_word";
    // Table name
    private static final String TABLE_NAME = "tb_words";
    private String COLUMN_WORD_ID ="Word_Id";
    private ArrayList<String> COLUMNS_NAME;
    public MyDatabaseHelper(Context context, ArrayList<String> columns_name)  {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.COLUMNS_NAME = columns_name;
    }

    // Create table
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i(TAG, "MyDatabaseHelper.onCreate ... ");
        String create_columns = "";
        for (int i = 0;i< COLUMNS_NAME.size();i++)
        {
            create_columns = create_columns.concat("COLUMNS_NAME"+i);
            if(i!=COLUMNS_NAME.size()-1)
                create_columns = create_columns.concat(" TEXT ,");

        }
        String script = "CREATE TABLE " + TABLE_NAME + "(" + COLUMN_WORD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + create_columns+ " TEXT" + ")";
        db.execSQL(script);
    }

    public void createDatabase()
    {
        Log.i(TAG, "MyDatabaseHelper.onCreate ... ");
        String create_columns = "";
        for (int i = 0;i< COLUMNS_NAME.size();i++)
        {
            create_columns = create_columns.concat("COLUMNS_NAME"+i);
            if(i!=COLUMNS_NAME.size()-1)
                create_columns = create_columns.concat(" TEXT ,");

        }
        String script = "CREATE TABLE " + TABLE_NAME + "(" + COLUMN_WORD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + create_columns+ " TEXT" + ")";
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        db.execSQL(script);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        Log.i(TAG, "MyDatabaseHelper.onUpgrade ... ");
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        // Create tables again
        onCreate(db);
    }
    public void createDefaultWordsIfNeed()  {
        int count = this.getWordsCount();
        if(count ==0 ) {
            Word word1 = new Word();
            Word word2 = new Word();
            this.addWord(word1);
            this.addWord(word2);
        }
    }
    public void restoreDB()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from "+ TABLE_NAME);
    }

    public void addWord(Word word) {
        Log.i(TAG, "MyDatabaseHelper.addWord ... " +word.getId()+ " - " + word.getAttributes());

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        for (int i = 0;i< COLUMNS_NAME.size();i++)
        {
            values.put("COLUMNS_NAME"+i, word.getAttributeByKey(COLUMNS_NAME.get(i)));
        }

        // Inserting Row
        db.insert(TABLE_NAME, null, values);
        // Closing database connection
        db.close();
    }


    public Word getWord(int id) {
        Log.i(TAG, "MyDatabaseHelper.getWord ... " + id);

        SQLiteDatabase db = this.getReadableDatabase();
        String create_columns = "";
        for (int i = 0;i< COLUMNS_NAME.size();i++)
        {
            create_columns = create_columns.concat(COLUMNS_NAME.get(i));
            if(i!=COLUMNS_NAME.size()-1)
                create_columns = create_columns.concat(",");
        }
        Cursor cursor = db.rawQuery("SELECT * FROM "+ TABLE_NAME +" WHERE "+ COLUMN_WORD_ID +" = " + id, null);
// Cursor cursor = db.query(TABLE_NOTE, new String[] { COLUMN_NOTE_ID,
//                        COLUMN_NOTE_TITLE, COLUMN_NOTE_CONTENT }, COLUMN_NOTE_ID + "=?",
//                new String[] { String.valueOf(id) }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        Word word = new Word();
        word.setId(Integer.parseInt(cursor.getString(0)));
        Map<String, String> attributes = new HashMap<>();
        for (int i = 0;i< COLUMNS_NAME.size();i++)
        {
            attributes.put(COLUMNS_NAME.get(i), cursor.getString(i+1));
        }
        word.setAttributes(attributes);
        // return word
        return word;
    }


    public List<Word> getAllWords() {
        Log.i(TAG, "MyDatabaseHelper.getAllWords ... " );

        ArrayList<Word> wordList = new ArrayList<Word>();
        // Select All Query
        String selectQuery = "SELECT * FROM " + TABLE_NAME;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Word word = new Word();
                Map<String,String> attributes = new HashMap<>();
                word.setId(Integer.parseInt(cursor.getString(0)));
                for (int i = 0;i< COLUMNS_NAME.size();i++)
                {
                    attributes.put(COLUMNS_NAME.get(i),cursor.getString(i+1));
                }
                word.setAttributes(attributes);
                // Adding word to list
                wordList.add(word);
            } while (cursor.moveToNext());
        }

        // return word list
        return wordList;
    }

    public int getWordsCount() {
        Log.i(TAG, "MyDatabaseHelper.getWordsCount ... " );

        String countQuery = "SELECT  * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();

        cursor.close();

        // return count
        return count;
    }


    public int updateWord(Word word) {
        Log.i(TAG, "MyDatabaseHelper.updateWord ... "  + word.getAttributes());

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        for (int i = 0;i< COLUMNS_NAME.size();i++)
        {
            values.put(COLUMNS_NAME.get(i),word.getAttributes().get(COLUMNS_NAME.get(i)));
        }

        // updating row
        return db.update(TABLE_NAME, values, COLUMN_WORD_ID + " = ?",
                new String[]{String.valueOf(word.getId())});
    }

    public void deleteWord(int id) {
        Log.i(TAG, "MyDatabaseHelper.deleteWord ... " );

        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, COLUMN_WORD_ID + " = ?",
                new String[] { String.valueOf(id) });
        db.close();
    }

}