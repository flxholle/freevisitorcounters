package com.asdoi.freevisitorcounters

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputEditText
import im.dacer.androidcharts.BarView
import im.dacer.androidcharts.LineView
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity(), View.OnClickListener {
    private var visitorCounter: VisitorCounter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val input = findViewById<TextInputEditText>(R.id.url_input_field)
        input.setOnEditorActionListener(object : TextView.OnEditorActionListener {
            override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    actionId == EditorInfo.IME_ACTION_DONE ||
                    event != null &&
                    event.action == KeyEvent.ACTION_DOWN &&
                    event.keyCode == KeyEvent.KEYCODE_ENTER
                ) {
                    if (event == null || !event.isShiftPressed) {
                        hideKeyboard(this@MainActivity)

                        val id = try {
                            Integer.parseInt(input.text.toString())
                        } catch (e: Exception) {
                            0
                        }

                        val url = if (id == 0)
                            input.text.toString()
                        else
                            "https://www.freevisitorcounters.com/en/home/stats/id/$id"


                        setLastURL(url)
                        loadSite(url)

                        return true
                    }
                }
                return false
            }

        })
        input.setText(getLastURL())
        loadSite(getLastURL())

        setDailyAlarm()
    }

    private fun loadSite(url: String) {
        Thread {
            visitorCounter = Parser.parseWebsite(url)
            runOnUiThread {
                generateUI(visitorCounter)
            }
        }.start()
    }

    private fun generateUI(visitorCounter: VisitorCounter?) {
        findViewById<LinearLayout>(R.id.main_linear).removeAllViews()

        if (visitorCounter != null) {
            //Domain name
            findViewById<TextView>(R.id.domain_tV).text = visitorCounter.domain

            //Overview
            val overviewView = layoutInflater.inflate(R.layout.overview, null)
            val overview = visitorCounter.overview
            overviewView.findViewById<TextView>(R.id.title).text = overview.title
            overviewView.findViewById<TextView>(R.id.title).paintFlags =
                Paint.UNDERLINE_TEXT_FLAG
            overviewView.findViewById<TextView>(R.id.textView1).text = overview.sinceTitle
            overviewView.findViewById<TextView>(R.id.textView2).text =
                overview.prognosisTitle
            overviewView.findViewById<TextView>(R.id.textView3).text = overview.bestDayTitle
            overviewView.findViewById<TextView>(R.id.textView4).text =
                overview.bestDayVisitorsTitle

            var df: DateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

            overviewView.findViewById<TextView>(R.id.textView11).text =
                df.format(overview.since)
            overviewView.findViewById<TextView>(R.id.textView21).text = overview.prognosis
            overviewView.findViewById<TextView>(R.id.textView31).text =
                df.format(overview.bestDay)
            overviewView.findViewById<TextView>(R.id.textView41).text =
                overview.bestDayVisitors.toString()

            findViewById<LinearLayout>(R.id.main_linear).addView(overviewView)

            //Visitors Overview
            val visitorsOverviewView = layoutInflater.inflate(R.layout.overview, null)
            val visitorsOverview = visitorCounter.visitorsOverview
            visitorsOverviewView.findViewById<TextView>(R.id.title).text =
                visitorsOverview.title
            visitorsOverviewView.findViewById<TextView>(R.id.title).paintFlags =
                Paint.UNDERLINE_TEXT_FLAG
            visitorsOverviewView.findViewById<TextView>(R.id.textView1).text =
                visitorsOverview.todayTitle
            visitorsOverviewView.findViewById<TextView>(R.id.textView2).text =
                visitorsOverview.yesterdayTitle
            visitorsOverviewView.findViewById<TextView>(R.id.textView3).text =
                visitorsOverview.allTitle
            visitorsOverviewView.findViewById<TextView>(R.id.textView4).text =
                visitorsOverview.onlineTitle

            visitorsOverviewView.findViewById<TextView>(R.id.textView11).text =
                visitorsOverview.today.toString()
            visitorsOverviewView.findViewById<TextView>(R.id.textView21).text =
                visitorsOverview.yesterday.toString()
            visitorsOverviewView.findViewById<TextView>(R.id.textView31).text =
                visitorsOverview.all.toString()
            visitorsOverviewView.findViewById<TextView>(R.id.textView31)
                .setTextColor(ContextCompat.getColor(this, R.color.colorAccent))
            visitorsOverviewView.findViewById<TextView>(R.id.textView41).text =
                visitorsOverview.online.toString()

            findViewById<LinearLayout>(R.id.main_linear).addView(visitorsOverviewView)


            val chartView = layoutInflater.inflate(R.layout.line_chart, null)
            chartView.findViewById<TextView>(R.id.title).text =
                visitorCounter.last30DaysTitle
            chartView.findViewById<TextView>(R.id.title).paintFlags =
                Paint.UNDERLINE_TEXT_FLAG

            val line = arrayListOf<Int>()
            for (i in visitorCounter.last30Days)
                line.add(0, i)
            val bottom = arrayListOf<String>();
            for (i in 1..line.size)
                bottom.add(i.toString())

            val lineView: LineView = chartView.findViewById(R.id.line_view)
            lineView.setDrawDotLine(true) //optional
            lineView.setShowPopup(LineView.SHOW_POPUPS_All) //optional
            lineView.setBottomTextList(bottom)
            lineView.setColorArray(
                intArrayOf(
                    ContextCompat.getColor(
                        this, R.color.colorAccent
                    )
                )
            )
            lineView.setDataList(arrayListOf(line))

            val barView = chartView.findViewById<BarView>(R.id.bar_view)
            barView.setBottomTextList(bottom)
            barView.setDataList(line, line.max() as Int + 1)
            barView.setBarColorEmptyPart(Color.TRANSPARENT)
            barView.setBarColorValuePart(ContextCompat.getColor(this, R.color.colorAccent))

            if (isLineChart()) {
                lineView.visibility = View.VISIBLE
                barView.visibility = View.GONE
            } else {
                lineView.visibility = View.GONE
                barView.visibility = View.VISIBLE
            }

            findViewById<LinearLayout>(R.id.main_linear).addView(chartView)


            val last10DaysView = layoutInflater.inflate(R.layout.last_10_days, null)
            last10DaysView.findViewById<TextView>(R.id.title).text =
                visitorCounter.last10VisitorsTitle
            last10DaysView.findViewById<TextView>(R.id.title).paintFlags =
                Paint.UNDERLINE_TEXT_FLAG
            df = SimpleDateFormat("dd.MM.yyyy - hh:mm", Locale.getDefault())
            for (i in visitorCounter.last10Visitors) {
                val date = layoutInflater.inflate(R.layout.date, null) as TextView
                date.text = df.format(i)
                last10DaysView.findViewById<LinearLayout>(R.id.last_10_days).addView(date)
            }

            findViewById<LinearLayout>(R.id.main_linear).addView(last10DaysView)

            val credits = TextView(this)
            credits.setText(R.string.chart_library_credit)
            credits.setPadding(8, 30, 8, 0)
            findViewById<LinearLayout>(R.id.main_linear).addView(credits)


        } else {
            findViewById<TextView>(R.id.domain_tV).text =
                getString(R.string.no_internet_connection)
        }
    }

    private fun setDailyAlarm() {
        val calendar = Calendar.getInstance()
        calendar[Calendar.HOUR_OF_DAY] = 12
        calendar[Calendar.MINUTE] = 0
        calendar[Calendar.SECOND] = 0
        calendar[Calendar.MILLISECOND] = 0
        val cur = Calendar.getInstance()
        if (cur.after(calendar)) {
            calendar.add(Calendar.DATE, 1)
        }
        val myIntent = Intent(this, BootReceiver::class.java)
        val ALARM1_ID = 10000
        val pendingIntent: PendingIntent = PendingIntent.getBroadcast(
            this, ALARM1_ID, myIntent, PendingIntent.FLAG_UPDATE_CURRENT
        )

        (getSystemService(Context.ALARM_SERVICE) as AlarmManager?)?.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_refresh -> {
                loadSite(getLastURL())
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onClick(p0: View?) {
        setLineChart(!isLineChart())
        generateUI(visitorCounter)
    }

    private fun getLastURL(): String {
        return PreferenceManager.getDefaultSharedPreferences(this).getString("last_url", "")!!
    }

    private fun setLastURL(value: String) {
        PreferenceManager.getDefaultSharedPreferences(this).edit().putString("last_url", value)
            .apply()
    }

    private fun isLineChart(): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(this).getBoolean("line_chart", true)
    }

    private fun setLineChart(value: Boolean) {
        PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("line_chart", value)
            .commit()
    }

    fun hideKeyboard(activity: Activity) {
        val imm: InputMethodManager =
            activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        var view: View? = activity.currentFocus
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}
