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
package com.example.android.pharmacyinventory.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Database helper for Pharmacy Inventory app. Manages database creation and version management.
 */
public class DrugDbHelper extends SQLiteOpenHelper {

    public static final String LOG_TAG = DrugDbHelper.class.getSimpleName();

    /** Name of the database file */
    private static final String DATABASE_NAME = "inventory.db";

    /**
     * Database version. If you change the database schema, you must increment the database version.
     */
    private static final int DATABASE_VERSION = 1;

    /**
     * Constructs a new instance of {@link DrugDbHelper}.
     *
     * @param context of the app
     */
    public DrugDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * This is called when the database is created for the first time.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a String that contains the SQL statement to create the drugs table
        String SQL_CREATE_DRUG_TABLE =  "CREATE TABLE " + DrugContract.DrugEntry.TABLE_NAME + " ("
                + DrugContract.DrugEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + DrugContract.DrugEntry.COLUMN_DRUG_NAME + " TEXT NOT NULL, "
                + DrugContract.DrugEntry.COLUMN_DRUG_QUANTITY + " INTEGER DEFAULT 0, "
                + DrugContract.DrugEntry.COLUMN_DRUG_SOLD + " INTEGER DEFAULT 0, "
                + DrugContract.DrugEntry.COLUMN_DRUG_PRICE + " REAL NOT NULL, "
                + DrugContract.DrugEntry.COLUMN_DRUG_IMAGE + " TEXT)";

        // Execute the SQL statement
        db.execSQL(SQL_CREATE_DRUG_TABLE);
    }

    /**
     * This is called when the database needs to be upgraded.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // The database is still at version 1, so there's nothing to do be done here.
    }
}