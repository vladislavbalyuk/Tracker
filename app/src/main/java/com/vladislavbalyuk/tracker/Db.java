package com.vladislavbalyuk.tracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteCantOpenDatabaseException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class Db {
    private static Db instance;

    private DBHelper dbh;
    private SQLiteDatabase db;

    private Db(Context context) {
        dbh = new DBHelper(context);
    }

    public static Db getInstance(Context context) {
        if (instance == null) {
            synchronized (Db.class) {
                if (instance == null) {
                    instance = new Db(context);
                }
            }
        }
        return instance;
    }

    public synchronized void updatePoint(Point point, Track track, int number) {
        ContentValues cv = new ContentValues();
        cv.put("_id", point.getUuid());
        cv.put("Track", track.getUuid());
        cv.put("Num", number);
        cv.put("Altitude", point.getAltitude());
        cv.put("Latitude", point.getLatitude());
        cv.put("Longitude", point.getLongitude());
        cv.put("Adress", point.getAdress());
        if (point.getDate() != null) {
            String dateString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(point.getDate());
            cv.put("Date", dateString);
        }
        try {
            db = dbh.getWritableDatabase();
            db.beginTransaction();

            if (db.update("Points", cv, "_id = ?", new String[]{point.getUuid()}) == 0) {
                insertPoint(point, track, number);
            }
            db.setTransactionSuccessful();
            db.endTransaction();
        }
        catch (SQLiteCantOpenDatabaseException e){}
        finally {
            dbh.close();
        }
    }

    private void insertPoint(Point point, Track track, int number) {
        ContentValues cv = new ContentValues();
        cv.put("_id", point.getUuid());
        cv.put("Track", track.getUuid());
        cv.put("Num", number);
        cv.put("Altitude", point.getAltitude());
        cv.put("Latitude", point.getLatitude());
        cv.put("Longitude", point.getLongitude());
        cv.put("Adress", point.getAdress());
        if (point.getDate() != null) {
            String dateString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(point.getDate());
            cv.put("Date", dateString);
        }

        db.insert("Points", null, cv);
    }

    public synchronized void deleteTrack(Track track) {
        try {
            db = dbh.getWritableDatabase();
            db.beginTransaction();

            db.delete("Tracks", "_id = ?", new String[]{track.getUuid()});
            db.delete("Points", "Track = ?", new String[]{track.getUuid()});
            db.setTransactionSuccessful();
            db.endTransaction();
        }
        catch (SQLiteCantOpenDatabaseException e){}
        finally {
            dbh.close();
        }
    }

    public synchronized void updateTrack(Track track) {
        ContentValues cv = new ContentValues();
        cv.put("_id", track.getUuid());
        cv.put("Name", track.getName());
        try {
            db = dbh.getWritableDatabase();
            db.beginTransaction();

            if (db.update("Tracks", cv, "_id = ?", new String[]{track.getUuid()}) == 0) {
                insertTrack(track);
            }
            db.setTransactionSuccessful();
            db.endTransaction();
        }
        catch (SQLiteCantOpenDatabaseException e){}
        finally {
            dbh.close();
        }
    }

    private void insertTrack(Track track) {
        ContentValues cv = new ContentValues();
         cv.put("_id", track.getUuid());
        cv.put("Name", track.getName());

        db.insert("Tracks", null, cv);
    }

    public synchronized List<Track> getTracks() {
        List<Track> tracks;
        tracks = new ArrayList<Track>();

        String sqlQuery = "select T._id, T.Name from Tracks as T ";

        try {
            db = dbh.getWritableDatabase();
            Cursor c = db.rawQuery(sqlQuery, null);
            if (c != null) {
                if (c.moveToFirst()) {
                    do {
                        Track track = new Track();
                        track.setUuid(c.getString(0));
                        track.setName(c.getString(1));
                        track.setPoints(getPoints(track));

                        tracks.add(track);
                    } while (c.moveToNext());
                }
            }
        }
        catch (SQLiteCantOpenDatabaseException e){}
        finally {
            dbh.close();
        }

        return tracks;
    }

    public synchronized Track getTrack(String uuid) {
        Track track = new Track();

        String sqlQuery = "select T._id, T.Name from Tracks as T where T._id = ?";

        try {
            db = dbh.getWritableDatabase();
            Cursor c = db.rawQuery(sqlQuery, new String[] {uuid});
            if (c != null) {
                if (c.moveToFirst()) {
                    do {
                        track.setUuid(c.getString(0));
                        track.setName(c.getString(1));
                    } while (c.moveToNext());
                }
            }
        }
        catch (SQLiteCantOpenDatabaseException e){}
        finally {
            dbh.close();
        }

        return track;
    }

    public synchronized String getUuidTrackForPoint(String uuid) {
        String sqlQuery = "select T.Track from Points as T where T._id = ?";
        String uuidTrack = "";

        try {
            db = dbh.getWritableDatabase();
            Cursor c = db.rawQuery(sqlQuery, new String[] {uuid});
            if (c != null) {
                if (c.moveToFirst()) {
                    do {
                        uuidTrack = c.getString(0);
                    } while (c.moveToNext());
                }
            }
        }
        catch (SQLiteCantOpenDatabaseException e){}
        finally {
            dbh.close();
        }

        return uuidTrack;
    }

    public synchronized int getNumForPoint(String uuid) {
        String sqlQuery = "select T.Num from Points as T where T._id = ?";
        int num = 0;

        try {
            db = dbh.getWritableDatabase();
            Cursor c = db.rawQuery(sqlQuery, new String[] {uuid});
            if (c != null) {
                if (c.moveToFirst()) {
                    do {
                        num = c.getInt(0);
                    } while (c.moveToNext());
                }
            }
        }
        catch (SQLiteCantOpenDatabaseException e){}
        finally {
            dbh.close();
        }

        return num;
    }

    public synchronized List<Point> getPoints(Track track) {
        List<Point> points;
        points = new ArrayList<Point>();

        String sqlQuery = "select P._id, P.Longitude, P.Latitude, P.Date, P.Adress, P.Altitude from Points as P where P.Track = ? order by P.Num";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        try {
            db = dbh.getWritableDatabase();
            Cursor c = db.rawQuery(sqlQuery, new String[] {track.getUuid()});
            if (c != null) {
                if (c.moveToFirst()) {
                    do {
                        Point point = new Point();
                        point.setUuid(c.getString(0));
                        point.setLongitude(new Double(c.getString(1)).doubleValue());
                        point.setLatitude(new Double(c.getString(2)).doubleValue());
                        point.setAltitude(new Double(c.getString(5)).doubleValue());
                        try {
                            point.setDate(sdf.parse(c.getString(3)));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        ;
                        point.setAdress(c.getString(4));

                        points.add(point);
                    } while (c.moveToNext());
                }
            }
        }
        catch (SQLiteCantOpenDatabaseException e){}
        finally {
            dbh.close();
        }

        return points;
    }

    //TEST
    public void getTest() {

        String sqlQuery = "select P._id, P.Track, P.Num, P.Altitude, P.Longitude, P.Latitude, P.Date, P.Adress from Points as P";
//        String sqlQuery = "select P._id, P.Name from Tracks as P";
        try {
            db = dbh.getWritableDatabase();
            Cursor c = db.rawQuery(sqlQuery, null);
            Log.d("MyTag","TEST SELECT POINTS");
            if (c != null) {
                if (c.moveToFirst()) {
                    do {
//                        Log.d("MyTag","" + c.getString(0) + "   " + c.getString(1));
                        Log.d("MyTag","" + c.getString(0) + "   " + c.getString(1) + "  " + c.getString(2) + "  " + c.getString(3) + "  " + c.getString(4) + "  " + c.getString(5) + "  " + c.getString(6) + "  " + c.getString(7));
                    } while (c.moveToNext());
                }
            }
        }
        catch (SQLiteCantOpenDatabaseException e){}
        finally {
            dbh.close();
        }

    }
    //TEST


    private class DBHelper extends SQLiteOpenHelper {
        Context context;

        private DBHelper(Context context) {
            super(context, "Tracker", null, 1);
            this.context = context;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("create table Tracks ("
                    + "_id text primary key,"
                    + "Name text" + ");");
            db.execSQL("create table Points ("
                    + "_id text primary key,"
                    + "Track text,"
                    + "Num numeric,"
                    + "Longitude real,"
                    + "Latitude real,"
                    + "Altitude real,"
                    + "Date datetime,"
                    + "Adress text" + ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }


}