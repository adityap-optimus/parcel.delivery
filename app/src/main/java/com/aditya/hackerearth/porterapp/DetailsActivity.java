package com.aditya.hackerearth.porterapp;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.aditya.hackerearth.porterapp.helper.network.APIUtils;
import com.aditya.hackerearth.porterapp.helper.network.ImageRequest;
import com.aditya.hackerearth.porterapp.helper.network.NetworkFunctions;
import com.aditya.hackerearth.porterapp.helper.network.RequestListener;
import com.aditya.hackerearth.porterapp.helper.ui.OverlayButton;
import com.aditya.hackerearth.porterapp.helper.ui.OverlayMenu;
import com.aditya.hackerearth.porterapp.model.ParcelAPIResponse;
import com.aditya.hackerearth.porterapp.model.ParcelUtils;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

public class DetailsActivity extends AppCompatActivity implements RequestListener<ParcelAPIResponse>, View.OnClickListener {

    public final static String KEY_OBJECT_PARCEL = "KEY_OBJECT_PARCEL";

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private ArrayList<LatLng> mLocations;
    private ParcelAPIResponse.Parcel mCurrentParcel;
    private NetworkImageView mProductImageView;
    private ImageLoader mImageLoader;
    private TextView mTvDate, mTvProductTitle, mTvProductType, mTvProductPrice, mTvProductWeight, mTvProductQuantity, mTvContactNo;
    private View mViewColor;
    private OverlayMenu mMoreMenu;
    private OverlayButton mOBShare, mOBLink, mOBsms, mOBRefresh;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(getResources().getColor(R.color.bg_statusbar));
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        setContentView(R.layout.activity_details);
        mCurrentParcel = (ParcelAPIResponse.Parcel) getIntent().getSerializableExtra(KEY_OBJECT_PARCEL);
        mLocations = new ArrayList<>();
        mLocations.add(new LatLng(mCurrentParcel.live_location.latitude, mCurrentParcel.live_location.longitude));
        initViews();
        setUpMapIfNeeded();
    }

    private void initViews() {
        mProductImageView = (NetworkImageView) findViewById(R.id
                .ivProductImage);

        mTvDate = (TextView) findViewById(R.id.tvDate);
        mTvDate.setText(ParcelUtils.getDate(mCurrentParcel.date));

        mTvProductTitle = (TextView) findViewById(R.id.tvProductTitle);
        mTvProductTitle.setText(mCurrentParcel.name);

        mTvProductType = (TextView) findViewById(R.id.tvProductType);
        mTvProductType.setText(mCurrentParcel.type);

        mTvProductPrice = (TextView) findViewById(R.id.tvProductPrice);
        mTvProductPrice.setText(mCurrentParcel.price);

        mTvProductWeight = (TextView) findViewById(R.id.tvProductWeight);
        mTvProductWeight.setText(mCurrentParcel.weight);

        mTvProductQuantity = (TextView) findViewById(R.id.tvProductQuantity);
        mTvProductQuantity.setText(String.valueOf(mCurrentParcel.quantity));

        mViewColor = findViewById(R.id.viewColor);
        int decode = Integer.parseInt(mCurrentParcel.color.replace("#", ""), 16);
        mViewColor.setBackgroundColor(0xff000000 + decode);

        mTvContactNo = (TextView) findViewById(R.id.tvContactNo);
        mTvContactNo.setText(mCurrentParcel.phone);

        mOBRefresh = (OverlayButton) findViewById(R.id.obRefresh);
        mOBRefresh.setOnClickListener(this);

        mMoreMenu = (OverlayMenu) findViewById(R.id.omShareMenu);
        mOBLink = (OverlayButton) findViewById(R.id.obLink);
        mOBLink.setOnClickListener(this);

        mOBShare = (OverlayButton) findViewById(R.id.obShare);
        mOBShare.setOnClickListener(this);

        mOBsms = (OverlayButton) findViewById(R.id.obSMS);
        mOBsms.setOnClickListener(this);

    }

    @Override
    protected void onStart() {
        super.onStart();
        // Instantiate the RequestQueue.
        mImageLoader = ImageRequest.getInstance(this.getApplicationContext())
                .getImageLoader();
        //Image URL - This can point to any image file supported by Android
        mImageLoader.get(mCurrentParcel.image, ImageLoader.getImageListener(mProductImageView,
                R.drawable.ic_placeholder, android.R.drawable
                        .ic_dialog_alert));
        mProductImageView.setImageUrl(mCurrentParcel.image, mImageLoader);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {
        if (mMap == null) {
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            if (mMap != null) {
                drawMap();
            }
        }
    }

    private void drawMap() {
        mMap.clear();
        LatLng lastLocation = mLocations.get(mLocations.size() - 1);
        mMap.addMarker(new MarkerOptions().position(lastLocation).title(mCurrentParcel.name));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                lastLocation, 14));
        if (mLocations.size() > 1) {
            PolylineOptions options = new PolylineOptions();
            options.geodesic(true);
            for (LatLng loc : mLocations) {
                options.add(loc);
            }
            mMap.addPolyline(options);
        }
    }

    public void getParcelsFromApi() {
        String url = APIUtils.getUrl(APIUtils.API_QUERY_PARCEL);
        NetworkFunctions.getGsonObjectFromQuery(DetailsActivity.this, true, url, ParcelAPIResponse.class, this);
        Toast.makeText(DetailsActivity.this, "Updating Location", Toast.LENGTH_LONG).show();
    }

    @Override
    public void OnRequestSucessful(ParcelAPIResponse requestResponse, boolean shouldShowProgressBar) {
        for (ParcelAPIResponse.Parcel obj : requestResponse.parcels) {
            if (obj.name.equals(mCurrentParcel.name)) {
                mCurrentParcel = obj;
                mLocations.add(new LatLng(obj.live_location.latitude, obj.live_location.longitude));
                drawMap();
            }
        }
    }

    @Override
    public void OnRequestFailed(String ErrorTag, boolean shouldShowProgressBar) {

    }

    @Override
    public void OnRequestStarted(String TAG, boolean shouldShowProgressBar) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.obRefresh:
                getParcelsFromApi();
                break;
            case R.id.obShare:
                mMoreMenu.collapse();
                startActivity(getShareIntent());
                break;
            case R.id.obLink:
                mMoreMenu.collapse();
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mCurrentParcel.link));
                startActivity(browserIntent);
                break;
            case R.id.obSMS:
                EnterPhoneNumberFragment dialog = new EnterPhoneNumberFragment();
                dialog.message = getShareMessage();
                dialog.show(getSupportFragmentManager(), "EnterPhoneNumberFragment");
                mMoreMenu.collapse();
                break;
        }
    }

    private Intent getShareIntent() {
        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.label_share, mCurrentParcel.name));
        intent.putExtra(Intent.EXTRA_TEXT, getShareMessage());
        return intent;
    }

    private static void sendSMS(String phoneNumber, String link) {
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phoneNumber, null, link, null, null);
    }


    public static class EnterPhoneNumberFragment extends DialogFragment {
        public String message;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            // Get the layout inflater
            LayoutInflater inflater = getActivity().getLayoutInflater();
            final View layout = inflater.inflate(R.layout.dialog_enter_number, null);
            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            builder.setView(layout)
                    // Add action buttons
                    .setPositiveButton(R.string.label_send, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            EditText phonehumber = (EditText) layout.findViewById(R.id.etPhoneNumber);
                            sendSMS(phonehumber.getText().toString(), message);
                            Toast.makeText(getActivity(), "Message Send", Toast.LENGTH_LONG).show();
                        }
                    })
                    .setNegativeButton(R.string.label_cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            EnterPhoneNumberFragment.this.getDialog().cancel();
                        }
                    });
            return builder.create();
        }
    }

    private String getShareMessage() {
        StringBuilder message = new StringBuilder();
        message.append("Link : ").append(mCurrentParcel.link)
                .append("\nLocation : http://maps.google.com/?q=")
                .append(mCurrentParcel.live_location.latitude)
                .append(",")
                .append(mCurrentParcel.live_location.longitude);
        return message.toString();
    }
}
