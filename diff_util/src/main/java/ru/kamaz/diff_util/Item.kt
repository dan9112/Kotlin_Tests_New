package ru.kamaz.diff_util

import android.os.Parcel
import android.os.Parcelable

data class Item(
    val id: Int,
    val name: String,
    val value: String
) : Parcelable {
    private constructor(`in`: Parcel) : this(
        `in`.readInt(),
        `in`.readString()!!,
        `in`.readString()!!
    )

    override fun writeToParcel(out: Parcel, flags: Int) {
        out.writeInt(id)
        out.writeString(name)
        out.writeString(value)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<Item> {
        override fun createFromParcel(`in`: Parcel): Item {
            return Item(`in`)
        }

        override fun newArray(size: Int): Array<Item?> {
            return arrayOfNulls(size)
        }
    }
}
