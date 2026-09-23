package seg3x02.converter

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.RequestParam

@Controller
class WebController {
    @ModelAttribute
    fun addAttributes(model: Model) {
        model.addAttribute("error", "")
        model.addAttribute("firstNum", "0")
        model.addAttribute("secondNum", "0")
        model.addAttribute("result", "0")
    }

    @GetMapping("/")
    fun home() = "home"

    @GetMapping("/calculate")
    fun calculate(
        @RequestParam(defaultValue = "") firstNum: String,
        @RequestParam(defaultValue = "") secondNum: String,
        @RequestParam(defaultValue = "") operation: String,
        model: Model
    ): String {
        model.addAttribute("firstNum", firstNum)
        model.addAttribute("secondNum", secondNum)

        val firstValue = firstNum.toDoubleOrNull()
        val secondValue = secondNum.toDoubleOrNull()
        if (firstValue == null || secondValue == null) {
            model.addAttribute("error", "NumberFormatError")
            return "home"
        }

        val result = when (operation) {
            "add" -> firstValue + secondValue
            "subtract" -> firstValue - secondValue
            "multiply" -> firstValue * secondValue
            "divide" -> {
                if (secondValue == 0.0) {
                    model.addAttribute("error", "DivisionByZeroError")
                    return "home"
                }
                firstValue / secondValue
            }
            else -> {
                model.addAttribute("error", "OperationFormatError")
                return "home"
            }
        }

        model.addAttribute("result", formatResult(result))
        return "home"
    }

    private fun formatResult(value: Double): String {
        if (value == 0.0) return "0"
        val integerValue = value.toLong()
        return if (value == integerValue.toDouble()) integerValue.toString() else value.toString()
    }
}
