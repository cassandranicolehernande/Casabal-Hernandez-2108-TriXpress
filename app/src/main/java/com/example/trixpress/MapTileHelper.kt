package com.example.trixpress

import org.osmdroid.tileprovider.tilesource.ITileSource
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.views.MapView

object MapTileHelper {
    val streetTileSource: ITileSource = XYTileSource(
        "OSMDe",
        0,
        19,
        256,
        ".png",
        arrayOf(
            "https://tile.openstreetmap.de/tiles/osmde/"
        ),
        "© OpenStreetMap contributors"
    )

    val lightTileSource: ITileSource = XYTileSource(
        "OSMFr",
        0,
        19,
        256,
        ".png",
        arrayOf(
            "https://a.tile.openstreetmap.fr/hot/"
        ),
        "© OpenStreetMap contributors"
    )

    fun applyDefaultStreetTiles(mapView: MapView) {
        mapView.setTileSource(streetTileSource)
        mapView.invalidate()
    }

    fun toggleStreetAndTopo(mapView: MapView, useTopo: Boolean) {
        mapView.setTileSource(if (useTopo) lightTileSource else streetTileSource)
        mapView.invalidate()
    }
}
