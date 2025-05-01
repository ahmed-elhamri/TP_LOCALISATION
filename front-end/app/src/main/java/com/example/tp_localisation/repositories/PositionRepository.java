package com.example.tp_localisation.repositories;

import android.content.Context;

import com.example.tp_localisation.apis.PositionApi;
import com.example.tp_localisation.classes.Position;
import com.example.tp_localisation.apis.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;

public class PositionRepository {
    private final Context context;

    public PositionRepository(Context context) {
        this.context = context;
    }

    public void sendPosition(Position position, Callback<String> callback) {
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        PositionApi positionApi = retrofit.create(PositionApi.class);

        // Utilisation de @Field, donc on passe les champs séparément
        Call<String> call = positionApi.sendPosition(
                position.getLatitude(),
                position.getLongitude(),
                position.getDate(),
                position.getImei()
        );

        call.enqueue(callback);
    }

}
