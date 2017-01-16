package com.example.android.inventoryapp;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;

import static java.lang.Integer.parseInt;

/**
 * Created by carlosblanco on 1/3/17.
 */

public class DetailActivity
        extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {


    private EditText mNameEditText;
    private EditText mDescriptionEditText;
    private EditText mPriceEditText;
    private TextView mQuantityTextView;
    private EditText mCodeEditText;

    private Spinner mUnitSpinner;
    private int mUnit = InventoryEntry.UNIT_UNKNOWN;

    private Uri mCurrentItemUri;

    private boolean mItemshasChanged = false;

    int currentItemQuantity;

    private String currentItemName;
    private String currentItemDescription;
    private Integer currentItemUnit;

    private ImageView pictureImageView;

    private Uri currentImageUri;

    private EditText addItemEditText;
    private EditText deductItemEditText;
    private EditText orderQuantityEditText;


    /**
     * Identifier for the pet data loader
     */
    private static final int EXISTING_PET_LOADER = 0;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mItemshasChanged = true;
            return false;
        }
    };

    private View.OnClickListener addItemOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            addItems();
        }
    };

    private View.OnClickListener deductItemOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            deductItems();
        }
    };

    private View.OnClickListener placeOrderOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            placeOrder();
        }
    };

    private View.OnClickListener pickPictureOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            pickPicture();
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_activity);


        Intent intent = getIntent();
        mCurrentItemUri = intent.getData();
        currentImageUri = Uri.parse("");

        Button addItemButton = (Button) findViewById(R.id.add_item_button);
        Button deductItemButton = (Button) findViewById(R.id.deduct_item_button);
        Button placeOrderButton = (Button) findViewById(R.id.place_order_button);
        Button pickPictureButton = (Button) findViewById(R.id.pick_picture_button);

        pictureImageView = (ImageView) findViewById(R.id.picture_image_view);

        addItemEditText = (EditText) findViewById(R.id.add_item_edit_text_view);
        deductItemEditText = (EditText) findViewById(R.id.deduct_item_edit_text_view);
        orderQuantityEditText = (EditText) findViewById(R.id.placer_order_quantity);

        mNameEditText = (EditText) findViewById(R.id.detail_activity_edit_text_name);
        mDescriptionEditText = (EditText) findViewById(R.id.detail_activity_edit_text_description);
        mUnitSpinner = (Spinner) findViewById(R.id.detail_activity_unit_spinner);
        mPriceEditText = (EditText) findViewById(R.id.detail_activity_edit_text_price);
        mQuantityTextView = (TextView) findViewById(R.id.detail_activity_text_view_quantity);
        mCodeEditText = (EditText) findViewById(R.id.detail_activity_edit_text_code);

        pickPictureButton.setOnClickListener(pickPictureOnClickListener);
        setupSpinner();

        if (mCurrentItemUri == null) {
            setTitle(getString(R.string.detail_activity_title_new_item));

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            invalidateOptionsMenu();

            addItemButton.setVisibility(View.GONE);
            deductItemButton.setVisibility(View.GONE);
            placeOrderButton.setVisibility(View.GONE);
            addItemEditText.setVisibility(View.GONE);
            deductItemEditText.setVisibility(View.GONE);
            orderQuantityEditText.setVisibility(View.GONE);

        } else {
            setTitle(getString(R.string.detail_activity_title_edit_item));

            // Initialize a loader to read the pet data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_PET_LOADER, null, this);

            addItemButton.setOnClickListener(addItemOnClickListener);
            deductItemButton.setOnClickListener(deductItemOnClickListener);
            placeOrderButton.setOnClickListener(placeOrderOnClickListener);

            mNameEditText.setOnTouchListener(mTouchListener);
            mDescriptionEditText.setOnTouchListener(mTouchListener);
            mUnitSpinner.setOnTouchListener(mTouchListener);
            mPriceEditText.setOnTouchListener(mTouchListener);
            mCodeEditText.setOnTouchListener(mTouchListener);
        }
    }

    public void pickPicture() {
        Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto, 1);//one can be replaced with any action code
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    currentImageUri = imageReturnedIntent.getData();
                    pictureImageView.setImageURI(currentImageUri);
                }
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_detail.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {

            case R.id.action_save:
                saveItem();
                return true;
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:

                if (!mItemshasChanged) {
                    NavUtils.navigateUpFromSameTask(DetailActivity.this);
                    return true;
                }

                DialogInterface.OnClickListener discardButtonOnClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int id) {
                        NavUtils.navigateUpFromSameTask(DetailActivity.this);
                    }
                };

                shoUnsavedChangesDialog(discardButtonOnClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void shoUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonListener) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(R.string.unsaved_dialog_message);

        builder.setPositiveButton(R.string.unsaved_changes_leave, discardButtonListener);

        builder.setNegativeButton(R.string.unsaved_changes_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int id) {
                if (dialogInterface != null) {
                    dialogInterface.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Get user input from editor and save item into database.
     */
    private void saveItem() {

        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String name = mNameEditText.getText().toString().trim();
        String description = mDescriptionEditText.getText().toString().trim();
        String code = mCodeEditText.getText().toString().trim();
        String intString = mPriceEditText.getText().toString().trim();

        int price = 0;
        int quantity = 0;

        ContentValues itemValues = new ContentValues();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(description) || TextUtils.isEmpty(code)
                || TextUtils.isEmpty(intString)) {
            Toast.makeText(this, getString(R.string.input_error_some_item_blank),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        if (!TextUtils.isEmpty(intString)) {
            price = parseInt(intString);
        }


        itemValues.put(InventoryEntry.COLUMN_ITEM_NAME, name);
        itemValues.put(InventoryEntry.COLUMN_ITEM_UNIT, mUnit);
        itemValues.put(InventoryEntry.COLUMN_ITEM_IMAGE_URI, currentImageUri.toString());
        itemValues.put(InventoryEntry.COLUMN_ITEM_CODE, code);
        itemValues.put(InventoryEntry.COLUMN_ITEM_PRICE, price);
        itemValues.put(InventoryEntry.COLUMN_ITEM_DESCRIPTION, description);
        itemValues.put(InventoryEntry.COLUMN_ITEM_QUANTITY, quantity);


        if (mCurrentItemUri == null) {
            Uri newItemUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, itemValues);

            if (newItemUri == null) {
                Toast.makeText(this, getString(R.string.insert_item_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.insert_item_succed),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            int rowsUpdated = getContentResolver().update(mCurrentItemUri, itemValues, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsUpdated == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.update_item_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.update_item_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    private void deleteItem() {

        if (mCurrentItemUri != null) {

            int rowsDeleted = getContentResolver().delete(mCurrentItemUri, null, null);

            if (rowsDeleted == 0) {
                Toast.makeText(this, getString(R.string.delete_item_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.delete_item_succed),
                        Toast.LENGTH_SHORT).show();
            }
        }

        finish();
    }

    /**
     * Setup the dropdown spinner that allows the user to select the gender of the pet.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter genderSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_unit_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mUnitSpinner.setAdapter(genderSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mUnitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.unit_no_provided)))
                        mUnit = InventoryEntry.UNIT_UNKNOWN;
                    else if (selection.equals(getString(R.string.unit_piece))) {
                        mUnit = InventoryEntry.UNIT_PIECE;
                    } else if (selection.equals(getString(R.string.unit_kilograms))) {
                        mUnit = InventoryEntry.UNIT_KG;
                    } else if (selection.equals(getString(R.string.unit_meter_square))) {
                        mUnit = InventoryEntry.UNIT_SQUARE_METER;
                    } else {
                        mUnit = InventoryEntry.UNIT_CUBIC_METER;
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mUnit = InventoryEntry.UNIT_UNKNOWN;
            }
        });
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_ITEM_NAME,
                InventoryEntry.COLUMN_ITEM_DESCRIPTION,
                InventoryEntry.COLUMN_ITEM_UNIT,
                InventoryEntry.COLUMN_ITEM_PRICE,
                InventoryEntry.COLUMN_ITEM_QUANTITY,
                InventoryEntry.COLUMN_ITEM_CODE,
                InventoryEntry.COLUMN_ITEM_IMAGE_URI
        };

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentItemUri,         // Query the content URI
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        if (cursor.moveToFirst()) {

            int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_NAME);
            int descriptionColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_DESCRIPTION);
            int unitColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_UNIT);
            int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_QUANTITY);
            int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_PRICE);
            int codeColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_CODE);
            int uriColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_IMAGE_URI);


            currentItemName = cursor.getString(nameColumnIndex);
            currentItemDescription = cursor.getString(descriptionColumnIndex);
            currentItemQuantity = cursor.getInt(quantityColumnIndex);
            int currentItemPrice = cursor.getInt(priceColumnIndex);
            String currentItemCode = cursor.getString(codeColumnIndex);
            currentItemUnit = cursor.getInt(unitColumnIndex);
            String currentImageUriString = cursor.getString(uriColumnIndex);

            if (!TextUtils.isEmpty(currentImageUriString)) {
                currentImageUri = Uri.parse(currentImageUriString);
                pictureImageView.setImageURI(currentImageUri);
            }

            mNameEditText.setText(currentItemName);
            mDescriptionEditText.setText(currentItemDescription);
            mQuantityTextView.setText(Integer.toString(currentItemQuantity));
            mPriceEditText.setText(Integer.toString(currentItemPrice));
            mCodeEditText.setText(currentItemCode);

            switch (currentItemUnit) {
                case (InventoryEntry.UNIT_UNKNOWN):
                    mUnitSpinner.setSelection(InventoryEntry.UNIT_UNKNOWN);
                    break;
                case (InventoryEntry.UNIT_PIECE):
                    mUnitSpinner.setSelection(InventoryEntry.UNIT_PIECE);
                    break;
                case (InventoryEntry.UNIT_KG):
                    mUnitSpinner.setSelection(InventoryEntry.UNIT_KG);
                    break;
                case (InventoryEntry.UNIT_SQUARE_METER):
                    mUnitSpinner.setSelection(InventoryEntry.UNIT_SQUARE_METER);
                    break;
                case (InventoryEntry.UNIT_CUBIC_METER):
                    mUnitSpinner.setSelection(InventoryEntry.UNIT_CUBIC_METER);
                    break;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameEditText.setText("");
        mDescriptionEditText.setText("");
        mQuantityTextView.setText("");
        mPriceEditText.setText("");
        mCodeEditText.setText("");
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(R.string.delete_confirmation_dialog);

        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int id) {
                if (dialogInterface != null) {
                    dialogInterface.dismiss();
                    deleteItem();
                }
            }
        });

        builder.setNegativeButton(R.string.delete_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int id) {
                if (dialogInterface != null) {
                    dialogInterface.dismiss();
                    deleteItem();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {

        if (!mItemshasChanged) {
            super.onBackPressed();
        } else {

            DialogInterface.OnClickListener onBackPressedClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                }
            };

            shoUnsavedChangesDialog(onBackPressedClickListener);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        super.onPrepareOptionsMenu(menu);

        if (mCurrentItemUri == null) {
            MenuItem deleteMenuItem = menu.findItem(R.id.action_delete);
            deleteMenuItem.setVisible(false);
        }

        return true;
    }

    public void addItems() {

        String addQuantityString = addItemEditText.getText().toString().trim();

        if (TextUtils.isEmpty(addQuantityString)) {
            Toast.makeText(this, getString(R.string.add_item_failed_no_input),
                    Toast.LENGTH_SHORT).show();
        } else {
            int addQuantity = Integer.parseInt(addQuantityString);

            currentItemQuantity += addQuantity;

            ContentValues addValues = new ContentValues();

            addValues.put(InventoryEntry.COLUMN_ITEM_QUANTITY, currentItemQuantity);

            int rowsUpdated = getContentResolver().update(mCurrentItemUri, addValues, null, null);

            if (rowsUpdated == 0) {
                Toast.makeText(this, getString(R.string.add_item_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.add_item_succed) + addQuantity,
                        Toast.LENGTH_SHORT).show();
            }

            mQuantityTextView.setText(Integer.toString(currentItemQuantity));

            addItemEditText.setText("");
        }
    }

    public void deductItems() {

        String removeQuantityString = deductItemEditText.getText().toString().trim();

        if (TextUtils.isEmpty(removeQuantityString)) {
            Toast.makeText(this, getString(R.string.remove_item_failed_no_input),
                    Toast.LENGTH_SHORT).show();
        } else {

            int removeQuantity = Integer.parseInt(removeQuantityString);

            if (removeQuantity >= currentItemQuantity) {
                Toast.makeText(this, getString(R.string.not_enought_items),
                        Toast.LENGTH_SHORT).show();
                return;
            }
            currentItemQuantity -= removeQuantity;

            ContentValues addValues = new ContentValues();

            addValues.put(InventoryEntry.COLUMN_ITEM_QUANTITY, currentItemQuantity);

            int rowsUpdated = getContentResolver().update(mCurrentItemUri, addValues, null, null);

            if (rowsUpdated == 0) {
                Toast.makeText(this, getString(R.string.deduct_item_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.deduct_item_succed) + removeQuantity,
                        Toast.LENGTH_SHORT).show();
            }

            mQuantityTextView.setText(Integer.toString(currentItemQuantity));

            addItemEditText.setText("");
        }
    }
    public void placeOrder() {
        String placeOrderQuantity = orderQuantityEditText.getText().toString();

        if (!TextUtils.isEmpty(placeOrderQuantity)) {
            String subjectOfEmail = "UDACITY, Order for " + currentItemName;
            String[] unitArray = getResources().getStringArray(R.array.array_unit_options);


            String bodyOfEmail =
                    "Hello Supplier \n" +
                            "We wish to place an order for " + currentItemName + "\n" +
                            "DESCRIPTION: " + currentItemDescription + "\n" +
                            "UNIT: " + unitArray[currentItemUnit] + "\n" +
                            "QUANTITY: " + placeOrderQuantity + "\n";

            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setType("message/rfc822");
            emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"supplier@example.com"});
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, subjectOfEmail);
            emailIntent.putExtra(Intent.EXTRA_TEXT, bodyOfEmail);
            try {
                startActivity(Intent.createChooser(emailIntent, "Send mail..."));
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(DetailActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(DetailActivity.this, "Please Input an Order Quantity", Toast.LENGTH_SHORT).show();
        }
    }

}
