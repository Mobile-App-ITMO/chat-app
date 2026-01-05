package com.example.myapplication.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

val InterFontFamily = FontFamily.Default

val AppTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.ExtraBold, //800
        fontSize = 32.sp,
        lineHeight = 40.sp
    ),

    headlineMedium = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.SemiBold, //600
        fontSize = 24.sp,
        lineHeight = 32.sp
    ),

    bodyLarge = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Normal,  //400
        fontSize = 18.sp,
        lineHeight = 26.sp
    ),

    bodyMedium = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Normal, //400
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),

    labelLarge = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp
    )
)

val Typography.buttonCenter: TextStyle
    get() = labelLarge.copy(textAlign = TextAlign.Center)

val Typography.titleCenter: TextStyle
    get() = headlineLarge.copy(textAlign = TextAlign.Center)