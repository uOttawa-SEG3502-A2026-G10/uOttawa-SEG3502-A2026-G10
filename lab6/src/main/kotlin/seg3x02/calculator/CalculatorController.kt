package seg3x02.calculator

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("calculator")
class ConverterController {
    @GetMapping("/add/{nombre1}/{nombre2}")
    fun addNumbers(@PathVariable nombre1: Double, @PathVariable nombre2: Double) = nombre1 + nombre2

    @GetMapping("/subtract/{nombre1}/{nombre2}")
    fun subNumbers(@PathVariable nombre1: Double, @PathVariable nombre2: Double) = nombre1 - nombre2

    @GetMapping("/multiply/{nombre1}/{nombre2}")
    fun mulNumbers(@PathVariable nombre1: Double, @PathVariable nombre2: Double) = nombre1 * nombre2

    @GetMapping("/divide/{nombre1}/{nombre2}")
    fun divNumbers(@PathVariable nombre1: Double, @PathVariable nombre2: Double) = nombre1 / nombre2
}