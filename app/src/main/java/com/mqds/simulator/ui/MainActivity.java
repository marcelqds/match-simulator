package com.mqds.simulator.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.snackbar.Snackbar;
import com.mqds.simulator.R;
import com.mqds.simulator.data.MatchesApi;
import com.mqds.simulator.databinding.ActivityMainBinding;
import com.mqds.simulator.domain.Match;
import com.mqds.simulator.ui.adapter.MatchesAdapter;

import java.util.List;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private MatchesApi matchesApi;
    private MatchesAdapter matchesAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setupHttpClient();
        setupMatchesList();
        setupMatchesRefresh();
        setupFloatingActionButton();
    }

    private void setupHttpClient() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://marcelqds.github.io/match-simulator-api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        matchesApi = retrofit.create(MatchesApi.class);
    }

    private void setupMatchesRefresh() {
        binding.srlMatches.setOnRefreshListener(this::findMatchesFromApi);
    }

    private void setupMatchesList() {
        binding.rvMatches.setHasFixedSize(true);
        binding.rvMatches.setLayoutManager(new LinearLayoutManager(this));
        findMatchesFromApi();
    }

    private void setupFloatingActionButton() {
        binding.fabSimulate.setOnClickListener(view -> {
            view.animate().rotationBy(360).setDuration(500).setListener( new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
//                  super.onAnimationEnd(animation);
                    Random random = new Random();
                    for(int i = 0; i< matchesAdapter.getItemCount(); i++){
                        Match match = matchesAdapter.getMatches().get(i);
                        match.getHomeTeam().setScore(random.nextInt(match.getHomeTeam().getStars() + 1));
                        match.getAwayTeam().setScore(random.nextInt(match.getAwayTeam().getStars() + 1));
                        matchesAdapter.notifyItemChanged(i);
                    }
                }
            });
        });
    }

    private void findMatchesFromApi(){
        binding.srlMatches.setRefreshing(true);
        matchesApi.getMatches().enqueue(new Callback<List<Match>>(){

            @Override
            public void onResponse(Call<List<Match>> call, Response<List<Match>> response) {
                if(!response.isSuccessful()){ showErrorMessage(); return;};

                List<Match> matches = response.body();
                matchesAdapter = new MatchesAdapter(matches);
                binding.rvMatches.setAdapter(matchesAdapter);
                binding.srlMatches.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<List<Match>> call, Throwable t) {
                showErrorMessage();
            }
        });

    }
    private void showErrorMessage(){
        binding.srlMatches.setRefreshing(false);
        Snackbar.make(binding.fabSimulate, R.string.error_api,Snackbar.LENGTH_LONG).show();


    }
}
