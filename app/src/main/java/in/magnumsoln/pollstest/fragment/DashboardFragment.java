package in.magnumsoln.pollstest.fragment;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.synnapps.carouselview.CarouselView;
import com.synnapps.carouselview.ImageClickListener;
import com.synnapps.carouselview.ViewListener;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import in.magnumsoln.pollstest.R;
import in.magnumsoln.pollstest.activity.CategoryActivity;
import in.magnumsoln.pollstest.adapter.LatestPollsAdapter;
import in.magnumsoln.pollstest.adapter.PollsRecyclerAdapter;
import in.magnumsoln.pollstest.adapter.TopicAdapter;
import in.magnumsoln.pollstest.firebase.PollFetcher;
import in.magnumsoln.pollstest.firebase.TopicFetcher;
import in.magnumsoln.pollstest.interfaces.FinishFetchingDataCallback;
import in.magnumsoln.pollstest.model.Poll;
import in.magnumsoln.pollstest.model.Topic;

/**
 * A simple {@link Fragment} subclass.
 */
public class DashboardFragment extends Fragment implements FinishFetchingDataCallback {

    private LatestPollsAdapter latestPollsAdapter;
    private TopicAdapter topicAdapter;
    private RecyclerView pollsRecyclerView,topicRecyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ShimmerFrameLayout shimmer;
    private RelativeLayout shimmerLayout;
    private List<Poll> polls;
    private List<Topic> topics;
    private Context context;
    private FinishFetchingDataCallback finishFetchingDataCallbackInterface;
    private SharedPreferences mSharedPreferences;
    private TextView latestQuestion;


    public DashboardFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        context = getActivity();
        finishFetchingDataCallbackInterface = this;
        pollsRecyclerView = view.findViewById(R.id.pollsRecyclerView);
        topicRecyclerView = view.findViewById(R.id.topicRecyclerView);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefresher);
        shimmer = view.findViewById(R.id.shimmer);
        shimmerLayout = view.findViewById(R.id.shimmer_layout);
        latestQuestion = view.findViewById(R.id.txtLatestQues);
        shimmer.startShimmer();
        mSharedPreferences = context.getSharedPreferences("user_details", Context.MODE_PRIVATE);
        pollsRecyclerView.setLayoutManager(new LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false));
        topicRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        //pollsRecyclerView.addItemDecoration(new DividerItemDecoration(pollsRecyclerView.getContext(), DividerItemDecoration.VERTICAL));
        new PollFetcher().fetchLatestPolls(finishFetchingDataCallbackInterface);
        new TopicFetcher().fetchTopics(finishFetchingDataCallbackInterface);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
//                shimmer.startShimmer();
//                shimmerLayout.setVisibility(View.VISIBLE);
                new PollFetcher().fetchLatestPolls(finishFetchingDataCallbackInterface);
                new TopicFetcher().fetchTopics(finishFetchingDataCallbackInterface);
            }
        });
        swipeRefreshLayout.setColorSchemeColors(context.getColor(R.color.colorAccent));
        return view;
    }

    public Object onFinish(Object object, String collectionType) {
        try {
            if (collectionType.equalsIgnoreCase("POLL")) {
                polls = (List<Poll>) object;
                latestPollsAdapter = new LatestPollsAdapter(context,polls);
                pollsRecyclerView.setAdapter(latestPollsAdapter);
                int visibility = (polls.size() == 0) ? View.GONE : View.VISIBLE;
                latestQuestion.setVisibility(visibility);
            } else if (collectionType.equalsIgnoreCase("TOPIC")) {
                topics = (List<Topic>) object;
                topicAdapter = new TopicAdapter(context,topics);
                topicRecyclerView.setAdapter(topicAdapter);
                swipeRefreshLayout.setRefreshing(false);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        shimmer.stopShimmer();
                        shimmerLayout.setVisibility(View.GONE);
                    }
                }, 500);

            }
        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(context, "Some error occured", Toast.LENGTH_SHORT).show();
        }
        return null;
    }

    @Override
    public Object onFailed(Object object, String collectionType) {
        if (collectionType.equalsIgnoreCase("POLL")) {
            polls = new ArrayList<>();
            latestPollsAdapter = new LatestPollsAdapter(context,polls);
            pollsRecyclerView.setAdapter(latestPollsAdapter);
            latestQuestion.setVisibility(View.GONE);
        }
        return null;
    }
}
