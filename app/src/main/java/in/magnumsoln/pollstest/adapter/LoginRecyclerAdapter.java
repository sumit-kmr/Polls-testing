package in.magnumsoln.pollstest.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import in.magnumsoln.pollstest.R;

public class LoginRecyclerAdapter extends RecyclerView.Adapter<LoginRecyclerAdapter.LoginViewHolder> {

    private int[] list;
    private Context context;
    public LoginRecyclerAdapter(Context context,int[] images_list){
        list = images_list;
        this.context = context;
    }

    @NonNull
    @Override
    public LoginViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recycler_item_login,parent,false);
        return new LoginViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LoginViewHolder holder, int position) {
        holder.imageView.setImageDrawable(context.getDrawable(list[position]));
    }

    @Override
    public int getItemCount() {
        return list.length;
    }

    class LoginViewHolder extends RecyclerView.ViewHolder{
        private ImageView imageView;
        public LoginViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imgLoginRecycler);
        }
    }
}
