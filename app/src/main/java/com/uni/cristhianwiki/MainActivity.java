package com.uni.cristhianwiki;

import android.content.Intent;
import android.os.Bundle;

public class MainActivity extends AbstractListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        findViewById(R.id.vision_card_unet_retina_click_area).setOnClickListener(v -> {
            final Intent intent = new Intent(MainActivity.this, ModelActivity.class);
            intent.putExtra(ModelActivity.TITLE, "Segmentación de la retina");
            intent.putExtra(ModelActivity.INTENT_MODULE_ASSET_NAME, "unet_retina_scripted.ptl");
            intent.putExtra(ModelActivity.INTENT_INFO_VIEW_TYPE,
                    InfoViewFactory.INFO_VIEW_TYPE_RETINA_IMAGE_SEGMENTATION);


            startActivity(intent);
        });
        findViewById(R.id.vision_card_unet_brain_click_area).setOnClickListener(v -> {
            final Intent intent = new Intent(MainActivity.this, ModelActivity.class);
            intent.putExtra(ModelActivity.TITLE, "Segmentación del cerebro");
            intent.putExtra(ModelActivity.INTENT_MODULE_ASSET_NAME, "unet_brain_scripted_optimized.ptl");
            intent.putExtra(ModelActivity.INTENT_INFO_VIEW_TYPE,
                    InfoViewFactory.INFO_VIEW_TYPE_BRAIN_IMAGE_SEGMENTATION);


            startActivity(intent);
        });
    }

    @Override
    protected int getListContentLayoutRes() {
        return R.layout.activity_main;
    }
}
