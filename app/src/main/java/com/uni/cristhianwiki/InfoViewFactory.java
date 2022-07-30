package com.uni.cristhianwiki;

import android.content.Context;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

public class InfoViewFactory {
    public static final int INFO_VIEW_TYPE_RETINA_IMAGE_SEGMENTATION = 1;
    public static final int INFO_VIEW_TYPE_BRAIN_IMAGE_SEGMENTATION = 2;

    public static View newInfoView(Context context, int infoViewType, @Nullable String additionalText) {
        LayoutInflater inflater = LayoutInflater.from(context);
        if (INFO_VIEW_TYPE_RETINA_IMAGE_SEGMENTATION == infoViewType) {
            View view = inflater.inflate(R.layout.info, null, false);
            TextView infoTextView = view.findViewById(R.id.info_title);
            TextView descriptionTextView = view.findViewById(R.id.info_description);
            TextView linkTextView = view.findViewById(R.id.link);

            infoTextView.setText(R.string.vision_card_unet_retina_title);
            StringBuilder sb = new StringBuilder(context.getString(R.string.vision_card_unet_retina_description));
            if (additionalText != null) {
                sb.append("\n\n").append(additionalText);
            }
            descriptionTextView.setText(sb.toString());
            linkTextView.setMovementMethod(LinkMovementMethod.getInstance());
            return view;
        } else if (INFO_VIEW_TYPE_BRAIN_IMAGE_SEGMENTATION == infoViewType) {
            View view = inflater.inflate(R.layout.info, null, false);
            TextView infoTextView = view.findViewById(R.id.info_title);
            TextView descriptionTextView = view.findViewById(R.id.info_description);

            infoTextView.setText(R.string.vision_card_unet_brain_title);
            StringBuilder sb = new StringBuilder(context.getString(R.string.vision_card_unet_brain_description));
            if (additionalText != null) {
                sb.append("\n\n").append(additionalText);
            }
            descriptionTextView.setText(sb.toString());
            return view;
        }
        throw new IllegalArgumentException("No se reconoce el info view type");
    }

    public static View newErrorDialogView(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.error_dialog, null, false);
        return view;
    }
}
