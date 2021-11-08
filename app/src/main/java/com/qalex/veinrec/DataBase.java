package com.qalex.veinrec;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


/**
 * @author Mohamed I. Sayed, Mohamed Taha, and Hala H. Zayed 
 * @date 01/10/2021
 * @version 1.0
 */

public class DataBase extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "myDB";
    private static final String TABLE_NAME = "myData";
    private static final String TABLE_NAME2 = "users";
    private static final String TABLE_NAME1 = "test";
    private static final String ID_COL = "id";
    private static final String NAME = "name";
    private static final String FEATURE_COL = "feat";
    private static final String HAND_COL = "hand";
    private static final String UID_COL = "u_id";


    private static final int DB_VERVSOIN = 1;


    public DataBase(Context context) {
        super(context, DATABASE_NAME, null, DB_VERVSOIN);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {


    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {


        onCreate(sqLiteDatabase);
    }

    public Cursor GetUserID (int feat_id){

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.query(TABLE_NAME,new String[]{ID_COL,FEATURE_COL,HAND_COL,UID_COL},ID_COL+"="+feat_id,null,null,null,null);

        if(c != null){

            c.moveToFirst();
        }

        db.close();
        return c;
    }
    public Cursor GetUserName (int user_id){

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.query(TABLE_NAME2,new String[]{NAME},ID_COL+"="+user_id,null,null,null,null);

        if(c != null){

            c.moveToFirst();
        }

        db.close();
        return c;
    }
    public Cursor GetAll(){

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor c = db.query(TABLE_NAME,new String[]{ID_COL,FEATURE_COL,HAND_COL,UID_COL},null,null,null,null,null);
        if(c != null){

            c.moveToFirst();
        }

         //db.close();
        return c;

    }
    public Cursor GetOne(int id, String hand){

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor c = db.query(TABLE_NAME,new String[]{ID_COL,FEATURE_COL,HAND_COL,UID_COL},UID_COL+"="+id ,null,null,null,null);
        if(c != null){

            c.moveToFirst();
        }


        return c;

    }
    public Cursor GetAllTest(){

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor c = db.query(TABLE_NAME1,new String[]{ID_COL,FEATURE_COL,HAND_COL,UID_COL},null,null,null,null,null);
        if(c != null){

            c.moveToFirst();
        }


        return c;
    }
    public Cursor GetOne(int id){

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.query(TABLE_NAME,new String[]{ID_COL,FEATURE_COL,HAND_COL,UID_COL},UID_COL+"="+id ,null,null,null,null);
        if(c != null){

            c.moveToFirst();
        }

        return c;

    }
}


