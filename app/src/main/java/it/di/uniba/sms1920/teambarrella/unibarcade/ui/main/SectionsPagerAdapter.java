package it.di.uniba.sms1920.teambarrella.unibarcade.ui.main;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import it.di.uniba.sms1920.teambarrella.unibarcade.R;
import it.di.uniba.sms1920.teambarrella.unibarcade.ui.main.scorefragment.ArkanoidScoreFragment;
import it.di.uniba.sms1920.teambarrella.unibarcade.ui.main.scorefragment.CannonballScoreFragment;
import it.di.uniba.sms1920.teambarrella.unibarcade.ui.main.scorefragment.SnakeScoreFragment;
import it.di.uniba.sms1920.teambarrella.unibarcade.ui.main.scorefragment.SpaceInvadersScoreFragment;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {

    @StringRes
    private static final int[] TAB_TITLES = new int[]{
            R.string.strSpaceInvaders,
            R.string.strArkanoid,
            R.string.strCannonball,
            R.string.strSnake};

    private final Context mContext;

    public SectionsPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a (defined as a static inner class below).

        switch (position) {
            case 0:
                return SpaceInvadersScoreFragment.newInstance();
            case 1:
                return ArkanoidScoreFragment.newInstance();
            case 2:
                return CannonballScoreFragment.newInstance();
            default:
                return SnakeScoreFragment.newInstance();
        }
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mContext.getResources().getString(TAB_TITLES[position]);
    }

    @Override
    public int getCount() {
        // Show 4 total pages.
        return 4;
    }
}