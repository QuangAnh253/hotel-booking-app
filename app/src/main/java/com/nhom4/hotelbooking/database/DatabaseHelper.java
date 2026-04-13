package com.nhom4.hotelbooking.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.nhom4.hotelbooking.models.Booking;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "hotel.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_BOOKING = "booking";
    private static final String COL_ID = "id";
    private static final String COL_USER_ID = "user_id";
    private static final String COL_ROOM_ID = "room_id";
    private static final String COL_ROOM_NAME = "room_name";
    private static final String COL_CHECK_IN = "check_in";
    private static final String COL_CHECK_OUT = "check_out";
    private static final String COL_TOTAL_PRICE = "total_price";
    private static final String COL_STATUS = "status";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + TABLE_BOOKING + " ("
                + COL_ID + " TEXT PRIMARY KEY, "
                + COL_USER_ID + " TEXT, "
                + COL_ROOM_ID + " TEXT, "
                + COL_ROOM_NAME + " TEXT, "
                + COL_CHECK_IN + " TEXT, "
                + COL_CHECK_OUT + " TEXT, "
                + COL_TOTAL_PRICE + " REAL, "
                + COL_STATUS + " TEXT)";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKING);
        onCreate(db);
    }

    public void insertBooking(Booking booking) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_ID, booking.getId());
        values.put(COL_USER_ID, booking.getUserId());
        values.put(COL_ROOM_ID, booking.getRoomId());
        values.put(COL_ROOM_NAME, booking.getRoomName());
        values.put(COL_CHECK_IN, booking.getCheckIn());
        values.put(COL_CHECK_OUT, booking.getCheckOut());
        values.put(COL_TOTAL_PRICE, booking.getTotalPrice());
        values.put(COL_STATUS, booking.getStatus());
        db.insert(TABLE_BOOKING, null, values);
        db.close();
    }

    public List<Booking> getAllBookings(String userId) {
        List<Booking> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_BOOKING, null,
                COL_USER_ID + "=?", new String[]{userId},
                null, null, null);
        if (cursor.moveToFirst()) {
            do {
                Booking booking = new Booking();
                booking.setId(cursor.getString(cursor.getColumnIndexOrThrow(COL_ID)));
                booking.setUserId(cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_ID)));
                booking.setRoomId(cursor.getString(cursor.getColumnIndexOrThrow(COL_ROOM_ID)));
                booking.setRoomName(cursor.getString(cursor.getColumnIndexOrThrow(COL_ROOM_NAME)));
                booking.setCheckIn(cursor.getString(cursor.getColumnIndexOrThrow(COL_CHECK_IN)));
                booking.setCheckOut(cursor.getString(cursor.getColumnIndexOrThrow(COL_CHECK_OUT)));
                booking.setTotalPrice(cursor.getDouble(cursor.getColumnIndexOrThrow(COL_TOTAL_PRICE)));
                booking.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(COL_STATUS)));
                list.add(booking);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }

    public void updateBookingStatus(String bookingId, String newStatus) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_STATUS, newStatus);
        db.update(TABLE_BOOKING, values, COL_ID + "=?", new String[]{bookingId});
        db.close();
    }

    public void deleteBooking(String bookingId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_BOOKING, COL_ID + "=?", new String[]{bookingId});
        db.close();
    }
}
