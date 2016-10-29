package com.evgenyvyaz.herlok;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.angads25.filepicker.controller.DialogSelectionListener;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;


public class MainActivity extends AppCompatActivity {

    private boolean hasMessage = true;
    private int PERMISSION_REQUEST = 1;
    private TextView messageTV;
    private Button fileBTN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fileBTN = (Button) findViewById(R.id.fileBTN);
        messageTV = (TextView) findViewById(R.id.messageTV);
        fileBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialogPicker();
            }
        });
        askForPhoneStatePermission();

    }

    public String decodeWavFile(final InputStream inputStream) throws IOException {
        String character = "";
        String message = "";
        int count = 0;
        inputStream.skip(44);
        while (hasMessage) {
            character += inputStream.read() % 2;
            if (character.length() == 5) {
                String stepChar = String.format("%c", Integer.parseInt(character, 2) + 'а');
                message += stepChar;
                if (stepChar.equals("а")) {
                    count++;
                } else {
                    count = 0;
                }
                character = "";
            }
            if (count >= 3) {
              hasMessage = false;
            }
            inputStream.read();
        }
        return message.split("ааа")[0];
    }

    private void showDialogPicker() {
        DialogProperties properties = new DialogProperties();
        properties.selection_mode = DialogConfigs.SINGLE_MODE;
        properties.selection_type = DialogConfigs.FILE_SELECT;
        properties.root = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getAbsolutePath());
        properties.extensions = null;
        FilePickerDialog dialog = new FilePickerDialog(MainActivity.this, properties);

        dialog.setDialogSelectionListener(new DialogSelectionListener() {
            @Override
            public void onSelectedFilePaths(String[] files) {
                //files is the array of the paths of files selected by the Application User.
                if (files.length > 0 && files[0].contains(".wav")) {
                    InputStream inputStream = null;
                    try {
                        inputStream = new FileInputStream(files[0]);
                        messageTV.setText(decodeWavFile(inputStream));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else {
                    Toast.makeText(getApplicationContext(), "Выберите .wav файл", Toast.LENGTH_LONG).show();
                }


            }
        });
        dialog.show();
    }

    public void askForPhoneStatePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED

                    ) {

                // Should we show an explanation?
                if (
                        ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)

                        ) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("write external storage access needed");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setMessage("please confirm write external storage");//TODO put real question
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @TargetApi(Build.VERSION_CODES.M)
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            requestPermissions(
                                    new String[]
                                            {Manifest.permission.WRITE_EXTERNAL_STORAGE}
                                    , PERMISSION_REQUEST);
                        }
                    });
                    builder.show();
                    // Show an expanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.

                } else {

                    // No explanation needed, we can request the permission.

                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            PERMISSION_REQUEST);

                    // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                }
            } else {

                showDialogPicker();

            }
        } else {
            showDialogPicker();

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST) {

            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                showDialogPicker();
                //   startApp();
                // permission was granted, yay! Do the
                // contacts-related task you need to do.

            } else {
                Toast.makeText(this, "No permission for write external storage", Toast.LENGTH_LONG).show();// ToastMaster.showMessage(MainActivity.this,"No permission for contacts");
                // permission denied, boo! Disable the
                // functionality that depends on this permission.
            }
            return;


            // other 'case' lines to check for other
            // permissions this app might request
        }
    }


}
