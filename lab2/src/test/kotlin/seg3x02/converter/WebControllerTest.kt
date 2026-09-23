package seg3x02.converter

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.model
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.view

@WebMvcTest
class WebControllerTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun request_to_home() {
        mockMvc.perform(get("/"))
            .andExpect(status().isOk)
            .andExpect(model().attribute("firstNum", "0"))
            .andExpect(model().attribute("secondNum", "0"))
            .andExpect(model().attribute("result", "0"))
            .andExpect(view().name("home"))
    }

    @Test
    fun adds_two_numbers() {
        calculate("9", "9", "add")
            .andExpect(model().attribute("result", "18"))
    }

    @Test
    fun subtracts_two_numbers() {
        calculate("9", "4", "subtract")
            .andExpect(model().attribute("result", "5"))
    }

    @Test
    fun multiplies_two_numbers() {
        calculate("9", "9", "multiply")
            .andExpect(model().attribute("result", "81"))
    }

    @Test
    fun divides_two_numbers() {
        calculate("9", "4", "divide")
            .andExpect(model().attribute("result", "2.25"))
    }

    @Test
    fun preserves_operands_after_calculation() {
        calculate("-2.5", "4", "multiply")
            .andExpect(model().attribute("firstNum", "-2.5"))
            .andExpect(model().attribute("secondNum", "4"))
            .andExpect(model().attribute("result", "-10"))
    }

    @Test
    fun rejects_invalid_numbers() {
        calculate("abc", "4", "add")
            .andExpect(model().attribute("error", "NumberFormatError"))
            .andExpect(model().attribute("result", "0"))
    }

    @Test
    fun rejects_division_by_zero() {
        calculate("9", "0", "divide")
            .andExpect(model().attribute("error", "DivisionByZeroError"))
            .andExpect(model().attribute("result", "0"))
    }

    @Test
    fun rejects_unknown_operations() {
        calculate("9", "4", "power")
            .andExpect(model().attribute("error", "OperationFormatError"))
            .andExpect(model().attribute("result", "0"))
    }

    private fun calculate(firstNum: String, secondNum: String, operation: String) =
        mockMvc.perform(
            get("/calculate")
                .param("firstNum", firstNum)
                .param("secondNum", secondNum)
                .param("operation", operation)
        )
            .andExpect(status().isOk)
            .andExpect(view().name("home"))
}
