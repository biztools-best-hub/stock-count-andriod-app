package com.biztools.stockcount.ui.utilities

import java.util.Calendar

val Calendar.customDisplay
    get() = "${this.get(Calendar.DATE)}/${this.get(Calendar.MONTH) + 1}/${
        this.get(Calendar.YEAR)
    } ${this.get(Calendar.HOUR)}:${this.get(Calendar.MINUTE)}"
val Double.toLocalString: String
    get() = if (this.toString().split(".").isEmpty()) ""
    else if (this.toString().split(".").size == 1) "$this.00"
    else if (this.toString().split(".")[1].length < 2) "${this}0"
    else if (this.toString().split(".")[1].length > 2)
        "${this.toString().split(".")[0]}.${this.toString().split(".")[1].substring(0, 2)}"
    else this.toString()