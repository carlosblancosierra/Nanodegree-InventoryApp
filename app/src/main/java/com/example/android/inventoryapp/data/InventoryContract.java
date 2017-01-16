package com.example.android.inventoryapp.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by carlosblanco on 1/3/17.
 */

public class InventoryContract {

    private InventoryContract() {
    }

    /**
     * The "Content authority" is a name for the entire content provider, similar to the
     * relationship between a domain name and its website.  A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on the
     * device.
     */
    public static final String CONTENT_AUTHORITY = "com.example.android.inventoryapp";

    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_INVENTORY = "inventory";

    public static final class InventoryEntry implements BaseColumns {

        /**
         * The content URI to access the pet data in the provider
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_INVENTORY);

        public static final String TABLE_NAME = PATH_INVENTORY;

        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_ITEM_NAME = "name";
        public final static String COLUMN_ITEM_DESCRIPTION = "description";
        public final static String COLUMN_ITEM_UNIT = "unit";
        public final static String COLUMN_ITEM_QUANTITY = "quantity";
        public final static String COLUMN_ITEM_PRICE = "price";
        public final static String COLUMN_ITEM_CODE = "code";
        public final static String COLUMN_ITEM_IMAGE_URI = "image";
        public final static int UNIT_UNKNOWN = 0;
        public final static int UNIT_PIECE = 1;
        public final static int UNIT_KG = 2;
        public final static int UNIT_SQUARE_METER = 3;
        public final static int UNIT_CUBIC_METER = 4;
    }


}
