package com.example.chat_app;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;




public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    // List of permissions needed for the program, add new ones as needed
    //See checkPermissions method below
    private final String[] PERMISSIONS = {Manifest.permission.CAMERA};
    private final int[] PERMISSION_CODES = {1};
    public static boolean createOn = true;
    private static FirebaseAuth mAuth = FirebaseAuth.getInstance();


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseUser currUser = mAuth.getCurrentUser();

        if (currUser == null) {
            setContentView(R.layout.create_account);
        } else {
            setContentView(R.layout.activity_main_page);
        }
        //displayMessages = (TextView) findViewById(R.id.textdisplay);


        //FirebaseAuth.getInstance().signOut();


        //Toast.makeText(this, currUser.getEmail(), Toast.LENGTH_LONG).show();
        if (currUser == null) {
            setContentView(R.layout.create_account);
            EditText name = findViewById(R.id.name);
            EditText email = findViewById(R.id.email);
            EditText password = findViewById(R.id.password);
            password.getText().toString();
            Button create = findViewById(R.id.create);
            TextView bottomText = findViewById(R.id.textView4);
            Button signIn = findViewById(R.id.button4);

            create.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    String userEmail = email.getText().toString();
                    String userPassword = password.getText().toString();
                    if (createOn) {
                        mAuth.createUserWithEmailAndPassword(userEmail, userPassword);

                    }
                    startSignIn(userEmail, userPassword);
                    //updateDisplay();
                }
            });

            signIn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    changeState();
                    if (createOn) {
                        create.setText("Create Account");
                        bottomText.setText("Already Have An Account?");
                        name.setVisibility(View.VISIBLE);
                        signIn.setText("Sign In");
                    } else {
                        create.setText("Sign In");
                        bottomText.setText("Need An Account?");
                        name.setVisibility(View.GONE);
                        signIn.setText("Create An Account");
                    }
                }
            });

        } else {
            startActivity(new Intent(this, MainPage.class));
        }

    }



    public static boolean changeState() {
        createOn = !createOn;
        return createOn;
    }


    private void startSignIn(String email, String password) {

        Context con = this;

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Success", Toast.LENGTH_LONG).show();
                            System.out.println("GoodDay: Success");
                            //setContentView(R.layout.activity_main);
                            //Do Success Thing

                            Intent intent = new Intent(con, MainPage.class);
                            startActivity(intent);

                        } else {
                            Toast.makeText(MainActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            System.out.println("GoodDay: Failure");
                            //Do Failure Thing
                        }
                    }
                });
    }


    private final int CAMERA_CODE = 1;

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Camera Permission Granted", Toast.LENGTH_LONG).show();
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_CODE);
            } else {
                Toast.makeText(this, "Camera Permission Denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void userSelect() throws Exception {
        final String[] userOptions = {"Use Camera", "Choose From Phone", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Upload Profile Photo");
        builder.setItems(userOptions, new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(PERMISSIONS, PERMISSION_CODES[0]);
                } else {
                    if (userOptions[i].equals("Use Camera")) {
                        // Open Camera
                        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivity(cameraIntent);

                    } else if (userOptions[i].equals("Choose From Phone")) {
                        Intent picker = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivity(picker);

                    } else {
                        dialogInterface.dismiss();
                    }
                }
            }
        });
        builder.show();
    }


    public static String getUID() {
        String s = mAuth.getCurrentUser().getUid();
        if (s == null) {
            return "";
        } else {
            return s;
        }
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            ContextWrapper cw = new ContextWrapper(getApplicationContext());

        }

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkPermissions() {
        for (int i = 0; i < PERMISSIONS.length; i++) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(PERMISSIONS, PERMISSION_CODES[i]);
            }
        }
    }

}