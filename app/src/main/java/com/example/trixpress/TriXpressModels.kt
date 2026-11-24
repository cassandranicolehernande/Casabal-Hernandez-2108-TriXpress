@file:Suppress("unused", "MemberVisibilityCanBePrivate", "RedundantVisibilityModifier")

package com.example.trixpress

// Interface enforcing SRP and ISP contracts (OOP Concepts & SOLID)
interface Bookable {
    fun createBooking(pickup: String, dropoff: String): Boolean
    fun cancelBooking(bookingId: String): Boolean
}

// Abstract Superclass demonstrating Abstraction & Encapsulation
abstract class User(
    val uid: String,
    var fullName: String,
    var contactNumber: String,
    var email: String
) {
    abstract fun getDashboardTitle(): String
}

// Concrete subclass Passenger extending User (Inheritance & Polymorphism)
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

// Concrete subclass Driver extending User (Inheritance & Polymorphism)
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

// Domain Model for Booking implementing Bookable interface
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

// Domain Model for Completed Trips
data class Trip(
    val tripId: String,
    val driverName: String,
    val pickup: String,
    val destination: String,
    val fare: String,
    val date: String,
    val rating: Float
)
