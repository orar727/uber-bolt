package ro.calculatorcursa

data class Offer(
    val platform: String,
    val fare: Double,
    val pickupMin: Double,
    val pickupKm: Double,
    val tripMin: Double,
    val tripKm: Double
)

object Parser {
    const val BOLT = "ee.mtakso.driver"
    const val UBER = "com.ubercab.driver"

    // Bolt: "29,92 lei (NET, taxe incluse)", "4 min • 1.8 km", "17 min • 9.7 km"
    private val boltFare = Regex("""(?<![\d.,])(\d[\d.]*(?:,\d{1,2})?)\s*lei""", RegexOption.IGNORE_CASE)
    private val boltLeg = Regex("""(\d+)\s*min\W{0,6}(\d+(?:[.,]\d+)?)\s*km""", RegexOption.IGNORE_CASE)

    // Uber: "37,78 RON", "+8,05 RON pentru preluare", "La 2 min. (0.6 km) distanță", "Cursă: 29 min. (16.3 km)"
    private val uberFare = Regex("""(?<![+\d.,])(\d[\d.]*[,.]\d{2})\s*RON""")
    private val uberPickup = Regex("""La\s+(\d+)\s*min\.?\s*\(\s*(\d+(?:[.,]\d+)?)\s*km\s*\)""", RegexOption.IGNORE_CASE)
    private val uberTrip = Regex("""Curs[ăa]\s*:?\s*(\d+)\s*min\.?\s*\(\s*(\d+(?:[.,]\d+)?)\s*km\s*\)""", RegexOption.IGNORE_CASE)

    fun num(s: String): Double? {
        var t = s.trim()
        if (t.contains(',') && t.contains('.')) {
            t = if (t.lastIndexOf(',') > t.lastIndexOf('.')) {
                t.replace(".", "").replace(',', '.')
            } else {
                t.replace(",", "")
            }
        } else if (t.contains(',')) {
            t = t.replace(',', '.')
        }
        return t.toDoubleOrNull()
    }

    fun parse(pkg: String, text: String): Offer? {
        return when (pkg) {
            BOLT -> parseBolt(text)
            UBER -> parseUber(text)
            else -> null
        }
    }

    private fun parseBolt(text: String): Offer? {
        val fare = boltFare.find(text)?.groupValues?.get(1)?.let { num(it) } ?: return null
        val legs = boltLeg.findAll(text).toList()
        if (legs.size < 2) return null
        val pMin = num(legs[0].groupValues[1]) ?: return null
        val pKm = num(legs[0].groupValues[2]) ?: return null
        val tMin = num(legs[1].groupValues[1]) ?: return null
        val tKm = num(legs[1].groupValues[2]) ?: return null
        return Offer("Bolt", fare, pMin, pKm, tMin, tKm)
    }

    private fun parseUber(text: String): Offer? {
        val fare = uberFare.find(text)?.groupValues?.get(1)?.let { num(it) } ?: return null
        val p = uberPickup.find(text) ?: return null
        val t = uberTrip.find(text) ?: return null
        val pMin = num(p.groupValues[1]) ?: return null
        val pKm = num(p.groupValues[2]) ?: return null
        val tMin = num(t.groupValues[1]) ?: return null
        val tKm = num(t.groupValues[2]) ?: return null
        return Offer("Uber", fare, pMin, pKm, tMin, tKm)
    }
}
