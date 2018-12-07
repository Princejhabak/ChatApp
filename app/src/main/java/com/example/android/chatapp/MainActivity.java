package com.example.android.chatapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.text.method.KeyListener;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.text.format.DateFormat;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private int SIGN_IN_REQUEST_CODE = 1;
    private FirebaseListAdapter<ChatMessages> adapter;
    private FloatingActionButton fab;

    private static final String TAG = "MainActivity";

    public static final String ANONYMOUS = "anonymous";
    public static final int DEFAULT_MSG_LENGTH_LIMIT = 1000;

    public static final int RC_SIGN_IN = 1;
    private static final int RC_PHOTO_PICKER = 2;

    private ListView mMessageListView;
    private MessageAdapter mMessageAdapter;
    private EditText inputEditText;
    private ProgressDialog dialog;

    // Firebase instance variables
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mMessagesDatabaseReference;
    private ChildEventListener mChildEventListener;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mChatPhotosStorageReference;

    private KeyListener originalKeyListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().setBackgroundDrawableResource(R.drawable.chat_background);

        // Initialize Firebase components
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseStorage = FirebaseStorage.getInstance();

        mMessagesDatabaseReference = mFirebaseDatabase.getReference().child("chatApp");
        mChatPhotosStorageReference = mFirebaseStorage.getReference().child("chat_photos");

        mMessageListView = (ListView) findViewById(R.id.list_of_message);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        inputEditText = (EditText) findViewById(R.id.et_message);

        dialog = new ProgressDialog(MainActivity.this);

        // Initialize message ListView and its adapter
        List<ChatMessages> friendlyMessages = new ArrayList<>();
        mMessageAdapter = new MessageAdapter(this, R.layout.chat_item, friendlyMessages);
        mMessageListView.setAdapter(mMessageAdapter);


        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        }
       /* else{
            // Display a toast
            displayChatMessage();
        }*/
        originalKeyListener = inputEditText.getKeyListener();
       hideKeyBoard();

        inputEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_BOTTOM = 3;

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (inputEditText.getRight() - inputEditText.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        // your action here
                      /*  Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setType("image/jpeg");
                        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                        startActivityForResult(Intent.createChooser(intent, "Complete action using"), RC_PHOTO_PICKER);*/

                        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(Intent.createChooser(intent, "Complete action using"), RC_PHOTO_PICKER);

                        return true;
                    }
                }
                return false;
            }


        });

        // Enable Send button when there's text to send
        inputEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    fab.setEnabled(true);
                } else {
                    fab.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        inputEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(DEFAULT_MSG_LENGTH_LIMIT)});


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               /* EditText inputEditText = (EditText)findViewById(R.id.et_message);
                FirebaseDatabase.getInstance().getReference().push().setValue(new ChatMessages(inputEditText.getText().toString()
                        ,new Date().getTime()));

                inputEditText.setText("");*/

                ChatMessages friendlyMessage = new ChatMessages(inputEditText.getText().toString(), new Date().getTime(), null);
                mMessagesDatabaseReference.child("messages").push().setValue(friendlyMessage);

                // Clear input box
                inputEditText.setText("");
            }
        });

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    onSignedInInitialize(user.getDisplayName());
                } else {
                    // User is signed out
                    onSignedOutCleanup();
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));

                }
            }
        };


        mMessageListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) {
                // ChatMessages messages = mMessageAdapter.getItem(position);

                // mMessageListView.setSelection(position);

                view.setBackgroundColor(getResources().getColor(R.color.select));

                //Toast.makeText(MainActivity.this, (String.valueOf( messages.getMessageTime())),Toast.LENGTH_SHORT).show();
                return false;
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                // Sign-in succeeded, set up the UI
                Toast.makeText(this, "Signed in!", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                // Sign in was canceled by the user, finish the activity
                Toast.makeText(this, "Sign in canceled", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else if (requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK) {
            Uri selectedImageUri = data.getData();

            // Get a reference to store file at chat_photos/<FILENAME>
            StorageReference photoRef = mChatPhotosStorageReference.child(selectedImageUri.getLastPathSegment());

            // Upload file to Firebase Storage
            photoRef.putFile(selectedImageUri)
                    .addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // When the image has successfully uploaded, we get its download URL
                            Uri downloadUrl = taskSnapshot.getDownloadUrl();

                            // Set the download URL to the message box, so that the user can send it to the database
                            ChatMessages friendlyMessage = new ChatMessages(null, new Date().getTime(), downloadUrl.toString());
                            mMessagesDatabaseReference.child("messages").push().setValue(friendlyMessage);
                            //
                            dialog.dismiss();
                        }
                    }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                    /*mProgressBar.setVisibility(ProgressBar.VISIBLE);
                    mProgressBar.setMax(100);
                    mProgressBar.setProgress((int)(taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount())*100);*/

                    dialog.setMessage("Sending...");
                    dialog.show();


                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.menu_sign_out:
                mFirebaseAuth.signOut();
                finish();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                break;

        }
        return super.onOptionsItemSelected(item);
    }

   /* private void displayChatMessage(){

        adapter = new FirebaseListAdapter<ChatMessages>(this,ChatMessages.class,R.layout.chat_item,FirebaseDatabase.getInstance().getReference()) {
            @Override
            protected void populateView(View v, ChatMessages model, int position) {
                TextView messageText , messageTime;
                messageText = (TextView)v.findViewById(R.id.chat_text);
                messageTime = (TextView)v.findViewById(R.id.chat_time);

                messageText.setText(model.getMessageText());
                messageTime.setText(DateFormat.format("HH:mm",model.getMessageTime()));
            }
        };
        listView.setAdapter(adapter);
    }*/


    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
        //mMessageAdapter.clear();
        //detachDatabaseReadListener();
    }


    private void onSignedInInitialize(String username) {
        //mUsername = username;
        attachDatabaseReadListener();
        saveUserInfo();
    }

    private void onSignedOutCleanup() {
        // mUsername = ANONYMOUS;
        mMessageAdapter.clear();
        detachDatabaseReadListener();
    }

    private void attachDatabaseReadListener() {
        if (mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    ChatMessages friendlyMessage = dataSnapshot.getValue(ChatMessages.class);
                    mMessageAdapter.add(friendlyMessage);
                }

                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                }

                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    ChatMessages friendlyMessage = dataSnapshot.getValue(ChatMessages.class);
                    mMessageAdapter.remove(friendlyMessage);
                }

                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                }

                public void onCancelled(DatabaseError databaseError) {
                }
            };
            mMessagesDatabaseReference.child("messages").addChildEventListener(mChildEventListener);
        }
    }

    private void detachDatabaseReadListener() {
        if (mChildEventListener != null) {
            mMessagesDatabaseReference.child("messages").removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
    }

    private void saveUserInfo() {

        DatabaseReference databaseReference;
        databaseReference = mFirebaseDatabase.getReference().child("chatApp");

        FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();

       /* String name = firebaseUser.getDisplayName().toString().trim();
        String email = firebaseUser.getEmail().toString().trim();*/


        UserInformation userInformation = new UserInformation(firebaseUser.getDisplayName(), firebaseUser.getEmail(), null);

        mMessagesDatabaseReference.child("users").child(firebaseUser.getUid()).setValue(userInformation);
    }

    private void hideKeyBoard() {

        // Restore key listener - this will make the field editable again.
        inputEditText.setKeyListener(originalKeyListener);

        // Focus the field.
        inputEditText.requestFocus();

        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(inputEditText.getWindowToken(), 0);
    }


}
