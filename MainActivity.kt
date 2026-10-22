package ro.calculatorcursa

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private lateinit var status: TextView
    private val fields = LinkedHashMap<String, EditText>()

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setPadding(dp(16), dp(24), dp(16), dp(32))

        val h = TextView(this)
        h.text = "Calculator Cursă"
        h.textSize = 26f
        h.setTypeface(Typeface.DEFAULT_BOLD)
        root.addView(h)

        status = TextView(this)
        status.textSize = 15f
        status.setPadding(0, dp(8), 0, dp(8))
        root.addView(status)

        val acc = Button(this)
        acc.text = "Deschide setările de accesibilitate"
        acc.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
        root.addView(acc)

        val info = TextView(this)
        info.textSize = 13f
        info.text = "Activează „Calculator Cursă” în Accesibilitate > Aplicații instalate. " +
            "Dacă Android blochează activarea: Setări > Aplicații > Calculator Cursă > meniul ⋮ > Permite setări restricționate."
        info.setPadding(0, dp(8), 0, dp(16))
        root.addView(info)

        for ((key, label, _) in Prefs.defs) {
            val l = TextView(this)
            l.text = label
            l.textSize = 13f
            root.addView(l)
            val e = EditText(this)
            e.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            e.setText(Prefs.raw(this, key))
            root.addView(e)
            fields[key] = e
        }

        val note = TextView(this)
        note.textSize = 12f
        note.text = "Bolt și Uber afișează deja suma netă. Lasă comisionul pe 0, dacă nu vrei să scazi încă o taxă."
        note.setPadding(0, dp(8), 0, dp(8))
        root.addView(note)

        val save = Button(this)
        save.text = "Salvează"
        save.setOnClickListener {
            for ((k, e) in fields) Prefs.save(this, k, e.text.toString())
            Toast.makeText(this, "Salvat", Toast.LENGTH_SHORT).show()
        }
        root.addView(save)

        val sv = ScrollView(this)
        sv.addView(root)
        setContentView(sv)
    }

    override fun onResume() {
        super.onResume()
        val enabled = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) ?: ""
        val on = enabled.contains(packageName)
        status.text = if (on) "Serviciu activ. Deschide Bolt sau Uber Driver." else "Serviciul nu este activ."
    }
}
