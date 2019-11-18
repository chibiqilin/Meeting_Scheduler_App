package com.andrewvu.meeting_scheduler;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        query(CurrentDate.getDate());
    }

    private void query(String date) {
        String[] fields=new String[]{"id","date","time","name","phone"};
        ListView lv=(ListView)findViewById(R.id.todaysList);
        ArrayList<String> entries=new ArrayList<>();

        DataHelper dh=new DataHelper(this);
        SQLiteDatabase datareader=dh.getReadableDatabase();

        String[] args = {date};
        Cursor cursor=datareader.query(DataHelper.DB_TABLE,fields,
                "date=?",args,null,null,null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            entries.add(Integer.toString(
                    cursor.getInt(0))+": "+
                    cursor.getString(1)+" @ "+
                    cursor.getString(2)+", with "+
                    cursor.getString(3)+" Phone: "+
                    cursor.getString(4));
            cursor.moveToNext();
        }
        if (cursor!=null && !cursor.isClosed()) cursor.close();
        ArrayAdapter<String> adapter=new ArrayAdapter<>( this,
                android.R.layout.simple_list_item_1,entries);

        lv.setAdapter(adapter);
        registerForContextMenu(lv);
        datareader.close();
    }


    public void createSchedule(View view) {
        startActivity(new Intent(this, ScheduleForm.class));
    }

    public void viewSchedule(View view) {
        startActivity(new Intent(this,ScheduleViewer.class));
    }
}
