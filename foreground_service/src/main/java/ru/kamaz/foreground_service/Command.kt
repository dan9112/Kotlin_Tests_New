package ru.kamaz.foreground_service

import android.os.Parcel
import android.os.Parcelable

sealed interface Command : Parcelable {

    data class Start(val route: List<DestinationInfo>) : Command {
        @Suppress("unchecked_cast")
        private constructor(parcel: Parcel) : this(
            route = parcel.readArray(Start::class.java.classLoader)!!.map {
                it as DestinationInfo
            }
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeList(route)
        }

        override fun describeContents() = 0

        companion object CREATOR : Parcelable.Creator<Start> {
            override fun createFromParcel(parcel: Parcel) = Start(parcel)

            override fun newArray(size: Int) = arrayOfNulls<Start?>(size)
        }
    }

    class Stop : Command {

        override fun writeToParcel(parcel: Parcel, flags: Int) {
        }

        override fun describeContents() = 0

        companion object CREATOR : Parcelable.Creator<Stop> {
            override fun createFromParcel(parcel: Parcel) = Stop()

            override fun newArray(size: Int) = arrayOfNulls<Stop?>(size)
        }
    }
}
