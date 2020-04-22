package in.magnumsoln.poll.adapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import in.magnumsoln.poll.fragment.ActiveFragment;
import in.magnumsoln.poll.fragment.ClosedFragment;
import in.magnumsoln.poll.fragment.DeclaredFragment;

public class TabFragmentAdapter extends FragmentPagerAdapter {

    String[] titles = {"Active","Closed","Declared"};

    public TabFragmentAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch(position){
            case 0:
                return new ActiveFragment();
            case 1:
                return new ClosedFragment();
            case 2:
                return new DeclaredFragment();
        }
        return null;
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return titles[position];
    }
}
