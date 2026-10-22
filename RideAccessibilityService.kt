package ro.calculatorcursa

import android.accessibilityservice.AccessibilityService
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.SystemClock
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.LinearLayout
import android.widget.TextView
import java.util.Locale

class RideAccessibilityService : AccessibilityService() {

    private var wm: WindowManager? = null
    private var overlay: LinearLayout? = null
    private var title: TextView? = null
    private var detail: TextView? = null
    private var lastScan = 0L
    private var lastOfferAt = 0L
    private var lastKey = ""

    override fun onServiceConnected() {
        super.onServiceConnected()
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
    }

    override fun onInterrupt() {
        removeOverlay()
    }

    override fun onDestroy() {
        removeOverlay()
        super.onDestroy()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val pkg = event?.packageName?.toString() ?: return
        if (pkg != Parser.BOLT && pkg != Parser.UBER) return

        val now = SystemClock.uptimeMillis()
        if (now - lastScan < 300) return
        lastScan = now

        val root = rootInActiveWindow ?: return
        val lines = ArrayList<String>()
        collect(root, lines, 0)
        val text = lines.joinToString("\n")

        val offer = try {
            Parser.parse(pkg, text)
        } catch (e: Exception) {
            null
        }

        if (offer == null) {
            if (overlay != null && now - lastOfferAt > 1500) removeOverlay()
            return
        }

        lastOfferAt = now
        val key = "${offer.platform}|${offer.fare}|${offer.pickupKm}|${offer.tripKm}|${offer.tripMin}"
        if (key == lastKey && overlay != null) return
        lastKey = key

        val r = Calc.compute(this, offer)
        Log.d("CalculatorCursa", "oferta=$offer rezultat=$r")
        show(offer, r)
    }

    private fun collect(n: AccessibilityNodeInfo?, out: MutableList<String>, depth: Int) {
        if (n == null || depth > 40) return
        val t = n.text
        if (t != null && t.isNotBlank()) {
            out.add(t.toString())
        } else {
            val d = n.contentDescription
            if (d != null && d.isNotBlank()) out.add(d.toString())
        }
        for (i in 0 until n.childCount) {
            collect(n.getChild(i), out, depth + 1)
        }
    }

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()

    private fun fmt(v: Double): String = String.format(Locale("ro", "RO"), "%.2f", v)

    private fun show(o: Offer, r: Result) {
        val manager = wm ?: return
        val (word, color) = when (r.verdict) {
            2 -> Pair("ACCEPTĂ", Color.parseColor("#E6118C5A"))
            1 -> Pair("LIMITĂ", Color.parseColor("#E6B36B00"))
            else -> Pair("REFUZĂ", Color.parseColor("#E6C62832"))
        }

        if (overlay == null) {
            val box = LinearLayout(this)
            box.orientation = LinearLayout.VERTICAL
            box.setPadding(dp(14), dp(10), dp(14), dp(10))

            val t = TextView(this)
            t.setTextColor(Color.WHITE)
            t.textSize = 22f
            t.setTypeface(Typeface.DEFAULT_BOLD)

            val d = TextView(this)
            d.setTextColor(Color.WHITE)
            d.textSize = 15f

            box.addView(t)
            box.addView(d)

            val lp = WindowManager.LayoutParams(
                resources.displayMetrics.widthPixels - dp(32),
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
            )
            lp.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            lp.y = dp(70)

            try {
                manager.addView(box, lp)
                overlay = box
                title = t
                detail = d
            } catch (e: Exception) {
                Log.e("CalculatorCursa", "overlay: $e")
                return
            }
        }

        val bg = GradientDrawable()
        bg.setColor(color)
        bg.cornerRadius = dp(10).toFloat()
        overlay?.background = bg
        title?.text = "$word   ${fmt(r.net)} lei net"
        detail?.text = "${fmt(r.perKm)} lei/km  •  ${fmt(r.perHour)} lei/oră  •  ${o.platform}"
    }

    private fun removeOverlay() {
        val v = overlay ?: return
        try {
            wm?.removeView(v)
        } catch (e: Exception) {
        }
        overlay = null
        title = null
        detail = null
        lastKey = ""
    }
}
