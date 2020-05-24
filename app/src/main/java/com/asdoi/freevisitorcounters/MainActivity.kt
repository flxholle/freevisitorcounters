package com.asdoi.freevisitorcounters

import android.app.Activity
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
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.android.material.textfield.TextInputEditText
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

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
    }

    private fun loadSite(url: String) {
        Thread {
            val visitorCounter =
                Parser.parseWebsite(url)
            runOnUiThread {
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
                    visitorsOverviewView.findViewById<TextView>(R.id.textView41).text =
                        visitorsOverview.online.toString()

                    findViewById<LinearLayout>(R.id.main_linear).addView(visitorsOverviewView)


                    val chartView = layoutInflater.inflate(R.layout.line_chart, null)
                    chartView.findViewById<TextView>(R.id.title).text =
                        visitorCounter.last30DaysTitle
                    chartView.findViewById<TextView>(R.id.title).paintFlags =
                        Paint.UNDERLINE_TEXT_FLAG
                    val chart = chartView.findViewById<LineChart>(R.id.chart1)
                    chart.isDragEnabled = true
                    chart.setScaleEnabled(true)
                    chart.isDoubleTapToZoomEnabled = true
                    chart.setPinchZoom(false)
                    chart.setNoDataText(getString(R.string.no_visitors_for_the_last_30_days))
                    chart.description.isEnabled = false

                    chart.data = getData(visitorCounter.last30Days)
                    chart.legend.isEnabled = false
                    chart.axisLeft.isEnabled = true
                    chart.axisRight.isEnabled = false
                    chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
                    chart.animateX(1000)

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

                } else {
                    findViewById<TextView>(R.id.domain_tV).text =
                        getString(R.string.no_internet_connection)
                }
            }
        }.start()
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

    private fun getData(visitors: List<Int>): LineData {
        val values: ArrayList<Entry> = ArrayList()
        for (i in visitors.indices) {
            values.add(Entry(i.toFloat(), visitors[i].toFloat()))
        }

        val dataSet = LineDataSet(values, "DataSet 1")
        dataSet.lineWidth = 3f
        dataSet.circleRadius = 4f
        dataSet.color = ContextCompat.getColor(this, R.color.colorPrimary)
        dataSet.setCircleColor(ContextCompat.getColor(this, R.color.colorAccent))

        // create a data object with the data sets
        return LineData(dataSet)
    }

    private fun getLastURL(): String {
        return PreferenceManager.getDefaultSharedPreferences(this).getString("last_url", "")!!
    }

    private fun setLastURL(value: String) {
        PreferenceManager.getDefaultSharedPreferences(this).edit().putString("last_url", value)
            .apply()
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
