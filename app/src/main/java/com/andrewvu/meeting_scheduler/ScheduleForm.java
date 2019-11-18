package com.andrewvu.meeting_scheduler;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author Andrew Vu (6044937)
 *
 * Form for adding a meeting to the database. Date is set based on calendar widget.
 * Time is set through a widget fragment created when the corresponding button is pressed.
 * Contact is set through an intent and listener.
 * Save commits the data to the database, while cancel brings up a prompt before discarding
 * unsaved data.
 */
public class ScheduleForm extends AppCompatActivity {
    CalendarView calendarView;      // calendar view
    private String contactName;     // contact name
    private String contactNumber;   // contact phone number
    private String date;    // date in string form DD/MM/YYYY for convenience
    private String time;    // time in 24hr notation 00:00
    private int day;    // day of month
    private int month;  // month of year
    private int year;   // year

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_form);

        // initialize values
        contactName = "";
        contactNumber = "";
        time = "00:00";
        day = CurrentDate.getDayOfMonth();
        month = CurrentDate.getMonth();
        year = CurrentDate.getYear();
        date = CurrentDate.getDate();

        // unbundles
        if (savedInstanceState != null) {
            contactName = savedInstanceState.getString("contactName");
            contactNumber = savedInstanceState.getString("contactNumber");
            date = savedInstanceState.getString("date");
            time= savedInstanceState.getString("time");
            day= savedInstanceState.getInt("day");
            month= savedInstanceState.getInt("month");
            year= savedInstanceState.getInt("year");
        }


        calendarView = (CalendarView) findViewById(R.id.calendarPicker);
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int y, int m, int dayOfMonth) {
                day = dayOfMonth;
                month = m;
                year = y;
                date = day + "/" + (month + 1) + "/" + year;
            }
        });
    }

    /**
     * Creates a dialog fragment displaying a time picker widget.
     * @param view
     */
    public void pickTime(View view) {
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getSupportFragmentManager(), "timePicker");
    }

    /**
     * Creates a new intent for picking contact information.
     * @param view
     */
    public void addContact(View view) {
        Intent pickContact = new Intent(Intent.ACTION_PICK, Uri.parse("content://contacts"));
        // Show user only contacts with phone numbers
        pickContact.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
        startActivityForResult(pickContact, 222);
    }

    /**
     * Listens for contact information
     * @param requestCode
     * @param resultCode
     * @param data
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 222) { // requestCode 222 - add contact
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                String[] cString = {
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Phone.NUMBER
                };
                Cursor cursor = getContentResolver().query(uri, cString, null, null, null);
                cursor.moveToFirst();
                contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                contactNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                Toast.makeText(this, "Contact: " + contactName + " added.", Toast.LENGTH_SHORT).show();
                TextView textView = (TextView) findViewById(R.id.contactName);
                textView.setText(contactName);
            }
        }
    }

    /**
     * Saves information by calling writeDatabase()
     * @param view
     */
    public void saveSchedule(View view) {
        // save info
        TextView textView = (TextView) findViewById(R.id.timeView);
        time = textView.getText().toString();

        writeDatabase();

        // Confirmation Toast & close activity
        String contactToast = ""; // contact name for toast message
        if (!contactName.equals("")) contactToast = " with " + contactName; // if there is a contact
        Toast.makeText(this,
                "Meeting added for " + date + " at " + time + contactToast + ".",
                Toast.LENGTH_LONG).show();

        closeActivity();
    }

    /**
     * Commits entry to database
     */
    private void writeDatabase() {
        DataHelper dh = new DataHelper(this);
        SQLiteDatabase dataChanger = dh.getWritableDatabase();

        ContentValues entry = new ContentValues();
        entry.put("date", date);
        entry.put("time", time);
        if (contactName.equals(""))
            entry.put("name", "No Contact");
        else
            entry.put("name", contactName);
        if (contactNumber.equals(""))
            entry.put("phone", "No Number");
        else
            entry.put("phone", contactNumber);


        dataChanger.insert(DataHelper.DB_TABLE, null, entry);
        dataChanger.close();
    }

    /**
     * Confirmation prompt for cancelling a meeting entry.
     * @param view
     */
    public void cancelSchedule(View view) {
        new AlertDialog.Builder(ScheduleForm.this)
                .setTitle(R.string.warning)
                .setMessage(R.string.cancelMessage)
                .setCancelable(true)
                .setNegativeButton(R.string.negativeButton, null)
                .setPositiveButton(R.string.positiveButton, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        closeActivity();
                    }
                }).show();
    }

    /**
     * Closes activity and returns to main
     */
    public void closeActivity() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    /**
     * Bundles form data
     * @param bundle
     */
    @Override
    public void onSaveInstanceState(Bundle bundle){
        bundle.putString("contactName",contactName);
        bundle.putString("contactNumber",contactNumber);
        bundle.putString("date",date);
        TextView textView = (TextView)findViewById(R.id.timeView);
        bundle.putString("time",textView.toString());
        bundle.putInt("day",day);
        bundle.putInt("month",month);
        bundle.putInt("year",year);
        super.onSaveInstanceState(bundle);
    }

}
