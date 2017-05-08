/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.pharmacyinventory;

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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.example.android.pharmacyinventory.data.DrugContract;

/**
 * Displays list of drugs that were entered and stored in the app.
 */
public class InventoryActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    /** Identifier for the drug data loader */
    private static final int DRUG_LOADER = 0;

    /** Adapter for the ListView */
    DrugCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        // Setup button to open EditorActivity
        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(InventoryActivity.this, com.example.android.pharmacyinventory.EditorActivity.class);
                startActivity(intent);
            }
        });

        // Find the ListView which will be populated with the pet data
        ListView drugListView = (ListView) findViewById(R.id.list);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        drugListView.setEmptyView(emptyView);

        // Setup an Adapter to create a list item for each row of drug data in the Cursor.
        // There is no drug data yet (until the loader finishes) so pass in null for the Cursor.
        mCursorAdapter = new DrugCursorAdapter(this, null);
        drugListView.setAdapter(mCursorAdapter);

        // Setup the item click listener
        drugListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // Create new intent to go to {@link EditorActivity}
                Intent intent = new Intent(InventoryActivity.this, EditorActivity.class);

                // Form the content URI that represents the specific pet that was clicked on,
                // by appending the "id" (passed as input to this method) onto the
                // {@link DrugEntry#CONTENT_URI}.
                // For example, the URI would be "content://com.example.android.pharmacyinventory/pharmacyinventory/2"
                // if the drug with ID 2 was clicked on.
                Uri currentDrugUri = ContentUris.withAppendedId(DrugContract.DrugEntry.CONTENT_URI, id);

                // Set the URI on the data field of the intent
                intent.setData(currentDrugUri);

                // Launch the {@link EditorActivity} to display the data for the current drug.
                startActivity(intent);
            }
        });

        // Kick off the loader
        getLoaderManager().initLoader(DRUG_LOADER, null, this);
    }

    /**
     * Helper method to insert hardcoded drug data into the database. For debugging purposes only.
     */
    private void insertDrug() {
        // Create a ContentValues object where column names are the keys,
        // and Toto's pet attributes are the values.
        ContentValues values = new ContentValues();
        values.put(DrugContract.DrugEntry.COLUMN_DRUG_NAME, "Ibuprofen");
        values.put(DrugContract.DrugEntry.COLUMN_DRUG_QUANTITY, 5);
        values.put(DrugContract.DrugEntry.COLUMN_DRUG_PRICE, 3.00);


        // Insert a new row for the drug Ibuprogen into the provider using the ContentResolver.
        // Use the {@link DrugEntry#CONTENT_URI} to indicate that we want to insert
        // into the drugs database table.
        // Receive the new content URI that will allow us to access the drug data in the future.
        Uri newUri = getContentResolver().insert(DrugContract.DrugEntry.CONTENT_URI, values);
    }

    /**
     * Helper method to delete all drugs in the database.
     */
    private void deleteAllPets() {
        int rowsDeleted = getContentResolver().delete(DrugContract.DrugEntry.CONTENT_URI, null, null);
        Log.v("InventoryActivity", rowsDeleted + " rows deleted from drug database");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertDrug();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                deleteAllPets();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Define a projection that specifies the columns from the table we care about.
        String[] projection = {
                DrugContract.DrugEntry._ID,
                DrugContract.DrugEntry.COLUMN_DRUG_NAME,
                DrugContract.DrugEntry.COLUMN_DRUG_QUANTITY };

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                DrugContract.DrugEntry.CONTENT_URI,   // Provider content URI to query
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update {@link DrugCursorAdapter} with this new cursor containing updated drug data
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted
        mCursorAdapter.swapCursor(null);
    }
}
