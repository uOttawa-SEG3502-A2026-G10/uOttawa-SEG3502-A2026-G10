package seg3x02.carhms.contracts

import io.cucumber.java.en.Given
import io.cucumber.java.en.When
import io.cucumber.java.en.Then
import org.junit.jupiter.api.Assertions.*
import seg3x02.carhms.application.dtos.queries.*
import seg3x02.carhms.application.services.Forecast
import seg3x02.carhms.domain.hikersorganizers.*
import seg3x02.carhms.domain.hikingevents.*
import seg3x02.carhms.domain.notifications.DeliveryStatus
import seg3x02.carhms.domain.registrations.*
import seg3x02.carhms.domain.*
import seg3x02.carhms.domain.trails.OpeningSeason
import java.time.MonthDay
import java.util.Date

class ContractSteps {
    private val s = ContractStubs()
    private var hike: Hike? = null
    private var error: Exception? = null
    private var result: SignUpResultDto? = null
    private var browsed = emptyList<HikeSummaryDto>()
    private var registrationQuery = s.registrationQuery()
    private var createQuery = s.createQuery()
    private var browseQuery = BrowseHikesQuery()
    private fun currentHike() = requireNotNull(hike)
    private fun attempt(action: () -> Unit) {
        try { action() } catch (failure: Exception) { error = failure }
    }
    private fun success() { assertNull(error, error?.message) }

    @Given("a valid unused invitation code")
    fun validInvitation() { s.invitations["VALID"] = Invitation("VALID") }

    @Given("an invalid invitation code")
    fun invalidInvitation() { registrationQuery = registrationQuery.copy(invitationCode = "INVALID") }

    @Given("the submitted email is not already registered")
    fun uniqueEmail() { assertFalse(s.hikers.containsKey(s.hikerEmail)) }

    @Given("the waiver requirements are satisfied")
    fun waiver() { registrationQuery = registrationQuery.copy(waiverAccepted = true) }

    @Given("an authenticated hiker")
    @Given("an authenticated hiker with active membership")
    @Given("an authenticated eligible hiker with active membership")
    fun hiker() { s.addHiker(); s.login() }

    @Given("an authenticated administrator")
    fun administrator() { s.login(s.adminEmail, admin = true) }

    @Given("an existing hiker")
    fun existingHiker() { s.addHiker() }

    @Given("an authenticated hiking organizer")
    fun organizer() { s.addHiker(s.organizerEmail, organizer = true); s.login(s.organizerEmail) }

    @Given("the organizer certification is valid on the planned date")
    fun validCertification() {
        assertTrue(requireNotNull(s.hikers[s.organizerEmail]?.certification).validOn(s.env.localDate(createQuery.date)))
    }

    @Given("the trail permits hiking during its opening season")
    fun openSeason() { s.trails.getValue("trail-1").requireAvailable(s.env.localDate(createQuery.date)) }

    @Given("the organizer level is sufficient for the trail difficulty")
    fun sufficientLevel() {
        assertTrue(requireNotNull(s.hikers[s.organizerEmail]?.certification).level.canLead(s.trails.getValue("trail-1").difficulty))
    }

    @Given("the trail is not open on the planned date")
    fun closedSeason() {
        s.trails["trail-1"] = s.trails.getValue("trail-1").copy(
            season = OpeningSeason(MonthDay.of(6, 1), MonthDay.of(8, 31)))
    }

    @Given("an open hike has one remaining place")
    fun onePlace() { hike = s.addHike(capacity = 1) }

    @Given("the hiker is eligible for the hike difficulty")
    fun eligible() { s.hikers.getValue(s.hikerEmail).requireEligible(s.trails.getValue("trail-1").difficulty) }

    @Given("the selected hike is complete")
    fun complete() {
        hike = s.addHike(capacity = 1)
        s.seedRegistration(currentHike(), s.waiterEmail)
    }

    @Given("a hike has a confirmed registration and a non-empty waitlist")
    fun waitlist() {
        hike = s.addHike(capacity = 1)
        s.seedRegistration(currentHike(), s.hikerEmail)
        s.seedRegistration(currentHike(), s.waiterEmail, s.now.plusSeconds(1))
        s.login()
    }

    @Given("the registration deadline has not passed")
    fun beforeDeadline() { assertTrue(s.now < currentHike().details.registrationDeadline) }

    @Given("the hike is closed")
    fun closed() {
        hike = s.addHike(capacity = 1)
        s.seedRegistration(currentHike(), s.hikerEmail)
        s.now = currentHike().details.registrationDeadline
        currentHike().close(s.now)
        s.login()
    }

    @Given("a non-cancelled hike whose registration deadline has been reached")
    fun deadline() {
        hike = s.addHike()
        s.now = currentHike().details.registrationDeadline
        s.system()
    }

    @Given("a non-cancelled hike is scheduled for tomorrow")
    fun tomorrow() {
        hike = s.addHike()
        // Also seed confirmed data for the existing weather-unavailable scenario,
        // whose Given list does not explicitly mention registrations.
        s.seedRegistration(currentHike(), s.hikerEmail)
        s.system()
    }

    @Given("confirmed registrations exist")
    fun confirmedExist() { assertFalse(s.env.registrations.confirmed(currentHike().id).isEmpty()) }

    @Given("the weather service forecasts rain or snow")
    fun rain() { s.weatherResult = Forecast(rain = true) }

    @Given("the weather service is unavailable")
    fun weatherUnavailable() { s.weatherFails = true }

    @Given("an authenticated authorized organizer or administrator")
    fun authorizedOrganizer() { organizer() }

    @Given("the hike has not started")
    fun notStarted() {
        hike = s.addHike(capacity = 1)
        s.seedRegistration(currentHike(), s.hikerEmail)
        s.seedRegistration(currentHike(), s.waiterEmail)
    }

    @Given("the hike has already started")
    fun started() {
        organizer()
        hike = s.addHike()
        s.now = currentHike().details.startsAt
    }

    @Given("open hikes exist in the system")
    fun openHikes() {
        s.addHike(days = 3)
        s.addHike(days = 1)
        val full = s.addHike(capacity = 1)
        s.seedRegistration(full, s.waiterEmail)
        val cancelled = s.addHike()
        cancelled.cancel(s.organizerEmail.value, "Reason", s.now)
        val closed = s.addHike()
        closed.close(closed.details.registrationDeadline)
    }

    @Given("open hikes exist for different trails and difficulty levels")
    fun differentTrails() {
        openHikes()
        s.trails["trail-2"] = s.trails.getValue("trail-1").copy(id = "trail-2", name = "Other trail", difficulty = Level.EXPERT)
        s.addHike(trailId = "trail-2")
    }

    @When("the person submits valid hiker information")
    @When("a person submits the registration form")
    fun register() = attempt { s.register.execute(registrationQuery) }

    @When("the hiker submits valid updated personal information")
    fun updateProfile() = attempt { s.update.execute(UpdateHikerQuery(s.hikerEmail.value, names = "Updated Name")) }

    @When("the administrator records a valid certification and organizer level")
    fun certify() = attempt {
        s.update.execute(UpdateHikerQuery(s.hikerEmail.value, certificateNumber = "CERT-2",
            certificateIssueDate = s.date(s.env.today()), certificateExpiryDate = s.date(s.env.today().plusYears(1)),
            organizerLevel = "expert"))
    }

    @When("the organizer submits valid hike information")
    @When("the organizer attempts to create the hike")
    fun create() = attempt { s.create.execute(createQuery) }

    @When("the hiker confirms the sign-up")
    fun signUp() = attempt { result = s.signUp.execute(SignUpForHikeQuery(s.hikerEmail.value, currentHike().id.value)) }

    @When("the hiker confirms cancellation")
    @When("the hiker requests cancellation")
    fun cancelRegistration() = attempt { s.cancelSignUp.execute(CancelSignUpQuery(s.hikerEmail.value, currentHike().id.value)) }

    @When("the scheduled closure process runs")
    fun close() = attempt { s.close.execute(CloseHikeQuery(currentHike().id.value, Date.from(s.now))) }

    @When("the reminder process runs")
    fun remind() = attempt { s.remind.execute(SendHikeReminderQuery(s.date(s.env.today().plusDays(1)))) }

    @When("the user submits a cancellation reason and confirms")
    @When("an organizer requests cancellation")
    fun cancelHike() = attempt { s.cancel.execute(CancelHikeQuery(s.organizerEmail.value, currentHike().id.value, "Unsafe conditions")) }

    @When("a visitor browses all open hikes sorted by date")
    fun browse() = attempt { browsed = s.browse.execute(browseQuery) }

    @When("a visitor searches by trail name and difficulty")
    fun filter() = attempt {
        browseQuery = BrowseHikesQuery(trailName = "Pink", difficulty = "moderate")
        browsed = s.browse.execute(browseQuery)
    }

    @Then("a hiker account is created")
    fun account() {
        success()
        assertNotNull(s.hikers[s.hikerEmail])
        assertNotEquals(registrationQuery.password, s.hikers.getValue(s.hikerEmail).passwordHash)
        assertTrue(s.invitations.getValue("VALID").used)
        assertTrue(s.events.any { it.type == "HikerRegistered" })
    }

    @Then("the membership status is {string}")
    fun membership(status: String) { success(); assertEquals(status, s.hikers.getValue(s.hikerEmail).membership.name.lowercase()) }

    @Then("no hiker account is created")
    fun noAccount() { assertTrue(s.hikers.isEmpty()) }

    @Then("a validation error is returned")
    fun validationError() { assertInstanceOf(IllegalArgumentException::class.java, error) }

    @Then("the hiker profile is updated")
    fun updated() { success(); assertEquals("Updated Name", s.hikers.getValue(s.hikerEmail).profile.names) }

    @Then("the hiker is recognized as a hiking organizer")
    fun recognizedOrganizer() {
        success()
        s.hikers.getValue(s.hikerEmail).requireOrganizer(s.env.today(), Level.EXPERT)
        assertTrue(s.events.any { it.type == "OrganizerApproved" })
    }

    @Then("the hike is saved with a unique identifier")
    fun savedHike() {
        success()
        assertEquals(1, s.hikes.size)
        hike = s.hikes.values.single()
        assertTrue(currentHike().id.value.isNotBlank())
    }

    @Then("the hike status is {string}")
    @Then("the hike status becomes {string}")
    fun hikeStatus(status: String) { success(); assertEquals(status, currentHike().status.name.lowercase()) }

    @Then("the creator is recorded in the lifecycle history")
    fun creatorHistory() {
        success()
        assertEquals(s.organizerEmail.value, currentHike().history.first().actor)
        assertTrue(s.events.any { it.type == "HikeCreated" })
    }

    @Then("the hike is not created")
    fun notCreated() { assertTrue(s.hikes.isEmpty()) }

    @Then("a confirmed registration is recorded")
    fun confirmedRegistration() { success(); assertEquals("confirmed", requireNotNull(result).status) }

    @Then("the registration timestamp is recorded")
    fun registrationTimestamp() { success(); assertEquals(s.now, s.registrations.values.single().registeredAt) }

    @Then("the hike becomes complete")
    fun becomesComplete() { hikeStatus("complete"); assertTrue(s.events.any { it.type == "HikeStatusChanged" }) }

    @Then("the registration status is {string}")
    @Then("the registration status becomes {string}")
    fun registrationStatus(status: String) {
        success()
        val registration = s.registrations.values.last { it.hikerEmail == s.hikerEmail }
        assertEquals(status, registration.status.name.lowercase())
    }

    @Then("the hiker receives their waitlist position")
    fun position() {
        success()
        assertEquals(1, requireNotNull(result).waitlistPosition)
        assertTrue(s.delivered.any { it.recipient == s.hikerEmail && it.body.contains("position 1") })
    }

    @Then("the first waitlisted hiker is promoted to confirmed")
    fun promoted() {
        success()
        assertEquals(RegistrationStatus.CONFIRMED, s.registrations.values.single { it.hikerEmail == s.waiterEmail }.status)
        assertTrue(s.events.any { it.type == "RegistrationPromoted" })
    }

    @Then("the promoted hiker is notified")
    fun promotionMail() { success(); assertTrue(s.delivered.any { it.recipient == s.waiterEmail && it.kind.startsWith("promotion-") }) }

    @Then("the cancellation is rejected")
    fun rejected() { assertNotNull(error); assertInstanceOf(IllegalArgumentException::class.java, error) }

    @Then("the lifecycle actor and timestamp are recorded")
    fun lifecycle() {
        success()
        assertEquals("SYSTEM", currentHike().history.last().actor)
        assertEquals(s.now, currentHike().history.last().at)
        assertTrue(s.events.any { it.type == "HikeStatusChanged" && it.actor == "SYSTEM" })
    }

    @Then("new registrations and cancellations are rejected")
    fun noFurtherRegistration() {
        success()
        s.addHiker(); s.login()
        assertThrows(IllegalArgumentException::class.java) {
            s.signUp.execute(SignUpForHikeQuery(s.hikerEmail.value, currentHike().id.value))
        }
        assertThrows(IllegalArgumentException::class.java) {
            s.cancelSignUp.execute(CancelSignUpQuery(s.hikerEmail.value, currentHike().id.value))
        }
    }

    @Then("each confirmed hiker is sent a reminder")
    fun reminder() {
        success()
        val recipients = s.env.registrations.confirmed(currentHike().id).map { it.hikerEmail }.toSet()
        assertFalse(recipients.isEmpty())
        assertEquals(recipients, s.delivered.filter { it.kind == "reminder" }.map { it.recipient }.toSet())
        assertTrue(s.notifications.values.all { it.status == DeliveryStatus.SENT })
        s.delivered.forEach {
            assertTrue(it.body.contains("Pink Lake") && it.body.contains("P2") && it.body.contains(currentHike().date.toString()))
        }
    }

    @Then("the reminder includes a weather warning")
    fun warning() { success(); assertTrue(s.delivered.all { it.body.contains("Avertissement météo") }) }

    @Then("each confirmed hiker is sent a reminder without a weather warning")
    fun noWarning() { reminder(); assertTrue(s.delivered.none { it.body.contains("Avertissement météo") }) }

    @Then("confirmed and waitlisted registrations become {string}")
    fun cancelledRegistrations(status: String) {
        success()
        assertEquals(2, s.registrations.size)
        assertTrue(s.registrations.values.all { it.status.name.lowercase() == status })
    }

    @Then("affected hikers are notified with the reason")
    fun cancellationMail() {
        success()
        assertEquals(setOf(s.hikerEmail, s.waiterEmail), s.delivered.map { it.recipient }.toSet())
        assertTrue(s.delivered.all { it.body.contains("Unsafe conditions") })
    }

    @Then("the system displays only open hikes in date order")
    fun sorted() {
        success()
        assertEquals(2, browsed.size)
        assertEquals(browsed.map { it.date }.sorted(), browsed.map { it.date })
        assertTrue(browsed.all { s.hikes.getValue(HikeId(it.id)).status == HikeStatus.OPEN })
    }

    @Then("each result includes hike details, organizer, and remaining places")
    fun summaries() {
        success()
        assertTrue(browsed.all { it.title.isNotBlank() && it.trailName.isNotBlank() && it.organizerName.isNotBlank() && it.remainingPlaces == 2 })
    }

    @Then("only matching open hikes are displayed")
    fun matching() {
        success()
        assertEquals(2, browsed.size)
        assertTrue(browsed.all { it.trailName == "Pink Lake" && it.difficulty == "moderate" })
    }
}
