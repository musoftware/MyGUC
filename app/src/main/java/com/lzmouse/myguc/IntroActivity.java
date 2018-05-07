package com.lzmouse.myguc;

import android.Manifest;
import android.os.Bundle;

import agency.tango.materialintroscreen.MaterialIntroActivity;
import agency.tango.materialintroscreen.SlideFragmentBuilder;

public class IntroActivity extends MaterialIntroActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        enableLastSlideAlphaExitTransition(true);
        addSlide(new SlideFragmentBuilder()
                        .backgroundColor(R.color.colorPrimary)
                        .buttonsColor(R.color.colorAccent)
                        .neededPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
                        .image(R.drawable.ic_fac)
                        .title("Intranet")
                        .description("My Guc app provides you with an easy way to access intranet and with one touch you can download files.")
                        .build());
        addSlide(new SlideFragmentBuilder()
                .backgroundColor(R.color.colorPrimary)
                .buttonsColor(R.color.colorAccent)
                .neededPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO})
                .image(R.drawable.ic_notebook)
                .title("Notebook")
                .description("Notebook tool will help you to record lectures or tutorials, taking photos or adding notes.")
                .build());

    }

    @Override
    public void onFinish() {
        super.onFinish();
        getSharedPreferences("INTRO",MODE_PRIVATE).edit().putBoolean("IS_FIRST",false).commit();
    }
}
