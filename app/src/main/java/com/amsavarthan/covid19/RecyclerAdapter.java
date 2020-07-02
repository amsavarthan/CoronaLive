package com.amsavarthan.covid19;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amsavarthan.covid19.models.Country;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.w3c.dom.Text;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

    onItemClickListener itemClickListener;
    List<Country> mCountries;
    Context mContext;

    public RecyclerAdapter(Context mContext,onItemClickListener itemClickListener, List<Country> mCountries) {
        this.mContext=mContext;
        this.itemClickListener = itemClickListener;
        this.mCountries = mCountries;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_country,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Country country =mCountries.get(position);
        holder.bind(country);
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public int getItemCount() {
        return mCountries.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        CircleImageView flag;
        TextView countryName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            flag=itemView.findViewById(R.id.flag);
            countryName=itemView.findViewById(R.id.countryName);

        }

        public void bind(Country country){

            countryName.setText(country.getCountryName());
            Glide.with(mContext)
                    .applyDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.ic_placeholder))
                    .load(country.getFlag())
                    .into(flag);
            itemView.setOnClickListener(v -> itemClickListener.onItemClicked(country));

        }

    }
}
