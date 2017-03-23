package nohorjo.application;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toolbar.LayoutParams;

import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Locale;

import nohorjo.output.FileOut;
import nohorjo.roar.R;
import nohorjo.roar.RoarComms;
import nohorjo.settings.SettingsException;
import nohorjo.settings.SettingsManager;

public class MainActivity extends Activity {
    private TextView out;
    private ScrollView oc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {

            @Override
            public void uncaughtException(Thread thread, Throwable ex) {
                FileOut.printStackTrace(ex);
            }
        });
        super.onCreate(savedInstanceState);
        App.setContext(this);
        setContentView(R.layout.activity_main);
        try {
            App.getPhoneNumber();
        } catch (SettingsException e) {
            setPhoneNumberDialog();
        }
        prepareWidgets();
    }

    private void setPhoneNumberDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
        final EditText input = new EditText(MainActivity.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        alertDialog.setView(input);

        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String number = input.getText().toString();
                SettingsManager.setSetting(SettingsManager.PHONE_NUMBER, number);
            }
        });

        alertDialog.show();
    }

    protected void openLocation(Float latitude, Float longitude) {
        String uri = String.format(Locale.ENGLISH, "geo:%f,%f", latitude, longitude);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        startActivity(intent);
    }

    public void addButton(int parentID, String label, OnClickListener onClick) {
        ViewGroup vg = (ViewGroup) findViewById(parentID);
        Button b = new Button(this);
        b.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        b.setText(label);
        b.setOnClickListener(onClick);
        vg.addView(b);
    }

    public void clear(int parentID) {
        ViewGroup vg = (ViewGroup) findViewById(parentID);
        vg.removeAllViews();
    }

    private void prepareWidgets() {
        try {

            ((TextView) findViewById(R.id.versionText))
                    .setText("Version: " + getPackageManager().getPackageInfo(getPackageName(), 0).versionCode);
        } catch (Exception e) {
            FileOut.printStackTrace(e);
        }
        {
            oc = (ScrollView) findViewById(R.id.oc);
            out = (TextView) oc.getChildAt(0);
            new Thread() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        String outText = out.getText().toString();
                                        String updatedText = FileOut.getLastNLines(App.getLinesInOut());
                                        if (!updatedText.equals(outText)) {
                                            out.setText(updatedText);
                                            oc.scrollTo(0, oc.getBottom());
                                        }
                                    } catch (IOException e) {
                                        FileOut.printStackTrace(e);
                                    }
                                }
                            });
                            Thread.sleep(500);
                        } catch (Exception e) {
                        }
                    }
                }

                ;
            }.start();
        }
    }

    public void sendTailServer(View view) {
        new RoarComms().printTail();
    }

    public void sendTrackRequest(View view) {
        try {
            final int count = Integer.parseInt(((EditText) findViewById(R.id.locationCount)).getText().toString());
            new RoarComms().getLocations(count, this);
        } catch (NumberFormatException e) {
            FileOut.println("Enter number!");
        }
    }

    public void sendMessage(View view) {

        final EditText messageBox = (EditText) findViewById(R.id.messageBox);
        new Thread() {
            @Override
            public void run() {
                try {
                    new RoarComms().forwardMessage(messageBox.getText().toString());
                } catch (Exception e) {
                    FileOut.printStackTrace(e);
                }
            }

            ;
        }.start();
    }

    public void clearLog(View view) {
        FileOut.clear();
    }
}
