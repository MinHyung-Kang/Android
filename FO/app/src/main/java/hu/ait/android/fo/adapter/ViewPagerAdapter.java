package hu.ait.android.fo.adapter;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import hu.ait.android.fo.fragments.Fragment_List_Tab;
import hu.ait.android.fo.fragments.Fragment_Map_Tab;
import hu.ait.android.fo.fragments.Fragment_My_Tab;

/**
 * Created by user on 2015-11-28.
 * Adapter for the viewpager (tabbed fragments)
 */
public class ViewPagerAdapter extends FragmentStatePagerAdapter {

    CharSequence Titles[];
    int numTabs;

    public Fragment_Map_Tab getMapFragment() {
        return mapFragment;
    }

    public Fragment_List_Tab getListFragment() {
        return listFragment;
    }

    public Fragment_My_Tab getMyFragment() {
        return myFragment;
    }

    private Fragment_Map_Tab mapFragment;
    private Fragment_List_Tab listFragment;
    private Fragment_My_Tab myFragment;

    //Constructor
    public ViewPagerAdapter(FragmentManager fm, CharSequence mTitles[], int mNumbOfTabsumb) {
        super(fm);
        this.Titles = mTitles;
        this.numTabs = mNumbOfTabsumb;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment createdFragment = (Fragment) super.instantiateItem(container, position);
        // save the appropriate reference depending on position
        switch (position) {
            case 0:
                mapFragment = (Fragment_Map_Tab) createdFragment;
                break;
            case 1:
                listFragment = (Fragment_List_Tab) createdFragment;
                break;
            case 2:
                myFragment = (Fragment_My_Tab) createdFragment;
                break;
        }
        return createdFragment;
    }


    //Returns different tabs (fragments)
    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return new Fragment_Map_Tab();
        } else if (position == 1) {
            return new Fragment_List_Tab();
        } else {
            return new Fragment_My_Tab();
        }
    }

    // Returns the title of each tab
    @Override
    public CharSequence getPageTitle(int position) {
        return Titles[position];
    }

    // Return total count of tabs
    @Override
    public int getCount() {
        return numTabs;
    }

}
