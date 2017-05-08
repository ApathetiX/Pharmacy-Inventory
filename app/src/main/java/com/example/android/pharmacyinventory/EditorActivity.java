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

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.pharmacyinventory.data.DrugContract;

/**
 * Allows user to create a new drug or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /** Identifier for the drug data loader */
    private static final int EXISTING_DRUG_LOADER = 0;

    /** Content URI for the existing drug (null if it's a new drug) */
    private Uri mCurrentDrugUri;

    /** EditText field to enter the drug name */
    private EditText mNameEditText;

    /** EditText field to enter the drug price */
    private EditText mPriceEditText;

    /** TextView field that displays drug quantity */
    private TextView mQuantityText;

    /** Boolean flag that keeps track of whether the drug has been edited (true) or not (false) */
    private boolean mDrugHasChanged = false;

    /** String that keeps track of drug name for email */
    private String mDrugname;

    /** String that keeps track of drug quantity for email */
    private String mDrugQuantity;

    /** String that keeps track of price for email */
    private String mDrugPrice;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mDrugHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mDrugHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Find the order button
        Button orderButton = (Button) findViewById(R.id.order_button);

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new drug or editing an existing one.
        Intent intent = getIntent();
        mCurrentDrugUri = intent.getData();

        // If the intent DOES NOT contain a drug content URI, then we know that we are
        // creating a new drug.
        if (mCurrentDrugUri == null) {
            // This is a new drug, so change the app bar to say "Add a Drug"
            setTitle(getString(R.string.editor_activity_title_new_drug));

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a drug that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing drug, so change app bar to say "Edit Drug"
            setTitle(getString(R.string.editor_activity_title_edit_drug));

            // Initialize a loader to read the drug data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_DRUG_LOADER, null, this);
        }

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_drug_name);
        mPriceEditText = (EditText) findViewById(R.id.edit_drug_price);
        mQuantityText = (TextView) findViewById(R.id.edit_drug_price);

        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mNameEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);

        orderButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                String priceMessage = createOrderSummary(mDrugname, mDrugQuantity, mDrugPrice);

                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:")); // only email apps should handle this
                intent.putExtra(Intent.EXTRA_TEXT, priceMessage);
                intent.putExtra(Intent.EXTRA_SUBJECT, "Pharmacy order");
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }

            }
        });

    }



    /**
     * Get user input from editor and save drug into database.
     */
    private void saveDrug() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String nameString = mNameEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();

        // Check if this is supposed to be a new drug
        // and check if all the fields in the editor are blank
        if (mCurrentDrugUri == null &&
                TextUtils.isEmpty(nameString) &&
                TextUtils.isEmpty(priceString)) {
            // Since no fields were modified, we can return early without creating a new drug.
            // No need to create ContentValues and no need to do any ContentProvider operations.
            return;
        }

        // Create a ContentValues object where column names are the keys,
        // and drug attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(DrugContract.DrugEntry.COLUMN_DRUG_NAME, nameString);

        // If the price is not provided by the user, don't try to parse the string into an
        // integer value. Use 0 by default.
        Double price = 0.00;
        if (!TextUtils.isEmpty(priceString)) {
            price = Double.parseDouble(priceString);
        }
        values.put(DrugContract.DrugEntry.COLUMN_DRUG_PRICE, price);

        // Determine if this is a new or existing drug by checking if mCurrentDrugUri is null or not
        if (mCurrentDrugUri == null) {
            // This is a NEW drug, so insert a new drug into the provider,
            // returning the content URI for the new drug.
            Uri newUri = getContentResolver().insert(DrugContract.DrugEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_drug_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_drug_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // Otherwise this is an EXISTING drug, so update the drug with content URI: mCurrentDrugUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentDrugUri will already identify the correct row in the database that
            // we want to modify.
            int rowsAffected = getContentResolver().update(mCurrentDrugUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_drug_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_drug_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new drug, hide the "Delete" menu item.
        if (mCurrentDrugUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save drug to database
                saveDrug();
                // Exit activity
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the drug hasn't changed, continue with navigating up to parent activity
                // which is the {@link InventoryActivity}.
                if (!mDrugHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the pet hasn't changed, continue with handling back button press
        if (!mDrugHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all drug attributes, define a projection that contains
        // all columns from the pet table
        String[] projection = {
                DrugContract.DrugEntry._ID,
                DrugContract.DrugEntry.COLUMN_DRUG_NAME,
                DrugContract.DrugEntry.COLUMN_DRUG_QUANTITY,
                DrugContract.DrugEntry.COLUMN_DRUG_PRICE,
                DrugContract.DrugEntry.COLUMN_DRUG_SOLD};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentDrugUri,         // Query the content URI for the current drug
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of drug attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(DrugContract.DrugEntry.COLUMN_DRUG_NAME);
            int quantityColumnIndex = cursor.getColumnIndex(DrugContract.DrugEntry.COLUMN_DRUG_QUANTITY);
            int quantitySoldColumnIndex = cursor.getColumnIndex(DrugContract.DrugEntry.COLUMN_DRUG_SOLD);
            int priceColumnIndex = cursor.getColumnIndex(DrugContract.DrugEntry.COLUMN_DRUG_PRICE);


            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            Double price = cursor.getDouble(priceColumnIndex);
            int quantitySold = cursor.getInt(quantityColumnIndex);

            mDrugname = name;
            mDrugQuantity = Integer.toString(quantity);
            mDrugPrice = Double.toString(price);

            // Update the views on the screen with the values from the database
            mNameEditText.setText(name);
            mQuantityText.setText(Integer.toString(quantity));
            mPriceEditText.setText(Double.toString(price));

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mNameEditText.setText("");
        mQuantityText.setText("");
        mPriceEditText.setText("");
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Prompt the user to confirm that they want to delete this drug.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the drug.
                deleteDrug();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the drug.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the drug in the database.
     */
    private void deleteDrug() {
        // Only perform the delete if this is an existing pet.
        if (mCurrentDrugUri != null) {
            // Call the ContentResolver to delete the pet at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentPetUri
            // content URI already identifies the pet that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentDrugUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_drug_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_drug_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }

    /**
     * Create summary of the order.
     *
     * @param name is the drug name
     * @param quantity is the drug quantity
     * @param price of the order
     * @return text summary
     */
    private String createOrderSummary(String name, String quantity, String price) {
        name = mDrugname;
        quantity = mDrugQuantity;
        price = mDrugPrice;

        String summary = "Drug Name: " + name;
        summary += "\n" + "Drug Quantity: " + quantity;
        summary += "\n" + "Drug Price: " + price;
        return summary;
    }
}