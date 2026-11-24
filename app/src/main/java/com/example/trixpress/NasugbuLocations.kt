package com.example.trixpress

import android.location.Location
import com.google.android.gms.maps.model.LatLng
import java.util.Locale

data class NasugbuPlace(
    val name: String,
    val latLng: LatLng,
    val isLandmark: Boolean = false
)

object NasugbuLocations {

    val barangays = listOf(
        NasugbuPlace("Barangay 1 (Poblacion)", LatLng(14.0725, 120.6305)),
        NasugbuPlace("Barangay 2 (Poblacion)", LatLng(14.0738, 120.6315)),
        NasugbuPlace("Barangay 3 (Poblacion)", LatLng(14.0742, 120.6326)),
        NasugbuPlace("Barangay 4 (Poblacion)", LatLng(14.0752, 120.6334)),
        NasugbuPlace("Barangay 5 (Poblacion)", LatLng(14.0745, 120.6345)),
        NasugbuPlace("Barangay 6 (Poblacion)", LatLng(14.0755, 120.6358)),
        NasugbuPlace("Barangay 7 (Poblacion)", LatLng(14.0735, 120.6355)),
        NasugbuPlace("Barangay 8 (Poblacion)", LatLng(14.0722, 120.6332)),
        NasugbuPlace("Barangay 9 (Poblacion)", LatLng(14.0712, 120.6312)),
        NasugbuPlace("Barangay 10 (Poblacion)", LatLng(14.0715, 120.6348)),
        NasugbuPlace("Barangay 11 (Poblacion)", LatLng(14.0762, 120.6318)),
        NasugbuPlace("Barangay 12 (Poblacion)", LatLng(14.0768, 120.6342)),
        NasugbuPlace("Barangay Aga", LatLng(14.0835, 120.7685)),
        NasugbuPlace("Barangay Balaytigui", LatLng(14.1390, 120.6085)),
        NasugbuPlace("Barangay Banilad", LatLng(14.0748, 120.6845)),
        NasugbuPlace("Barangay Bilaran", LatLng(14.0790, 120.7150)),
        NasugbuPlace("Barangay Bucana", LatLng(14.0668, 120.6255)),
        NasugbuPlace("Barangay Bulihan", LatLng(14.0858, 120.6512)),
        NasugbuPlace("Barangay Bunducan", LatLng(14.0945, 120.6470)),
        NasugbuPlace("Barangay Butucan", LatLng(14.0545, 120.6490)),
        NasugbuPlace("Barangay Calayo", LatLng(14.1485, 120.6115)),
        NasugbuPlace("Barangay Catandaan", LatLng(14.0965, 120.6920)),
        NasugbuPlace("Barangay Cogunan", LatLng(14.1045, 120.6740)),
        NasugbuPlace("Barangay Dayap", LatLng(14.0772, 120.7010)),
        NasugbuPlace("Barangay Latag", LatLng(14.0615, 120.6580)),
        NasugbuPlace("Barangay Looc", LatLng(14.1560, 120.6450)),
        NasugbuPlace("Barangay Lumbangan", LatLng(14.0735, 120.6690)),
        NasugbuPlace("Barangay Malapad na Bato", LatLng(14.1185, 120.6620)),
        NasugbuPlace("Barangay Mataas na Pulo", LatLng(14.1380, 120.6540)),
        NasugbuPlace("Barangay Maugat", LatLng(14.0630, 120.6750)),
        NasugbuPlace("Barangay Munting Indan", LatLng(14.0560, 120.6365)),
        NasugbuPlace("Barangay Natipuan", LatLng(14.1283, 120.6019)),
        NasugbuPlace("Barangay Pantalan", LatLng(14.0785, 120.6255)),
        NasugbuPlace("Barangay Papaya", LatLng(14.1980, 120.6180)),
        NasugbuPlace("Barangay Putat", LatLng(14.0762, 120.6455)),
        NasugbuPlace("Barangay Reparo", LatLng(14.0910, 120.7420)),
        NasugbuPlace("Barangay Talangan", LatLng(14.0685, 120.6410)),
        NasugbuPlace("Barangay Tumalim", LatLng(14.0885, 120.6880)),
        NasugbuPlace("Barangay Utod", LatLng(14.0625, 120.6440)),
        NasugbuPlace("Barangay Wawa", LatLng(14.0845, 120.6248)),
        NasugbuPlace("Barangay Kaylaway", LatLng(14.0895, 120.8030)),
        NasugbuPlace("Barangay Kayrilaw", LatLng(14.0980, 120.7850))
    )

    val landmarks = listOf(
        NasugbuPlace("BatStateU ARASOF - Nasugbu Campus", LatLng(14.0673, 120.6262), true),
        NasugbuPlace("Nasugbu East Central School", LatLng(14.0750, 120.6350), true),
        NasugbuPlace("Nasugbu West Central School", LatLng(14.0725, 120.6308), true),
        NasugbuPlace("Bunducan Elementary School", LatLng(14.0945, 120.6470), true),
        NasugbuPlace("Looc National High School", LatLng(14.1552, 120.6445), true),
        NasugbuPlace("Nasugbu Municipal Hall", LatLng(14.0736, 120.6322), true),
        NasugbuPlace("Nasugbu Town Plaza", LatLng(14.0731, 120.6320), true),
        NasugbuPlace("St. Francis Xavier Parish Church", LatLng(14.0736, 120.6318), true),
        NasugbuPlace("Nasugbu Police & Fire Station", LatLng(14.0739, 120.6325), true),
        NasugbuPlace("Nasugbu Public Market", LatLng(14.064070, 120.633164), true),
        NasugbuPlace("Jollibee Nasugbu Crossing", LatLng(14.0744, 120.6346), true),
        NasugbuPlace("Waltermart Nasugbu", LatLng(14.0762, 120.6442), true),
        NasugbuPlace("Savemore Market Nasugbu", LatLng(14.0748, 120.6352), true),
        NasugbuPlace("Nasugbu Grand Bus & Van Terminal", LatLng(14.0746, 120.6350), true),
        NasugbuPlace("Apacible Memorial District Hospital (Lumbangan)", LatLng(14.0742, 120.6685), true),
        NasugbuPlace("Wawa Port / Nasugbu Fish Port", LatLng(14.0845, 120.6248), true),
        NasugbuPlace("Bucana Beach / Nasugbu Baywalk", LatLng(14.0655, 120.6230), true),
        NasugbuPlace("Apacible Blvd / White Beach", LatLng(14.0710, 120.6245), true),
        NasugbuPlace("Natipuan Beach", LatLng(14.1283, 120.6019), true),
        NasugbuPlace("Terrazas / Peninsula de Punta Fuego", LatLng(14.1245, 120.5985), true),
        NasugbuPlace("Tali Beach (Balaytigui)", LatLng(14.1395, 120.6065), true),
        NasugbuPlace("Calayo Beach Resort", LatLng(14.1485, 120.6115), true),
        NasugbuPlace("Pico de Loro Cove / Hamilo Coast", LatLng(14.1845, 120.6160), true),
        NasugbuPlace("Central Azucarera Don Pedro (CADPI)", LatLng(14.0728, 120.6710), true),
        NasugbuPlace("Ternate-Nasugbu Highway Junction", LatLng(14.0955, 120.6485), true),
        NasugbuPlace("Kaybiang Tunnel Boundary", LatLng(14.2115, 120.6320), true),
        NasugbuPlace("East-West Road View Deck (Kaylaway)", LatLng(14.0895, 120.8030), true),
        NasugbuPlace("Caleruega / Tagaytay-Nasugbu Border", LatLng(14.0718, 120.8345), true),
        NasugbuPlace("Canyon Cove Hotel & Spa", LatLng(14.0882, 120.6225), true),
        NasugbuPlace("Nasugbu Pavilion Beach Park", LatLng(14.0695, 120.6242), true),
        NasugbuPlace("Kawayan Cove", LatLng(14.0965, 120.6145), true),
        NasugbuPlace("Maya-Maya Yacht Club & Subdivision", LatLng(14.1165, 120.6055), true),
        NasugbuPlace("Santelmo Cove", LatLng(14.1355, 120.6040), true),
        NasugbuPlace("Bituin Cove / Angara Cove Jump-off", LatLng(14.1540, 120.6060), true),
        NasugbuPlace("Layag-Layag Beach", LatLng(14.1695, 120.6105), true),
        NasugbuPlace("Pico Sands Hotel / Hamilo Coast Terminal", LatLng(14.1845, 120.6160), true),
        NasugbuPlace("Fortune Island Boat Jump-off (Wawa Port)", LatLng(14.0845, 120.6248), true),
        NasugbuPlace("Forest Crest Nature Hotel & Resort", LatLng(14.0830, 120.8220), true),
        NasugbuPlace("Mt. Batulao Jump-off (Kaylaway)", LatLng(14.0615, 120.8085), true),
        NasugbuPlace("Mt. Talamitan Jump-off (Sitio Bayabasan, Aga)", LatLng(14.0885, 120.7710), true),
        NasugbuPlace("Mt. Pico de Loro Trailhead (Papaya)", LatLng(14.2105, 120.6350), true),
        NasugbuPlace("Kainan sa Dalampasigan", LatLng(14.0728, 120.6295), true),
        NasugbuPlace("Fisherman's Deck of Wawa", LatLng(14.0853, 120.6234), true),
        NasugbuPlace("El Cocinero by Chef Arnaldo", LatLng(14.0736, 120.6319), true),
        NasugbuPlace("La Locanda Ristorante Pizzeria", LatLng(14.0738, 120.6315), true),
        NasugbuPlace("McDonald's Nasugbu Town Proper", LatLng(14.0743, 120.6342), true),
        NasugbuPlace("Villa Gregorio Subdivision", LatLng(14.0698, 120.6285), true),
        NasugbuPlace("Roxaco / CADPI Housing (Lumbangan)", LatLng(14.0740, 120.6725), true),
        NasugbuPlace("Tali Beach Subdivision Gate", LatLng(14.1395, 120.6080), true)
    )

    val allPlaces: List<NasugbuPlace> = landmarks + barangays

    val allPlaceNames: Array<String> = allPlaces.map { it.name }.toTypedArray()

    fun findCoordinatesByName(query: String): LatLng {
        val lower = query.lowercase(Locale.getDefault())
        val found = allPlaces.firstOrNull { it.name.lowercase(Locale.getDefault()).contains(lower) }
        return found?.latLng ?: LatLng(14.064070, 120.633164)
    }

    fun findNearestPlaceName(lat: Double, lng: Double): String {
        var closestName = "Nasugbu Location"
        var minDistance = Float.MAX_VALUE
        val results = FloatArray(1)

        for (place in allPlaces) {
            Location.distanceBetween(lat, lng, place.latLng.latitude, place.latLng.longitude, results)
            if (results[0] < minDistance) {
                minDistance = results[0]
                closestName = place.name
            }
        }
        return closestName
    }
}
