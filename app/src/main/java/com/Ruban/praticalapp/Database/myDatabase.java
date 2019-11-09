package com.Ruban.praticalapp.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.Ruban.praticalapp.ModelClass.ModelData;

import androidx.annotation.Nullable;

import static com.Ruban.praticalapp.ModelClass.ModelData.DatabaseModelView.COLUMN_EMAIL;

public class myDatabase extends SQLiteOpenHelper {


    public myDatabase(@Nullable Context context) {
        super(context, ModelData.DatabaseModelView.DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String DB_table = "create table "+ ModelData.DatabaseModelView.TABLE_NAME+ " ( "+ ModelData.DatabaseModelView.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "+ ModelData.DatabaseModelView.COLUMN_USER_NAME+ " text,"+ ModelData.DatabaseModelView.COLUMN_IMAGE+ " text,"+ ModelData.DatabaseModelView.COLUMN_USER_ADDRESS+" text ,"+ COLUMN_EMAIL+" text"+")";
        db.execSQL(DB_table);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists "+ModelData.DatabaseModelView.TABLE_NAME);
        onCreate(db);
    }

    public  boolean valuesInsert(String name, String byte_arr,String email, String address) {
        SQLiteDatabase Userdb =  this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(ModelData.DatabaseModelView.COLUMN_USER_NAME,name);
        values.put(ModelData.DatabaseModelView.COLUMN_IMAGE,byte_arr);
        values.put(ModelData.DatabaseModelView.COLUMN_EMAIL,email);
        values.put(ModelData.DatabaseModelView.COLUMN_USER_ADDRESS,address);


        long result = Userdb.insert(ModelData.DatabaseModelView.TABLE_NAME,null,values);
        if (result==-1)
            return false;
        else
            return  true;
    }
      public boolean updateValues(String name, String byte_arr,String email, String address){
       SQLiteDatabase Userdb =  this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(ModelData.DatabaseModelView.COLUMN_USER_NAME,name);
        values.put(ModelData.DatabaseModelView.COLUMN_IMAGE,byte_arr);
        values.put(COLUMN_EMAIL,email);
        values.put(ModelData.DatabaseModelView.COLUMN_USER_ADDRESS,address);

        long result = (int) Userdb.insertWithOnConflict(ModelData.DatabaseModelView.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);

        if (result==-1) {
            Userdb.update(ModelData.DatabaseModelView.TABLE_NAME,values,COLUMN_EMAIL+"=?",new String[]{email});
            return false;
        }
        else
            return  true;
         }




    public Boolean checkIfRecordExist(String email){
        SQLiteDatabase Userdb =  this.getWritableDatabase();
        String Query = "Select * from " + ModelData.DatabaseModelView.TABLE_NAME + " where " + COLUMN_EMAIL + "=?";
        Cursor cursor = Userdb.rawQuery(Query, new String[]{email});
        if(cursor.getCount() <= 0){
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }
}
