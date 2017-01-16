package com.example.android.inventoryapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;

/**
 * Created by carlosblanco on 1/3/17.
 */

public class InventoryDbHelper extends SQLiteOpenHelper {

    public static final String LOG_TAG = InventoryDbHelper.class.getSimpleName();

    /**
     * Name of the database file
     */
    private static final String DATABASE_NAME = "company.db";

    /**
     * Database version. If you change the database schema, you must increment the database version.
     */
    private static final int DATABASE_VERSION = 1;

    /**
     * Constructs a new instance of {@link InventoryDbHelper}.
     *
     * @param context of the app
     */
    public InventoryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a String that contains the SQL statement to create the pets table
        String SQL_CREATE_PETS_TABLE = "CREATE TABLE " + InventoryEntry.TABLE_NAME + " ("
                + InventoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + InventoryEntry.COLUMN_ITEM_NAME + " TEXT NOT NULL, "
                + InventoryEntry.COLUMN_ITEM_DESCRIPTION + " TEXT, "
                + InventoryEntry.COLUMN_ITEM_UNIT + " INTEGER NOT NULL, "
                + InventoryEntry.COLUMN_ITEM_PRICE + " INTEGER, "
                + InventoryEntry.COLUMN_ITEM_QUANTITY + " INTEGER, "
                + InventoryEntry.COLUMN_ITEM_IMAGE_URI + " TEXT, "
                + InventoryEntry.COLUMN_ITEM_CODE + " TEXT NOT NULL);";

        Log.v("SQL CREATE COMMAND: ", SQL_CREATE_PETS_TABLE);
        // Execute the SQL statement
        db.execSQL(SQL_CREATE_PETS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        // The database is still at version 1, so there's nothing to do be done here.
    }
}
