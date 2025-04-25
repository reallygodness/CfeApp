package com.example.cfeprjct.Activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cfeprjct.Adapters.CatalogItem;
import com.example.cfeprjct.AppDatabase;
import com.example.cfeprjct.Entities.Review;
import com.example.cfeprjct.Entities.Volume;
import com.example.cfeprjct.R;
import com.example.cfeprjct.Sync.CatalogSync;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductDetailActivity extends AppCompatActivity {

    public static final String EXTRA_ITEM = "extra_catalog_item";

    private AppDatabase db;
    private float basePrice;
    private TextView tvPrice;
    private MaterialButtonToggleGroup sizes;
    private int mlS, mlM, mlL;

    // Отзывы
    private RatingBar ratingBar;
    private EditText etComment;
    private MaterialButton btnSubmitReview;
    private RecyclerView rvReviews;
    private ReviewAdapter reviewAdapter;
    private FirebaseFirestore firestore;
    private String userId;
    private String productType;
    private int productId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        // edge-to-edge
        View root = findViewById(R.id.coordinator_root);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets sb = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            MaterialToolbar tb = findViewById(R.id.toolbar);
            tb.setPadding(tb.getPaddingLeft(), sb.top, tb.getPaddingRight(), tb.getPaddingBottom());
            return insets;
        });

        // Получаем переданный объект
        CatalogItem item = (CatalogItem) getIntent().getSerializableExtra(EXTRA_ITEM);
        if (item == null) {
            finish();
            return;
        }
        productType = item.getType();  // "drink", "dish" или "dessert"
        productId   = item.getId();

        // Toolbar + back
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // View-элементы товара
        ImageView iv         = findViewById(R.id.detailImage);
        TextView tvTitle     = findViewById(R.id.detailTitle);
        TextView tvRating    = findViewById(R.id.detailRating);
        TextView tvDesc      = findViewById(R.id.detailDescription);
        sizes                = findViewById(R.id.sizeToggle);
        MaterialButton btnAdd= findViewById(R.id.btnAddToCart);
        tvPrice             = findViewById(R.id.detailPrice);

        // Заполняем UI товара
        tvTitle.setText(item.getTitle());
        tvRating.setText(String.valueOf(item.getRating()));
        tvDesc.setText(item.getDescription());
        Glide.with(this)
                .load(item.getImageUrl())
                .placeholder(R.drawable.ic_placeholder)
                .centerCrop()
                .into(iv);

        // Базовая цена (S)
        basePrice = item.getPrice();
        tvPrice.setText(String.format("%d ₽", (int) basePrice));

        // Room
        db = AppDatabase.getInstance(this);

        // Firestore + Auth
        firestore = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        } else {
            userId = null;
        }

        // --- Инициализация UI отзывов ---
        ratingBar       = findViewById(R.id.ratingBar);
        etComment       = findViewById(R.id.etComment);
        btnSubmitReview = findViewById(R.id.btnSubmitReview);
        rvReviews       = findViewById(R.id.rvReviews);

        rvReviews.setLayoutManager(new LinearLayoutManager(this));
        reviewAdapter = new ReviewAdapter();
        rvReviews.setAdapter(reviewAdapter);

        // Показываем или прячем форму отзыва
        if (userId == null) {
            ratingBar.setVisibility(View.GONE);
            etComment.setVisibility(View.GONE);
            btnSubmitReview.setVisibility(View.GONE);
        } else {
            ratingBar.setVisibility(View.VISIBLE);
            etComment.setVisibility(View.VISIBLE);
            btnSubmitReview.setVisibility(View.VISIBLE);
        }

        // Подписываемся на локальные отзывы для этого товара
        LiveData<List<Review>> liveReviews;
        switch (productType) {
            case "drink":
                liveReviews = db.reviewDAO().getReviewsForDrinkId(productId);
                break;
            case "dish":
                liveReviews = db.reviewDAO().getReviewsForDishId(productId);
                break;
            default: // "dessert"
                liveReviews = db.reviewDAO().getReviewsForDessertId(productId);
        }
        liveReviews.observe(this, reviews -> reviewAdapter.submitList(reviews));

        // Обработка кнопки «Отправить отзыв»
        btnSubmitReview.setOnClickListener(v -> {
            if (userId == null) {
                Toast.makeText(this,
                        "Пожалуйста, войдите, чтобы оставлять отзывы",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            int rating = (int) ratingBar.getRating();
            String text = etComment.getText().toString().trim();
            if (rating < 1) {
                Toast.makeText(this, "Поставьте оценку от 1 до 5", Toast.LENGTH_SHORT).show();
                return;
            }

            // Подготавливаем Review
            Review r = new Review();
            r.setUserId(userId);
            r.setRating(rating);
            r.setText(text);
            r.setReviewDate(System.currentTimeMillis());
            if ("drink".equals(productType))   r.setDrinkId(productId);
            if ("dish".equals(productType))    r.setDishId(productId);
            if ("dessert".equals(productType)) r.setDessertId(productId);

            // Сохраняем в Room
            new Thread(() -> db.reviewDAO().insert(r)).start();

            // Пушим в Firestore
            Map<String,Object> map = new HashMap<>();
            map.put("userId",     userId);
            map.put("rating",     rating);
            map.put("text",       text);
            map.put("reviewDate", r.getReviewDate());
            if ("drink".equals(productType))   map.put("drinkId",   productId);
            if ("dish".equals(productType))    map.put("dishId",    productId);
            if ("dessert".equals(productType)) map.put("dessertId", productId);

            firestore.collection("reviews")
                    .add(map)
                    .addOnSuccessListener(__ -> {
                        Toast.makeText(this, "Отзыв отправлен", Toast.LENGTH_SHORT).show();
                        ratingBar.setRating(0);
                        etComment.setText("");
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Ошибка отправки отзыва", Toast.LENGTH_SHORT).show()
                    );
        });
        // --- /UI отзывов ---

        // --- Существующая логика выбора размера и цены ---
        if ("drink".equals(item.getType())) {
            sizes.setVisibility(View.VISIBLE);
            new CatalogSync(this).syncVolumes(() -> {
                List<Volume> vols = db.volumeDAO().getAllVolumes();
                for (Volume v : vols) {
                    switch (v.getSize()) {
                        case "S": mlS = v.getMl(); break;
                        case "M": mlM = v.getMl(); break;
                        case "L": mlL = v.getMl(); break;
                    }
                }
                runOnUiThread(this::configureSizeToggle);
            });
        } else {
            sizes.setVisibility(View.GONE);
        }

        btnAdd.setOnClickListener(v -> {
            // TODO: добавить в корзину
        });
    }

    private void configureSizeToggle() {
        sizes.check(R.id.btnSizeS);
        sizes.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            float newPrice;
            if (checkedId == R.id.btnSizeM) {
                newPrice = basePrice + 100;
            } else if (checkedId == R.id.btnSizeL) {
                newPrice = basePrice + 150;
            } else {
                newPrice = basePrice;
            }
            tvPrice.setText(String.format("%d ₽", (int) newPrice));
        });
    }

    /** Адаптер для списка отзывов */
    private static class ReviewAdapter
            extends ListAdapter<Review, ReviewAdapter.VH> {

        ReviewAdapter() {
            super(new DiffUtil.ItemCallback<Review>() {
                @Override public boolean areItemsTheSame(Review a, Review b) {
                    return a.getReviewId() == b.getReviewId();
                }
                @Override public boolean areContentsTheSame(Review a, Review b) {
                    return a.getRating() == b.getRating()
                            && a.getText().equals(b.getText())
                            && a.getReviewDate() == b.getReviewDate();
                }
            });
        }

        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_review, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(VH holder, int position) {
            Review r = getItem(position);
            holder.ratingBar.setRating(r.getRating());
            holder.tvText.setText(r.getText());
        }

        static class VH extends RecyclerView.ViewHolder {
            private RatingBar ratingBar;
            TextView tvText;
            VH(View itemView) {
                super(itemView);
                ratingBar = itemView.findViewById(R.id.itemRatingBar);
                tvText    = itemView.findViewById(R.id.itemReviewText);
            }
        }
    }
}
