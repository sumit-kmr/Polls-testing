package in.magnumsoln.pollstest.adapter;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import in.magnumsoln.pollstest.R;
import in.magnumsoln.pollstest.activity.PollActivity;
import in.magnumsoln.pollstest.model.Poll;
import in.magnumsoln.pollstest.util.InternetChecker;

public class LatestPollsAdapter extends RecyclerView.Adapter<LatestPollsAdapter.LatestPollViewHolder> {

    private Context context;
    private List<Poll> polls;

    public LatestPollsAdapter(Context context,List<Poll> polls){
        this.context = context;
        this.polls = polls;
    }
    @NonNull
    @Override
    public LatestPollViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cardview_layout,parent,false);
        return new LatestPollViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final LatestPollViewHolder holder, final int position) {
        try{
            final String imageUrl = polls.get(position).getIMAGE_URL();

            Picasso.with(context)
                    .load(imageUrl)
                    .resize(2048, 1600).onlyScaleDown()
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .into(holder.imageView, new Callback() {
                        @Override
                        public void onSuccess() {
                            holder.shimmer.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError() {
                            //Try again online if cache failed
                            Picasso.with(context)
                                    .load(imageUrl)
                                    .resize(2048, 1600).onlyScaleDown()
                                    .into(holder.imageView, new Callback() {
                                        @Override
                                        public void onSuccess() {
                                            holder.shimmer.setVisibility(View.GONE);
                                        }

                                        @Override
                                        public void onError() {
                                            holder.shimmer.setVisibility(View.GONE);
                                        }
                                    });
                        }
                    });

            ///////
            /*
            Picasso.get().load(imageUrl)
                    .resize(2048, 1600).onlyScaleDown()
                    .error(R.drawable.sample).placeholder(R.drawable.sample)
                    .into(holder.imageView, new Callback() {
                        @Override
                        public void onSuccess() {
                            holder.shimmer.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError(Exception e) {
                            //////
                            e.printStackTrace();
                            holder.shimmer.setVisibility(View.GONE);
                            //////
                        }
                    });

             */
            ////////
            holder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(!InternetChecker.isInternetAvailable(context)){
                        Toast.makeText(context, "You're offline", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Intent intent = new Intent(context, PollActivity.class);
                    intent.putExtra("poll", polls.get(position));
                    intent.putExtra("poll_status", polls.get(position).getSTATUS());
                    context.startActivity(intent);
                }
            });
        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(context, "Some error occured", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() {
        return polls.size();
    }

    class LatestPollViewHolder extends RecyclerView.ViewHolder{
        private ImageView imageView;
        private ShimmerFrameLayout shimmer;
        public LatestPollViewHolder(@NonNull View itemView) {
            super(itemView);
            this.imageView = itemView.findViewById(R.id.imgCardViewLayout);
            this.shimmer = itemView.findViewById(R.id.shimmerCardView);
        }
    }
}
