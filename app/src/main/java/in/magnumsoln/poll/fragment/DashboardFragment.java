package in.magnumsoln.poll.fragment;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.shimmer.ShimmerFrameLayout;
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
import in.magnumsoln.poll.R;
import in.magnumsoln.poll.activity.CategoryActivity;
import in.magnumsoln.poll.activity.MainActivity;
import in.magnumsoln.poll.adapter.PollsRecyclerAdapter;
import in.magnumsoln.poll.firebase.PollFetcher;
import in.magnumsoln.poll.firebase.TopicFetcher;
import in.magnumsoln.poll.interfaces.FinishFetchingDataCallback;
import in.magnumsoln.poll.model.Poll;
import in.magnumsoln.poll.model.Topic;

/**
 * A simple {@link Fragment} subclass.
 */
public class DashboardFragment extends Fragment implements FinishFetchingDataCallback{

    private PollsRecyclerAdapter pollsRecyclerAdapter;
    private RecyclerView pollsRecyclerView;
    private CarouselView carouselView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ShimmerFrameLayout shimmer;
    private RelativeLayout shimmerLayout;
    private List<Poll> polls;
    private List<Topic> topics;
    private Context context;
    private FinishFetchingDataCallback finishFetchingDataCallbackInterface;
    private SharedPreferences mSharedPreferences;
    private TextView latestQuestion;


    public DashboardFragment() { }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        context = getActivity();
        finishFetchingDataCallbackInterface = this;
        pollsRecyclerView = view.findViewById(R.id.pollsRecyclerView);
        carouselView = view.findViewById(R.id.carouselView);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefresher);
        shimmer = view.findViewById(R.id.shimmer);
        shimmerLayout = view.findViewById(R.id.shimmer_layout);
        latestQuestion = view.findViewById(R.id.txtLatestQues);
        shimmer.startShimmer();
        mSharedPreferences = context.getSharedPreferences("user_details",Context.MODE_PRIVATE);
        pollsRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        pollsRecyclerView.addItemDecoration(new DividerItemDecoration(pollsRecyclerView.getContext(), DividerItemDecoration.VERTICAL));
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
        carouselView.setImageClickListener(new ImageClickListener() {
            @Override
            public void onClick(int position) {
//                Toast.makeText(context,topics.get(position).getTOPIC_NAME(),Toast.LENGTH_SHORT).show();
                startActivity(new Intent(context, CategoryActivity.class)
                        .putExtra("category_name",topics.get(position).getTOPIC_NAME()));
            }
        });
        return view;
    }

    public Object onFinish(Object object, String collectionType) {
        if (collectionType.equalsIgnoreCase("POLL")) {
            polls = (List<Poll>) object;
            pollsRecyclerAdapter = new PollsRecyclerAdapter(context, polls);
            pollsRecyclerView.setAdapter(pollsRecyclerAdapter);
            int visibility = (polls.size() == 0) ? View.GONE : View.VISIBLE;
            latestQuestion.setVisibility(visibility);
        } else if (collectionType.equalsIgnoreCase("TOPIC")) {
            topics = (List<Topic>) object;

            carouselView.setViewListener(new ViewListener() {
                @Override
                public View setViewForPosition(int position) {
                    View view = getLayoutInflater().inflate(R.layout.carouselview_layout,null);
                    ImageView img = view.findViewById(R.id.carouselImg);
                    Picasso.get().load(topics.get(position).getIMAGE_URL()).placeholder(R.drawable.sample).error(R.drawable.sample).into(img);
                    return view;
                }
            });
            carouselView.setPageCount(topics.size());
            swipeRefreshLayout.setRefreshing(false);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    shimmer.stopShimmer();
                    shimmerLayout.setVisibility(View.GONE);
                }
            },500);

        }
        return null;
    }

    @Override
    public Object onFailed(Object object, String collectionType) {
        if(collectionType.equalsIgnoreCase("POLL")){
            polls = new ArrayList<>();
            pollsRecyclerAdapter = new PollsRecyclerAdapter(context, polls);
            pollsRecyclerView.setAdapter(pollsRecyclerAdapter);
            latestQuestion.setVisibility(View.GONE);
        }
        return null;
    }
}
