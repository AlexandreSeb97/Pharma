package com.example.erich.pharma;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.JsonReader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;


import com.example.erich.pharma.api.CupcakeApi;
import com.example.erich.pharma.api.CupcakeResponse;
import com.example.erich.pharma.dummy.DummyContent;
import com.example.erich.pharma.model.Cupcake;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * An activity representing a list of Medicaments. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link MedicamentDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class MedicamentListActivity extends AppCompatActivity {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private static final String LOG_TAG = MedicamentListActivity.class.getSimpleName();
    public static final String API_URL_PROD = "https://pharmabeta.herokuapp.com";
    private RealmConfiguration mRealmConfig;
    private Realm mRealm;
    private List<CupcakeResponse> mCupcakeList;
    private ProgressBar progessBar;
    private View recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medicament_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        progessBar = (ProgressBar) findViewById(R.id.progressBar);
        recyclerView = findViewById(R.id.medicament_list);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);


        initiateCupcakeApi(recyclerView);

        mRealmConfig = new RealmConfiguration.Builder(this).build();
        Realm.setDefaultConfiguration(mRealmConfig);
        mRealm = Realm.getDefaultInstance();

        if (findViewById(R.id.medicament_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }

    }

    private void initiateCupcakeApi(final View recyclerView) {
        Log.d(LOG_TAG,"initiateCupcakeApi");
        Gson gson = new GsonBuilder().setLenient().create();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(API_URL_PROD)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        CupcakeApi api = retrofit.create(CupcakeApi.class);
        Call<CupcakeResponse[]> call = api.getCupcakesList("json");
        call.enqueue(new Callback<CupcakeResponse[]>() {
            @Override
            public void onResponse(Call<CupcakeResponse[]> call, Response<CupcakeResponse[]> response) {

                if (response.isSuccessful()) {
                    Log.d(LOG_TAG, "success - response is " + response.body());

                } else {
                    Log.d(LOG_TAG, "failure response is " + response.raw().toString());

                }
            }

            @Override
            public void onFailure(Call<CupcakeResponse[]> call, Throwable t) {
                Log.e(LOG_TAG, " Error :  " + t.getMessage());
            }

        });

    }

    private void executeRealmWriteTransaction (final List<CupcakeResponse> cupcakeList) {

        Log.d(LOG_TAG,"saveRealmObjects : " + cupcakeList.size());
        mRealm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {

                for (int i = 0; i < cupcakeList.size(); i++) {

                    Cupcake cake = realm.createObject(Cupcake.class);
                    cake.setName(cupcakeList.get(i).name);
                    cake.setRating(cupcakeList.get(i).rating);
                    cake.setPrice(cupcakeList.get(i).price);
                    cake.setImage(cupcakeList.get(i).image);
                    cake.setWritere(cupcakeList.get(i).writer);
                    cake.setCreatedAt(cupcakeList.get(i).createdAt);
                }


            }
        },new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                Log.d(LOG_TAG,"savedRealmObjects");
            }

        },new Realm.Transaction.OnError(){

            @Override
            public void onError(Throwable error) {
                Log.d(LOG_TAG,"error while writing to realm db :" + error.getMessage());
            }
        });
    }

    private void deleteRealmObjects() {

        mRealm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.delete(Cupcake.class);
            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                Log.d(LOG_TAG,"realm objects deleted");
                initiateCupcakeApi(recyclerView);
            }
        });
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(DummyContent.ITEMS));
    }

    public class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final List<DummyContent.DummyItem> mValues;

        public SimpleItemRecyclerViewAdapter(List<DummyContent.DummyItem> items) {
            mValues = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.medicament_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.mItem = mValues.get(position);
            holder.mIdView.setText(mValues.get(position).id);
            holder.mContentView.setText(mValues.get(position).content);

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mTwoPane) {
                        Bundle arguments = new Bundle();
                        arguments.putString(MedicamentDetailFragment.ARG_ITEM_ID, holder.mItem.id);
                        MedicamentDetailFragment fragment = new MedicamentDetailFragment();
                        fragment.setArguments(arguments);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.medicament_detail_container, fragment)
                                .commit();
                    } else {
                        Context context = v.getContext();
                        Intent intent = new Intent(context, MedicamentDetailActivity.class);
                        intent.putExtra(MedicamentDetailFragment.ARG_ITEM_ID, holder.mItem.id);

                        context.startActivity(intent);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final TextView mIdView;
            public final TextView mContentView;
            public DummyContent.DummyItem mItem;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mIdView = (TextView) view.findViewById(R.id.id);
                mContentView = (TextView) view.findViewById(R.id.content);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + mContentView.getText() + "'";
            }
        }

    }
}
