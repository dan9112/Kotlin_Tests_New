package ru.kamaz.foreground_service

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.IdRes

data class DestinationInfo(
    @IdRes val destId: Int,
    val destArgs: Bundle?
) : Parcelable {
    private constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readBundle(Bundle::class.java.classLoader)
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(destId)
        parcel.writeBundle(destArgs)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<DestinationInfo> {
        override fun createFromParcel(parcel: Parcel) = DestinationInfo(parcel)

        override fun newArray(size: Int) = arrayOfNulls<DestinationInfo?>(size)
    }
}
