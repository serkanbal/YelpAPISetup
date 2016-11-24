package net.serkanbal.yelpapiexample;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import net.serkanbal.yelpapiexample.JSONtoPOJO.Business;
import net.serkanbal.yelpapiexample.JSONtoPOJO.Category;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by NikitaShuvalov on 11/24/16.
 */

public class BusinessRecyclerViewAdapter extends RecyclerView.Adapter<BusinessViewHolder> {
    List<Business> mBusinessList;

    public BusinessRecyclerViewAdapter(List<Business> businessList) {
        mBusinessList = businessList;
    }

    @Override
    public BusinessViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_business, null);
        return new BusinessViewHolder(view);
    }

    @Override
    public void onBindViewHolder(BusinessViewHolder holder, int position) {
        holder.bindDataToView(mBusinessList.get(position));
    }

    @Override
    public int getItemCount() {
        return mBusinessList.size();
    }
    public void replaceList(List<Business> newSearchResult){
        mBusinessList = newSearchResult;
    }
}

class BusinessViewHolder extends RecyclerView.ViewHolder{
    TextView mNameView, mDescView, mPrice, mRating;
    ImageView mPicture;

    public BusinessViewHolder(View itemView) {
        super(itemView);
        mNameView = (TextView)itemView.findViewById(R.id.business_name_view);
        mDescView = (TextView)itemView.findViewById(R.id.description_text_view);
        mPrice = (TextView)itemView.findViewById(R.id.price_view);
        mRating = (TextView)itemView.findViewById(R.id.rating_view);

        mPicture = (ImageView)itemView.findViewById(R.id.picture);
    }

    public void bindDataToView(Business business){
        mNameView.setText(business.getName());
        String categoriesText = "";
        for (Category category: business.getCategories()){
            categoriesText+=(category.getTitle()+", ");
        }
        mDescView.setText(business.getLocation()+"\n"+business.getPhone()+"\n"+categoriesText);
        mPrice.setText(business.getPrice());
        mRating.setText(String.valueOf(business.getRating()));
        //ToDo:Set image by using picasso with image.url

    }
}
