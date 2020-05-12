package in.magnumsoln.pollstest.fragment;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import in.magnumsoln.pollstest.R;
import in.magnumsoln.pollstest.activity.CategoryActivity;
import in.magnumsoln.pollstest.adapter.PollsRecyclerAdapter;
import in.magnumsoln.pollstest.firebase.PollFetcher;
import in.magnumsoln.pollstest.interfaces.FinishFetchingDataCallback;
import in.magnumsoln.pollstest.model.Poll;

public class ActiveFragment extends Fragment implements FinishFetchingDataCallback {

    private PollsRecyclerAdapter adapter;
    private List<Poll> activePolls;
    private RecyclerView recyclerView;
    private RelativeLayout loading,no_polls;
    private CategoryActivity activity;
    private ActiveFragment currentFragment;
    private SwipeRefreshLayout active_refresh;
    private LinearLayout recycler_container;
    public ActiveFragment() { }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_active, container, false);
        recyclerView = view.findViewById(R.id.activePollsRecyclerView);
        loading = view.findViewById(R.id.loading);
        no_polls = view.findViewById(R.id.no_polls_active);
        active_refresh = view.findViewById(R.id.refresh_active);
        recycler_container = view.findViewById(R.id.active_recycler_container);
        activity = (CategoryActivity) getActivity();
        currentFragment = this;
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        //recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));
        new PollFetcher().fetchActivePolls(this,activity.title);
        active_refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new PollFetcher().fetchActivePolls(currentFragment,activity.title);
            }
        });
        active_refresh.setColorSchemeColors(activity.getResources().getColor(R.color.colorAccent));
        return view;
    }

    @Override
    public Object onFinish(Object object, String collection) {
        activePolls = (List<Poll>) object;
        adapter = new PollsRecyclerAdapter(getActivity(), activePolls);
        recyclerView.setAdapter(adapter);
        active_refresh.setRefreshing(false);
        recycler_container.setVisibility(View.VISIBLE);
        int visibility = (activePolls.size() == 0) ? View.VISIBLE: View.GONE;
        no_polls.setVisibility(visibility);
        loading.setVisibility(View.GONE);
        return null;
    }

    @Override
    public Object onFailed(Object object, String collection) {
        recycler_container.setVisibility(View.GONE);
        active_refresh.setRefreshing(false);
        loading.setVisibility(View.GONE);
        no_polls.setVisibility(View.VISIBLE);
        return null;
    }
}
