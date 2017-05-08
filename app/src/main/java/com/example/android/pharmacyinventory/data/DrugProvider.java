package com.example.android.pharmacyinventory.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

/**
 * {@link ContentProvider} for Pharmacy Inventory app.
 */
public class DrugProvider extends ContentProvider {

    /** Tag for the log messages */
    public static final String LOG_TAG = DrugProvider.class.getSimpleName();

    /** URI matcher code for the content URI for the drugs table */
    private static final int DRUGS = 100;

    /** URI matcher code for the content URI for a single drug in the drugs table */
    private static final int DRUG_ID = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.

        // The content URI of the form "content://com.example.android.pharmacyinventory/pharmacyinventory" will map to the
        // integer code {@link #DRUGS}. This URI is used to provide access to MULTIPLE rows
        // of the drugs table.
        sUriMatcher.addURI(DrugContract.CONTENT_AUTHORITY, DrugContract.PATH_DRUG, DRUGS);

        // The content URI of the form "content://com.example.android.pharmacyinventory/pharmacyinventory/#" will map to the
        // integer code {@link #DRUG_ID}. This URI is used to provide access to ONE single row
        // of the drugs table.
        //
        // In this case, the "#" wildcard is used where "#" can be substituted for an integer.
        // For example, "content://com.example.android.pharmacyinventory/pharmacyinventory/3" matches, but
        // "content://com.example.android.pharmacyinventory/pharmacyinventory" (without a number at the end) doesn't match.
        sUriMatcher.addURI(DrugContract.CONTENT_AUTHORITY, DrugContract.PATH_DRUG + "/#", DRUG_ID);
    }

    /** Database helper object */
    private DrugDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new DrugDbHelper(getContext());
        return true;
    }

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
            case DRUGS:
                // For the DRUGS code, query the drugs table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the pets table.
                cursor = database.query(DrugContract.DrugEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case DRUG_ID:
                // For the DRUG_ID code, extract out the ID from the URI.
                // For an example URI such as "content://com.example.android.pharmacyinventory/pharmacyinventory/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = DrugContract.DrugEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                // This will perform a query on the drugs table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(DrugContract.DrugEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        // Set notifcation URI on the cursor
        // So what know what content URI the cursor was created
        // If the data at this URI changes, we know we need to update the cursor
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        // return the cursor
        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case DRUGS:
                return insertDrug(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert a drug into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertDrug(Uri uri, ContentValues values) {
        // Check that the name is not null
        String name = values.getAsString(DrugContract.DrugEntry.COLUMN_DRUG_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Drug requires a name");
        }

        // Check that the price is not null
        Double price = values.getAsDouble(DrugContract.DrugEntry.COLUMN_DRUG_PRICE);
        if (price == null) {
            throw new IllegalArgumentException("Drug requires valid price");
        }

        // No need to check the image, any value is valid (including null).

        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert the new drug with the given values
        long id = database.insert(DrugContract.DrugEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the drug content URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case DRUGS:
                return updateDrug(uri, contentValues, selection, selectionArgs);
            case DRUG_ID:
                // For the DRUG_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = DrugContract.DrugEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateDrug(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Update drugs in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more drugs).
     * Return the number of rows that were successfully updated.
     */
    private int updateDrug(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // If the {@link DrugEntry#COLUMN_DRUG_NAME} key is present,
        // check that the name value is not null.
        if (values.containsKey(DrugContract.DrugEntry.COLUMN_DRUG_NAME)) {
            String name = values.getAsString(DrugContract.DrugEntry.COLUMN_DRUG_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Drug requires a name");
            }

        }

        // If the {@link DrugEntry#COLUMN_DRUG_QUANTITY} key is present,
        // check that the quantity value is valid.
        if (values.containsKey(DrugContract.DrugEntry.COLUMN_DRUG_QUANTITY)) {
            Integer quantity = values.getAsInteger(DrugContract.DrugEntry.COLUMN_DRUG_QUANTITY);
            if (quantity == null) {
                throw new IllegalArgumentException("Drug requires valid quanity");
            }
        }

        // If the {@link DrugEntry#COLUMN_DRUG_PRICE} key is present,
        // check that the price value is valid.
        if (values.containsKey(DrugContract.DrugEntry.COLUMN_DRUG_PRICE)) {
            // Check that the price not null
            Double price = values.getAsDouble(DrugContract.DrugEntry.COLUMN_DRUG_PRICE);
            if (price == null) {
                throw new IllegalArgumentException("Drug requires valid price");
            }
        }

        // No need to check the image, any value is valid (including null).

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Otherwise, get writeable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Returns the number of database rows affected by the update statement
        int rowUpdated = database.update(DrugContract.DrugEntry.TABLE_NAME, values, selection, selectionArgs);

        // if 1 or more rows were updated, then notify all listeners that the data at
        // the given URI has changed
        if (rowUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows updated
        return rowUpdated;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Track the number of rows that were deleted
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case DRUGS:
                // Delete all rows that match the selection and selection args
                // For case DRUGS
                rowsDeleted = database.delete(DrugContract.DrugEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case DRUG_ID:
                // Delete a single row given by the ID in the URI
                selection = DrugContract.DrugEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                // For case DRUG_ID:
                // Delete a single row given by the ID in the URI
                rowsDeleted = database.delete(DrugContract.DrugEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows updated
        return rowsDeleted;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case DRUGS:
                return DrugContract.DrugEntry.CONTENT_LIST_TYPE;
            case DRUG_ID:
                return DrugContract.DrugEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}