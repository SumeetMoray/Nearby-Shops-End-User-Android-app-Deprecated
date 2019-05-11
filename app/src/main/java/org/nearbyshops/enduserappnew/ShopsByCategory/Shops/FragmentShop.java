package org.nearbyshops.enduserappnew.ShopsByCategory.Shops;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.nearbyshops.enduserappnew.DaggerComponentBuilder;
import org.nearbyshops.enduserappnew.Model.ItemCategory;
import org.nearbyshops.enduserappnew.Model.Shop;
import org.nearbyshops.enduserappnew.ModelEndPoints.ShopEndPoint;
import org.nearbyshops.enduserappnew.Preferences.PrefLocation;
import org.nearbyshops.enduserappnew.R;
import org.nearbyshops.enduserappnew.API.ShopService;
import org.nearbyshops.enduserappnew.ShopsByCategory.Interfaces.NotifyCategoryChanged;
import org.nearbyshops.enduserappnew.ShopsByCategory.Interfaces.NotifyGeneral;
import org.nearbyshops.enduserappnew.ShopsByCategory.Interfaces.NotifySort;
import org.nearbyshops.enduserappnew.ShopsByCategory.Interfaces.NotifyTitleChanged;
import org.nearbyshops.enduserappnew.Utility.DividerItemDecoration;
import org.nearbyshops.enduserappnew.Shops.SlidingLayerSort.PrefSortShopsByCategory;

import java.util.ArrayList;

import javax.inject.Inject;

//import icepick.Icepick;
//import icepick.State;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by sumeet on 25/5/16.
 */
public class FragmentShop extends Fragment implements
        SwipeRefreshLayout.OnRefreshListener, NotifyCategoryChanged, NotifySort{


        ItemCategory notifiedCurrentCategory;

        ArrayList<Shop> dataset = new ArrayList<>();

        boolean isSaved;


        @Inject ShopService shopService;

        RecyclerView recyclerView;
        AdapterShop adapter;
        GridLayoutManager layoutManager;

        SwipeRefreshLayout swipeContainer;


        boolean isbackPressed = false;


        final private int limit = 10;
        int offset = 0;
        int item_count = 0;



        boolean isDestroyed;



        public FragmentShop() {
            // inject dependencies through dagger
            DaggerComponentBuilder.getInstance()
                    .getNetComponent().Inject(this);


            notifiedCurrentCategory = new ItemCategory();
            notifiedCurrentCategory.setItemCategoryID(1);
            notifiedCurrentCategory.setCategoryName("");
            notifiedCurrentCategory.setParentCategoryID(-1);

            Log.d("applog","Shop Fragment Constructor");

        }

    /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static FragmentShop newInstance(int sectionNumber, ItemCategory itemCategory) {

            FragmentShop fragment = new FragmentShop();
            Bundle args = new Bundle();
            args.putParcelable("itemCat",itemCategory);
            fragment.setArguments(args);

            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            setRetainInstance(true);
            View rootView = inflater.inflate(R.layout.fragment_shops, container, false);
            //TextView textView = (TextView) rootView.findViewById(R.id.section_label);

//            itemCategory = getArguments().getParcelable("itemCat");

            recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
            swipeContainer = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeContainer);


                if(savedInstanceState==null)
                {

                    // ensure that there is no swipe to right on first fetch
                    isbackPressed = true;
                    makeRefreshNetworkCall();
                    isSaved = true;

                }
                else
                {
                    Log.d("shopsbycategory","saved State");
                    onViewStateRestored(savedInstanceState);
                }


            setupRecyclerView();
            setupSwipeContainer();

            return rootView;
        }



        void setupSwipeContainer()
        {
            if(swipeContainer!=null) {

                swipeContainer.setOnRefreshListener(this);
                swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                        android.R.color.holo_green_light,
                        android.R.color.holo_orange_light,
                        android.R.color.holo_red_light);
            }

        }





        void setupRecyclerView()
        {

            adapter = new AdapterShop(dataset,getActivity(),this);

            recyclerView.setAdapter(adapter);

            layoutManager = new GridLayoutManager(getActivity(),1);
            recyclerView.setLayoutManager(layoutManager);

            recyclerView.addItemDecoration(
                    new DividerItemDecoration(getActivity(),DividerItemDecoration.VERTICAL_LIST)
            );

            recyclerView.addItemDecoration(
                    new DividerItemDecoration(getActivity(),DividerItemDecoration.HORIZONTAL_LIST)
            );

            //itemCategoriesList.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL_LIST));

            DisplayMetrics metrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

//            layoutManager.setSpanCount(metrics.widthPixels/350);


            int spanCount = (int) (metrics.widthPixels/(230 * metrics.density));

            if(spanCount==0){
                spanCount = 1;
            }

            layoutManager.setSpanCount(spanCount);


            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);

                    if(layoutManager.findLastVisibleItemPosition()==dataset.size())
                    {

                        // trigger fetch next page

                        if(dataset.size()== previous_position)
                        {
                            return;
                        }


                        // trigger fetch next page

                        if((offset+limit)<=item_count)
                        {
                            offset = offset + limit;
                            makeNetworkCall(false);
                        }

                        previous_position = dataset.size();

                    }
                }
            });
        }


    int previous_position = -1;





        private void makeRefreshNetworkCall() {

            swipeContainer.post(new Runnable() {
                @Override
                public void run() {
                    swipeContainer.setRefreshing(true);

                    try {

                        onRefresh();

                    } catch (IllegalArgumentException ex)
                    {
                        ex.printStackTrace();

                    }
                }
            });

        }



        @Override
        public void onRefresh() {

            offset = 0; // reset the offset
//            dataset.clear();
            makeNetworkCall(true);
        }







        private void makeNetworkCall(final boolean clearDataset)
        {

            if(notifiedCurrentCategory==null)
            {
                swipeContainer.setRefreshing(false);
                return;
            }

            String current_sort = "";

            current_sort = PrefSortShopsByCategory.getSort(getContext()) + " " + PrefSortShopsByCategory.getAscending(getContext());


            Call<ShopEndPoint> callEndpoint = shopService.filterShopsByItemCategory(

                    notifiedCurrentCategory.getItemCategoryID(),
                    null,
                    PrefLocation.getLatitude(getActivity()),
                    PrefLocation.getLongitude(getActivity()),
                    null,
                    null,
                    null,
                    current_sort,limit,offset,false
            );



            callEndpoint.enqueue(new Callback<ShopEndPoint>() {
                @Override
                public void onResponse(Call<ShopEndPoint> call, Response<ShopEndPoint> response) {

                    if(isDestroyed)
                    {
                        return;
                    }

    //                dataset.clear();

                    if(response.body()!=null)
                    {
                        if(clearDataset)
                        {
                            dataset.clear();
                        }
                        dataset.addAll(response.body().getResults());

                        if(response.body().getItemCount()!=null)
                        {
                            item_count = response.body().getItemCount();
                        }




                        if(!notifiedCurrentCategory.getisAbstractNode() && item_count>0 && !isbackPressed)
                        {
                            if(getActivity() instanceof NotifyGeneral)
                            {
                                ((NotifyGeneral)getActivity()).notifySwipeToright();
                            }

                            // reset the flag
                            isbackPressed = false;
                        }
                    }


                    notifyTitleChanged();

                    adapter.notifyDataSetChanged();
                    swipeContainer.setRefreshing(false);

                }

                @Override
                public void onFailure(Call<ShopEndPoint> call, Throwable t) {

                    if(isDestroyed)
                    {
                        return;
                    }

                    showToastMessage(getString(R.string.network_request_failed));
                    swipeContainer.setRefreshing(false);
                }
            });

        }



        void showToastMessage(String message)
        {
            Toast.makeText(getActivity(),message,Toast.LENGTH_SHORT).show();
        }





    // apply ice pack


//        @Override
//        public void onSaveInstanceState(Bundle outState) {
//            super.onSaveInstanceState(outState);
//
//            Icepick.saveInstanceState(this, outState);
//        }



//        @Override
//        public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
//            super.onViewStateRestored(savedInstanceState);
//
//            Icepick.restoreInstanceState(this, savedInstanceState);
//            notifyTitleChanged();
//        }


        @Override
        public void itemCategoryChanged(ItemCategory currentCategory, Boolean isBackPressed) {


            notifiedCurrentCategory = currentCategory;
//            dataset.clear();
            offset = 0 ; // reset the offset
            makeNetworkCall(true);

            this.isbackPressed = isBackPressed;
        }



        void notifyTitleChanged()
        {
            String name = "";

            if(notifiedCurrentCategory!=null)
            {
                name = notifiedCurrentCategory.getCategoryName();
            }


            if(getActivity() instanceof NotifyTitleChanged)
            {
                ((NotifyTitleChanged)getActivity())
                        .NotifyTitleChanged( name +
                                " Shops (" + String.valueOf(dataset.size())
                                + "/" + String.valueOf(item_count) + ")",1);
            }
        }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isDestroyed = true;
    }



    @Override
    public void notifySortChanged() {
        makeRefreshNetworkCall();
    }


    public int getItemCount() {
        return item_count;
    }
}
