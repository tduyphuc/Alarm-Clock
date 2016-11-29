package com.example.phuctdse61834.timernotify;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.lang.reflect.Field;

public class SongActitity extends AppCompatActivity {
    private int[] songIDArr = {R.raw.yeu, R.raw.kisstherain, R.raw.indigo};
    private String[] songNames;
    private SharedPreferences mPref;
    private SharedPreferences.Editor editor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choose_song_actitity);
        ListView listSongView = (ListView)findViewById(R.id.list_song);
        mPref = getSharedPreferences("SAVE", MODE_PRIVATE);
        editor = mPref.edit();

        songNames = new String[songIDArr.length];
        for (int i = 0; i < songIDArr.length; i++){
            songNames[i] = getResources().getResourceEntryName(songIDArr[i]);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.list_song_item, R.id.txt_song_item, songNames);
        listSongView.setAdapter(adapter);
        listSongView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                editor.putInt("SONG_ID", songIDArr[position]);
                editor.commit();
                setResult(Activity.RESULT_OK);
                finish();
            }
        });
    }
}
