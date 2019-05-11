package org.nearbyshops.enduserappnew.ShopDetail;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
//
//import com.google.android.gms.maps.CameraUpdateFactory;
//import com.google.android.gms.maps.GoogleMap;
//import com.google.android.gms.maps.OnMapReadyCallback;
//import com.google.android.gms.maps.SupportMapFragment;
//import com.google.android.gms.maps.model.Circle;
//import com.google.android.gms.maps.model.CircleOptions;
//import com.google.android.gms.maps.model.LatLng;
//import com.google.android.gms.maps.model.Marker;
//import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.nearbyshops.enduserappnew.DaggerComponentBuilder;
import org.nearbyshops.enduserappnew.Interfaces.NotifyReviewUpdate;
import org.nearbyshops.enduserappnew.Login.Login;
import org.nearbyshops.enduserappnew.Model.Shop;
import org.nearbyshops.enduserappnew.Model.ShopImage;
import org.nearbyshops.enduserappnew.ModelEndPoints.FavouriteShopEndpoint;
import org.nearbyshops.enduserappnew.ModelEndPoints.ShopImageEndPoint;
import org.nearbyshops.enduserappnew.ModelEndPoints.ShopReviewEndPoint;
import org.nearbyshops.enduserappnew.ModelReviewShop.FavouriteShop;
import org.nearbyshops.enduserappnew.ModelReviewShop.ShopReview;
import org.nearbyshops.enduserappnew.ModelRoles.User;
import org.nearbyshops.enduserappnew.R;
import org.nearbyshops.enduserappnew.API.FavouriteShopService;
import org.nearbyshops.enduserappnew.API.ShopImageService;
import org.nearbyshops.enduserappnew.API.ShopReviewService;
import org.nearbyshops.enduserappnew.ShopImages.ShopImageList;
import org.nearbyshops.enduserappnew.Preferences.PrefGeneral;
import org.nearbyshops.enduserappnew.Preferences.PrefLogin;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;



public class ShopDetail_ extends AppCompatActivity implements Target, RatingBar.OnRatingBarChangeListener , NotifyReviewUpdate {

    public final static String SHOP_DETAIL_INTENT_KEY = "intent_key_shop_detail";


    boolean isDestroyed = false;

    @Inject
    ShopReviewService shopReviewService;

    @Inject
    FavouriteShopService favouriteShopService;

    @Inject
    ShopImageService shopImageService;

//    private GoogleMap mMap;
//    Marker currentMarker;

    Shop shop;

    @BindView(R.id.fab)
    FloatingActionButton fab;

    @BindView(R.id.book_title)
    TextView bookTitle;

    @BindView(R.id.author_name)
    TextView authorName;

    @BindView(R.id.date_of_publish)
    TextView publishDate;


//    @Bind(R.id.publisher_name)
//    TextView publisherName;

    @BindView(R.id.book_description)
    TextView bookDescription;

    @BindView(R.id.book_cover)
    ImageView bookCover;

    @BindView(R.id.rating_text)
    TextView ratingText;

    @BindView(R.id.ratings_count)
    TextView ratingsCount;

    @BindView(R.id.ratingBar)
    RatingBar ratingsBar;

    @BindView(R.id.user_rating_review)
    LinearLayout user_review_ratings_block;

    @BindView(R.id.edit_review_text)
    TextView edit_review_text;

    @BindView(R.id.ratingBar_rate)
    RatingBar ratingBar_rate;

    @BindView(R.id.read_all_reviews_button)
    TextView read_all_reviews_button;

    @BindView(R.id.member_profile_image)
    ImageView member_profile_image;

    @BindView(R.id.member_name)
    TextView member_name;

    @BindView(R.id.member_rating)
    RatingBar member_rating_indicator;


    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout collapsingToolbarLayout;

    @BindView(R.id.image_count) TextView imagesCount;


//    Unbinder unbinder;


    public ShopDetail_() {

        DaggerComponentBuilder.getInstance()
                .getNetComponent()
                .Inject(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_detail);

        ButterKnife.bind(this);

        ratingBar_rate.setOnRatingBarChangeListener(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        shop = getIntent().getParcelableExtra(SHOP_DETAIL_INTENT_KEY);
        bindViews(shop);

        if (shop != null) {
            getSupportActionBar().setTitle(shop.getShopName());
            getSupportActionBar().setSubtitle(shop.getShortDescription());
        }


        if (shop != null) {
            checkUserReview();
        }


//        Log.d("ShopLog",String.valueOf(shop.getRt_rating_avg()) + ":" + String.valueOf(shop.getRt_rating_count()));

        /*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        checkFavourite();
        getShopImageCount();
//        setupMap();
    }


    void bindViews(Shop shop) {

        if (shop != null) {


            if (shop.getShopName() == null) {
                bookTitle.setText("Shop Title");
            } else {
                bookTitle.setText(shop.getShopName());
            }

            authorName.setText(shop.getShopAddress() + "\n" + shop.getCity()
                    + "\n\n" + "Phone : " + shop.getCustomerHelplineNumber()
                    + "\n" + "Delivery Support : " + shop.getDeliveryHelplineNumber()
            );

//            publisherName.setText("Published By : " + shop.getShopAddress());

            /*if(shop.getDateOfPublish()!=null)
            {
                Log.d("date","Date of Publish binding " + shop.getDateOfPublish().toString());

                SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM ''yyyy");

                //"EEE, MMM d, ''yy"
                //"yyyy-MM-dd"

                publishDate.setText(dateFormat.format(shop.getDateOfPublish()));
            }*/

            if (shop.getDateTimeStarted().getTime() == 0) {
                publishDate.setText("Date Started not available !");

            } else {
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(shop.getDateTimeStarted().getTime());
                SimpleDateFormat dateFormat = new SimpleDateFormat(getString(R.string.date_format_simple));

                //"EEEEEEE, MMMMMMMMM dd yyyy"
                //getString(R.string.date_format_simple)

                //"MMMM d ''yyyy"
//                publishDate.setText("Started : " + dateFormat.format(calendar.getTime()));
                publishDate.setText("Started : " + shop.getDateTimeStarted().toLocaleString());
            }


            // set Book Cover Image


//            String imagePath = UtilityGeneral.getImageEndpointURL(this)
//                    + shop.getLogoImagePath();


            String imagePath = PrefGeneral.getServiceURL(this) + "/api/v1/Shop/Image/five_hundred_"
                    + shop.getLogoImagePath() + ".jpg";

//            if (!shop.getBookCoverImageURL().equals("")) {

            Drawable placeholder = VectorDrawableCompat
                    .create(getResources(),
                            R.drawable.ic_nature_people_white_48px, getTheme());

            Picasso.with(this).load(imagePath)
                    .placeholder(placeholder)
                    .into(bookCover);

            Picasso.with(this)
                    .load(imagePath)
                    .placeholder(placeholder)
                    .into(this);

//            }

            if (shop.getRt_rating_count() == 0) {

                ratingText.setText(R.string.rating_not_available);
                ratingsCount.setText((getString(R.string.not_yet_rated)));
                ratingsBar.setVisibility(View.GONE);

            } else {
                ratingText.setText("Rating : " + String.format("%.1f", shop.getRt_rating_avg()));
                ratingsCount.setText((int) shop.getRt_rating_count() + " Ratings");
                ratingsBar.setRating(shop.getRt_rating_avg());
            }


            bookDescription.setText(shop.getLongDescription());

            /*if (shop.getLongDescription()!=null && !shop.getLongDescription().equals("null") && !shop.getDe.equals("")) {

            }*/
//                bookDescription.setText("Book description Not Available.");
        }
    }


    @BindView(R.id.edit_review_block)
    RelativeLayout edit_review_block;

    @BindView(R.id.review_title)
    TextView review_title;

    @BindView(R.id.review_description)
    TextView review_description;

    @BindView(R.id.review_date)
    TextView review_date;

    ShopReview reviewForUpdate;


    // method to check whether the user has written the review or not if the user is currently logged in.
    void checkUserReview() {

        if (PrefLogin.getUser(this) == null) {

            user_review_ratings_block.setVisibility(View.GONE);

        } else {

            // Unhide review dialog


            if (shop.getRt_rating_count() == 0) {

                user_review_ratings_block.setVisibility(View.VISIBLE);
                edit_review_block.setVisibility(View.GONE);

                edit_review_text.setText(R.string.shop_review_be_the_first_to_review);
            } else if (shop.getRt_rating_count() > 0) {


                Call<ShopReviewEndPoint> call = shopReviewService.getReviews(shop.getShopID(),
                        PrefLogin.getUser(this).getUserID(), true, "REVIEW_DATE", null, null, null);

//                Log.d("review_check",String.valueOf(UtilityGeneral.getUserID(this)) + " : " + String.valueOf(shop.getBookID()));

                call.enqueue(new Callback<ShopReviewEndPoint>() {
                    @Override
                    public void onResponse(Call<ShopReviewEndPoint> call, Response<ShopReviewEndPoint> response) {


                        if (response.body() != null) {
                            if (response.body().getItemCount() > 0) {

//                                edit_review_text.setText("Edit your review and Rating !");


                                if (edit_review_block == null) {
                                    // If the views are not bound then return. This can happen in delayed response. When this call is executed
                                    // after the activity have gone out of scope.
                                    return;
                                }

                                edit_review_block.setVisibility(View.VISIBLE);
                                user_review_ratings_block.setVisibility(View.GONE);

                                reviewForUpdate = response.body().getResults().get(0);

                                review_title.setText(response.body().getResults().get(0).getReviewTitle());
                                review_description.setText(response.body().getResults().get(0).getReviewText());

                                review_date.setText(response.body().getResults().get(0).getReviewDate().toLocaleString());

                                member_rating_indicator.setRating(response.body().getResults().get(0).getRating());


//                                user_review.setText(response.body().getResults().get(0).getReviewText());
//                                ratingBar_rate.setRating(response.body().getResults().get(0).getRating());

                                User member = response.body().getResults().get(0).getRt_end_user_profile();
                                member_name.setText(member.getName());

//                                String imagePath = PrefGeneral.getImageEndpointURL(ShopDetail_.this)
//                                        + member.getProfileImagePath();



                                String imagepath = PrefGeneral.getServiceURL(getApplicationContext()) + "/api/v1/User/Image/five_hundred_"
                                        + member.getProfileImagePath() + ".jpg";


                                Drawable placeholder = VectorDrawableCompat
                                        .create(getResources(),
                                                R.drawable.ic_nature_people_white_48px, getTheme());

                                Picasso.with(ShopDetail_.this).load(imagepath)
                                        .placeholder(placeholder)
                                        .into(member_profile_image);


                            } else if (response.body().getItemCount() == 0) {
                                edit_review_text.setText("Rate this shop !");
                                edit_review_block.setVisibility(View.GONE);
                                user_review_ratings_block.setVisibility(View.VISIBLE);

                            }
                        }


                    }

                    @Override
                    public void onFailure(Call<ShopReviewEndPoint> call, Throwable t) {


//                        showToastMessage("Network Request Failed. Check your internet connection !");

                    }
                });


            }

            // check shop ratings count
            // If ratings count is 0 then set message : Be the first to review


            // If ratings count is >0 then
            // check if user has written the review or not
            // if Yes
            // Write messsage : Edit your review and rating
            // If NO
            // Write message : Rate and Review this shop

        }

    }


    void showToastMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {


        Palette palette = Palette.from(bitmap).generate();

        int color = getResources().getColor(R.color.colorPrimaryDark);
        int colorLight = getResources().getColor(R.color.colorPrimary);
        int vibrant = palette.getVibrantColor(color);
        int vibrantLight = palette.getLightVibrantColor(color);
        int vibrantDark = palette.getDarkVibrantColor(colorLight);
        int muted = palette.getMutedColor(color);
        int mutedLight = palette.getLightMutedColor(color);
        int mutedDark = palette.getDarkMutedColor(color);

        Palette.Swatch vibrantSwatch = palette.getVibrantSwatch();

        //if(vibrantSwatch!=null) {
        //  originalTitle.setTextColor(vibrantSwatch.getTitleTextColor());
        //}


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            this.getWindow().setStatusBarColor(vibrantDark);

        }




        bookTitle.setTextColor(vibrant);
        authorName.setTextColor(vibrant);


        if (fab != null && vibrantDark != 0) {

            fab.setBackgroundTintList(ColorStateList.valueOf(vibrantDark));

        }//fab.setBackgroundColor(vibrantDark);

        //originalTitle.setBackgroundColor(vibrantDark);


        if (collapsingToolbarLayout != null) {

            collapsingToolbarLayout.setContentScrimColor(vibrant);

        }

    }

    @Override
    public void onBitmapFailed(Drawable errorDrawable) {

    }

    @Override
    public void onPrepareLoad(Drawable placeHolderDrawable) {

    }


    @Override
    public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {

        write_review_click();
    }


    @OnClick({R.id.edit_icon, R.id.edit_review_label})
    void edit_review_Click() {

        if (reviewForUpdate != null) {
            FragmentManager fm = getSupportFragmentManager();
            RateReviewDialog dialog = new RateReviewDialog();
            dialog.show(fm, "rate");
            dialog.setMode(reviewForUpdate, true, reviewForUpdate.getShopID());
        }

    }




    @OnClick({R.id.edit_review_text, R.id.ratingBar_rate})
    void write_review_click() {

        FragmentManager fm = getSupportFragmentManager();
        RateReviewDialog dialog = new RateReviewDialog();
        dialog.show(fm, "rate");

        if (shop != null) {
            dialog.setMode(null, false, shop.getShopID());
        }
    }


    @Override
    public void notifyReviewUpdated() {

        checkUserReview();
    }

    @Override
    public void notifyReviewDeleted() {

        shop.setRt_rating_count(shop.getRt_rating_count() - 1);
        checkUserReview();
    }

    @Override
    public void notifyReviewSubmitted() {

        shop.setRt_rating_count(shop.getRt_rating_count() + 1);
        checkUserReview();
    }


    @OnClick(R.id.read_all_reviews_button)
    void readAllReviewsButton() {

//        Intent intent = new Intent(this, ShopReviews.class);
//        intent.putExtra(ShopReviews.SHOP_INTENT_KEY, shop);
//        startActivity(intent);

    }


    @BindView(R.id.coordinatorLayout)
    CoordinatorLayout coordinatorLayout;


    void showMessageSnackBar(String message) {

        Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_SHORT).show();
    }


    @OnClick(R.id.fab)
    void fabClick() {

        if (PrefLogin.getUser(this) == null) {
            // User Not logged In.
//            showMessageSnackBar("Please LoginUsingOTP to add shop to Favourites !");
            showToastMessage("Please LoginUsingOTP to use this Feature !");

            showLoginDialog();


        } else {
            toggleFavourite();
        }
    }


    private void showLoginDialog() {
//        FragmentManager fm = getSupportFragmentManager();
//        LoginDialog loginDialog = new LoginDialog();
//        loginDialog.show(fm,"serviceUrl");

        Intent intent = new Intent(this, Login.class);
        startActivity(intent);
    }


//    @Override
//    public void NotifyLogin() {
//
////        fabClick();
//    }


    void toggleFavourite() {

        if (shop != null && PrefLogin.getUser(this) != null) {

            Call<FavouriteShopEndpoint> call = favouriteShopService.getFavouriteShops(shop.getShopID(), PrefLogin.getUser(this).getUserID()
                    , null, null, null, null);


            call.enqueue(new Callback<FavouriteShopEndpoint>() {
                @Override
                public void onResponse(Call<FavouriteShopEndpoint> call, Response<FavouriteShopEndpoint> response) {


                    if (response.body() != null) {
                        if (response.body().getItemCount() >= 1) {
                            deleteFavourite();

                        } else if (response.body().getItemCount() == 0) {
                            insertFavourite();
                        }
                    }

                }

                @Override
                public void onFailure(Call<FavouriteShopEndpoint> call, Throwable t) {

                    showToastMessage("Network Request failed. Check Network Connection !");
                }
            });
        }
    }


    void insertFavourite() {


        if (shop != null && PrefLogin.getUser(this) != null) {

            FavouriteShop favouriteBook = new FavouriteShop();
            favouriteBook.setShopID(shop.getShopID());
            favouriteBook.setEndUserID(PrefLogin.getUser(this).getUserID());

            Call<FavouriteShop> call = favouriteShopService.insertFavouriteShop(favouriteBook);

            call.enqueue(new Callback<FavouriteShop>() {
                @Override
                public void onResponse(Call<FavouriteShop> call, Response<FavouriteShop> response) {

                    if (response.code() == 201) {
                        // created successfully

                        setFavouriteIcon(true);
                    }
                }

                @Override
                public void onFailure(Call<FavouriteShop> call, Throwable t) {

                    showToastMessage("Network Request failed !");

                }
            });
        }


    }

    void deleteFavourite() {

        if (shop != null && PrefLogin.getUser(this) != null) {
            Call<ResponseBody> call = favouriteShopService.deleteFavouriteShop(shop.getShopID(),
                    PrefLogin.getUser(this).getUserID());


            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                    if (response.code() == 200) {
                        setFavouriteIcon(false);
                    }

                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {

                    showToastMessage("Network Request Failed !");
                }
            });
        }

    }


    void setFavouriteIcon(boolean isFavourite) {

        if (fab == null) {
            return;
        }

        if (isFavourite) {
            Drawable drawable = VectorDrawableCompat.create(getResources(), R.drawable.ic_favorite_white_24px, getTheme());
            fab.setImageDrawable(drawable);
        } else {
            Drawable drawable2 = VectorDrawableCompat.create(getResources(), R.drawable.ic_favorite_border_white_24px, getTheme());
            fab.setImageDrawable(drawable2);
        }
    }


    void checkFavourite() {

        // make a network call to check the favourite

        if (shop != null && PrefLogin.getUser(this) != null) {

            Call<FavouriteShopEndpoint> call = favouriteShopService.getFavouriteShops(shop.getShopID(), PrefLogin.getUser(this).getUserID()
                    , null, null, null, null);


            call.enqueue(new Callback<FavouriteShopEndpoint>() {
                @Override
                public void onResponse(Call<FavouriteShopEndpoint> call, Response<FavouriteShopEndpoint> response) {


                    if (response.body() != null) {
                        if (response.body().getItemCount() >= 1) {
                            setFavouriteIcon(true);

                        } else if (response.body().getItemCount() == 0) {
                            setFavouriteIcon(false);
                        }
                    }

                }

                @Override
                public void onFailure(Call<FavouriteShopEndpoint> call, Throwable t) {

                    showToastMessage("Network Request failed. Check Network Connection !");
                }
            });

        }
    }



//
//
//    @OnClick(R.id.share_buttons)
//    void shareButtonClick() {
//
//        Intent intent = ShareCompat.IntentBuilder.from(this)
//                .setType("image/jpg")
//                .getIntent();
//
//        String url = PrefGeneral.getServiceURL(this) + "/api/Images" + String.valueOf(shop.getLogoImagePath());
////        intent.putExtra(Intent.EXTRA_TEXT,url);
//        intent.putExtra(Intent.EXTRA_TEXT, url);
////        intent.putExtra(Intent.EXTRA_TITLE,shop.getBookName());
//        startActivity(Intent.createChooser(intent, "Share Link"));
//    }





    @BindView(R.id.read_full_button)
    TextView readFullDescription;

    @OnClick(R.id.read_full_button)
    void readFullButtonClick() {
/*
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.BELOW,R.id.author_name);
        layoutParams.setMargins(0,10,0,0);
        bookDescription.setLayoutParams(layoutParams);
*/

        bookDescription.setMaxLines(Integer.MAX_VALUE);
        readFullDescription.setVisibility(View.GONE);
    }







    @OnClick(R.id.book_cover)
    void profileImageClick() {
//        listItemClick();
        Intent intent = new Intent(this, ShopImageList.class);
        intent.putExtra("shop_id", shop.getShopID());
        startActivity(intent);
    }


    @Override
    protected void onResume() {
        super.onResume();
        isDestroyed = false;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        isDestroyed = true;
    }




    void getShopImageCount()
    {
        Call<ShopImageEndPoint> call = shopImageService.getShopImages(
                shop.getShopID(), ShopImage.IMAGE_ORDER,
                null,null,
                true,true
        );


        call.enqueue(new Callback<ShopImageEndPoint>() {
            @Override
            public void onResponse(Call<ShopImageEndPoint> call, Response<ShopImageEndPoint> response) {


                if(isDestroyed)
                {
                    return;
                }

                if(response.body()!=null)
                {
                    int count = response.body().getItemCount();


                    if(count==0)
                    {
                        imagesCount.setVisibility(View.GONE);
                    }
                    else
                    {
                        imagesCount.setText(String.valueOf(count)  + " Photos");
                    }

                }
            }

            @Override
            public void onFailure(Call<ShopImageEndPoint> call, Throwable t) {


                if(isDestroyed)
                {
                    return;
                }


                showToastMessage("Loading Images Failed !");
            }
        });


    }







    @OnClick(R.id.get_directions_button)
    void getDirections()
    {
        String str_latitude = String.valueOf(shop.getLatCenter());
        String str_longitude = String.valueOf(shop.getLonCenter());

        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + str_latitude +  "," + str_longitude);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
    }








}
