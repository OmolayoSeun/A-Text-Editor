package com.omolayoseun.atexteditor;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@SuppressWarnings("deprecation")
public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_WRITE_CODE = 123;
    private final EditTextFragment editTextFragment = new EditTextFragment();
    private final TextViewFragment textViewFragment = new TextViewFragment();
    Button btn_save, btn_clear, btn_rename, btn_view;
    private static byte b = 0;
    private StringBuilder text = new StringBuilder();
    private boolean grantedPermission = false;
    private String path;
    private boolean isFromFile = false;
    private boolean saveNotSuccessful = false;
    private StringBuilder initialText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_save = findViewById(R.id.btn_save);
        btn_clear = findViewById(R.id.btn_clear);
        btn_rename = findViewById(R.id.btn_rename);
        btn_view = findViewById(R.id.btn_view);
        switchView(null);

        if (!checkPermission()) requestPermission();
        else grantedPermission = true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.newf:
                text = new StringBuilder();
                path = null;
                isFromFile = false;
                b = 0;
                switchView(null);
                editTextFragment.refresh(text);
                setTitle("A Text Editor");
                break;
            case R.id.openf:
                getFile();
                break;
            case R.id.sharef:
                text = new StringBuilder(editTextFragment.getString());

                // TODO
                //  a) Try to make save finish running before the if (!saveNotSuccessful) is called
                //  b) Try to create a good condition for text to be checked against initialText in order to
                //  know when to save before sharing.
                if (text != initialText){
                    save(null);
                }
                if (!saveNotSuccessful){
                    String[] ab = path.split(":");
                    File f = new File(Environment.getExternalStorageDirectory() + "/" + ab[1]);
                    Uri uri = Uri.fromFile(f);
                    Intent shareIntent = new Intent();
                    shareIntent.setAction(Intent.ACTION_SEND);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        shareIntent.putExtra(Intent.EXTRA_FROM_STORAGE, uri);
                    }
                    else shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                    shareIntent.setType("*/*");
                    startActivity(Intent.createChooser(shareIntent, null));
                }

                break;
            case R.id.setting:
                startActivityForResult(new Intent(MainActivity.this, Setting.class), 0);
                break;
            case R.id.exit:
                finish();
                break;
        }
        return true;
    }


    public void save(View view) {

        text = new StringBuilder(editTextFragment.getString());
        if (isFromFile) {
            String[] ab = path.split(":");

            File f = new File(Environment.getExternalStorageDirectory() + "/" + ab[1]);

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setCancelable(false);
            builder.setMessage("Do you want to overwrite this file?");
            builder.setIcon(R.drawable.ic_baseline_info);
            builder.setTitle("Overwrite file");
            builder.setNegativeButton("Cancel", (dialogInterface, i) -> {
                toast("Failed to save file");
                dialogInterface.dismiss();
            });
            builder.setPositiveButton("Ok", (dialogInterface, i) -> {
                try {
                    FileWriter writer = new FileWriter(f);
                    writer.write(text.toString());
                    writer.close();
                    saveNotSuccessful = false;
                    initialText = text;
                    toast("Saved successfully!");
                } catch (Exception e) {
                    toast("Failed to save file, check permission");
                    saveNotSuccessful = true;
                    e.printStackTrace();
                }
                dialogInterface.dismiss();
            });

            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        } else {
            File f = new File(Tools.folderDir);
            if (!f.exists()) if (!f.mkdir()) return;

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            LayoutInflater inf = getLayoutInflater();
            View v = inf.inflate(R.layout.alert_layout_edit, null);
            builder.setView(v);
            EditText et = v.findViewById(R.id.edit_alert_name);
            builder.setIcon(R.drawable.ic_baseline_info);
            builder.setTitle("Filename");
            builder.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss());
            builder.setPositiveButton("Ok", (dialogInterface, i) -> {

                String fileName = et.getText().toString();
                if (fileName.equals("") || fileName.equals(" ") || fileName.equals("  "))
                    fileName = "Untitled.txt";
                File f1 = new File(Tools.folderDir + fileName);

                if (f1.exists() && fileName.startsWith("Untitled")) {
                    int count = 1;
                    while (true) {
                        fileName = "Untitled" + count + ".txt";
                        f1 = new File(Tools.folderDir + fileName);
                        if (f1.exists()) {
                            count++;
                        } else break;
                    }
                }
                else if (f1.exists()) {
                    toast("File name exist");
                    saveNotSuccessful = true;
                    return;
                }


                try {
                    if (!f1.createNewFile()) return;

                    FileWriter writer = new FileWriter(f1);
                    writer.write(text.toString());
                    writer.close();
                    path = Tools.simpleDir + fileName;
                    isFromFile = true;
                    setTitle(fileName);
                    saveNotSuccessful = false;
                    initialText = text;
                    toast("Saved Successfully!");
                } catch (IOException e) {
                    saveNotSuccessful = true;
                    toast("Failed to save file, check permission");
                    e.printStackTrace();
                }

            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
    }

    public void toast(String msg) {
        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
    }

    public void clear(View view) {
        if (b == 1) {
            text = new StringBuilder();
            editTextFragment.refresh(text);
            // complete clearing text here
        }
    }

    public void rename(View view) {
        // prepare alert dialog to edit text.
    }

    public void switchView(View view) {
        switch (b) {
            case 0:
                editTextFragment.setString(text);
                getFragmentManager().beginTransaction().remove(textViewFragment).remove(editTextFragment).add(R.id.frame, editTextFragment).commit();
                btn_view.setBackgroundResource(R.drawable.ic_baseline_edit);
                b = 1;
                break;
            case 1:
                text = new StringBuilder(editTextFragment.getString());
                textViewFragment.setString(text);
                getFragmentManager().beginTransaction().remove(editTextFragment).add(R.id.frame, textViewFragment).commit();
                btn_view.setBackgroundResource(R.drawable.ic_baseline_remove_red_eye);
                //
                b = 0;
                break;
        }
    }

    private void getFile() {
        if (grantedPermission) {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            //noinspection deprecation
            startActivityForResult(intent, 10);
        } else {
            requestPermission();
            grantPermission();
            if (grantedPermission) getFile();
            toast("Can't read files because permission was not granted");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //TODO work here
        toast("request: " + requestCode + "\nresult: " + resultCode);
        if (resultCode == -1) {
            assert data != null;
            Uri content_describer = data.getData();
            BufferedReader reader;
            path = content_describer.getPath();
            try {
                // open the user-picked file for reading:
                InputStream in = getContentResolver().openInputStream(content_describer);
                // now read the content:
                reader = new BufferedReader(new InputStreamReader(in));
                String line;
                StringBuilder builder = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    builder.append(line).append('\n');
                }
                text = builder;
                initialText = builder;
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            isFromFile = true;
            int a = path.lastIndexOf('/') + 1;
            setTitle(path.substring(a));
            switch (b) {
                case 1:
                    editTextFragment.refresh(text);
                    break;
                case 0:
                    textViewFragment.refresh(text);
                    break;
            }
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    // Permission section
    private void grantPermission() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            grantedPermission = checkPermission();
            if (!grantedPermission) {
                toast("Grant permission else app won't work");
                requestPermission();
            }
        }
    }

    private boolean checkPermission() {
        int result;
        result = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            toast("Write External Storage permission allows us to writes files. Please allow this permission in App Settings.");
        } else {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_WRITE_CODE);
        }
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_WRITE_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                toast("Granted permission");
            } else {
                toast("Permission failed");
            }
        }
    }

}