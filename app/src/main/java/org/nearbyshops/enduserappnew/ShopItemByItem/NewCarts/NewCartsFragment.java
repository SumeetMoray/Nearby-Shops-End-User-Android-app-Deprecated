package org.nearbyshops.enduserappnew.ShopItemByItem.NewCarts;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.nearbyshops.enduserappnew.DaggerComponentBuilder;
import org.nearbyshops.enduserappnew.ItemDetailNew.ItemDetailFragment;
import org.nearbyshops.enduserappnew.ItemDetailNew.ItemDetailNew;
import org.nearbyshops.enduserappnew.Login.Login;
import org.nearbyshops.enduserappnew.Model.Item;
import org.nearbyshops.enduserappnew.Model.Shop;
import org.nearbyshops.enduserappnew.ModelEndPoints.ShopItemEndPoint;
import org.nearbyshops.enduserappnew.ModelRoles.User;
import org.nearbyshops.enduserappnew.Preferences.UtilityFunctions;
import org.nearbyshops.enduserappnew.R;
import org.nearbyshops.enduserappnew.API.ShopItemService;
import org.nearbyshops.enduserappnew.ShopHome.ShopHome;
import org.nearbyshops.enduserappnew.ShopItemByItem.Interfaces.NotifyFillCartsChanged;
import org.nearbyshops.enduserappnew.ShopItemByItem.Interfaces.NotifyNewCartsChanged;
import org.nearbyshops.enduserappnew.Preferences.PrefLocation;
import org.nearbyshops.enduserappnew.ShopsByCategory.Interfaces.NotifySort;
import org.nearbyshops.enduserappnew.ShopsByCategory.Interfaces.NotifyTitleChanged;
import org.nearbyshops.enduserappnew.Preferences.PrefLogin;
import org.nearbyshops.enduserappnew.ShopItemByItemNew.SlidingLayerSort.PrefSortShopItems;
import org.nearbyshops.enduserappnew.Preferences.PrefShopHome;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewCartsFragment extends Fragment
        implements SwipeRefreshLayout.OnRefreshListener,
        AdapterNewCarts.NotifyAddToCart, NotifyFillCartsChanged,
        NotifySort {



    Item item;

    @Inject
    ShopItemService shopItemService;

    RecyclerView recyclerView;
    AdapterNewCarts adapter;


    ArrayList<Object> dataset = new ArrayList<>();

    GridLayoutManager layoutManager;
    SwipeRefreshLayout swipeContainer;

//    NotifyPagerAdapter notifyPagerAdapter;

    boolean isDestroyed;




//    TextView itemDescription;
//    TextView itemName;
//    ImageView itemImage;
//    TextView priceRange;
//    TextView shopCount;
//
//
//    @Bind(R.id.item_rating)
//    TextView itemRating;
//
//    @Bind(R.id.rating_count)
//    TextView ratingCount;



    private int limit = 10;
    int offset = 0;
    int item_count = 0;



    public NewCartsFragment() {

        DaggerComponentBuilder.getInstance()
                .getNetComponent()
                .Inject(this);
    }



    public static NewCartsFragment newInstance(Item item) {

        NewCartsFragment fragment = new NewCartsFragment();
        Bundle args = new Bundle();
        args.putParcelable("item",item);
        fragment.setArguments(args);
        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        setRetainInstance(true);
        View rootView = inflater.inflate(R.layout.fragment_new_carts, container, false);

        ButterKnife.bind(this,rootView);


        item = getArguments().getParcelable("item");
        dataset.add(0,item);

        recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);


        swipeContainer = (SwipeRefreshLayout)rootView.findViewById(R.id.swipeContainer);

        if(swipeContainer!=null) {

            swipeContainer.setOnRefreshListener(this);
            swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                    android.R.color.holo_green_light,
                    android.R.color.holo_orange_light,
                    android.R.color.holo_red_light);
        }


        // bindings for Item
//        itemDescription = (TextView) rootView.findViewById(R.id.itemDescription);
//        itemName = (TextView) rootView.findViewById(R.id.itemName);
//        itemImage = (ImageView) rootView.findViewById(R.id.itemImage);
//        priceRange = (TextView) rootView.findViewById(R.id.price_range);
//        shopCount = (TextView) rootView.findViewById(R.id.shop_count);

//        bindItem();


        if(savedInstanceState == null)
        {

            makeRefreshNetworkCall();
        }
        else
        {
            onViewStateRestored(savedInstanceState);
        }

        setupRecyclerView();
        return rootView;
    }


    void setupRecyclerView()
    {
        adapter = new AdapterNewCarts(dataset,getActivity(),this,item,(AppCompatActivity) getActivity(),this);

        recyclerView.setAdapter(adapter);

        layoutManager = new GridLayoutManager(getActivity(),1);
//        layoutManager.setAutoMeasureEnabled(true);
        recyclerView.setLayoutManager(layoutManager);

        //recyclerView.addItemDecoration(
        //        new DividerItemDecoration(this,DividerItemDecoration.VERTICAL_LIST)
        //);

        //recyclerView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.HORIZONTAL_LIST));

        //itemCategoriesList.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL_LIST));

        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

//        layoutManager.setSpanCount(metrics.widthPixels/400);


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
                        makeNetworkCall(false, false);
                    }

                    previous_position = dataset.size();

                }
            }
        });
    }


    int previous_position = -1;



    @Override
    public void onRefresh() {

//        dataset.clear();
        offset=0;
        makeNetworkCall(false, true);
    }



    void makeRefreshNetworkCall()
    {

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



    void makeNetworkCall(final boolean notifyChange, final boolean clearDataset)
    {

            User endUser = PrefLogin.getUser(getActivity());

            Integer endUserID = null;


            if(endUser != null)
            {
                endUserID = endUser.getUserID();
            }




        String current_sort = "";

        current_sort = PrefSortShopItems.getSort(getContext())
                + " " + PrefSortShopItems.getAscending(getContext());


        // Network Available
            Call<ShopItemEndPoint> call = shopItemService.getShopItemEndpoint(
                    null,null,item.getItemID(),
                    PrefLocation.getLatitude(getActivity()),
                    PrefLocation.getLongitude(getActivity()),
                    null, null,
                    null,
                    endUserID,
                    false,
                    null,null,null,null,
                    null,true,current_sort,
                    limit,offset,null,true
            );

            call.enqueue(new Callback<ShopItemEndPoint>() {
                @Override
                public void onResponse(Call<ShopItemEndPoint> call, Response<ShopItemEndPoint> response) {


                    if(isDestroyed)
                    {
                        return;
                    }

                    if(response.body()!=null)
                    {

                        if(clearDataset)
                        {
                            dataset.clear();
                            dataset.add(0,item);
                        }
                        dataset.addAll(response.body().getResults());
                        item_count = response.body().getItemCount();


                        if(notifyChange)
                        {
                            if(getActivity() instanceof NotifyNewCartsChanged)
                            {
                                ((NotifyNewCartsChanged)getActivity()).notifyNewCartsChanged();
                            }
                        }



                    }

                    /*else
                    {
//                        dataset.clear();
                        adapter.notifyDataSetChanged();

                        if(notifyChange)
                        {
                            if(getActivity() instanceof NotifyNewCartsChanged)
                            {
                                ((NotifyNewCartsChanged)getActivity()).notifyNewCartsChanged();
                            }

                        }


                    }
*/

                    adapter.notifyDataSetChanged();
                    notifyTitleChanged();
                    swipeContainer.setRefreshing(false);


                }

                @Override
                public void onFailure(Call<ShopItemEndPoint> call, Throwable t) {


                    if(isDestroyed)
                    {
                        return;
                    }

                    showToastMessage("Network Request failed !");
                    swipeContainer.setRefreshing(false);
                }
            });


    }








    private void showLoginDialog()
    {
//        FragmentManager fm = getActivity().getSupportFragmentManager();
//        LoginDialog loginDialog = new LoginDialog();
//        loginDialog.show(fm,"serviceUrl");


        Intent intent = new Intent(getActivity(),Login.class);
        startActivity(intent);
    }




    void showToastMessage(String message)
    {
        if(getActivity()!=null)
        {
            Toast.makeText(getActivity(),message,Toast.LENGTH_SHORT).show();
        }

    }



    @Override
    public void notifyAddToCart() {

        // change to true
//        dataset.clear();

        swipeContainer.post(new Runnable() {
            @Override
            public void run() {
                swipeContainer.setRefreshing(true);


                offset = 0;
                makeNetworkCall(true,true);

            }
        });
    }



    @Override
    public void listItemHeaderClick(Item item) {

        Intent intent = new Intent(getActivity(), ItemDetailNew.class);
//        intent.putExtra(ItemDetail.ITEM_DETAIL_INTENT_KEY,item);
        String itemJson = UtilityFunctions.provideGson().toJson(item);
        intent.putExtra(ItemDetailFragment.TAG_JSON_STRING,itemJson);
        getActivity().startActivity(intent);
    }




    @Override
    public void notifyShopLogoClick(Shop shop) {

//        Intent intent = new Intent(getActivity(), MarketDetail.class);
//        intent.putExtra(MarketDetail.SHOP_DETAIL_INTENT_KEY,shop);
//        startActivity(intent);

        Intent shopHomeIntent = new Intent(getActivity(), ShopHome.class);
        PrefShopHome.saveShop(shop,getActivity());
        startActivity(shopHomeIntent);

    }

    @Override
    public void notifyFilledCartsChanged() {
//        onRefresh();

//        dataset.clear();
//        makeNetworkCall(false);
        makeRefreshNetworkCall();
    }





    void notifyTitleChanged()
    {
        if(getActivity() instanceof NotifyTitleChanged)
        {
            ((NotifyTitleChanged) getActivity())
                    .NotifyTitleChanged(
                            " New Carts (" + String.valueOf(dataset.size()-1) + "/" + item_count + ")",1
                    );
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
