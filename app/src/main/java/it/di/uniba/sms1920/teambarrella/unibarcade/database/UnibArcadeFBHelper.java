package it.di.uniba.sms1920.teambarrella.unibarcade.database;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;

public class UnibArcadeFBHelper {

    private static final String TAG = "UnibArcadeFBHelper";

    private FirebaseDatabase db = FirebaseDatabase.getInstance();

    private DatabaseReference mRef;

    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    private String nick;

    public FirebaseUser getUser() {
        return user;
    }

    // Insert score on Firebase
    public void insertNewScore(Score score) {
        mRef = db.getReference("scores");

        //userId is the uid of current user
        String userId = score.getUserId();

        // check no user logged or registered
        if (!userId.equals("Guest")) {
            nick = user.getDisplayName();
            String gameId = score.getGameId();
            Map<String, Object> gameData = new HashMap<>();
            gameData.put(gameId, score.getScore());

            mRef.child("users")
                    .child(userId)
                    .child("nickname")
                    .child(nick)
                    .child("game")
                    .updateChildren(gameData);
        } else {
            nick = "Guest";
        }

        mRef.keepSynced(true);
    }

    private String setNick() {
        if (user == null)
            return "Guest";
        else
            return user.getDisplayName();
    }

    public void updateScore(final Score score) {
        String userId = score.getUserId();
        final String gameId = score.getGameId();
        final String nick = setNick();
        final String newScore = score.getScore();

        final String path = "scores/users/" + userId + "/nickname/" + nick + "/game/" + gameId;

        mRef = db.getReference(path);

        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String oldScore = dataSnapshot.getValue(String.class);

                    if (Integer.valueOf(oldScore) < Integer.valueOf(newScore)) {
                        Log.d(TAG, "Old score: " + Integer.valueOf(oldScore));
                        mRef.setValue(newScore);
                    }
                } else {
                    insertNewScore(score);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        mRef.keepSynced(true);
    }

    // Check user session and userId == null
    public String checkUserSession() {
        String userId;
        if (user != null) {
            userId = user.getUid();
            Log.d(TAG, "User logged: " + user.getUid() + " " + user.getEmail());
        } else {
            Log.d(TAG, "No user logged: ");
            userId = "Guest";
        }
        return userId;
    }

    //Check user exists
    public boolean userExists() {
        return user != null ? true : false;
    }
}
