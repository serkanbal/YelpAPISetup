package net.serkanbal.yelpapiexample;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import net.serkanbal.yelpapiexample.JSONtoPOJO.Business;
import net.serkanbal.yelpapiexample.JSONtoPOJO.RestaurantsMainObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static java.lang.Integer.parseInt;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    public static final String TAG = "Serkan";
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    Double mLatValue;
    Double mLonValue;
    TextView mLatText, mLonText;
    EditText mQuery, mRadiusInMeters;
    Button mSearch;
    String mBearerToken = "";
    List<Business> mBusinessList;

    RecyclerView mRecyclerView;
    BusinessRecyclerViewAdapter mAdapter;

    public static final String[] PERMISSION_LOCATION = {
            android.Manifest.permission.ACCESS_COARSE_LOCATION
    };
    public static final int REQUEST_CODE_LOCATION = 1;

    public static final String YELP_GRANT_TYPE = "client_credentials";
    public static final String YELP_CLIENT_ID = "9YI_a1fQDy1CtSBiZsq6Yw";
    public static final String YELP_APP_SECRET = "edmHqYlFEY4SxJWqkgM76RmkVaXnRyr6UUdc9jXzQglM8TCELkWRGN37CBG3Ztpy";
    public static final String YELP_TOKEN_BASE_URL = "https://api.yelp.com/oauth2/";
    public static final String YELP_SEARCH_BASE_URL = "https://api.yelp.com/v3/businesses/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViews();

        mBusinessList = new ArrayList<>();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mAdapter = new BusinessRecyclerViewAdapter(mBusinessList);

        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(linearLayoutManager);



        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        getBearerToken();

        mSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String radiusInString = mRadiusInMeters.getText().toString();
                int radiusInInt = (Integer.parseInt(radiusInString))*1000;//Changed to km from meters
                getRestaurants(mQuery.getText().toString(), radiusInInt);
            }
        });

    }

    /**
     * This is to get the bearer token which we will be used to authenticate our api requests.
     */
    public void getBearerToken() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(YELP_TOKEN_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            YelpBearerTokenService service = retrofit.create(YelpBearerTokenService.class);
            Call<YelpBearerTokenObject> call = service.getBearerToken(YELP_GRANT_TYPE, YELP_CLIENT_ID,
                    YELP_APP_SECRET);

            call.enqueue(new Callback<YelpBearerTokenObject>() {
                @Override
                public void onResponse(Call<YelpBearerTokenObject> call, Response<YelpBearerTokenObject> response) {
                    mBearerToken = response.body().getAccessToken();
                    Log.d(TAG, "onResponse: " + mBearerToken);
                }

                @Override
                public void onFailure(Call<YelpBearerTokenObject> call, Throwable t) {
                    //Show error message.
                }
            });
        }
    }

    public void getRestaurants(String query, int radius) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(YELP_SEARCH_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        YelpSearchService service = retrofit.create(YelpSearchService.class);
        Call<RestaurantsMainObject> call = service.getRestaurants("Bearer "+mBearerToken, query, "restaurants",
                40, mLatValue, mLonValue, radius);

        call.enqueue(new Callback<RestaurantsMainObject>() {
            @Override
            public void onResponse(Call<RestaurantsMainObject> call, Response<RestaurantsMainObject> response) {
                mBusinessList = response.body().getBusinesses();
                List<Business> randomList = pickRandom(mBusinessList,3);

                mAdapter.replaceList(randomList);
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<RestaurantsMainObject> call, Throwable t) {
                //Do nothing.
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }


    /**
     * Checking for permission; if permission granted get Lat & Lon value; else ask for permission.
     */

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
            if (mLastLocation != null) {
                mLatValue = mLastLocation.getLatitude();
                mLonValue = mLastLocation.getLongitude();

                mLatText.setText("Your lat :\n" + mLatValue.toString());
                mLonText.setText("Your lon :\n" + mLonValue.toString());

            }
        } else {
            verifyLocationPermissions(this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        //Do nothing.
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        //Do nothing.
    }

    /**
     * This method is to ask for location permission.
     */

    public static void verifyLocationPermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity,
                android.Manifest.permission.ACCESS_COARSE_LOCATION);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSION_LOCATION,
                    REQUEST_CODE_LOCATION
            );
        }
    }

    /**
     * What to do after we get the location permission for the first time.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_LOCATION: {
                //Ignore this correction; we only run this if/when we get the permission; so it can never be a problem.
                mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                        mGoogleApiClient);
                if (mLastLocation != null) {
                    mLatValue = mLastLocation.getLatitude();
                    mLonValue = mLastLocation.getLongitude();

                    mLatText.setText("Your lat :\n" + mLatValue.toString());
                    mLonText.setText("Your lon :\n" + mLonValue.toString());
                }
                break;
            }
        }
    }
    public void findViews(){
        mLatText = (TextView) findViewById(R.id.lat);
        mLonText = (TextView) findViewById(R.id.lon);
        mQuery = (EditText) findViewById(R.id.query);
        mRadiusInMeters = (EditText) findViewById(R.id.radius);
        mSearch = (Button) findViewById(R.id.search);
        mRecyclerView =(RecyclerView)findViewById(R.id.recycler_view);

    }

    public List<Business> pickRandom(List<Business> businessList, int numberToPick){
        List<Business> randomPicks = new ArrayList<>();
        Random picker = new Random();
        if (businessList.size()<=numberToPick){
            Toast.makeText(this,"Not enough choices",Toast.LENGTH_LONG).show();
            return businessList;
        }
        for (int i =0; i<numberToPick;i++){
            int randomIndex = picker.nextInt(businessList.size());
            randomPicks.add(businessList.get(randomIndex));
            businessList.remove(randomIndex);
        }
        return randomPicks;
    }
}
