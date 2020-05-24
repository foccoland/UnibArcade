package it.di.uniba.sms1920.teambarrella.unibarcade.ui.main.scorefragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.ListFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import it.di.uniba.sms1920.teambarrella.unibarcade.R;

public class CannonballScoreFragment extends ListFragment {

    private ArrayList<String> nicknames;
    private ArrayList<Integer> scores;
    private TreeMap<Integer, String> sortedScores;

    private int textColor;
    private final String TAG = "CannonballScoreFragment";

    private View scoreView;
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference mRef;
    private ListView cannonballScoresList;

    public CannonballScoreFragment() {}

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        cannonballScoresList = getListView();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause called");
    }

    @Override
    public void onStart() {
        super.onStart();


        nicknames = new ArrayList<>();
        scores = new ArrayList<>();
        sortedScores = new TreeMap<>(Collections.<Integer>reverseOrder());

        textColor = getActivity().getResources().getColor(R.color.colorAccentCannonball, null);

        mRef = db.getReference("scores/users");

        final GenericTypeIndicator<Map<String, Object>> scoreType = new GenericTypeIndicator<Map<String, Object>>() {
            @Override
            public int hashCode() {
                return super.hashCode();
            }
        };


        mRef.addChildEventListener(new ChildEventListener() {
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                Map<String,Object> users, nickname;

                if (dataSnapshot.exists()) {

                    users = dataSnapshot.getValue(scoreType);
                    nickname = (Map<String,Object>) users.get("nickname");

                    for (String nick : nickname.keySet()) {

                        String score = dataSnapshot.child("nickname/" + nick + "/game/4").getValue(String.class);

                        if (score != null) {
                            nicknames.add(nick);
                            scores.add(Integer.valueOf(score));
                            sortedScores.put(Integer.valueOf(score), nick);
                        }
                    }
                }

                Log.d(TAG, "sortedScores: " + sortedScores.toString());

                nicknames.clear();
                scores.clear();

                scores = new ArrayList<>(sortedScores.keySet());
                nicknames = new ArrayList<>(sortedScores.values());

                Log.d(TAG, "scores: " + scores.toString());
                Log.d(TAG, "nicknames: " + nicknames.toString());

                ScoreArrayAdapter scoreArrayAdapter =
                        new ScoreArrayAdapter(getActivity(), R.layout.item_score, nicknames, scores, textColor);

                scoreArrayAdapter.notifyDataSetChanged();
                cannonballScoresList.setAdapter(scoreArrayAdapter);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {


            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        scoreView = inflater.inflate(R.layout.fragment_score_snake, container, false);
        return scoreView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public static CannonballScoreFragment newInstance() {
        Bundle args = new Bundle();
        CannonballScoreFragment fragment = new CannonballScoreFragment();
        fragment.setArguments(args);
        return fragment;
    }
}
