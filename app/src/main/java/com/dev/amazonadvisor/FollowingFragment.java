package com.dev.amazonadvisor;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.OvershootInterpolator;

import com.github.clans.fab.FloatingActionMenu;

import java.util.ArrayList;

public class FollowingFragment extends Fragment {

    private FloatingActionMenu menuFab;
    private Handler uiHandler = new Handler();
    private RecyclerView recyclerView;
    private ListAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_following, container, false);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.my_recycler_view);
        menuFab = (FloatingActionMenu) rootView.findViewById(R.id.menu_yellow);
        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swiperefresh);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        ArrayList<AmazonProduct> itemList = new ArrayList<>();
        AmazonProduct item = new AmazonProduct("Intel Core i7-7700K", "Grazie alla tecnologia Intel Turbo Boost 2.023, il tuo computer può contare su livelli senza precedenti di potenza e reattività per aumentare la tua produttività. Lo streaming fluido per i contenuti 4K premium e l’intrattenimento HD rendono possibili esperienze immersive a tutto schermo 4K e a 360 gradi, per un gaming incredibilmente intenso e visualizzazione di alta qualità. Potrai inoltre contare sulla potenza per creare, modificare e condividere contenuti 4K e a 360 gradi, il tutto con l’incredibile velocità di trasferimento dei dati della tecnologia Thunderbolt 3. Riscontrerai un livello di prestazioni e versatilità come mai prima d’ora.",
                                               "EUR 368,77", getResources().getDrawable(R.drawable.product_demo));
        for (int i = 0; i < 10; i++)
            itemList.add(item);                 //just a demo
        adapter = new ListAdapter(itemList, getActivity());
        recyclerView.setAdapter(adapter);
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                int position = viewHolder.getAdapterPosition();
                adapter.getDataset().remove(position);
                adapter.notifyItemRemoved(position);
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
        swipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {

                    }
                }
        );
        menuFab.setClosedOnTouchOutside(true);
        menuFab.hideMenuButton(false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        int delay = 400;
        uiHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    menuFab.showMenuButton(true);
                }
            }, delay);
        delay += 150;

        menuFab.setOnMenuButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (menuFab.isOpened()) {
                    //do something
                }

                menuFab.toggle(true);
            }
        });

        createCustomAnimation();
    }

    private void createCustomAnimation() {

        AnimatorSet set = new AnimatorSet();

        ObjectAnimator scaleOutX = ObjectAnimator.ofFloat(menuFab.getMenuIconView(), "scaleX", 1.0f, 0.2f);
        ObjectAnimator scaleOutY = ObjectAnimator.ofFloat(menuFab.getMenuIconView(), "scaleY", 1.0f, 0.2f);

        ObjectAnimator scaleInX = ObjectAnimator.ofFloat(menuFab.getMenuIconView(), "scaleX", 0.2f, 1.0f);
        ObjectAnimator scaleInY = ObjectAnimator.ofFloat(menuFab.getMenuIconView(), "scaleY", 0.2f, 1.0f);

        scaleOutX.setDuration(50);
        scaleOutY.setDuration(50);

        scaleInX.setDuration(150);
        scaleInY.setDuration(150);

        scaleInX.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                Animation rotation = AnimationUtils.loadAnimation(getContext(), R.anim.button_rotation);
                rotation.setRepeatMode(Animation.RELATIVE_TO_SELF);
                menuFab.getMenuIconView().startAnimation(rotation);
                if(menuFab.isOpened())
                    menuFab.getMenuIconView().setRotation(0f);
                else
                    menuFab.getMenuIconView().setRotation(45f);
            }
        });

        set.play(scaleOutX).with(scaleOutY);
        set.play(scaleInX).with(scaleInY).after(scaleOutX);
        set.setInterpolator(new OvershootInterpolator(2));

        menuFab.setIconToggleAnimatorSet(set);
    }
}
