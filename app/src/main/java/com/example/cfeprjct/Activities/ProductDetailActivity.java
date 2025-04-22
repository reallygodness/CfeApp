package com.example.cfeprjct.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.cfeprjct.Adapters.CatalogItem;
import com.example.cfeprjct.AppDatabase;
import com.example.cfeprjct.Entities.Volume;
import com.example.cfeprjct.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;

import java.util.List;

public class ProductDetailActivity extends AppCompatActivity {

    public static final String EXTRA_ITEM = "extra_catalog_item";

    private AppDatabase db;
    private float basePrice;
    private TextView tvPrice;
    private MaterialButtonToggleGroup sizes;
    private int mlS, mlM, mlL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        // edge-to-edge отступ под статус‑бар
        final View root = findViewById(R.id.coordinator_root);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets sb = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            MaterialToolbar tb = findViewById(R.id.toolbar);
            tb.setPadding(tb.getPaddingLeft(), sb.top, tb.getPaddingRight(), tb.getPaddingBottom());
            return insets;
        });

        // получаем переданный объект
        CatalogItem item = (CatalogItem) getIntent().getSerializableExtra(EXTRA_ITEM);
        if (item == null) {
            finish();
            return;
        }

        // toolbar + кнопка «назад»
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // view-шки
        ImageView iv    = findViewById(R.id.detailImage);
        TextView tvTitle      = findViewById(R.id.detailTitle);
        TextView tvRating     = findViewById(R.id.detailRating);
        TextView tvDesc       = findViewById(R.id.detailDescription);
        sizes                 = findViewById(R.id.sizeToggle);
        MaterialButton btnS   = findViewById(R.id.btnSizeS);
        MaterialButton btnM   = findViewById(R.id.btnSizeM);
        MaterialButton btnL   = findViewById(R.id.btnSizeL);
        tvPrice              = findViewById(R.id.detailPrice);
        MaterialButton btnAdd = findViewById(R.id.btnAddToCart);

        // общие поля
        tvTitle.setText(item.getTitle());
        tvRating.setText(String.valueOf(item.getRating()));
        tvDesc.setText(item.getDescription());
        Glide.with(this)
                .load(item.getImageUrl())
                .placeholder(R.drawable.ic_placeholder)
                .centerCrop()
                .into(iv);

        // базовая цена — это цена для S
        basePrice = item.getPrice();
        tvPrice.setText(String.format("%d ₽", (int) basePrice));

        // инициализируем Room (без allowMainThreadQueries!)
        db = AppDatabase.getInstance(this);

        // если это кофе — отображаем выбор размера
        if ("drink".equals(item.getType())) {
            sizes.setVisibility(View.VISIBLE);

            // загружаем объёмы в фоне
            new Thread(() -> {
                List<Volume> vols = db.volumeDAO().getAllVolumes();
                int s = 0, m = 0, l = 0;
                for (Volume v : vols) {
                    switch (v.getSize()) {
                        case "S": s = v.getMl(); break;
                        case "M": m = v.getMl(); break;
                        case "L": l = v.getMl(); break;
                    }
                }
                mlS = s; mlM = m; mlL = l;

                // после загрузки — конфигурируем переключатель на UI‑потоке
                runOnUiThread(this::configureSizeToggle);
            }).start();

        } else {
            sizes.setVisibility(View.GONE);
        }

        btnAdd.setOnClickListener(v -> {
            // TODO: добавить в корзину
        });
    }

    /**
     * Вызывается на UI‑потоке после того, как mlS, mlM и mlL инициализированы в фоне.
     */
    private void configureSizeToggle() {
        // по умолчанию S
        sizes.check(R.id.btnSizeS);

        sizes.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked || mlS == 0) return;

            float newPrice = basePrice;
            if (checkedId == R.id.btnSizeM && mlM != 0) {
                newPrice = basePrice * mlM / (float) mlS;
            } else if (checkedId == R.id.btnSizeL && mlL != 0) {
                newPrice = basePrice * mlL / (float) mlS;
            }
            tvPrice.setText(String.format("%d ₽", (int)newPrice));
        });
    }
}
