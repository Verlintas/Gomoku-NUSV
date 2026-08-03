package com.gomoku.nusv

import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarUnitDay
import platform.Foundation.NSCalendarUnitMonth
import platform.Foundation.NSCalendarUnitYear
import platform.Foundation.NSDate

actual fun todayStr(): String {
    val cal = NSCalendar()
    val comps = cal.components(
        NSCalendarUnitYear or NSCalendarUnitMonth or NSCalendarUnitDay,
        fromDate = NSDate()
    )
    val y = comps.year ?: 1970
    val m = comps.month ?: 1
    val d = comps.day ?: 1
    val mm = if (m < 10) "0$m" else "$m"
    val dd = if (d < 10) "0$d" else "$d"
    return "$y-$mm-$dd"
}
