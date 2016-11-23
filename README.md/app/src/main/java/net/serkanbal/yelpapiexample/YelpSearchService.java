package net.serkanbal.yelpapiexample;

import net.serkanbal.yelpapiexample.JSONtoPOJO.RestaurantsMainObject;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by Serkan on 23/11/16.
 */

public interface YelpSearchService {
    @GET("search")
    Call<RestaurantsMainObject> getRestaurants(@Query("term") String term,
                                               @Query("categories") String categories,
                                               @Query("limit") int limit,
                                               @Query("latitude") Double lat,
                                               @Query("longitude") Double lon);
}
