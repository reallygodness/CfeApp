package com.example.cfeprjct.Activities;

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

        // инициализируем Room и DAO
        db = AppDatabase.getInstance(this);
        List<Volume> vols = db.volumeDAO().getAllVolumes();

        // находим ml каждого объёма
        int mlS = 0, mlM = 0, mlL = 0;
        for (Volume v : vols) {
            switch (v.getSize()) {
                case "S": mlS = v.getMl(); break;
                case "M": mlM = v.getMl(); break;
                case "L": mlL = v.getMl(); break;
            }
        }
        // базовая цена — это цена для S
        final float basePrice = item.getPrice();

        // toolbar + кнопка «назад»
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // view-шки
        ImageView iv          = findViewById(R.id.detailImage);
        TextView tvTitle      = findViewById(R.id.detailTitle);
        TextView tvRating     = findViewById(R.id.detailRating);
        TextView tvDesc       = findViewById(R.id.detailDescription);
        MaterialButtonToggleGroup sizes = findViewById(R.id.sizeToggle);
        MaterialButton btnS   = findViewById(R.id.btnSizeS);
        MaterialButton btnM   = findViewById(R.id.btnSizeM);
        MaterialButton btnL   = findViewById(R.id.btnSizeL);
        TextView tvPrice      = findViewById(R.id.detailPrice);
        MaterialButton btnAdd = findViewById(R.id.btnAddToCart);

        // заполняем общие поля
        tvTitle.setText(item.getTitle());
        tvRating.setText(String.valueOf(item.getRating()));
        tvDesc.setText(item.getDescription());
        Glide.with(this)
                .load(item.getImageUrl())
                .placeholder(R.drawable.ic_placeholder)
                .centerCrop()
                .into(iv);

        // показываем цену для S по умолчанию
        tvPrice.setText(String.format("%d ₽", (int)basePrice));

        // если это кофе — отображаем выбор размера
        if ("drink".equals(item.getType())) {
            sizes.setVisibility(View.VISIBLE);
            // по умолчанию S
            sizes.check(R.id.btnSizeS);

            int finalMlS = mlS;
            int finalMlM = mlM;
            int finalMlL = mlL;
            sizes.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
                if (!isChecked) return;
                float newPrice = basePrice;
                if (checkedId == R.id.btnSizeM && finalMlS != 0) {
                    newPrice = basePrice * finalMlM / (float) finalMlS;
                } else if (checkedId == R.id.btnSizeL && finalMlS != 0) {
                    newPrice = basePrice * finalMlL / (float) finalMlS;
                }
                tvPrice.setText(String.format("%d ₽", (int)newPrice));
            });
        } else {
            sizes.setVisibility(View.GONE);
        }

        btnAdd.setOnClickListener(v -> {
            // TODO: добавить в корзину
        });
    }
}
