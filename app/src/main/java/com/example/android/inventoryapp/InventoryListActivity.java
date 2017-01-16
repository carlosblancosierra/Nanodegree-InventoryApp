package com.example.android.inventoryapp;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;

public class InventoryListActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int INVENTORY_LOADER = 0;
    private InventoryCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory_list);


        ListView listView = (ListView) findViewById(R.id.activity_inventory_list_view);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        mCursorAdapter = new InventoryCursorAdapter(this, null);
        listView.setAdapter(mCursorAdapter);
        listView.setEmptyView(findViewById(R.id.empty_view));

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(InventoryListActivity.this, DetailActivity.class);

                Uri currentPetUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, id);

                // Set the URI on the data field of the intent
                intent.setData(currentPetUri);

                startActivity(intent);
            }
        });

        // Kick off the loader
        getLoaderManager().initLoader(INVENTORY_LOADER, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_inventory, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertDummyData();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                deleteAllPets();
                return true;
            case R.id.action_add_item:
                Intent detailActivityIntent = new Intent(InventoryListActivity.this, DetailActivity.class);
                startActivity(detailActivityIntent);
        }
        return super.onOptionsItemSelected(item);
    }

    private Uri insertDummyData() {
        //Create a Content Values Object with Dummy Data
        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_ITEM_NAME, "CABILLA");
        values.put(InventoryEntry.COLUMN_ITEM_DESCRIPTION, "12mm");
        values.put(InventoryEntry.COLUMN_ITEM_PRICE, "1000");
        values.put(InventoryEntry.COLUMN_ITEM_QUANTITY, "10");
        values.put(InventoryEntry.COLUMN_ITEM_UNIT, InventoryEntry.UNIT_KG);
        values.put(InventoryEntry.COLUMN_ITEM_CODE, "CAB");

        /**
         * Insert a new row for the Dummy Data into the provider using the ContentResolver.
         * Use the {@link InventoryEntry.CONTENT_URI} to indicate that we want to insert
         * into the inventory database table.
         * Receive the new content URI that will allow us to access the Dummy data in the future.
         */

        Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);
        return newUri;
    }

    private void deleteAllPets() {
        int rowsDeleted = getContentResolver().delete(InventoryEntry.CONTENT_URI, null, null);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_ITEM_NAME,
                InventoryEntry.COLUMN_ITEM_DESCRIPTION,
                InventoryEntry.COLUMN_ITEM_UNIT,
                InventoryEntry.COLUMN_ITEM_PRICE,
                InventoryEntry.COLUMN_ITEM_QUANTITY,
                InventoryEntry.COLUMN_ITEM_CODE
        };

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                InventoryEntry.CONTENT_URI,   // Provider content URI to query
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted
        mCursorAdapter.swapCursor(null);
    }

}
