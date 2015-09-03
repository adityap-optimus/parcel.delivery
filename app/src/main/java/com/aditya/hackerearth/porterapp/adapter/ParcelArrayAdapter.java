package com.aditya.hackerearth.porterapp.adapter;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.aditya.hackerearth.porterapp.R;
import com.aditya.hackerearth.porterapp.model.ParcelAPIResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by adityapratap on 29/08/2015.
 */
public class ParcelArrayAdapter extends ArrayAdapter<ParcelAPIResponse.Parcel> implements Filterable {

    private Context mContext;
    private List<ParcelAPIResponse.Parcel> mListParcelAPIResponses;
    private List<ParcelAPIResponse.Parcel> mFilteredList;
    private ParcelFiler mFilter;
    private LayoutInflater inflater;

    public ParcelArrayAdapter(Context context, int resource,
                              List<ParcelAPIResponse.Parcel> objects) {
        super(context, resource, objects);
        this.mContext = context;
        this.mListParcelAPIResponses = objects;
        this.mFilteredList = objects;
        this.inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void updateParcelsList(List<ParcelAPIResponse.Parcel> objects) {
        this.mListParcelAPIResponses = objects;
        this.mFilteredList = objects;
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        View view = convertView;
        if (convertView == null) {
            view = inflater.inflate(R.layout.list_item_parcel_row, parent, false);
            holder = new ViewHolder();
            holder.tvItemName = (TextView) view.findViewById(R.id.tvItemName);
            holder.tvItemPrice = (TextView) view.findViewById(R.id.tvItemPrice);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            view.setElevation(2);
        }
        holder.tvItemName.setText(getItem(position).name);
        holder.tvItemPrice.setText(mContext.getString(R.string.label_item_price, getItem(position).price));
        return view;
    }

    static class ViewHolder {
        TextView tvItemName;
        TextView tvItemPrice;
    }


    @Override
    public ParcelAPIResponse.Parcel getItem(int position) {
        return this.mFilteredList.get(position);
    }

    @Override
    public int getCount() {
        return this.mFilteredList.size();
    }

    @Override
    public Filter getFilter() {
        if (mFilter == null) {
            mFilter = new ParcelFiler();
        }
        return this.mFilter;
    }

    public class ParcelFiler extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults = new FilterResults();
            if (constraint == null || constraint.length() == 0) {
                return filterResults;
            }
            String[] keywords = constraint.toString().split(" ");
            ArrayList<ParcelAPIResponse.Parcel> results = new ArrayList<>();
            for (ParcelAPIResponse.Parcel parcel : mListParcelAPIResponses) {
                if (stringContainsItemFromList(parcel, keywords)) {
                    results.add(parcel);
                }
            }
            filterResults.count = results.size();
            filterResults.values = results;
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mFilteredList = (ArrayList<ParcelAPIResponse.Parcel>) results.values;
            if (mFilteredList == null) {
                mFilteredList = new ArrayList<>();
            }
            notifyDataSetChanged();
        }

    }

    public static boolean stringContainsItemFromList(ParcelAPIResponse.Parcel parcelObj, String[] keywords) {
        for (String keyword : keywords) {
            if (parcelObj.name.toLowerCase().contains(keyword.toLowerCase())) {
                return true;
            } else if (parcelObj.type.toLowerCase().contains(keyword.toLowerCase())) {
                return true;
            } else if (parcelObj.weight.toLowerCase().contains(keyword.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

}
