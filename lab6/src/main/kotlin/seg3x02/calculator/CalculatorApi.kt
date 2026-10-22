package seg3x02.calculator

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CalculatorApi

fun main(args: Array<String>) {
	runApplication<CalculatorApi>(*args)
}
