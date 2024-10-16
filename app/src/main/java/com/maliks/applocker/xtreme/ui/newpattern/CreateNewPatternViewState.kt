package com.maliks.applocker.xtreme.ui.newpattern

import android.content.Context
import com.maliks.applocker.xtreme.R
import com.maliks.applocker.xtreme.ui.newpattern.CreateNewPatternViewModel.PatternEvent.*

data class CreateNewPatternViewState(val patternEvent: CreateNewPatternViewModel.PatternEvent) {

    fun getPromptText(context: Context): String =
        when (patternEvent) {
            INITIALIZE -> context.getString(R.string.draw_pattern_title)
            FIRST_COMPLETED -> context.getString(R.string.redraw_pattern_title)
            SECOND_COMPLETED -> context.getString(R.string.create_pattern_successful)
            ERROR -> context.getString(R.string.recreate_pattern_error)
        }

    fun isCreatedNewPattern(): Boolean = patternEvent == SECOND_COMPLETED
}