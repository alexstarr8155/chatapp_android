package com.example.chat_app;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.ui.AppBarConfiguration;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static android.content.ContentValues.TAG;

public class ChatDisplay extends AppCompatActivity {

    private static FirebaseFirestore fdb;

    private static TextView displayMessages;
    private static EditText enterMessages;
    private static Button sendMessage;
    private static ImageView sender_photo;
    private AppBarConfiguration mAppBarConfiguration;


    public static ArrayList<Message> list;
    private ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_display);

        fdb = FirebaseFirestore.getInstance();

        enterMessages = (EditText) findViewById(R.id.textbox);
        sendMessage = (Button) findViewById(R.id.sendButton);
        mListView = (ListView) findViewById(R.id.textdisplay);
        list = new ArrayList<>();

        mListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        mListView.setStackFromBottom(true);

        CustomListAdapter adapter = new CustomListAdapter(this, R.layout.message_display, list);
        mListView.setAdapter(adapter);

        Context con = this;

        String myUid = FirebaseAuth.getInstance().getUid();

        Bundle extras = getIntent().getExtras();

        String otherUid = extras.getString("otherUid");
        String otherURL = extras.getString("photoURL");

        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (enterMessages.getText().toString().equals("Sign Out")) {
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(con, MainActivity.class));
                } else {
                    if (otherUid.length() == 0) {
                        sendMessage();
                    } else {
                        sendMessage(myUid, otherUid);
                    }
                }
            }
        });

        if (otherUid.length() != 0) {


            fdb.collection("new_messages/rooms/" + myUid + "," + otherUid).addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                    if (error != null) {
                        return;
                    }

                    //Do not know how we are storing personal photo, temp for now
                    String tempURL = "https://www.biography.com/.image/ar_1:1%2Cc_fill%2Ccs_srgb%2Cg_face%2Cq_auto:good%2Cw_300/MTc2Njk4NDEwOTMyMzgxNjc1/margaret-thatcher_500x500_gettyimages-108932085.jpg";

                    for (QueryDocumentSnapshot doc : value) {
                        Message temp = doc.toObject(Message.class);
                        temp.photoURL = tempURL;
                        temp.uid = myUid;
                        list.add(temp);
                    }

                    Collections.sort(list, new MessageComparator());
                    updateDisplay();

                }
            });

            fdb.collection("new_messages/rooms/" + otherUid + "," + myUid).addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                    if (error != null) {
                        return;
                    }

                    for (QueryDocumentSnapshot doc : value) {
                        Message temp = doc.toObject(Message.class);
                        temp.photoURL = otherURL;
                        temp.uid = otherUid;
                        list.add(temp);
                    }

                    Collections.sort(list, new MessageComparator());
                    updateDisplay();

                }
            });

        } else {

            fdb.collection("messages").addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                    if (error != null) {
                        Log.w(TAG, "Listen failed.", error);
                        return;
                    }

                    list.clear();

                    for (QueryDocumentSnapshot doc : value) {
                        list.add(doc.toObject(Message.class));
                    }
                    Collections.sort(list, new MessageComparator());
                    updateDisplay();
                }
            });
        }
    }

    public void updateDisplay() {

        CustomListAdapter adapter = new CustomListAdapter(this, R.layout.message_display, list);

        mListView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        mListView.setAdapter(adapter);

    }

    public void getMessagesOnDB() {

        //MainActivity.displayMessages.append("Running" + '\n');
        fdb.collection("messages")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if (task.isSuccessful()) {
                            if (task.getResult() != null) {
                                List<Message> readData = task.getResult().toObjects(Message.class);
                                list.clear();
                                list.addAll(readData);
                                Collections.sort(list);
                            }
                        } else {
                            Log.w(TAG, "Goofed");
                        }
                    }
                });
    }

    public void processMessages(Message m) {

        list.add(m);
        updateDisplay();

    }

    public void sendMessage() {

        String text = enterMessages.getText().toString();
        enterMessages.setText("");

        String url = "https://www.borgenmagazine.com/wp-content/uploads/2013/09/george-bush-eating-corn.jpg";
        String url1 = "https://images-wixmp-ed30a86b8c4ca887773594c2.wixmp.com/f/b87d2841-ca20-4e28-819f-ac43f7bfe8ea/de4ezgs-274b3117-50ed-4073-9c8f-4ac1d9cc67dd.jpg?token=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1cm46YXBwOiIsImlzcyI6InVybjphcHA6Iiwib2JqIjpbW3sicGF0aCI6IlwvZlwvYjg3ZDI4NDEtY2EyMC00ZTI4LTgxOWYtYWM0M2Y3YmZlOGVhXC9kZTRlemdzLTI3NGIzMTE3LTUwZWQtNDA3My05YzhmLTRhYzFkOWNjNjdkZC5qcGcifV1dLCJhdWQiOlsidXJuOnNlcnZpY2U6ZmlsZS5kb3dubG9hZCJdfQ.v3QBzdLRI8ZT4R5JiYQPFxIxTIHEu7qMDa8N_DBMDn0";


        fdb.collection("messages").add(new Message(new Date(), text, url1, MainActivity.getUID()));

    }

    public void sendMessage(String myUID, String otherUID) {
        String text = enterMessages.getText().toString();
        enterMessages.setText("");

        //Temp personal profile photo, where is it being stored?
        String tempURL = "https://www.biography.com/.image/ar_1:1%2Cc_fill%2Ccs_srgb%2Cg_face%2Cq_auto:good%2Cw_300/MTc2Njk4NDEwOTMyMzgxNjc1/margaret-thatcher_500x500_gettyimages-108932085.jpg";

        fdb.collection("new_messages/" + myUID + "," + otherUID + "/rooms").add(new Message(new Date(), text, tempURL, myUID));

    }
}