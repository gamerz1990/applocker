package com.maliks.applocker.xtreme.service

import com.maliks.applocker.xtreme.data.database.pattern.PatternDot
import com.maliks.applocker.xtreme.util.helper.PatternChecker
import io.reactivex.functions.BiFunction

class PatternValidatorFunction : BiFunction<List<PatternDot>, List<PatternDot>, Boolean> {
    override fun apply(t1: List<PatternDot>, t2: List<PatternDot>): Boolean {
        return PatternChecker.checkPatternsEqual(t1, t2)
    }
}