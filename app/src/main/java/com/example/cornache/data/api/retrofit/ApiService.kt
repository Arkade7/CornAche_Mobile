package com.example.cornache.data.api.retrofit

import com.example.cornache.data.api.response.EditProfileResponse
import com.example.cornache.data.api.response.EditRoomResponse
import com.example.cornache.data.api.response.ErrorResponse
import com.example.cornache.data.api.response.LoginResponse
import com.example.cornache.data.api.response.PostCommentResponse
import com.example.cornache.data.api.response.Prediction
import com.example.cornache.data.api.response.RoomResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @FormUrlEncoded
    @POST("login")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): LoginResponse

    @FormUrlEncoded
    @POST("register")
    suspend fun register(
        @Field("username") name: String,
        @Field("password") password: String,
        @Field("confirmPass") confirmPass:String
    ): LoginResponse
    @Multipart
    @POST("room")
    suspend fun createRoom(
        @Part("user_id") userId:RequestBody,
        @Part("name") name: RequestBody,
        @Part file:MultipartBody.Part,
        @Part("description") description:RequestBody
    ):RoomResponse

    @Multipart
    @PUT("profile/{user_id}")
    suspend fun updateProfile(
        @Path("user_id") userId:String,
        @Part("username") username:RequestBody,
        @Part file:MultipartBody.Part
    ):EditProfileResponse

    @Multipart
    @PUT("profile/{user_id}")
    suspend fun updateProfileWithoutImage(
        @Path("user_id") userId: String,
        @Part("username") username: RequestBody
    ): EditProfileResponse

    @Multipart
    @PUT("room/{room_id}")
    suspend fun updateRoom(
        @Path("room_id") roomId:String,
        @Part("user_id") userId:RequestBody,
        @Part("name") name: RequestBody,
        @Part file:MultipartBody.Part,
        @Part("description") description:RequestBody
    ):EditRoomResponse

    @DELETE("room/{room_id}")
    suspend fun deleteRoom(
        @Path("room_id") roomId:String
    ):ErrorResponse
    @FormUrlEncoded
    @POST("chat")
    suspend fun postComment(
        @Field("room_id") roomId:String,
        @Field("content") content:String,
        @Field("user_id") userId:String
    ):PostCommentResponse
}