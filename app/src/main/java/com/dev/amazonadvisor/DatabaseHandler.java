package com.dev.amazonadvisor;

/**
 * Created by daniele on 09/06/2017.
 */

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_NAME = "productsManager";

    private static final String TABLE_PRODUCTS = "products";

    private static final String KEY_ID = "id";
    private static final String KEY_TITLE = "title";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_PRICE = "price";
    private static final String KEY_ASIN = "asin";
    private static final String KEY_SELLER = "seller";
    private static final String KEY_AVAILABILITY = "availability";
    private static final String KEY_PRICE_DROP = "price_drop";
    private static final String KEY_RATING = "tating";
    private static final String KEY_WARRANTY = "warranty";
    private static final String KEY_URL = "url";
    private static final String KEY_IMAGE = "image";
    private static final String KEY_PRIME = "prime";

    private static final String COLUMNS[] = {KEY_ID, KEY_TITLE, KEY_DESCRIPTION, KEY_PRICE, KEY_ASIN, KEY_SELLER,
                                             KEY_AVAILABILITY, KEY_PRICE_DROP, KEY_RATING, KEY_WARRANTY, KEY_URL,
                                             KEY_IMAGE, KEY_PRIME};

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_PRODUCTS_TABLE = "CREATE TABLE " + TABLE_PRODUCTS + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_TITLE + " TEXT,"
                + KEY_DESCRIPTION + " TEXT," + KEY_PRICE + " TEXT," + KEY_ASIN +" TEXT," + KEY_SELLER + " TEXT," + KEY_AVAILABILITY + " TEXT,"
                + KEY_PRICE_DROP + " TEXT," + KEY_RATING + " TEXT," + KEY_WARRANTY + " TEXT," + KEY_URL + " TEXT, " + KEY_IMAGE + " BLOB,"
                + KEY_PRIME + " INTEGER)";
        db.execSQL(CREATE_PRODUCTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCTS);
        onCreate(db);
    }

    void addProduct(AmazonProduct product) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_TITLE, product.title);
        values.put(KEY_DESCRIPTION, product.description);
        values.put(KEY_PRICE, product.price);
        values.put(KEY_PRICE_DROP, product.priceDrop);
        values.put(KEY_PRIME, product.prime);
        values.put(KEY_RATING, product.rating);
        values.put(KEY_SELLER, product.seller);
        values.put(KEY_URL, product.url);
        values.put(KEY_TITLE, product.title);
        values.put(KEY_WARRANTY, product.warranty);
        values.put(KEY_ASIN, product.productId);
        values.put(KEY_AVAILABILITY, product.availability);
        values.put(KEY_IMAGE, product.image);
        db.insert(TABLE_PRODUCTS, null, values);
        db.close();
    }

    public List<AmazonProduct> getAllProducts() {
        List<AmazonProduct> productList = new ArrayList<AmazonProduct>();
        String selectQuery = "SELECT  * FROM " + TABLE_PRODUCTS;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(TABLE_PRODUCTS, COLUMNS, "",
                                 null,
                                 null,
                                 null,
                                 null,
                                 null);
        if (cursor.moveToFirst()) {
            do {
                AmazonProduct product = new AmazonProduct();
                product.title = cursor.getString(1);
                product.description = cursor.getString(2);
                product.price = cursor.getString(3);
                product.productId = cursor.getString(4);
                product.seller = cursor.getString(5);
                product.availability = cursor.getString(6);
                product.priceDrop = cursor.getString(7);
                product.rating = cursor.getString(8);
                product.warranty = cursor.getString(9);
                product.url = cursor.getString(10);
                product.image = cursor.getBlob(11);
                product.prime = cursor.getInt(12) == 1;
                productList.add(product);
            } while (cursor.moveToNext());
        }

        return productList;
    }

    public void erase() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.rawQuery("DELETE FROM " + TABLE_PRODUCTS, null);
        db.close();
    }

    public int getProductsCount() {
        String countQuery = "SELECT  * FROM " + TABLE_PRODUCTS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();
        return cursor.getCount();
    }

}
