package com.andrewvu.meeting_scheduler;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CalendarView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * @author Andrew Vu (6044937)
 *
 * Activity displays a calendar and allows the user to view a list of all meetings on
 * any selected date. There are also various button options for removing meetings on a
 * specified date, removing all expired meetings, or completely removing all meetings stored.
 */
public class ScheduleViewer extends AppCompatActivity {
    CalendarView calendarView;      // calendar view
    String date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_viewer);

        date = CurrentDate.getDate();
        query(date);

        calendarView = (CalendarView) findViewById(R.id.calendarViewer);
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int y, int m, int dayOfMonth) {
                date = dayOfMonth + "/" + (m + 1) + "/" + y;
                query(date);
            }
        });

        if (savedInstanceState != null) {
            calendarView.setDate(savedInstanceState.getLong("calendarDate"));
            date = savedInstanceState.getString("savedDate");
            query(date);
        }
    }

    /**
     * queries table based on selected date
     * @param date
     */
    private void query(String date) {
        String[] fields = new String[]{"id", "date", "time", "name", "phone"};
        ListView lv = (ListView) findViewById(R.id.calendarList);
        lv.setAdapter(null);
        ArrayList<String> entries = new ArrayList<>();

        DataHelper dh = new DataHelper(this);
        SQLiteDatabase datareader = dh.getReadableDatabase();

        String[] args = {date};
        Cursor cursor = datareader.query(DataHelper.DB_TABLE, fields,
                "date=?", args, null, null, null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            entries.add(Integer.toString(
                    cursor.getInt(0)) + ": " +
                    cursor.getString(1) + " @ " +
                    cursor.getString(2) + ", with " +
                    cursor.getString(3) + " Phone: " +
                    cursor.getString(4));
            cursor.moveToNext();
        }
        if (cursor != null && !cursor.isClosed()) cursor.close();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, entries);

        lv.setAdapter(adapter);
        registerForContextMenu(lv);
        datareader.close();
    }


    /**
     * Dialogue confirmation for clearing all meetings on a specified date
     *
     * @param view
     */
    public void clearDate(View view) {
        new AlertDialog.Builder(ScheduleViewer.this)
                .setTitle(R.string.warning)
                .setMessage(R.string.clearDateMessage)
                .setCancelable(true)
                .setNegativeButton(R.string.negativeButton, null)
                .setPositiveButton(R.string.positiveButton, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        deleteDate();
                    }
                }).show();
    }

    /**
     * Delete entries on specified date
     */
    private void deleteDate() {
        DataHelper dh = new DataHelper(this);
        SQLiteDatabase dataChanger = dh.getWritableDatabase();
        String[] args = {date};
        dataChanger.delete(DataHelper.DB_TABLE, "date=?", args);
        ListView lv = (ListView) findViewById(R.id.calendarList);
        lv.setAdapter(null);
    }

    /**
     * Confirmation prompt for clearing all expired meetings
     *
     * @param view
     */
    public void clearExpired(View view) {
        new AlertDialog.Builder(ScheduleViewer.this)
                .setTitle(R.string.warning)
                .setMessage(R.string.clearExpiredMessage)
                .setCancelable(true)
                .setNegativeButton(R.string.negativeButton, null)
                .setPositiveButton(R.string.positiveButton, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        deleteExpired();
                    }
                }).show();
    }

    /**
     * deletes entry in table if it is expired
     */
    private void deleteExpired() {
        DataHelper dh = new DataHelper(this);
        SQLiteDatabase dataReader = dh.getReadableDatabase();
        SQLiteDatabase dataChanger = dh.getWritableDatabase();

        // read entire table
        Cursor cursor = dataReader.rawQuery("SELECT * FROM " + DataHelper.DB_TABLE, null);

        ArrayList<String> ar = new ArrayList<String>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) { // adds ID of expired entries to ar
            String checkDate = cursor.getString(1);
            if (expired(checkDate)) ar.add("" + cursor.getInt(0));
            cursor.moveToNext();
        }
        if (cursor != null && !cursor.isClosed()) cursor.close();
        for (int i = 0; i < ar.size(); i++) {
            String[] args = {ar.get(i)};
            dataChanger.delete(DataHelper.DB_TABLE, "id=?", args);
        }


        dataReader.close();
        dataChanger.close();
    }

    /**
     * checks if checkDate is expired by comparing to current date
     * @param checkDate date being checked
     * @return true or false
     */
    private boolean expired(String checkDate) {
        StringTokenizer str = new StringTokenizer(checkDate, "/");
        if (str.countTokens() == 3) {
            int day = Integer.parseInt(str.nextToken());
            int month = Integer.parseInt(str.nextToken());
            int year = Integer.parseInt(str.nextToken());
            // expired conditions
            if (year < CurrentDate.getYear()) return true;
            if (year == CurrentDate.getYear() && month < (CurrentDate.getMonth() + 1)) return true;
            if (year == CurrentDate.getYear() && month == (CurrentDate.getMonth() + 1) && day < CurrentDate.getDayOfMonth())
                return true;
        }
        // else return false
        return false;
    }

    /**
     * confirmation prompt for wiping all meeting data
     * @param view
     */
    public void wipeAll(View view) {
        new AlertDialog.Builder(ScheduleViewer.this)
                .setTitle(R.string.warning)
                .setMessage(R.string.clearAllMessage)
                .setCancelable(true)
                .setNegativeButton(R.string.negativeButton, null)
                .setPositiveButton(R.string.positiveButton, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        nuke();
                    }
                }).show();
    }

    /**
     * erases all meeting data without dropping table to avoid crashing
     */
    private void nuke() {
        DataHelper dh = new DataHelper(this);
        SQLiteDatabase dataReader = dh.getReadableDatabase();
        SQLiteDatabase dataChanger = dh.getWritableDatabase();

        // read entire table
        Cursor cursor = dataReader.rawQuery("SELECT * FROM " + DataHelper.DB_TABLE, null);

        ArrayList<String> ar = new ArrayList<String>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) { // adds ID of expired entries to ar
            ar.add("" + cursor.getInt(0));
            cursor.moveToNext();
        }
        if (cursor != null && !cursor.isClosed()) cursor.close();
        for (int i = 0; i < ar.size(); i++) {
            String[] args = {ar.get(i)};
            dataChanger.delete(DataHelper.DB_TABLE, "id=?", args);
        }


        dataReader.close();
        dataChanger.close();
        startActivity(new Intent(this, MainActivity.class));
    }

    /**
     * bundles information on which date is being viewed
     * @param bundle
     */
    @Override
    public void onSaveInstanceState(Bundle bundle) {
        bundle.putLong("calendarDate", calendarView.getDate());
        bundle.putString("savedDate", date);
        super.onSaveInstanceState(bundle);
    }
}
