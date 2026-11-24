package seg3x02.carhms.application.services

import java.time.LocalDate

interface WeatherService {
    fun forecast(departurePoint: String, date: LocalDate): Forecast?
}
