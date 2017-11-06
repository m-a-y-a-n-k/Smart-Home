package com.solutions.isecpowify.smarthome;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

/**
 * Created by mayank on 5/11/17.
 */

class Helpers {

    static SharedPreferences getSharedPreferences(Context appContext) {
        return appContext.getSharedPreferences(String.valueOf(R.string.preference_file_key), Context.MODE_PRIVATE);
    }

    static void fixBackgroundRepeat(View view) {
        Drawable bg = view.getBackground();
        if (bg != null) {
            if (bg instanceof BitmapDrawable) {
                BitmapDrawable bmp = (BitmapDrawable) bg;
                bmp.mutate();
                bmp.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
            }
        }
    }

    static void hideKeyboard(View view, Context context) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    static void restartApplication(Activity current) {

        Intent i = current.getBaseContext().getPackageManager()
                .getLaunchIntentForPackage( current.getBaseContext().getPackageName() );
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        current.finish();
        current.startActivity(i);
    }
}
