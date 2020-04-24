package in.magnumsoln.poll.fragment;


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
import in.magnumsoln.poll.R;
import in.magnumsoln.poll.activity.CategoryActivity;
import in.magnumsoln.poll.adapter.PollsRecyclerAdapter;
import in.magnumsoln.poll.firebase.PollFetcher;
import in.magnumsoln.poll.interfaces.FinishFetchingDataCallback;
import in.magnumsoln.poll.model.Poll;

public class DeclaredFragment extends Fragment implements FinishFetchingDataCallback {

    private SwipeRefreshLayout declared_refresh;
    private RecyclerView declaredRecyclerView;
    private RelativeLayout no_polls,loading;
    private PollsRecyclerAdapter adapter;
    private List<Poll> declaredPolls;
    private CategoryActivity categoryActivity;
    private DeclaredFragment currentFragment;
    private LinearLayout recycler_container;

    public DeclaredFragment() { }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_declared, container, false);
        declared_refresh = view.findViewById(R.id.refresh_declared);
        no_polls = view.findViewById(R.id.no_polls_declared);
        declaredRecyclerView = view.findViewById(R.id.declaredRecyclerView);
        loading = view.findViewById(R.id.declared_loading);
        recycler_container = view.findViewById(R.id.declared_recycler_container);
        categoryActivity = (CategoryActivity) getActivity();
        currentFragment = this;
        declaredRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        declaredRecyclerView.addItemDecoration(new DividerItemDecoration(declaredRecyclerView.getContext(),DividerItemDecoration.VERTICAL));
        new PollFetcher().fetchDeclaredPolls(this,categoryActivity.title);
        declared_refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new PollFetcher().fetchDeclaredPolls(currentFragment,categoryActivity.title);
            }
        });
        declared_refresh.setColorSchemeColors(categoryActivity.getResources().getColor(R.color.colorAccent));
        return view;
    }

    @Override
    public Object onFinish(Object object, String collection) {
        declaredPolls = (List<Poll>) object;
        adapter = new PollsRecyclerAdapter(getActivity(),declaredPolls);
        declaredRecyclerView.setAdapter(adapter);
        declared_refresh.setRefreshing(false);
        recycler_container.setVisibility(View.VISIBLE);
        int visibility = (declaredPolls.size() == 0) ? View.VISIBLE: View.GONE;
        no_polls.setVisibility(visibility);
        loading.setVisibility(View.GONE);
        return null;
    }

    @Override
    public Object onFailed(Object object, String collection) {
        declared_refresh.setRefreshing(false);
        recycler_container.setVisibility(View.GONE);
        loading.setVisibility(View.GONE);
        no_polls.setVisibility(View.VISIBLE);
        return null;
    }
}
