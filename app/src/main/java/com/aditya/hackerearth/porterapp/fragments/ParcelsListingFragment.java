package com.aditya.hackerearth.porterapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.aditya.hackerearth.porterapp.DetailsActivity;
import com.aditya.hackerearth.porterapp.R;
import com.aditya.hackerearth.porterapp.adapter.ParcelArrayAdapter;
import com.aditya.hackerearth.porterapp.helper.network.APIUtils;
import com.aditya.hackerearth.porterapp.helper.network.NetworkFunctions;
import com.aditya.hackerearth.porterapp.helper.network.RequestListener;
import com.aditya.hackerearth.porterapp.helper.ui.OverlayButton;
import com.aditya.hackerearth.porterapp.helper.ui.OverlayMenu;
import com.aditya.hackerearth.porterapp.model.HitsAPIResponse;
import com.aditya.hackerearth.porterapp.model.ParcelAPIResponse;
import com.aditya.hackerearth.porterapp.model.ParcelUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Shows parcels list
 */
public class ParcelsListingFragment extends Fragment implements AdapterView.OnItemClickListener, RequestListener, SwipeRefreshLayout.OnRefreshListener, View.OnClickListener {

    private SwipeRefreshLayout mSwipeRefreshContainer;
    private ListView mListView;
    private ParcelArrayAdapter mAdapter;
    private OverlayMenu mSortMenu;
    private OverlayButton mSortByName, mSortByPrice, mSortByWeight;
    private List<ParcelAPIResponse.Parcel> mParcelsList;
    private TextView mAPIHitsCount, mParcelsCount;

    public ParcelsListingFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_parcels_listing, container, false);
        initViews(view);
        return view;
    }


    private void initViews(View parentView) {
        mSwipeRefreshContainer = (SwipeRefreshLayout) parentView.findViewById(R.id.parcels_list_swipe_container);
        mSwipeRefreshContainer.setOnRefreshListener(this);
        mSwipeRefreshContainer.setProgressViewOffset(false, 0,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics()));
        mSwipeRefreshContainer.setColorSchemeResources(R.color.swipe_refresh_green,
                R.color.swipe_refresh_blue);
        mListView = (ListView) parentView.findViewById(R.id.parcels_list_view);
        mListView.setOnItemClickListener(this);

        mAPIHitsCount = (TextView) parentView.findViewById(R.id.tvAPIHitsCount);
        mParcelsCount = (TextView) parentView.findViewById(R.id.tvParcelsCount);

        mSortMenu = (OverlayMenu) parentView.findViewById(R.id.omSortMenu);
        mSortByName = (OverlayButton) parentView.findViewById(R.id.obSortByName);
        mSortByName.setOnClickListener(this);
        mSortByPrice = (OverlayButton) parentView.findViewById(R.id.obSortByPrice);
        mSortByPrice.setOnClickListener(this);
        mSortByWeight = (OverlayButton) parentView.findViewById(R.id.obSortByWeight);
        mSortByWeight.setOnClickListener(this);
        getParcelsFromApi();
    }

    @Override
    public void onResume() {
        super.onResume();
        getAPIHits();
    }

    public void getParcelsFromApi() {
        String url = APIUtils.getUrl(APIUtils.API_QUERY_PARCEL);
        NetworkFunctions.getGsonObjectFromQuery(getActivity(), true, url, ParcelAPIResponse.class, this);
    }


    public void getAPIHits() {
        String url = APIUtils.getUrl(APIUtils.API_QUERY_HITS);
        NetworkFunctions.getGsonObjectFromQuery(getActivity(), false, url, HitsAPIResponse.class, this);
    }

    @Override
    public void OnRequestSucessful(Object requestResponse, boolean shouldShowProgressBar) {
        if (requestResponse instanceof ParcelAPIResponse) {
            mParcelsList = new ArrayList<>();
            mParcelsList = Arrays.asList(((ParcelAPIResponse) requestResponse).parcels);
            mParcelsCount.setText(getString(R.string.label_parcels_count, String.valueOf(mParcelsList.size())));
            mParcelsCount.setVisibility(View.VISIBLE);
            if (mAdapter == null) {
                mAdapter = new ParcelArrayAdapter(getActivity(), R.layout.list_item_parcel_row, mParcelsList);
                mListView.setAdapter(mAdapter);
            } else {
                mAdapter.updateParcelsList(mParcelsList);
                mAdapter.notifyDataSetChanged();
            }
            mSwipeRefreshContainer.setRefreshing(false);
        } else if (requestResponse instanceof HitsAPIResponse) {
            String count = ((HitsAPIResponse) requestResponse).api_hits;
            mAPIHitsCount.setText(getString(R.string.label_no_of_api_hits, count));
            mAPIHitsCount.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void OnRequestFailed(String ErrorTag, boolean shouldShowProgressBar) {
        if (shouldShowProgressBar) {
            mSwipeRefreshContainer.setRefreshing(false);
        }
    }

    @Override
    public void OnRequestStarted(String TAG, boolean shouldShowProgressBar) {
        if (shouldShowProgressBar) {
            mSwipeRefreshContainer.setRefreshing(true);
        }
    }

    @Override
    public void onRefresh() {
        getParcelsFromApi();
        getAPIHits();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.obSortByName:
                sortList(new ParcelUtils.NameComparator());
                mSortMenu.collapse();
                break;
            case R.id.obSortByPrice:
                sortList(new ParcelUtils.PriceComparator());
                mSortMenu.collapse();
                break;
            case R.id.obSortByWeight:
                sortList(new ParcelUtils.WeightComparator());
                mSortMenu.collapse();
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ParcelAPIResponse.Parcel object = mAdapter.getItem(position);
        Intent intent = new Intent(getActivity(), DetailsActivity.class);
        intent.putExtra(DetailsActivity.KEY_OBJECT_PARCEL, object);
        startActivity(intent);
    }

    private void sortList(Comparator<ParcelAPIResponse.Parcel> comparator) {
        Collections.sort(mParcelsList, comparator);
        mAdapter.updateParcelsList(mParcelsList);
        mAdapter.notifyDataSetChanged();
    }

    public void filter(String searchConstraint) {
        if (mAdapter != null) {
            if (TextUtils.isEmpty(searchConstraint)) {
                mAdapter.updateParcelsList(mParcelsList);
                mAdapter.notifyDataSetChanged();
            } else {
                mAdapter.getFilter().filter(searchConstraint);
            }
        }
    }

}
