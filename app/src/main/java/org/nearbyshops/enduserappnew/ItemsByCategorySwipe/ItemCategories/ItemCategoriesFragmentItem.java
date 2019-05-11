package org.nearbyshops.enduserappnew.ItemsByCategorySwipe.ItemCategories;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.nearbyshops.enduserappnew.DaggerComponentBuilder;
import org.nearbyshops.enduserappnew.Model.ItemCategory;
import org.nearbyshops.enduserappnew.ModelEndPoints.ItemCategoryEndPoint;
import org.nearbyshops.enduserappnew.Preferences.PrefLocation;
import org.nearbyshops.enduserappnew.R;
import org.nearbyshops.enduserappnew.API.ItemCategoryService;
import org.nearbyshops.enduserappnew.ShopsByCategory.Interfaces.NotifyBackPressed;
import org.nearbyshops.enduserappnew.ShopsByCategory.Interfaces.NotifyCategoryChanged;
import org.nearbyshops.enduserappnew.ShopsByCategory.Interfaces.NotifyGeneral;
import org.nearbyshops.enduserappnew.ShopsByCategory.Interfaces.NotifyTitleChanged;
import org.nearbyshops.enduserappnew.ShopsByCategory.Interfaces.ToggleFab;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
//import icepick.Icepick;
//import icepick.State;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ItemCategoriesFragmentItem extends Fragment
        implements ItemCategoriesAdapter.ReceiveNotificationsFromAdapter,
        SwipeRefreshLayout.OnRefreshListener,
        NotifyBackPressed {




    ArrayList<ItemCategory> dataset = new ArrayList<>();

    RecyclerView itemCategoriesList;
    ItemCategoriesAdapter listAdapter;
    GridLayoutManager layoutManager;

    boolean show = true;

    @Inject
    ItemCategoryService itemCategoryService;


    ItemCategory currentCategory = null;



    private int limit = 30;
    int offset = 0;
    int item_count = 0;


    @BindView(R.id.swipeContainer)
    SwipeRefreshLayout swipeContainer;


    boolean isDestroyed;





    public ItemCategoriesFragmentItem() {
        super();

        // Inject the dependencies using Dependency Injection
        DaggerComponentBuilder.getInstance()
                .getNetComponent().Inject(this);

        currentCategory = new ItemCategory();
        currentCategory.setItemCategoryID(1);
        currentCategory.setCategoryName("");
        currentCategory.setParentCategoryID(-1);
    }




    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        setRetainInstance(true);
        super.onCreateView(inflater, container, savedInstanceState);


        View rootView = inflater.inflate(R.layout.fragment_item_categories_items_by_category, container, false);
        ButterKnife.bind(this,rootView);

//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        itemCategoriesList = (RecyclerView)rootView.findViewById(R.id.recyclerViewItemCategories);


        if(savedInstanceState==null)
        {
            // make request to the network only for the first time and not the second time or when the context is changed.

            // reset the offset before making request


            swipeContainer.post(new Runnable() {
                @Override
                public void run() {
                    swipeContainer.setRefreshing(true);

                    try {


                        // make a network call
                        offset = 0;
                        dataset.clear();
                        makeRequestRetrofit(false,false);


                    } catch (IllegalArgumentException ex)
                    {
                        ex.printStackTrace();

                    }
                }
            });


        }
        else
        {
            onViewStateRestored(savedInstanceState);
        }



        setupRecyclerView();
        setupSwipeContainer();


        return  rootView;

    }


    void setupRecyclerView()
    {

        listAdapter = new ItemCategoriesAdapter(dataset,getActivity(),this,this);
        itemCategoriesList.setAdapter(listAdapter);
        layoutManager = new GridLayoutManager(getActivity(),1, LinearLayoutManager.VERTICAL,false);
        itemCategoriesList.setLayoutManager(layoutManager);


        // Code for Staggered Grid Layout
        /*layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return (position % 3 == 0 ? 2 : 1);
            }
        });
        */


        final DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

//        layoutManager.setSpanCount(metrics.widthPixels/350);


        int spanCount = (int) (metrics.widthPixels/(230 * metrics.density));

        if(spanCount==0){
            spanCount = 1;
        }

        layoutManager.setSpanCount(spanCount);



        itemCategoriesList.addOnScrollListener(new RecyclerView.OnScrollListener() {


            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if(layoutManager.findLastVisibleItemPosition()==dataset.size()-1)
                {
                    // trigger fetch next page

                    if((offset+limit)<=item_count)
                    {
                        offset = offset + limit;
                        makeRequestRetrofit(false,false);
                    }

                }
            }


            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);


                if(dy > 20)
                {

                    boolean previous = show;

                    show = false ;

                    if(show!=previous)
                    {
                        // changed
                        Log.d("scrolllog","show");

                        if(getActivity() instanceof ToggleFab)
                        {
                            ((ToggleFab)getActivity()).hideFab();
                        }
                    }

                }else if(dy < -20)
                {

                    boolean previous = show;

                    show = true;

                    if(show!=previous)
                    {
                        Log.d("scrolllog","hide");

                        if(getActivity() instanceof ToggleFab)
                        {
                            ((ToggleFab)getActivity()).showFab();
                        }
                    }
                }


            }

        });

    }



    void setupSwipeContainer()
    {

        if(swipeContainer!=null) {

            swipeContainer.setOnRefreshListener(this);
            swipeContainer.setColorSchemeResources(
                    android.R.color.holo_blue_bright,
                    android.R.color.holo_green_light,
                    android.R.color.holo_orange_light,
                    android.R.color.holo_red_light);
        }

    }



    public void makeRequestRetrofit(final boolean notifyItemCategoryChanged, final boolean backPressed)
    {

//        Shop currentShop = ApplicationState.getInstance().getCurrentShop();


        Call<ItemCategoryEndPoint> endPointCall = itemCategoryService.getItemCategoriesEndPoint(
                null,
                currentCategory.getItemCategoryID(),
                null,
                PrefLocation.getLatitude(getActivity()),
                PrefLocation.getLongitude(getActivity()),
                null,
                null,
                null,true,
                "id",limit,offset,false);

        Log.d("applog","DetachedTabs: Network call made !");


        endPointCall.enqueue(new Callback<ItemCategoryEndPoint>() {
            @Override
            public void onResponse(Call<ItemCategoryEndPoint> call, Response<ItemCategoryEndPoint> response) {


                if(isDestroyed)
                {
                    return;
                }


                if(response.body()!=null)
                {
                    ItemCategoryEndPoint endPoint = response.body();
                    item_count = endPoint.getItemCount();
                    dataset.addAll(endPoint.getResults());
                    listAdapter.notifyDataSetChanged();

                    if(notifyItemCategoryChanged)
                    {
                        if(currentCategory!=null)
                        {

                            ((NotifyCategoryChanged)getActivity())
                                    .itemCategoryChanged(currentCategory,backPressed);
                        }
                    }

                    notifyTitleChanged();


                }

                swipeContainer.setRefreshing(false);

            }

            @Override
            public void onFailure(Call<ItemCategoryEndPoint> call, Throwable t) {


                if(isDestroyed)
                {
                    return;
                }


                showToastMessage("Network request failed. Please check your connection !");

                if(swipeContainer!=null)
                {
                    swipeContainer.setRefreshing(false);
                }
            }
        });

    }




    private void showToastMessage(String message)
    {
        if(getActivity()!=null)
        {
            Toast.makeText(getActivity(),message,Toast.LENGTH_SHORT).show();
        }
    }




/*
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 1)
        {
            if(resultCode == Activity.RESULT_OK)
            {
                ItemCategory parentCategory = data.getParcelableExtra("result");

                if(parentCategory!=null)
                {

                    listAdapter.getRequestedChangeParent().setParentCategoryID(parentCategory.getItemCategoryID());

                    makeRequestUpdate(listAdapter.getRequestedChangeParent());
                }
            }
        }

        if(requestCode == 2)
        {
            if(resultCode == Activity.RESULT_OK)
            {
                ItemCategory parentCategory = data.getParcelableExtra("result");

                if(parentCategory!=null)
                {

                    List<ItemCategory> tempList = new ArrayList<>();

                    for(Map.Entry<Integer,ItemCategory> entry : listAdapter.selectedItems.entrySet())
                    {
                        entry.getValue().setParentCategoryID(parentCategory.getItemCategoryID());
                        tempList.add(entry.getValue());
                    }

                    makeRequestUpdateBulk(tempList);
                }

            }
        }
    }
*/



/*
    void makeRequestUpdate(ItemCategory itemCategory)
    {
        Call<ResponseBody> call = itemCategoryService.updateItemCategory(itemCategory,itemCategory.getItemCategoryID());

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                if(response.code() == 200)
                {
                    showToastMessage("Change Parent Successful !");

                    dataset.clear();
                    offset = 0 ; // reset the offset
                    makeRequestRetrofit(false,false);

                }else
                {
                    showToastMessage("Change Parent Failed !");
                }

                listAdapter.setRequestedChangeParent(null);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

                showToastMessage("Network request failed. Please check your connection !");

                listAdapter.setRequestedChangeParent(null);
            }
        });
    }
*/



/*
    void makeRequestUpdateBulk(final List<ItemCategory> list)
    {
        Call<ResponseBody> call = itemCategoryService.updateItemCategoryBulk(list);


        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.code() == 200)
                {
                    showToastMessage("Update Successful !");

                    clearSelectedItems();

                }else if (response.code() == 206)
                {
                    showToastMessage("Partially Updated. Check for data changes !");

                    clearSelectedItems();

                }else if(response.code() == 304)
                {

                    showToastMessage("No item updated !");

                }else
                {
                    showToastMessage("Unknown server error or response !");
                }


                onRefresh();

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {


                showToastMessage("Network Request failed. Check your internet / network connection !");

            }
        });

    }*/


    void clearSelectedItems()
    {
        // clear the selected items
        listAdapter.selectedItems.clear();
    }




    @Override
    public void notifyItemCategorySelected() {

        if(getActivity() instanceof ToggleFab)
        {
            ((ToggleFab)getActivity()).showFab();
        }
    }



    @Override
    public void onRefresh() {

        // reset the offset and make a network call
        offset = 0;
        dataset.clear();
        makeRequestRetrofit(false,false);
    }



    @Override
    public void notifyRequestSubCategory(ItemCategory itemCategory) {

        ItemCategory temp = currentCategory;
        currentCategory = itemCategory;
        currentCategory.setParentCategory(temp);


        if(getActivity() instanceof NotifyGeneral)
        {
            ((NotifyGeneral)getActivity()).insertTab(currentCategory.getCategoryName());
        }

        dataset.clear();
        offset = 0 ; // reset the offset
        makeRequestRetrofit(true,false);
    }


    @Override
    public boolean backPressed() {

        int currentCategoryID = 1; // the ID of root category is always supposed to be 1

        // clear the selected items when back button is pressed
        listAdapter.selectedItems.clear();

        if(currentCategory!=null) {


            if(getActivity() instanceof NotifyGeneral)
            {
                ((NotifyGeneral)getActivity()).removeLastTab();
            }


            if (currentCategory.getParentCategory() != null) {

                currentCategory = currentCategory.getParentCategory();
                currentCategoryID = currentCategory.getItemCategoryID();

            } else {
                currentCategoryID = currentCategory.getParentCategoryID();
            }


            if (currentCategoryID != -1) {

                dataset.clear();
                offset =0; // reset the offset
                makeRequestRetrofit(true,true);
            }

        }

        if(currentCategoryID == -1)
        {
//            super.onBackPressed();

            return  true;
        }else
        {
            return  false;
        }

    }


//    @Override
//    public void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//        Icepick.saveInstanceState(this, outState);
//    }



//    @Override
//    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
//        super.onViewStateRestored(savedInstanceState);
//
////        Icepick.restoreInstanceState(this, savedInstanceState);
//        notifyTitleChanged();
//    }




    void notifyTitleChanged()
    {
        if(getActivity() instanceof NotifyTitleChanged)
        {
            ((NotifyTitleChanged) getActivity())
                    .NotifyTitleChanged(currentCategory.getCategoryName()
                             + " Subcategories ("
                            + String.valueOf(dataset.size()) + ")",0
                    );
        }
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();

        isDestroyed = true;
    }


}