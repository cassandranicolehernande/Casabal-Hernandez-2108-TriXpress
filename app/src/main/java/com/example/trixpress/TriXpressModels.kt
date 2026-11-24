@file:Suppress("unused", "MemberVisibilityCanBePrivate", "RedundantVisibilityModifier")

package com.example.trixpress

interface Bookable {
    fun createBooking(pickup: String, dropoff: String): Boolean
    fun cancelBooking(bookingId: String): Boolean
}

abstract class User(
    val uid: String,
    var fullName: String,
    var contactNumber: String,
    var email: String
) {
    abstract fun getDashboardTitle(): String
}

class Passenger(
    uid: String,
    fullName: String,
    contactNumber: String,
    email: String,
    var preferredZone: String = "Nasugbu Zone 1"
) : User(uid, fullName, contactNumber, email) {
    override fun getDashboardTitle(): String {
        return "Passenger Portal: $fullName"
    }
}


class Driver(
    uid: String,
    fullName: String,
    contactNumber: String,
    email: String,
    var licenseNumber: String,
    var todaAssociation: String,
    var plateNumber: String
) : User(uid, fullName, contactNumber, email) {
    override fun getDashboardTitle(): String {
        return "TODA Driver Portal: $fullName ($todaAssociation)"
    }
}

data class Booking(
    val bookingId: String,
    val passengerName: String,
    val pickupLocation: String,
    val dropoffLocation: String,
    val serviceType: String,
    val estimatedFare: String,
    var status: String = "PENDING"
) : Bookable {
    override fun createBooking(pickup: String, dropoff: String): Boolean {
        return pickup.isNotBlank() && dropoff.isNotBlank()
    }

    override fun cancelBooking(bookingId: String): Boolean {
        return bookingId.isNotBlank()
    }
}

data class Trip(
    val tripId: String,
    val driverName: String,
    val pickup: String,
    val destination: String,
    val fare: String,
    val date: String,
    val rating: Float
)
