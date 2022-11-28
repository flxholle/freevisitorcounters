package com.asdoi.freevisitorcounters

import org.jsoup.Jsoup
import java.text.SimpleDateFormat
import java.util.*

class VisitorCounter(
    var overview: Overview,
    var visitorsOverview: VisitorsOverview,
    var last10VisitorsTitle: String,
    var last10Visitors: List<Date>,
    var last30DaysTitle: String,
    var last30Days: List<Int>,
    var domain: String
)

class Overview(
    var title: String = "",
    var since: Date = Date(),
    var sinceTitle: String = "",
    var prognosis: String = "",
    var prognosisTitle: String = "",
    var bestDay: Date = Date(),
    var bestDayTitle: String = "",
    var bestDayVisitors: Int = 0,
    var bestDayVisitorsTitle: String = ""
)

class VisitorsOverview(
    var title: String = "",
    var today: Int = 0,
    var todayTitle: String = "",
    var yesterday: Int = 0,
    var yesterdayTitle: String = "",
    var all: Int = 0,
    var allTitle: String = "",
    var online: Int = 0,
    var onlineTitle: String = ""
)

class Parser {
    companion object {
        fun parseWebsite(url: Int): VisitorCounter? {
            return parseWebsite(Constants.getStatsUrl(url))
        }

        fun parseWebsite(url: String): VisitorCounter? {
            try {
                val doc = Jsoup.connect(url).get() ?: return null
                val mid = doc.select("div#content").select(".row")

                val overview = Overview()
                overview.title = mid[1].select(".table").select("tbody")[0].select("tr").text()
                var overviewRows = mid[1].select(".table").select("tbody")[1].select("tr")
                var dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                overview.since = dateFormat.parse(overviewRows[0].select("td")[1].text())!!
                overview.sinceTitle = overviewRows[0].select("td")[0].text()
                overview.prognosis = overviewRows[1].select("td")[1].text()
                overview.prognosisTitle = overviewRows[1].select("td")[0].text()
                overview.bestDay = dateFormat.parse(overviewRows[2].select("td")[1].text())!!
                overview.bestDayTitle = overviewRows[2].select("td")[0].text()
                overview.bestDayVisitors = Integer.parseInt(overviewRows[3].select("td")[1].text())
                overview.bestDayVisitorsTitle = overviewRows[3].select("td")[0].text()

                val visitorsOverview = VisitorsOverview()
                visitorsOverview.title =
                    mid[2].select(".table").select("tbody")[0].select("tr").text()
                overviewRows = mid[2].select(".table").select("tbody")[1].select("tr")
                visitorsOverview.today = Integer.parseInt(overviewRows[0].select("td")[1].text())
                visitorsOverview.todayTitle = overviewRows[0].select("td")[0].text()
                visitorsOverview.yesterday =
                    Integer.parseInt(overviewRows[1].select("td")[1].text())
                visitorsOverview.yesterdayTitle =
                    overviewRows[1].select("td")[0].text()
                visitorsOverview.all = Integer.parseInt(overviewRows[2].select("td")[1].text())
                visitorsOverview.allTitle = overviewRows[2].select("td")[0].text()
                visitorsOverview.online = Integer.parseInt(overviewRows[3].select("td")[1].text())
                visitorsOverview.onlineTitle = overviewRows[3].select("td")[0].text()

                val last10Visitors = mutableListOf<Date>()
                val last10Title = mid[3].select(".table").select("tbody")[0].select("tr").text()
                val last10VisitorsElements = mid[3].select(".table").select("tbody")[1].select("tr")
                dateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm", Locale.getDefault())
                for (element in last10VisitorsElements) {
                    last10Visitors.add(0, dateFormat.parse(element.select("td")[1].text())!!)
                }

                val last30Days = mutableListOf<Int>()
                val last30Title = mid[5].select(".table").select("tbody")[0].select("tr").text()
                val last30DaysElements = mid[5].select(".table").select("tbody")[1].select("tr")
                for (element in last30DaysElements) {
                    last30Days.add(Integer.parseInt(element.select("td")[1].text()))
                }

                val domain = doc.select("div#page-heading").select("h2").select("i").text()

                return VisitorCounter(
                    overview,
                    visitorsOverview,
                    last10Title,
                    last10Visitors,
                    last30Title,
                    last30Days,
                    domain
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }
    }
}

object Constants {
    fun getStatsUrl(id: Int): String {
        return "https://www.freevisitorcounters.com/${Locale.getDefault().language}/home/stats/id/$id"
    }
}
