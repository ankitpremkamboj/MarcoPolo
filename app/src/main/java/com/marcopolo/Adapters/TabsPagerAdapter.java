package com.marcopolo.Adapters;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;
import com.marcopolo.Tabs.TabActivity;
import com.marcopolo.audiowave.NewRecorder;
import com.morcopolo.fragments.Fragmentss.HomeFragment;
import com.morcopolo.fragments.SetPhraseFragment;
import com.morcopolo.fragments.SettingFragment;

/**
 * Created by kamran on 3/3/17.
 */
public class TabsPagerAdapter extends FragmentStatePagerAdapter {
    SparseArray registeredFragments = new SparseArray();
    CharSequence Titles[];
    int NumbOfTabs;
    Fragment fragment;
    int[] icons;
    Activity activity;


    public TabsPagerAdapter(FragmentManager fm, CharSequence mTitles[], int mNumbOfTabsumb, int[] icons, TabActivity tabActivity) {
        super(fm);
        this.Titles = mTitles;
        this.NumbOfTabs = mNumbOfTabsumb;
        this.icons = icons;
        this.activity = tabActivity;


    }

    @Override
    public CharSequence getPageTitle(int position) {

        return null;
    }

    public int getDrawableId(int position) {
        return icons[position];
    }


    // This method return the Number of tabs for the tabs Strip
    @Override
    public int getCount() {
        return NumbOfTabs;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment fragment = (Fragment) super.instantiateItem(container, position);
        registeredFragments.put(position, fragment);
        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        registeredFragments.remove(position);
        super.destroyItem(container, position, object);
    }

    public Fragment getRegisteredFragment(int position) {
        return (Fragment) registeredFragments.get(position);

    }

    @Override
    public Fragment getItem(int index) {

        switch (index) {
            case 0:
                // Top Rated fragment activity
                return new HomeFragment();
            case 1:
                // Games fragment activity
                return new SetPhraseFragment();

            case 2:
                // Movies fragment activity
                return new NewRecorder();
            case 3:
                // Movies fragment activity
                return new SettingFragment();
        }

        return null;
    }

}