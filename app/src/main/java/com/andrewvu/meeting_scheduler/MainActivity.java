package com.andrewvu.meeting_scheduler;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.Calendar;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * @author Andrew Vu (6044937)
 *
 * Main activity. Displays the current date's meetings in a listview automatically so that users
 * do not have to fumble through any menus to view relevant appointments.
 *
 * Buttons located on the bottom of the screen allow for adding additional meetings,
 * switching to a schedule view, switching to a contact view, or pushing the meetings
 * of the current day to a later date.
 *
 * Pushing on a weekday will push scheduled meetings to the next weekday day. Similarly, pushing
 * on a weekend will push scheduled meetings to the next weekend day.
 */
public class MainActivity extends AppCompatActivity {

    /**
     * Does not use bundles as there is no information to bundle when rotating screen.
     * queries the current date automatically on create.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        query(CurrentDate.getDate());
    }

    /**
     * query all meetings based on an input date
     * @param date
     */
    private void query(String date) {
        String[] fields = new String[]{"id", "date", "time", "name", "phone"};
        ListView lv = (ListView) findViewById(R.id.todaysList);
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
     * Starts a new meeting form
     * @param view
     */
    public void createSchedule(View view) {
        startActivity(new Intent(this, ScheduleForm.class));
    }

    /**
     * Starts a view schedule activity, to view schedule based on dates
     * @param view
     */
    public void viewSchedule(View view) {
        startActivity(new Intent(this, ScheduleViewer.class));
    }

    /**
     * Starts a view contact activity, to view contacts and relevant meetings
     * @param view
     */
    public void viewContacts(View view) {
        startActivity(new Intent(this, ContactViewer.class));
    }

    /**
     * Confirmation prompt before pushing today's meetings.
     * @param view
     */
    public void pushMeetings(View view) {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle(R.string.warning)
                .setMessage(R.string.pushMessage)
                .setCancelable(true)
                .setNegativeButton(R.string.negativeButton, null)
                .setPositiveButton(R.string.positiveButton, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        pushToday();
                        ListView listView = (ListView) findViewById(R.id.todaysList);
                        listView.setAdapter(null);
                    }
                }).show();
    }

    /**
     * Pushes meetings to the next weekday/weekend corresponding with the current day of the week.
     */
    private void pushToday() {
        DataHelper dh = new DataHelper(this);
        SQLiteDatabase dataChanger = dh.getWritableDatabase();
        ContentValues cv = new ContentValues();
        String[] args = {CurrentDate.getDate()};

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, checkPush());
        String pushDay = calendar.get(Calendar.DAY_OF_MONTH) + "/" +
                (calendar.get(Calendar.MONTH) + 1) + "/" +
                calendar.get(Calendar.YEAR);
        System.out.println("Push day is:" + pushDay);

        cv.put("date", pushDay);
        dataChanger.update(DataHelper.DB_TABLE, cv, "date=?", args);

        dataChanger.close();
    }

    /**
     * checkPush ensures that if the current day is a weekday, all meetings get pushed
     * to the next weekday, and likewise for weekends.
     *
     * @return number of days to push ahead
     */
    private int checkPush() {
        Calendar c = Calendar.getInstance();
        int day = c.get(Calendar.DAY_OF_WEEK); // sunday through saturday, int 1-7
        int result = 0;
        switch (day) {
            case 1: // sunday -> saturday = 6 days
                result = 6;
                break;
            case 2: // monday through thursday push forward 1 day, as well as saturday to sunday
            case 3:
            case 4:
            case 5:
            case 7:
                result = 1;
                break;
            case 6: // friday -> monday = 3 days
                result = 3;
        }
        return result;
    }
}
