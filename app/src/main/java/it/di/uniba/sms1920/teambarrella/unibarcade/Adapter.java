package it.di.uniba.sms1920.teambarrella.unibarcade;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import java.util.List;

public class Adapter extends PagerAdapter {

    private List<GameModel> gameModels;
    private LayoutInflater layoutInflater;
    private Context context;

    public Adapter(List<GameModel> gameModels, Context applicationContext) {
        this.gameModels = gameModels;
        this.context = applicationContext;
    }

    @Override
    public int getCount() {
        return gameModels.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view.equals(object);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, final int position) {
        layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.item, container, false);

        ImageView imageView;
        TextView title, desc;

        imageView = view.findViewById(R.id.image);
        title = view.findViewById(R.id.title);
        desc = view.findViewById(R.id.desc);

        imageView.setImageResource(gameModels.get(position).getImage());
        title.setText(gameModels.get(position).getTitle());
        desc.setText(gameModels.get(position).getDesc());

        //Needed for click listener
        final String gameTitle = title.getText().toString();

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToGame(gameTitle);
                // finish();
            }
        });

        container.addView(view, 0);

        return view;
    }

    private void goToGame(String choosenGame) {

        switch (choosenGame) {

            case "Space Invaders":
                goToSpaceInvaders();
                break;

            case "Arkanoid":
                goToArkanoid();
                break;

            case "Cannon Ball":
                goToCannonApp();
                break;

            case "Snake":
                goToSnake();
                break;
        }
    }

    private void goToSpaceInvaders() {
        Intent intent = new Intent(context, LoadingActivity.class);
        intent.putExtra("GAME", "SpaceInvadersActivity");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private void goToArkanoid() {
        Intent intent = new Intent (context, LoadingActivity.class);
        intent.putExtra("GAME", "ArkanoidActivity");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private void goToCannonApp() {
        Intent intent = new Intent (context, LoadingActivity.class);
        intent.putExtra("GAME", "CannonApp");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private void goToSnake() {
        Intent intent = new Intent (context, LoadingActivity.class);
        intent.putExtra("GAME", "Snake");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}
