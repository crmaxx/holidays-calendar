package ru.d10xa.holidayconsultant

import groovy.json.JsonOutput
import org.openqa.selenium.WebDriver
import org.openqa.selenium.phantomjs.PhantomJSDriver
import geb.Browser

import java.time.LocalDate

class Main {

    final static def MONTHS = [
        "Январь",
        "Февраль",
        "Март",
        "Апрель",
        "Май",
        "Июнь",
        "Июль",
        "Август",
        "Сентябрь",
        "Октябрь",
        "Ноябрь",
        "Декабрь"
    ]

    static void main(String[] args) {
        int year = Integer.valueOf(args[-3])
        def url = args[-2]
        def outputJson = args[-1]

        println "year: $year"
        println "url: $url"
        println "output json: $outputJson"
        def outputFile = new File(outputJson)

        assert outputFile.parentFile.isDirectory()

        WebDriver driver = new PhantomJSDriver()
        def browser = new Browser(driver: driver)

        browser.go url

        def table = browser.$("table.calendar-table")

        browser.waitFor {table.$("td.weekend").size() > 100}

        def holidays = table.$("td.weekend").collect {
            def mStr = it.parent().parent().previous().$(".month").text()
            def m = Integer.valueOf(MONTHS.indexOf(mStr)) + 1
            def d = Integer.valueOf(it.text())
            LocalDate.of(year, m, d)
        }

        def preholidays = table.$("td.preholiday").collect {
            def d = Integer.valueOf(it.text().replace("*",""))
            def mStr = it.parent().parent().previous().$(".month").text()
            def m = Integer.valueOf(MONTHS.indexOf(mStr)) + 1
            LocalDate.of(year, m, d)
        }

        browser.quit()

        println("holidays:")
        println("${holidays.collect{it.toString()}.join('\n')}")
        println("preholidays:")
        println("${preholidays.collect{it.toString()}.join('\n')}")

        String json = JsonOutput.toJson([
            "holidays": holidays.collect{it.toString()},
            "preholidays": preholidays.collect{it.toString()}
        ])

        outputFile.text = JsonOutput.prettyPrint(json)
    }
}