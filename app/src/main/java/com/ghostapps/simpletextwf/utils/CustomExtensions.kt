package com.ghostapps.simpletextwf.utils

import android.content.Context
import android.graphics.Canvas
import com.ghostapps.simpletextwf.R

fun Int.toText(context: Context): String {
    return if (this <= 20) {
        numberToText(context, this)
    } else {
        val decimal = this / 10
        val unit = this % 10

        if (unit == 0) {
            numberToText(context, decimal * 10)
        } else {
            "${numberToText(
                context,
                decimal * 10
            )} ${numberToText(context, unit)}"
        }
    }
}

private fun numberToText(context: Context, number: Int): String {
    return when (number) {
        0 -> context.getString(R.string.zero)
        1 -> context.getString(R.string.one)
        2 -> context.getString(R.string.two)
        3 -> context.getString(R.string.three)
        4 -> context.getString(R.string.four)
        5 -> context.getString(R.string.five)
        6 -> context.getString(R.string.six)
        7 -> context.getString(R.string.seven)
        8 -> context.getString(R.string.eight)
        9 -> context.getString(R.string.nine)
        10 -> context.getString(R.string.ten)
        11 -> context.getString(R.string.eleven)
        12 -> context.getString(R.string.twelve)
        13 -> context.getString(R.string.thirteen)
        14 -> context.getString(R.string.fourteen)
        15 -> context.getString(R.string.fifteen)
        16 -> context.getString(R.string.sixteen)
        17 -> context.getString(R.string.seventeen)
        18 -> context.getString(R.string.eighteen)
        19 -> context.getString(R.string.nineteen)
        20 -> context.getString(R.string.twenty)
        30 -> context.getString(R.string.thirty)
        40 -> context.getString(R.string.forty)
        50 -> context.getString(R.string.fifty)
        else -> ""
    }
}

fun Int.numberToMonth(context: Context): String {
    return when (this) {
        0 -> context.getString(R.string.january)
        1 -> context.getString(R.string.february)
        2 -> context.getString(R.string.march)
        3 -> context.getString(R.string.april)
        4 -> context.getString(R.string.may)
        5 -> context.getString(R.string.june)
        6 -> context.getString(R.string.july)
        7 -> context.getString(R.string.august)
        8 -> context.getString(R.string.september)
        9 -> context.getString(R.string.october)
        10 -> context.getString(R.string.november)
        11 -> context.getString(R.string.december)
        else -> ""
    }
}
