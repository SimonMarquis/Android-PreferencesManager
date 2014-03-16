/*
 * Copyright (C) 2013 Simon Marquis (http://www.simon-marquis.fr)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package fr.simon.marquis.preferencesmanager.util;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.simon.marquis.preferencesmanager.roboto.RobotoTypefaceManager;

public class Ui {


    public static void hideSoftKeyboard(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static SpannableString applyCustomTypeFace(CharSequence src, Context ctx) {
        SpannableString span = new SpannableString(src);

        span.setSpan(new CustomTypefaceSpan("", RobotoTypefaceManager.obtainTypeface(ctx, RobotoTypefaceManager.ROBOTOSLAB_REGULAR)), 0,
                span.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return span;
    }

    public static SpannableStringBuilder createSpannable(Pattern pattern, int color, String s) {
        final SpannableStringBuilder spannable = new SpannableStringBuilder(s);
        if (pattern == null)
            return spannable;
        final Matcher matcher = pattern.matcher(s);
        while (matcher.find()) {
            final ForegroundColorSpan span = new ForegroundColorSpan(color);
            final StyleSpan span2 = new StyleSpan(android.graphics.Typeface.BOLD);
            spannable.setSpan(span2, matcher.start(), matcher.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannable.setSpan(span, matcher.start(), matcher.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return spannable;
    }

    public static void animateView(Context ctx, View view, boolean show, boolean animate) {
        view.setVisibility(show ? View.VISIBLE : View.GONE);
        if (animate) {
            Animation animation = AnimationUtils.loadAnimation(ctx, show ? android.R.anim.fade_in : android.R.anim.fade_out);
            if (animation != null) {
                view.startAnimation(animation);
            }
        }
    }
}
