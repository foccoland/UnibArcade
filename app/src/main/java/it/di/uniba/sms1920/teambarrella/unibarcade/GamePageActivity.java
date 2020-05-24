package it.di.uniba.sms1920.teambarrella.unibarcade;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.animation.ArgbEvaluator;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;


import java.util.ArrayList;
import java.util.List;

import it.di.uniba.sms1920.teambarrella.unibarcade.database.UnibArcadeFBHelper;


public class GamePageActivity extends AppCompatActivity {

    ViewPager viewPager;
    Adapter adapter;
    List<GameModel> gameModels;
    Integer[] colors = null;
    ArgbEvaluator argbEvaluator = new ArgbEvaluator();
    private MusicService mServ;
    private boolean mIsBound;
    UnibArcadeFBHelper fbHelper;
    private static final String TAG = "GamePageActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_page);
        View view = findViewById(R.id.gamePageLayout);
        fbHelper = new UnibArcadeFBHelper();

        doBindService();

        gameModels = new ArrayList<>();
        gameModels.add(new GameModel(R.drawable.space_invaders, getString(R.string.strSpaceInvaders), getString(R.string.strSpaceInvadersDescription)));
        gameModels.add(new GameModel(R.drawable.arkanoid, getString(R.string.strArkanoid), getString(R.string.strArkanoidDescription)));
        gameModels.add(new GameModel(R.drawable.cannonball, getString(R.string.strCannonball), getString(R.string.strCannonballDescription)));
        gameModels.add(new GameModel(R.drawable.snake, getString(R.string.strSnake), getString(R.string.strSnakeDescription)));

        adapter = new Adapter(gameModels, getApplicationContext());

        viewPager = (ViewPager) findViewById(R.id.viewPager);
        viewPager.setAdapter(adapter);
        viewPager.setPadding(130,0,130,0);

        Integer[] colorsTemp = {
                getResources().getColor(R.color.colorPrimarySpaceInvadersLight, null),
                getResources().getColor(R.color.colorPrimaryArkanoidLight, null),
                getResources().getColor(R.color.colorSecondaryCannonballLight, null),
                getResources().getColor(R.color.colorSecondarySnakeLightWithAlpha, null)};

        colors = colorsTemp;

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (position < (adapter.getCount() - 1) && position< (colors.length - 1)) {
                    viewPager.setBackgroundColor((Integer) argbEvaluator.evaluate(positionOffset,
                            colors[position],
                            colors[position + 1]));
                } else {
                    viewPager.setBackgroundColor(colors[colors.length - 1]);
                }
            }

            @Override
            public void onPageSelected(int position) {}

            @Override
            public void onPageScrollStateChanged(int state) {}
        });

        // check if user is guest and show message
        String uid = fbHelper.checkUserSession();
        if (uid.equals("Guest")) {
            showGuestMessage(view);
        } else {
            Log.d(TAG, "User id: " + uid);
        }
    }

    private void showGuestMessage(View view) {
        final Snackbar guestMessage = Snackbar.make(view, getString(R.string.strNoLoggedUser), Snackbar.LENGTH_INDEFINITE);
        guestMessage.setAction(R.string.strDismiss, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                guestMessage.dismiss();
            }
        });

        guestMessage.show();
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mServ = ((MusicService.ServiceBinder)service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mServ = null;
        }
    };

    void doBindService(){
        bindService(new Intent(this,MusicService.class),
                serviceConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        mServ.pauseMusic();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mServ != null)
            mServ.resumeMusic();
    }
}
