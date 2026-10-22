package ro.calculatorcursa

import android.content.Context

object Prefs {
    // cheie, etichetă, valoare implicită
    val defs = listOf(
        Triple("price", "Preț combustibil (lei/l sau lei/kWh)", "8.6"),
        Triple("cons", "Consum (l sau kWh la 100 km)", "7.0"),
        Triple("wear", "Uzură și întreținere (lei/km)", "0.35"),
        Triple("comBolt", "Comision Bolt scăzut în plus (%)", "0"),
        Triple("comUber", "Comision Uber scăzut în plus (%)", "0"),
        Triple("minKm", "Minim lei/km net", "1.5"),
        Triple("minH", "Minim lei/oră net", "40")
    )

    fun raw(ctx: Context, key: String): String {
        val d = defs.first { it.first == key }.third
        return ctx.getSharedPreferences("cfg", Context.MODE_PRIVATE).getString(key, d) ?: d
    }

    fun get(ctx: Context, key: String): Double {
        val d = defs.first { it.first == key }.third
        return raw(ctx, key).replace(',', '.').toDoubleOrNull() ?: d.toDouble()
    }

    fun save(ctx: Context, key: String, value: String) {
        ctx.getSharedPreferences("cfg", Context.MODE_PRIVATE).edit().putString(key, value).apply()
    }
}

data class Result(
    val net: Double,
    val perKm: Double,
    val perHour: Double,
    val verdict: Int // 2 = acceptă, 1 = limită, 0 = refuză
)

object Calc {
    fun compute(ctx: Context, o: Offer): Result {
        val km = o.pickupKm + o.tripKm
        val min = maxOf(o.pickupMin + o.tripMin, 1.0)
        val com = if (o.platform == "Bolt") Prefs.get(ctx, "comBolt") else Prefs.get(ctx, "comUber")
        val fuel = km * Prefs.get(ctx, "cons") / 100.0 * Prefs.get(ctx, "price")
        val wear = km * Prefs.get(ctx, "wear")
        val net = o.fare * (1.0 - com / 100.0) - fuel - wear
        val perKm = if (km > 0) net / km else 0.0
        val perHour = net / (min / 60.0)
        val okKm = perKm >= Prefs.get(ctx, "minKm")
        val okH = perHour >= Prefs.get(ctx, "minH")
        val v = when {
            net <= 0.0 -> 0
            okKm && okH -> 2
            okKm || okH -> 1
            else -> 0
        }
        return Result(net, perKm, perHour, v)
    }
}
