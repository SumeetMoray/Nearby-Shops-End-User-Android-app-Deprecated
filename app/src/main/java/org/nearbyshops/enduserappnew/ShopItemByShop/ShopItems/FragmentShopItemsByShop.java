package org.nearbyshops.enduserappnew.ShopItemByShop.ShopItems;

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
import android.widget.TextView;
import android.widget.Toast;

import org.nearbyshops.enduserappnew.DaggerComponentBuilder;
import org.nearbyshops.enduserappnew.Model.ItemCategory;
import org.nearbyshops.enduserappnew.Model.Shop;
import org.nearbyshops.enduserappnew.Model.ShopItem;
import org.nearbyshops.enduserappnew.ModelEndPoints.ShopItemEndPoint;
import org.nearbyshops.enduserappnew.R;
import org.nearbyshops.enduserappnew.API.ShopItemService;
import org.nearbyshops.enduserappnew.ShopsByCategory.Interfaces.NotifyCategoryChanged;
import org.nearbyshops.enduserappnew.ShopsByCategory.Interfaces.NotifyGeneral;
import org.nearbyshops.enduserappnew.ShopsByCategory.Interfaces.NotifySort;
import org.nearbyshops.enduserappnew.ShopsByCategory.Interfaces.NotifyTitleChanged;
import org.nearbyshops.enduserappnew.Preferences.PrefShopHome;
import org.nearbyshops.enduserappnew.ItemsInShopByCat.SlidingLayerSort.UtilitySortItemsInShop;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by sumeet on 25/5/16.
 */
public class FragmentShopItemsByShop extends Fragment implements
        SwipeRefreshLayout.OnRefreshListener, NotifyCategoryChanged, NotifySort{



//        @Inject
//        CartItemService cartItemService;

//        @Inject
//        CartStatsService cartStatsService;



        ItemCategory notifiedCurrentCategory;


        Shop shop;

        ArrayList<ShopItem> dataset = new ArrayList<>();

        boolean isSaved;


        @Inject
        ShopItemService shopItemService;

        RecyclerView recyclerView;
        AdapterShopItems adapter;
        GridLayoutManager layoutManager;

        SwipeRefreshLayout swipeContainer;


        boolean isbackPressed = false;


        final private int limit = 30;
        int offset = 0;
        int item_count = 0;


        boolean isDestroyed;


        @BindView(R.id.itemsInCart)
        public TextView itemsInCart;

        @BindView(R.id.cartTotal)
        public TextView cartTotal;



        public FragmentShopItemsByShop() {
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
        public static FragmentShopItemsByShop newInstance(int sectionNumber, ItemCategory itemCategory) {

            FragmentShopItemsByShop fragment = new FragmentShopItemsByShop();
            Bundle args = new Bundle();
            args.putParcelable("itemCat",itemCategory);
            fragment.setArguments(args);

            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.fragment_shop_item_by_shop, container, false);
            //TextView textView = (TextView) rootView.findViewById(R.id.section_label);

//            itemCategory = getArguments().getParcelable("itemCat");

            ButterKnife.bind(this,rootView);

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
//                    adapter.notifyDataSetChanged();

                }


            setupRecyclerView();
            setupSwipeContainer();

            shop = PrefShopHome.getShop(getActivity());

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

            adapter = new AdapterShopItems(dataset,getActivity(),this);

            recyclerView.setAdapter(adapter);

            layoutManager = new GridLayoutManager(getActivity(),1);
            recyclerView.setLayoutManager(layoutManager);

/*
            recyclerView.addItemDecoration(
                    new DividerItemDecoration(getActivity(),DividerItemDecoration.VERTICAL_LIST)
            );

            recyclerView.addItemDecoration(
                    new DividerItemDecoration(getActivity(),DividerItemDecoration.HORIZONTAL_LIST)
            );
*/

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

                    if(layoutManager.findLastVisibleItemPosition()==dataset.size()-1)
                    {
                        // trigger fetch next page

                        if((offset+limit)<=item_count)
                        {
                            offset = offset + limit;
                            makeNetworkCall(false);
                        }

                    }
                }
            });
        }





        private void makeRefreshNetworkCall() {

            swipeContainer.post(new Runnable() {
                @Override
                public void run() {
                    swipeContainer.setRefreshing(true);

                    try {

                        offset = 0; // reset the offset
//                        dataset.clear();
                        makeNetworkCall(true);

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

            current_sort = UtilitySortItemsInShop.getSort(getContext())
                            + " " + UtilitySortItemsInShop.getAscending(getContext());



            Call<ShopItemEndPoint> shopItemCall = shopItemService.getShopItemEndpoint(
                    notifiedCurrentCategory.getItemCategoryID(),shop.getShopID(),
                    null,null,null,
                    null,null,null,
                    null,null,null,null,
                    null, null,
                    null,true,current_sort,
                    limit,offset,null,
                    true
            );



            shopItemCall.enqueue(new Callback<ShopItemEndPoint>() {
                @Override
                public void onResponse(Call<ShopItemEndPoint> call, Response<ShopItemEndPoint> response) {

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




                        if(!notifiedCurrentCategory.getisAbstractNode() && item_count > 0 && !isbackPressed)
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
                public void onFailure(Call<ShopItemEndPoint> call, Throwable t) {


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
                                " Items (" + String.valueOf(dataset.size())
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
}
