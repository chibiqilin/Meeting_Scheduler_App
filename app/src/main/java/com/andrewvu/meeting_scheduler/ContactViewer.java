package com.andrewvu.meeting_scheduler;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class ContactViewer extends AppCompatActivity {
    ArrayList<String> ar;
    int pointer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_viewer);

        queryContacts();
        if (savedInstanceState != null) { // if savedInstanceState is not null, assigns values
            pointer = savedInstanceState.getInt("pointer");
            pointer--;
            if (pointer == -1) pointer = 2;
            nextContact(null);
        }
    }

    private void queryContacts() {
        ar = new ArrayList<String>();
        pointer = 0;
        DataHelper dh = new DataHelper(this);
        SQLiteDatabase dataReader = dh.getReadableDatabase();

        // read entire table
        Cursor cursor = dataReader.rawQuery("SELECT * FROM " + DataHelper.DB_TABLE, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) { // adds ID of expired entries to ar
            if (!ar.contains(cursor.getString(3)))
                ar.add(cursor.getString(3));
            cursor.moveToNext();
        }
        if (cursor != null && !cursor.isClosed()) cursor.close();
        for (int i = 0; i < ar.size(); i++) {
            System.out.println("Contact: " + ar.get(i));
        }
    }

    public void purgeContact(View view) {
        new AlertDialog.Builder(ContactViewer.this)
                .setTitle(R.string.warning)
                .setMessage(R.string.clearContactMessage)
                .setCancelable(true)
                .setNegativeButton(R.string.negativeButton, null)
                .setPositiveButton(R.string.positiveButton, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        deleteContact();
                    }
                }).show();
    }

    private void deleteContact() {
        DataHelper dh = new DataHelper(this);
        SQLiteDatabase dataChanger = dh.getWritableDatabase();
        String[] args = {ar.get(pointer)};
        dataChanger.delete(DataHelper.DB_TABLE, "name=?", args);
        ListView lv = (ListView) findViewById(R.id.contactList);
        lv.setAdapter(null);
    }

    public void nextContact(View view) {
        pointer = (pointer + 1) % ar.size();
        TextView textView = (TextView) findViewById(R.id.contactView);
        textView.setText(ar.get(pointer));
        query(ar.get(pointer));
    }

    private void query(String contact) {
        String[] fields = new String[]{"id", "date", "time", "name", "phone"};
        ListView lv = (ListView) findViewById(R.id.contactList);
        lv.setAdapter(null);
        ArrayList<String> entries = new ArrayList<>();

        DataHelper dh = new DataHelper(this);
        SQLiteDatabase datareader = dh.getReadableDatabase();

        String[] args = {contact};
        Cursor cursor = datareader.query(DataHelper.DB_TABLE, fields,
                "name=?", args, null, null, null);
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

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        bundle.putInt("pointer", pointer);
        super.onSaveInstanceState(bundle);
    }
}
