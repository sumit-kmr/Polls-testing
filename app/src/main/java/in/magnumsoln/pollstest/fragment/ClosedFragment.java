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


public class ClosedFragment extends Fragment implements FinishFetchingDataCallback {

    private RelativeLayout loading,no_polls;
    private RecyclerView closedRecyclerView;
    private PollsRecyclerAdapter adapter;
    private CategoryActivity activity;
    private List<Poll> closedPolls;
    private SwipeRefreshLayout closed_refresh;
    private ClosedFragment currentFragment;
    private LinearLayout recycler_container;
    public ClosedFragment() {}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_closed, container, false);
        closedRecyclerView = view.findViewById(R.id.closedRecyclerView);
        loading = view.findViewById(R.id.closed_loading);
        no_polls = view.findViewById(R.id.no_polls);
        closed_refresh = view.findViewById(R.id.refresh_closed);
        recycler_container = view.findViewById(R.id.closed_recycler_container);
        activity = (CategoryActivity) getActivity();
        currentFragment = this;
        closedRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        //closedRecyclerView.addItemDecoration(new DividerItemDecoration(closedRecyclerView.getContext(),DividerItemDecoration.VERTICAL));
        new PollFetcher().fetchClosedPolls(this,activity.title);
        closed_refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new PollFetcher().fetchClosedPolls(currentFragment,activity.title);
            }
        });
        closed_refresh.setColorSchemeColors(activity.getResources().getColor(R.color.colorAccent));
        return view;
    }

    @Override
    public Object onFinish(Object object, String collection) {
        closedPolls = (List<Poll>) object;
        adapter = new PollsRecyclerAdapter(getActivity(),closedPolls);
        closedRecyclerView.setAdapter(adapter);
        closed_refresh.setRefreshing(false);
        recycler_container.setVisibility(View.VISIBLE);
        int visibility = (closedPolls.size() == 0) ? View.VISIBLE: View.GONE;
        no_polls.setVisibility(visibility);
        loading.setVisibility(View.GONE);
        return null;
    }

    @Override
    public Object onFailed(Object object, String collection) {
        closed_refresh.setRefreshing(false);
        recycler_container.setVisibility(View.GONE);
        loading.setVisibility(View.GONE);
        no_polls.setVisibility(View.VISIBLE);
        return null;
    }
}
