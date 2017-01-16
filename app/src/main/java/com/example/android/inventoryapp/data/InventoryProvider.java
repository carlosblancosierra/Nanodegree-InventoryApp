package com.example.android.inventoryapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.example.android.inventoryapp.R;
import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;

/**
 * Created by carlosblanco on 1/3/17.
 */

public class InventoryProvider extends ContentProvider {

    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = InventoryProvider.class.getSimpleName();

    private InventoryDbHelper mDbHelper;

    /**
     * URI matcher code for the content URI for the inventory table
     */
    private static final int INVENTORY = 100;

    /**
     * URI matcher code for the content URI for a single item in the inventory table
     */
    private static final int INVENTORY_ID = 101;


    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        /**
         The calls to addURI() go here, for all of the content URI patterns that the provider
         should recognize. All paths added to the UriMatcher have a corresponding code to return
         when a match is found.

         The content URI of the form "content://com.example.android.inventoryapp/inventory" will map to the
         integer code {@link #Inventory}. This URI is used to provide access to MULTIPLE rows
         of the inventory table.
         */
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_INVENTORY, INVENTORY);

        // The content URI of the form "content://com.example.android.inventoryapp/inventory/#" will map to the
        // integer code {@link #INVENTORY_ID}. This URI is used to provide access to ONE single row
        // of the inventory table.
        //
        // In this case, the "#" wildcard is used where "#" can be substituted for an integer.
        // For example, "content://com.example.inventoryapp/inventory/3" matches, but
        // "content://com.example.android.inventoryapp/inventory" (without a number at the end) doesn't match.
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_INVENTORY + "/#", INVENTORY_ID);
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new InventoryDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);

        switch (match) {
            case INVENTORY:

                cursor = database.query(InventoryEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case INVENTORY_ID:

                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                cursor = database.query(InventoryEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        // Set notification URI on the Cursor,
        // so we know what content URI the Cursor was created for.
        // If the data at this URI changes, then we know we need to update the Cursor.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                return insertItem(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertItem(Uri uri, ContentValues values) {

        // Check that the name is not null
        String name = values.getAsString(InventoryEntry.COLUMN_ITEM_NAME);
        String description = values.getAsString(InventoryEntry.COLUMN_ITEM_DESCRIPTION);
        String price = values.getAsString(InventoryEntry.COLUMN_ITEM_PRICE);
        String code = values.getAsString(InventoryEntry.COLUMN_ITEM_CODE);


        if (name == null) {
            throw new IllegalArgumentException("Item requires a name");
        }

        if (description == null) {
            throw new IllegalArgumentException("Item requires a description");
        }

        if (price == null) {
            throw new IllegalArgumentException("Item requires a price");
        }

        if (code == null) {
            throw new IllegalArgumentException("Item requires a code");
        }

        if (name == null || description == null || price == null || code == null){
            Toast.makeText(getContext(), R.string.no_item_values, Toast.LENGTH_SHORT).show();
            throw new IllegalArgumentException("Item requires all values");
        }

        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert the new pet with the given values
        long id = database.insert(InventoryEntry.TABLE_NAME, null, values);

        if (id == -1) {
            // If the ID is -1, then the insertion failed. Log an error and return null.
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        getContext().getContentResolver().notifyChange(uri, null);

        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        //Get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        //Track the number of rows deleted
        int rowsDeleted;

        //Math uri
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case INVENTORY:
                rowsDeleted = database.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case INVENTORY_ID:
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Delete is not supported for " + uri);
        }

        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {

        //Math uri
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case INVENTORY:
                return updateItem(uri, contentValues, selection, selectionArgs);
            case INVENTORY_ID:
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateItem(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Delete is not supported for " + uri);

        }
    }

    private int updateItem(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {

        //Get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        int rowsUpdated;

        rowsUpdated = database.update(InventoryEntry.TABLE_NAME, contentValues, selection, selectionArgs);

        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;


    }
}
