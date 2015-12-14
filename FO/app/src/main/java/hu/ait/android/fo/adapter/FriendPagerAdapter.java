package hu.ait.android.fo.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

import hu.ait.android.fo.fragments.Fragment_Friend_List;
import hu.ait.android.fo.fragments.Fragment_Friend_My;

/**
 * Created by user on 2015-12-12.
 */
public class FriendPagerAdapter extends FragmentStatePagerAdapter{

    CharSequence Titles[];
    int numTabs;

    private Fragment_Friend_List friendListFragment;
    private Fragment_Friend_My friendMyFragment;

    public Fragment_Friend_List getFriendListFragment() {
        return friendListFragment;
    }

    public Fragment_Friend_My getFriendMyFragment() {
        return friendMyFragment;
    }

    public FriendPagerAdapter(FragmentManager fm, CharSequence mTitles[], int mNumOfTabs){
        super(fm);
        this.Titles = mTitles;
        this.numTabs = mNumOfTabs;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment createdFragment = (Fragment) super.instantiateItem(container, position);
        // save the appropriate reference depending on position
        switch (position) {
            case 0:
                friendListFragment = (Fragment_Friend_List) createdFragment;
                break;
            case 1:
                friendMyFragment = (Fragment_Friend_My) createdFragment;
                break;
        }
        return createdFragment;
    }

    @Override
    public Fragment getItem(int position) {
        if(position ==0){
            return new Fragment_Friend_List();
        }else if(position == 1){
            return new Fragment_Friend_My();
        }
        return null;
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
