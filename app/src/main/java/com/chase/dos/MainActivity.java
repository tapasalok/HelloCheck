package com.chase.dos;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends ActionBarActivity {

    private static final String TAG = "MainActivity";
    private EditText editText = null;
    private TextView userNameText = null;
    private Button recvBtn = null;
    private Button sendBtn = null;
    private ListView listView = null;
    private Color origColor = null;
//    private ToggleButton toggleButton;
    private TextView text;
    private Sender tx = null;
    private Receiver rx = null;
    private Context context;
    private Set<String> strings = new HashSet<>();
    private AlertDialog.Builder builder;
    private List<String> stringList;
    private ArrayAdapter<String> arrayAdapter;
    private SharedPreferences pref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = (EditText) findViewById(R.id.editText);
        userNameText = (TextView) findViewById(R.id.userNameText);
        recvBtn = (Button) findViewById(R.id.recvBtn);
        sendBtn = (Button) findViewById(R.id.sendBtn);
        listView = (ListView) findViewById(R.id.listView);

        pref = getApplicationContext().getSharedPreferences("DOS", 0); // 0 - for private mode

        String storedUserName = pref.getString("username", null); // getting String
        if (TextUtils.isEmpty(storedUserName)){
            showAlertForNoUserNameSet();
        }else {
            userNameText.setText("Welcome, "+storedUserName);
        }

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

//        toggleButton = (ToggleButton) findViewById(R.id.toggleButton1);
//        text = (TextView) findViewById(R.id.textView1);
//
//        toggleButton.setOnCheckedChangeListener(new ToggleButton.OnCheckedChangeListener() {
//
//            @Override
//            public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
//                if (isChecked){
//                    text.setText("Status: Send");
//                    View view = null;
//                    onSend(view);
//                }else {
//                    text.setText("Status: Receive");
//                    onReceive(null);
//                }
//
//            }
//        });
    }

    void toast(String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    void setText(String s) {
        editText.setText(s);
        if (TextUtils.isEmpty(s)){
            // Do Nothing
        }else {
            editText.setSelection(s.length());
        }
    }

    public void onReceive(View v) {
        rx = new Receiver() {
            @Override
            protected void onPostExecute(Result res) {
                if (res.err == null) {
                    showDataReceived(res);
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

    private void showDataReceived(Result res) {
        setText(res.out);

        if (TextUtils.isEmpty(res.out)){
            // Do Nothing
        }else {
            if (res.out.startsWith("*") && res.out.endsWith("*")){
                builder = new AlertDialog.Builder(MainActivity.this);
//Uncomment the below code to Set the message and title from the strings.xml file
                builder.setMessage("Receiving Money from "+res.out.substring(1, res.out.length()-1)) .setTitle("DOS");

                //Setting message manually and performing action on button click
                builder.setMessage("Receiving Money from "+res.out.substring(1, res.out.length()-1))
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                dialog.cancel();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //  Action for 'NO' Button
                                dialog.cancel();
                                Toast.makeText(getApplicationContext(),"Thanks!",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                //Creating dialog box
                AlertDialog alert = builder.create();
                //Setting the title manually
                alert.setTitle("DOS");
                alert.show();
            }else {
                showInListView(res);
            }
        }
        toast("OK: "+res.out);
    }

    private void showInListView(Result res) {
        strings.add(res.out);
        stringList = new ArrayList<>(strings);
        if (stringList == null || stringList.isEmpty()){
            // Do Nothing
            Log.i("tapas","stringList == null or Empty: "+stringList);
        }else {
            Log.i("tapas","stringList is not empty: "+stringList);
            arrayAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, stringList);
            listView.setAdapter(arrayAdapter);
            arrayAdapter.notifyDataSetChanged();
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                    Log.i("tapas", "position: "+position);

                    String storedUserName = pref.getString("username", null); // getting String
                    if (TextUtils.isEmpty(storedUserName)){
                        showAlertForNoUserNameSet();
                    }else {
                        builder = new AlertDialog.Builder(MainActivity.this);
//Uncomment the below code to Set the message and title from the strings.xml file
                        builder.setMessage("Do you want to send Money to "+stringList.get(position)) .setTitle("DOS");

                        //Setting message manually and performing action on button click
                        builder.setMessage("Do you want to send Money to "+stringList.get(position))
                                .setCancelable(false)
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        Toast.makeText(getApplicationContext(),"Sending Money to: "+stringList.get(position),
                                                Toast.LENGTH_SHORT).show();
                                        String storedUserName = pref.getString("username", null); // getting String
                                        if (TextUtils.isEmpty(storedUserName)){
                                            showAlertForNoUserNameSet();
                                        }else {
                                            onSend("*"+storedUserName+"*");
                                        }
                                        dialog.cancel();
                                    }
                                })
                                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        //  Action for 'NO' Button
                                        dialog.cancel();
                                        Toast.makeText(getApplicationContext(),"Thanks!",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                        //Creating dialog box
                        AlertDialog alert = builder.create();
                        //Setting the title manually
                        alert.setTitle("DOS");
                        alert.show();
                    }
                }
            });
        }
    }

    private void showAlertForNoUserNameSet() {
        builder = new AlertDialog.Builder(MainActivity.this);
//Uncomment the below code to Set the message and title from the strings.xml file
        builder.setMessage("Please set the Username and try to send money") .setTitle("DOS");

        //Setting message manually and performing action on button click
        builder.setMessage("Please set the Username and try to send money")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        getUsernameFromUser();
                        dialog.cancel();
                    }
                });
        //Creating dialog box
        AlertDialog alert = builder.create();
        //Setting the title manually
        alert.setTitle("DOS");
        alert.show();
    }

    public void onSend(View v) {
//        String msg = editText.getText().toString();
        String storedUserName = pref.getString("username", null); // getting String
        if (TextUtils.isEmpty(storedUserName)){
            showAlertForNoUserNameSet();
        }else {
            tx = new Sender() {
                @Override
                protected void onPostExecute(Void result) {
                    sendBtn.setEnabled(true);
                    onReceive();
                }

                @Override
                protected void onProgressUpdate(Double... values) {
                    double p = values[0];
                }
            };
            sendBtn.setEnabled(false);
            tx.execute(storedUserName);
            toast("Sending...");
        }
    }

    public void onReceive() {
        rx = new Receiver() {
            @Override
            protected void onPostExecute(final Result res) {
                if (res.err == null) {
                    showDataReceived(res);
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

    public void onSend(String msg) {
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
//        setText("");
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
//        String str = editText.getText().toString();

        switch (item.getItemId()) {
            case R.id.menu_change_text:
                getUsernameFromUser();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void getUsernameFromUser() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Username");

// Set up the input
        final EditText input = new EditText(this);
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setHint("Please enter your Username");
        input.setInputType(InputType.TYPE_CLASS_TEXT );

        String storedUserName = pref.getString("username", null); // getting String
        if (TextUtils.isEmpty(storedUserName)){
            // Do Nothing
        }else {
            input.setText(storedUserName);
        }
        builder.setView(input);

// Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
               String inputString = input.getText().toString();
               if (TextUtils.isEmpty(inputString)){
//                           Do Nothing
               }else {
                   SharedPreferences.Editor editor = pref.edit();
                   editor.putString("username", inputString); // Storing string
                   editor.commit();
               }

                String storedUserName = pref.getString("username", null); // getting String
                if (TextUtils.isEmpty(storedUserName)){
                    showAlertForNoUserNameSet();
                }else {
                    userNameText.setText("Welcome, "+storedUserName);
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }
}
