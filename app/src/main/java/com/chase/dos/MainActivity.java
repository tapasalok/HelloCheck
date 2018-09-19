package com.chase.dos;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends ActionBarActivity {

    private static final String TAG = "MainActivity";
    private EditText editText = null;
    private Button recvBtn = null;
    private Button sendBtn = null;
    private ListView listView = null;
    private Color origColor = null;

    private Sender tx = null;
    private Receiver rx = null;
    private Context context;
    private Set<String> strings = new HashSet<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = (EditText) findViewById(R.id.editText);
        recvBtn = (Button) findViewById(R.id.recvBtn);
        sendBtn = (Button) findViewById(R.id.sendBtn);
        listView = (ListView) findViewById(R.id.listView);

        // Get intent, action and MIME type
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        String str = "";
        Log.d(TAG, "type: " + type);

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            switch (type) {
                case "text/plain":
                    str = intent.getStringExtra(Intent.EXTRA_TEXT);
                    break;
            }
        }

        Log.d(TAG, "str: " + str);
        editText.setText(str);
        editText.setSelection(str.length());

        context = getApplicationContext();
    }

    void toast(String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    void setText(String s) {
        editText.setText(s);
        editText.setSelection(s.length());
    }

    public void onReceive(View v) {
        rx = new Receiver() {
            @Override
            protected void onPostExecute(Result res) {
                if (res.err == null) {
                    setText(res.out);
                    strings.add(res.out);
                    List<String> stringList = new ArrayList<>(strings);
                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, stringList);
                    listView.setAdapter(arrayAdapter);
                    arrayAdapter.notifyDataSetChanged();
                    toast("OK");
                } else {
                    setText("");
                    toast("Error: " + res.err);
                }
                recvBtn.setEnabled(true);
            }

            @Override
            protected void onProgressUpdate(Double... values) {
                double p = values[0];
            }
        };
        recvBtn.setEnabled(false);
        rx.execute();
        toast("Receiving...");
    }

    public void onSend(View v) {
        String msg = editText.getText().toString();
        tx = new Sender() {
            @Override
            protected void onPostExecute(Void result) {
                sendBtn.setEnabled(true);
            }

            @Override
            protected void onProgressUpdate(Double... values) {
                double p = values[0];
            }
        };
        sendBtn.setEnabled(false);
        tx.execute(msg);
        toast("Sending...");
    }

    public void onStop(View v) {
        setText("");
        if (rx != null) {
            rx.stop();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String str = editText.getText().toString();

        switch (item.getItemId()) {
            case R.id.menu_change_text:
                ClipData clip = ClipData.newPlainText("text", str);
                ClipboardManager mgr = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                mgr.setPrimaryClip(clip);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
