package com.app.spark.models

import android.graphics.Bitmap
import android.os.Parcelable
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import kotlinx.android.parcel.Parcelize
import androidx.databinding.library.baseAdapters.BR

@Parcelize
data class TagListModel (
    var tagName : String,
    var isSelected : Boolean=false,
    var isEditable: Boolean=false
): Parcelable


@Parcelize
data class ConnectCounsellorRequest (
    var preferredName : String="",
    var yourAge : String="",
    var yourIssue : String="",
    var relationshipStatus : String="",
    var currentMood : String="",
    var currentSituation : String=""
): Parcelable

class UpdateFlied: BaseObservable() {
    @Bindable
    @Transient
    var relationshipStatus: String = ""
        set(value) {
            if (field != value) {
                field = value
            }
            notifyPropertyChanged(BR.relationshipStatus)
        }
    @Bindable
    @Transient
    var ageStatus: String = ""
        set(value) {
            if (field != value) {
                field = value
            }
            notifyPropertyChanged(BR.ageStatus)
        }
    @Bindable
    @Transient
    var charStatus: Int = 200
        set(value) {
            if (field != value) {
                field = value
            }
            notifyPropertyChanged(BR.charStatus)
        }
}
