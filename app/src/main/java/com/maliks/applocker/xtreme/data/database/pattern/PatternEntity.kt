package com.maliks.applocker.xtreme.data.database.pattern

import androidx.room.*
import com.andrognito.patternlockview.PatternLockView

@Entity(tableName = "pattern")
@TypeConverters(PatternTypeConverter::class)
data class PatternEntity(
    @PrimaryKey
    @ColumnInfo(name = "pattern_metadata")
    val patternMetadata: PatternDotMetadata
)