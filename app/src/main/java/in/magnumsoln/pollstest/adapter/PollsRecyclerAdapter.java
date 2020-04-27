package in.magnumsoln.pollstest.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import in.magnumsoln.pollstest.R;
import in.magnumsoln.pollstest.activity.PollActivity;
import in.magnumsoln.pollstest.model.Poll;

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
    public void onBindViewHolder(@NonNull PollsViewHolder holder, final int position) {
        holder.txtQuestion.setText(polls.get(position).getQUESTION());
        String imageUrl = polls.get(position).getIMAGE_URL();
        Picasso.get().load(imageUrl).error(R.drawable.sample).into(holder.imgPollThumbnail);
        holder.recycler_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, PollActivity.class);
                intent.putExtra("poll",polls.get(position));
                intent.putExtra("poll_status",polls.get(position).getSTATUS());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return polls.size();
    }

    class PollsViewHolder extends RecyclerView.ViewHolder{
        private TextView txtQuestion;
        private ImageView imgPollThumbnail;
        private RelativeLayout recycler_item;
        public PollsViewHolder(@NonNull View itemView) {
            super(itemView);
            this.txtQuestion = itemView.findViewById(R.id.txtQuestion);
            this.imgPollThumbnail = itemView.findViewById(R.id.imgPollThumbnail);
            this.recycler_item = itemView.findViewById(R.id.polls_recycler_item);
        }
    }
}
