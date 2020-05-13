package in.magnumsoln.pollstest.adapter;

import android.content.Context;
import android.content.Intent;
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
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import in.magnumsoln.pollstest.R;
import in.magnumsoln.pollstest.activity.PollActivity;
import in.magnumsoln.pollstest.model.Poll;
import in.magnumsoln.pollstest.util.InternetChecker;

public class PollsRecyclerAdapter extends RecyclerView.Adapter<PollsRecyclerAdapter.PollsViewHolder> {

    Context context;
    List<Poll> polls;
    public PollsRecyclerAdapter(Context context, List<Poll> polls) {
        this.context = context;
        this.polls = polls;
    }

    @NonNull
    @Override
    public PollsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.polls_recyclerview_item,parent,false);
        return new PollsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final PollsViewHolder holder, final int position) {
        try {
            holder.txtQuestion.setText(polls.get(position).getQUESTION());
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

            /////////
            /*
            Picasso.get().load(imageUrl).resize(2048, 1600).onlyScaleDown()
                    .error(R.drawable.sample).placeholder(R.drawable.sample)
                    .into(holder.imgPollThumbnail, new Callback() {
                        @Override
                        public void onSuccess() {
                            // stop shimmer
//                holder.imageShimmer.stopShimmer();
                            holder.imageShimmer.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError(Exception e) {
                            //failed to load image
                            Log.w("Recycler view image ::", "Failed to load image");
                            holder.imageShimmer.setVisibility(View.GONE);
                        }
                    });
             */
            //////////
            holder.recycler_item.setOnClickListener(new View.OnClickListener() {
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
        }catch(Exception e){
            e.printStackTrace();
            Toast.makeText(context, "Some error occured", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() {
        try {
            return polls.size();
        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(context, "Some error occured", Toast.LENGTH_SHORT).show();
            return 0;
        }
    }

    class PollsViewHolder extends RecyclerView.ViewHolder{
        private TextView txtQuestion;
        private ImageView imageView;
        private RelativeLayout recycler_item;
        private ShimmerFrameLayout shimmer;
        public PollsViewHolder(@NonNull View itemView) {
            super(itemView);
            this.txtQuestion = itemView.findViewById(R.id.txtQuestion);
            this.imageView = itemView.findViewById(R.id.imgPollThumbnail);
            this.recycler_item = itemView.findViewById(R.id.polls_recycler_item);
            this.shimmer = itemView.findViewById(R.id.imgShimmerRecItem);
        }
    }
}
