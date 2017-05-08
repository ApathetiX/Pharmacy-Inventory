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

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * API Contract for the Pets app.
 */
public final class DrugContract {

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    private DrugContract() {}

    /**
     * The "Content authority" is a name for the entire content provider, similar to the
     * relationship between a domain name and its website.  A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on the
     * device.
     */
    public static final String CONTENT_AUTHORITY = "com.example.android.pharmacyinventory";

    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Possible path (appended to base content URI for possible URI's)
     * For instance, content://com.example.android.pharmacyinventory/pharmacyinventory/ is a valid path for
     * looking at drug data. content://com.example.android.pharmacyinventory/staff/ will fail,
     * as the ContentProvider hasn't been given any information on what to do with "staff".
     */
    public static final String PATH_DRUG = "drugs";

    /**
     * Inner class that defines constant values for the drugs database table.
     * Each entry in the table represents a single drug.
     */
    public static final class DrugEntry implements BaseColumns {

        /** The content URI to access the drug data in the provider */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_DRUG);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of drugs.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_DRUG;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single drug.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_DRUG;

        /** Name of database table for drugs */
        public final static String TABLE_NAME = "drugs";

        /**
         * Unique ID number for the drug (only for use in the database table).
         *
         * Type: INTEGER
         */
        public final static String _ID = BaseColumns._ID;

        /**
         * Name of the drug.
         *
         * Type: TEXT
         */
        public final static String COLUMN_DRUG_NAME ="name";

        /**
         * Quantity of the drug.
         *
         * Type: INTEGER
         */
        public final static String COLUMN_DRUG_QUANTITY = "quantity";

        /**
         * Quantity sold drug.
         *
         * Type: INTEGER
         */
        public final static String COLUMN_DRUG_SOLD = "sold";

        /**
         * Price of the drug.
         *
         * Type: INTEGER
         */
        public final static String COLUMN_DRUG_PRICE = "price";


    }

}

