package com.example.android.inventoryapp;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;

/**
 * Created by carlosblanco on 1/3/17.
 * {@link InventoryCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of pet data as its data source. This adapter knows
 * how to create list items for each row of pet data in the {@link Cursor}.
 */
public class InventoryCursorAdapter extends CursorAdapter {

    /**
     * Constructs a new {@link InventoryCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public InventoryCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in inventory_list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.inventory_list_item, parent, false);
    }

    /**
     * This method binds the inventory data (in the current row pointed to by cursor) to the given
     * list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        // Find individual views that we want to modify in the list item layout
        TextView nameTextView = (TextView) view.findViewById(R.id.inventory_list_item_name);
        TextView descriptionTextView = (TextView) view.findViewById(R.id.inventory_list_item_description);
        TextView unitTextView = (TextView) view.findViewById(R.id.inventory_list_item_unit);
        TextView quantityTextView = (TextView) view.findViewById(R.id.inventory_list_item_quantity);
        TextView priceTextView = (TextView) view.findViewById(R.id.inventory_list_item_price);

        int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_NAME);
        int descriptionColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_DESCRIPTION);
        int unitColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_UNIT);
        int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_QUANTITY);
        int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_PRICE);

        String itemName = cursor.getString(nameColumnIndex);
        String itemDescription = cursor.getString(descriptionColumnIndex);
        final String itemQuantity = cursor.getString(quantityColumnIndex);
        String itemPrice = cursor.getString(priceColumnIndex);
        final String id = cursor.getString(cursor.getColumnIndex(InventoryEntry._ID));
        final Uri uri = Uri.withAppendedPath(InventoryEntry.CONTENT_URI, id);

        int itemUnit = cursor.getInt(unitColumnIndex);
        String itemUnitString;
        switch (itemUnit) {
            case InventoryEntry.UNIT_KG:
                itemUnitString = context.getString(R.string.unit_kilograms);
                break;
            case InventoryEntry.UNIT_PIECE:
                itemUnitString = context.getString(R.string.unit_piece);
                break;
            case InventoryEntry.UNIT_SQUARE_METER:
                itemUnitString = context.getString(R.string.unit_meter_square);
                break;
            case InventoryEntry.UNIT_CUBIC_METER:
                itemUnitString = context.getString(R.string.unit_meter_cubic);
                break;
            default:
                itemUnitString = context.getString(R.string.unit_no_provided);
        }


        // Update the TextViews with the attributes for the current item
        nameTextView.setText(itemName);
        descriptionTextView.setText(itemDescription);
        unitTextView.setText(itemUnitString);
        quantityTextView.setText(itemQuantity);
        priceTextView.setText(itemPrice);

        final EditText sellQuantityEditText = (EditText) view.findViewById(R.id.inventory_list_item_sell_quantity);

        Button sellButton = (Button) view.findViewById(R.id.inventory_list_item_sell_button);

        sellButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String sellQuantityString = sellQuantityEditText.getText().toString().trim();

                if (TextUtils.isEmpty(sellQuantityString))
                {
                    Toast.makeText(context, R.string.remove_item_failed_no_input, Toast.LENGTH_SHORT).show();
                    return;
                }
                int sellQuantity = Integer.parseInt(sellQuantityString);
                int currentQuantity = Integer.parseInt(itemQuantity);
                int newQuantity = currentQuantity - sellQuantity;

                if (currentQuantity >= sellQuantity) {

                    ContentValues values = new ContentValues();
                    values.put(InventoryEntry.COLUMN_ITEM_QUANTITY, newQuantity);

                    values.put(InventoryEntry.COLUMN_ITEM_QUANTITY, newQuantity);


                    int rowsUpdated = context.getContentResolver().update(uri, values, null, null);

                    if (rowsUpdated == 0) {
                        Toast.makeText(context, "Failed to sell Items", Toast.LENGTH_SHORT).show();

                    } else {
                        Toast.makeText(context, "Succedfully sold: " + sellQuantity + " items", Toast.LENGTH_SHORT).show();

                    }

                } else {
                    Toast.makeText(context, "There are no enought items in stock", Toast.LENGTH_SHORT).show();
                }
                sellQuantityEditText.setText("");
            }
        });


        Button openDetailActivityButton = (Button) view.findViewById(R.id.inventory_list_item_open_detail_activity_button);

        View.OnClickListener openDetailActivityClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, DetailActivity.class);

                // Set the URI on the data field of the intent
                intent.setData(uri);

                context.startActivity(intent);
            }
        };

        openDetailActivityButton.setOnClickListener(openDetailActivityClickListener);
    }

}
