package com.example.phuctdse61834.timernotify;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    private AlarmManager alarmManager;
    private TimePicker timePicker;
    private PendingIntent pendingIntent;
    private boolean isStarting;
    private SharedPreferences mPref;
    private SharedPreferences.Editor editor;
    private LinearLayout dateLayout;
    private TextView dateText;
    private TextView txtSong;
    final Calendar calendar = Calendar.getInstance();
    private BroadcastReceiver receiver;
    private SeekBar volumeSeekbar;

    public static final int SONG_ACTIVITY_RESULT = 100;

    @Override
    protected void onStart() {
        super.onStart();

        // if user has been setting time before, load their time
        long timeSet = mPref.getLong("TIME", System.currentTimeMillis());
        if(timeSet > 0){
            calendar.setTimeInMillis(timeSet);
        }
        else {
            calendar.setTimeInMillis(System.currentTimeMillis());
        }
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int min = calendar.get(Calendar.MINUTE);
        // set time picker
        if(Build.VERSION.SDK_INT >= 23){
            timePicker.setHour(hour);
            timePicker.setMinute(min);
        }
        else{
            timePicker.setCurrentHour(hour);
            timePicker.setCurrentMinute(min);
        }
        // set date picker
        String dateStr = calendar.get(Calendar.MONTH) + "-" +
                calendar.get(Calendar.DAY_OF_MONTH) + "-" + calendar.get(Calendar.YEAR);
        dateText.setText(dateStr);

        LocalBroadcastManager.getInstance(this)
                .registerReceiver(receiver, new IntentFilter(NotifyService.SERVICE_COMPLETE));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // init resrc
        volumeSeekbar = (SeekBar)findViewById(R.id.volume_adjust);
        timePicker = (TimePicker) findViewById(R.id.timePicker);
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        dateLayout = (LinearLayout)findViewById(R.id.date_layout);
        dateText = (TextView)dateLayout.findViewById(R.id.txt_day);
        mPref = getSharedPreferences("SAVE", MODE_PRIVATE);
        editor = mPref.edit();
        LinearLayout songLayout = (LinearLayout)findViewById(R.id.sound_layout);
        isStarting = mPref.getBoolean("IS_STARTING", false);
        final Intent intent = new Intent(this, Receiver.class);
        pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // set text to txtSong
        txtSong = (TextView) findViewById(R.id.txt_song);
        int songId = mPref.getInt("SONG_ID", R.raw.kisstherain);
        txtSong.setText(getResources().getResourceEntryName(songId));

        // init button star
        final Button btn_start = (Button) findViewById(R.id.btn_Start);
        if(isStarting){
            btn_start.setText((String) getResources().getString(R.string.cancel_btn));
            btn_start.setTextColor(getResources().getColor(R.color.btn_text_cancel));
        }
        else{
            btn_start.setText((String) getResources().getString(R.string.star_btn));
            btn_start.setTextColor(getResources().getColor(R.color.btn_text_start));
        }
        // set start event for button
        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isStarting) {
                    calendar.set(Calendar.SECOND, 0);
                    //get time
                    if(Build.VERSION.SDK_INT >= 23){
                        calendar.set(Calendar.HOUR_OF_DAY, timePicker.getHour());
                        calendar.set(Calendar.MINUTE, timePicker.getMinute());
                    }
                    else{
                        calendar.set(Calendar.HOUR_OF_DAY, timePicker.getCurrentHour());
                        calendar.set(Calendar.MINUTE, timePicker.getCurrentMinute());
                    }
                    // check if user set time in the past
                    if(calendar.getTimeInMillis() > System.currentTimeMillis()){
                        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                        isStarting = true;
                        btn_start.setText((String) getResources().getString(R.string.cancel_btn));
                        btn_start.setTextColor(getResources().getColor(R.color.btn_text_cancel));
                    }
                    else {
                        Toast.makeText(MainActivity.this,"Unable to set time in the past", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    isStarting = false;
                    btn_start.setText((String) getResources().getString(R.string.star_btn));
                    btn_start.setTextColor(getResources().getColor(R.color.btn_text_start));

                    // cancel
                    alarmManager.cancel(pendingIntent);
                }
            }
        });

        // date event
        dateLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        calendar.set(year, monthOfYear, dayOfMonth);
                        String dateStr = calendar.get(Calendar.MONTH) + "-" +
                                calendar.get(Calendar.DAY_OF_MONTH) + "-" + calendar.get(Calendar.YEAR);
                        dateText.setText(dateStr);
                    }
                };
                DatePickerDialog datePickerDialog = new
                        DatePickerDialog(MainActivity.this, 0, listener,
                        calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.setMessage("Set date");
                datePickerDialog.setCancelable(true);
                datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
                datePickerDialog.show();
            }
        });

        // song event
        songLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentToSong = new Intent(MainActivity.this, SongActitity.class);
                startActivityForResult(intentToSong, SONG_ACTIVITY_RESULT);
            }
        });

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                isStarting = false;
                btn_start.setText((String) getResources().getString(R.string.star_btn));
                btn_start.setTextColor(getResources().getColor(R.color.btn_text_start));
            }
        };

        // volume seeker
        int lastProgress = mPref.getInt("VOLUME", 100);
        volumeSeekbar.setProgress(lastProgress);
        volumeSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                editor.putInt("VOLUME", progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                editor.commit();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        editor.putBoolean("IS_STARTING", isStarting);
        if(isStarting){
            editor.putLong("TIME", calendar.getTimeInMillis());
        }
        else{
            editor.putLong("TIME", -1);
        }
        editor.commit();
    }

    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        super.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == SONG_ACTIVITY_RESULT){
            if(resultCode == RESULT_OK){
                int songId = mPref.getInt("SONG_ID", R.raw.kisstherain);
                txtSong.setText(getResources().getResourceEntryName(songId));
            }
        }
    }
}
