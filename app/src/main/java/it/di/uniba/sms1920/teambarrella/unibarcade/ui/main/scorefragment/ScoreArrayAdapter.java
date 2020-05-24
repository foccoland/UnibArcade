package it.di.uniba.sms1920.teambarrella.unibarcade.ui.main.scorefragment;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

import it.di.uniba.sms1920.teambarrella.unibarcade.R;
import it.di.uniba.sms1920.teambarrella.unibarcade.database.UnibArcadeFBHelper;

public class ScoreArrayAdapter extends ArrayAdapter<String> {

    private static final String TAG = "ScoreArrayAdapter";

    private final Context context;
    private final ArrayList<String> nicknames;
    private final ArrayList<Integer> scores;
    private final int textColor;
    private int resource;

    private TextView txtPosition, txtNickname, txtScore;
    private LinearLayout scoreItemLayout;

    private UnibArcadeFBHelper fbHelper;

    public ScoreArrayAdapter(@NonNull Context context, int resource,
                             @NonNull ArrayList<String> nicknames,
                             @NonNull ArrayList<Integer> scores,
                             @NonNull int textColor) {
        super(context, resource, nicknames);
        this.resource = resource;
        this.context = context;
        this.textColor = textColor;
        this.nicknames = nicknames;
        this.scores = scores;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);

        convertView = inflater.inflate(resource, parent, false);

        //Assigning TextViews
        txtPosition = (TextView) convertView.findViewById(R.id.txtPosition);
        txtNickname = (TextView) convertView.findViewById(R.id.txtNickname);
        txtScore = (TextView) convertView.findViewById(R.id.txtScoreData);

        scoreItemLayout = (LinearLayout) convertView.findViewById(R.id.scoreItem);

        txtPosition.setText(String.valueOf(position + 1));
        txtNickname.setText(nicknames.get(position));
        txtScore.setText(String.valueOf(scores.get(position)));

        Log.d(TAG, "Nickname added: " + nicknames.get(position) + "; " + "Score added: " + scores.get(position) + ";");

        // Highlighted current user
        highlightCurrentUser(scoreItemLayout, nicknames.get(position));

        //Colors
        txtPosition.setTextColor(textColor);
        txtNickname.setTextColor(textColor);
        txtScore.setTextColor(textColor);

        return convertView;
    }

    private void highlightCurrentUser(LinearLayout scoreItemLayout, String nickFromList) {
        fbHelper = new UnibArcadeFBHelper();
        // Check if user logged
        if (fbHelper.userExists()) {
            Log.d(TAG, "userExists: true! " + fbHelper.getUser().getDisplayName());
            String nick = fbHelper.getUser().getDisplayName();
            if (nick.equals(nickFromList)) {
                scoreItemLayout.setBackgroundColor(getContext()
                        .getResources()
                        .getColor(R.color.colorPrimaryDark, getContext().getTheme()));
            }
        } else {
            Log.d(TAG, "userExists: false!");
        }
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

}
