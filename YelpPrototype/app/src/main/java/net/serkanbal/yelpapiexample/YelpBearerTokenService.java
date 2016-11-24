package net.serkanbal.yelpapiexample;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by Serkan on 23/11/16.
 */

public interface YelpBearerTokenService {

    @FormUrlEncoded
    @POST("token")
    Call<YelpBearerTokenObject> getBearerToken(@Field("grant_type") String grant_type,
                                               @Field("client_id") String client_id,
                                               @Field("client_secret") String client_secret);
}