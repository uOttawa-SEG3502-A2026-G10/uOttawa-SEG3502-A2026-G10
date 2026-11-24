package seg3x02.carhms.contracts

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import seg3x02.carhms.application.dtos.queries.*
import seg3x02.carhms.domain.hikersorganizers.*
import seg3x02.carhms.domain.hikingevents.*
import seg3x02.carhms.domain.notifications.DeliveryStatus
import seg3x02.carhms.domain.registrations.RegistrationStatus
import seg3x02.carhms.domain.*
import seg3x02.carhms.domain.ClubTime.Companion.CLUB_ZONE
import seg3x02.carhms.domain.trails.OpeningSeason
import java.time.MonthDay
import java.util.Date
import java.util.concurrent.Callable
import java.util.concurrent.Executors

class DomainRulesTest {
    private fun prepared(capacity: Int = 2): Pair<ContractStubs, Hike> {
        val s = ContractStubs()
        s.addHiker(); s.login()
        return s to s.addHike(capacity = capacity)
    }

    @Test
    fun `invalid or reused invitation does not create a hiker`() {
        val s = ContractStubs()
        assertThrows(IllegalArgumentException::class.java) { s.register.execute(s.registrationQuery()) }
        s.invitations["VALID"] = Invitation("VALID", used = true)
        assertThrows(IllegalArgumentException::class.java) { s.register.execute(s.registrationQuery()) }
        assertTrue(s.hikers.isEmpty())
        assertTrue(s.events.isEmpty())
    }

    @Test
    fun `duplicate normalized email does not consume invitation`() {
        val s = ContractStubs()
        s.addHiker()
        s.invitations["VALID"] = Invitation("VALID")
        assertThrows(IllegalArgumentException::class.java) {
            s.register.execute(s.registrationQuery().copy(email = " HIKER@EXAMPLE.COM "))
        }
        assertFalse(s.invitations.getValue("VALID").used)
        assertTrue(s.events.isEmpty())
    }

    @Test
    fun `minor needs guardian acceptance and name`() {
        val s = ContractStubs()
        s.invitations["VALID"] = Invitation("VALID")
        val query = s.registrationQuery().copy(dateOfBirth = s.date(s.env.today().minusYears(17)))
        assertThrows(IllegalArgumentException::class.java) { s.register.execute(query) }
        assertFalse(s.invitations.getValue("VALID").used)
        s.register.execute(query.copy(waiverAccepted = false, guardianWaiverAccepted = true, guardianName = "Parent"))
        assertEquals("Parent", s.hikers.getValue(s.hikerEmail).waiver.guardianName)
    }

    @Test
    fun `eighteenth birthday permits personal waiver`() {
        val s = ContractStubs()
        s.invitations["VALID"] = Invitation("VALID")
        s.register.execute(s.registrationQuery().copy(dateOfBirth = s.date(s.env.today().minusYears(18))))
        assertNull(s.hikers.getValue(s.hikerEmail).waiver.guardianName)
    }

    @Test
    fun `future birth date and blank profile are rejected without consuming code`() {
        val s = ContractStubs()
        s.invitations["VALID"] = Invitation("VALID")
        assertThrows(IllegalArgumentException::class.java) {
            s.register.execute(s.registrationQuery().copy(dateOfBirth = s.date(s.env.today().plusDays(1))))
        }
        assertThrows(IllegalArgumentException::class.java) { s.register.execute(s.registrationQuery().copy(names = " ")) }
        assertFalse(s.invitations.getValue("VALID").used)
    }

    @Test
    fun `self update cannot change membership or certification`() {
        val (s, _) = prepared()
        assertThrows(IllegalArgumentException::class.java) {
            s.update.execute(UpdateHikerQuery(s.hikerEmail.value, membershipStatus = "suspended", names = "Not saved"))
        }
        assertEquals(MembershipStatus.ACTIVE, s.hikers.getValue(s.hikerEmail).membership)
        assertNotEquals("Not saved", s.hikers.getValue(s.hikerEmail).profile.names)
    }

    @Test
    fun `partial certification rejected and no-op emits no event`() {
        val s = ContractStubs()
        s.addHiker(); s.login(s.adminEmail, admin = true)
        assertThrows(IllegalArgumentException::class.java) {
            s.update.execute(UpdateHikerQuery(s.hikerEmail.value, certificateNumber = "PARTIAL"))
        }
        s.update.execute(UpdateHikerQuery(s.hikerEmail.value))
        assertTrue(s.events.isEmpty())
    }

    @Test
    fun `invalid membership string does not change profile`() {
        val s = ContractStubs()
        s.addHiker(); s.login(s.adminEmail, admin = true)
        assertThrows(IllegalArgumentException::class.java) {
            s.update.execute(UpdateHikerQuery(s.hikerEmail.value, names = "Not saved", membershipStatus = "wrong"))
        }
        assertNotEquals("Not saved", s.hikers.getValue(s.hikerEmail).profile.names)
    }

    @Test
    fun `expired certification cannot create hike`() {
        val s = ContractStubs()
        val organizer = s.addHiker(s.organizerEmail, organizer = true)
        organizer.update(organizer.profile, organizer.membership,
            Certification("OLD", s.env.today().minusYears(1), s.env.today(), Level.EXPERT), s.env.today())
        s.login(s.organizerEmail)
        assertThrows(IllegalArgumentException::class.java) { s.create.execute(s.createQuery()) }
        assertTrue(s.hikes.isEmpty())
    }

    @Test
    fun `certificate expiration date is inclusive`() {
        val s = ContractStubs()
        val organizer = s.addHiker(s.organizerEmail, organizer = true)
        organizer.update(organizer.profile, organizer.membership,
            Certification("VALID", s.env.today(), s.env.today().plusDays(7), Level.INTERMEDIATE), s.env.today())
        s.login(s.organizerEmail)
        s.create.execute(s.createQuery())
        assertEquals(1, s.hikes.size)
    }

    @Test
    fun `invalid capacity deadline or unavailable trail prevents creation`() {
        val s = ContractStubs()
        s.addHiker(s.organizerEmail, organizer = true); s.login(s.organizerEmail)
        val q = s.createQuery()
        assertThrows(IllegalArgumentException::class.java) { s.create.execute(q.copy(capacity = 0)) }
        assertThrows(IllegalArgumentException::class.java) {
            s.create.execute(q.copy(registrationDeadline = s.date(s.env.today().minusDays(30))))
        }
        assertThrows(IllegalArgumentException::class.java) {
            s.create.execute(q.copy(registrationDeadline = s.date(s.env.today().plusDays(8))))
        }
        s.trails.clear()
        assertThrows(IllegalArgumentException::class.java) { s.create.execute(q) }
        assertTrue(s.hikes.isEmpty())
    }

    @Test
    fun `unapproved organizer or insufficient organizer level cannot create`() {
        val s = ContractStubs()
        s.addHiker(s.organizerEmail); s.login(s.organizerEmail)
        assertThrows(IllegalArgumentException::class.java) { s.create.execute(s.createQuery()) }
        val hiker = s.hikers.getValue(s.organizerEmail)
        hiker.update(hiker.profile, hiker.membership,
            Certification("LOW", s.env.today(), s.env.today().plusYears(1), Level.BEGINNER), s.env.today())
        assertThrows(IllegalArgumentException::class.java) { s.create.execute(s.createQuery()) }
    }

    @Test
    fun `winter season wraps over year boundary and includes endpoints`() {
        val season = OpeningSeason(MonthDay.of(11, 1), MonthDay.of(3, 31))
        assertTrue(season.includes(java.time.LocalDate.of(2026, 11, 1)))
        assertTrue(season.includes(java.time.LocalDate.of(2027, 3, 31)))
        assertFalse(season.includes(java.time.LocalDate.of(2027, 7, 1)))
    }

    @Test
    fun `inactive or insufficiently skilled hiker cannot sign up`() {
        val (s, hike) = prepared()
        val hiker = s.hikers.getValue(s.hikerEmail)
        hiker.update(hiker.profile, MembershipStatus.SUSPENDED, null, s.env.today())
        assertThrows(IllegalArgumentException::class.java) {
            s.signUp.execute(SignUpForHikeQuery(s.hikerEmail.value, hike.id.value))
        }
        hiker.update(hiker.profile.copy(skill = Level.BEGINNER), MembershipStatus.ACTIVE, null, s.env.today())
        // Rollback replaces repository instances; persist the updated test fixture explicitly.
        s.hikers[s.hikerEmail] = hiker
        s.trails["trail-1"] = s.trails.getValue("trail-1").copy(difficulty = Level.EXPERT)
        assertThrows(IllegalArgumentException::class.java) {
            s.signUp.execute(SignUpForHikeQuery(s.hikerEmail.value, hike.id.value))
        }
        assertTrue(s.registrations.isEmpty())
    }

    @Test
    fun `higher skilled hikers can join easier hikes`() {
        assertTrue(Level.EXPERT.permits(Level.BEGINNER))
        assertTrue(Level.BEGINNER.permits(Level.INTERMEDIATE))
        assertFalse(Level.BEGINNER.permits(Level.ADVANCED))
    }

    @Test
    fun `duplicate registration rejected without extra mail or events`() {
        val (s, hike) = prepared()
        val query = SignUpForHikeQuery(s.hikerEmail.value, hike.id.value)
        s.signUp.execute(query)
        val eventCount = s.events.size
        assertThrows(IllegalArgumentException::class.java) { s.signUp.execute(query) }
        assertEquals(1, s.registrations.size)
        assertEquals(1, s.delivered.size)
        assertEquals(eventCount, s.events.size)
    }

    @Test
    fun `deadline blocks registration and cancellation without closure job`() {
        val (s, hike) = prepared()
        s.seedRegistration(hike, s.hikerEmail)
        s.now = hike.details.registrationDeadline
        assertThrows(IllegalArgumentException::class.java) {
            s.cancelSignUp.execute(CancelSignUpQuery(s.hikerEmail.value, hike.id.value))
        }
        s.addHiker(s.waiterEmail); s.login(s.waiterEmail)
        assertThrows(IllegalArgumentException::class.java) {
            s.signUp.execute(SignUpForHikeQuery(s.waiterEmail.value, hike.id.value))
        }
    }

    @Test
    fun `cancellation without waiter reopens hike and retains history`() {
        val (s, hike) = prepared(capacity = 1)
        s.seedRegistration(hike, s.hikerEmail)
        s.cancelSignUp.execute(CancelSignUpQuery(s.hikerEmail.value, hike.id.value))
        assertEquals(HikeStatus.OPEN, s.hikes.getValue(hike.id).status)
        assertEquals(listOf(HikeStatus.OPEN, HikeStatus.COMPLETE, HikeStatus.OPEN), s.hikes.getValue(hike.id).history.map { it.status })
        assertEquals(RegistrationStatus.CANCELLED, s.registrations.values.single().status)
        s.signUp.execute(SignUpForHikeQuery(s.hikerEmail.value, hike.id.value))
        assertEquals(2, s.registrations.size)
    }

    @Test
    fun `FIFO tie uses insertion order and cancelling waiter does not promote`() {
        val (s, hike) = prepared(capacity = 1)
        s.seedRegistration(hike, s.hikerEmail)
        s.seedRegistration(hike, s.waiterEmail)
        val third = Email.of("third@example.com")
        s.seedRegistration(hike, third)
        s.login(s.waiterEmail)
        s.cancelSignUp.execute(CancelSignUpQuery(s.waiterEmail.value, hike.id.value))
        assertEquals(RegistrationStatus.WAITLISTED, s.registrations.values.single { it.hikerEmail == third }.status)
        s.login()
        s.cancelSignUp.execute(CancelSignUpQuery(s.hikerEmail.value, hike.id.value))
        assertEquals(RegistrationStatus.CONFIRMED, s.registrations.values.single { it.hikerEmail == third }.status)
    }

    @Test
    fun `mail failure preserves registration and records failure event`() {
        val (s, hike) = prepared()
        s.emailFails = true
        val result = s.signUp.execute(SignUpForHikeQuery(s.hikerEmail.value, hike.id.value))
        assertEquals("confirmed", result.status)
        assertEquals(1, s.registrations.size)
        assertEquals(DeliveryStatus.FAILED, s.notifications.values.single().status)
        assertTrue(s.events.any { it.type == "NotificationFailed" })
    }

    @Test
    fun `closure is idempotent and cancelled hike is ignored`() {
        val (s, hike) = prepared()
        s.now = hike.details.registrationDeadline
        s.system()
        val query = CloseHikeQuery(hike.id.value, Date.from(s.now))
        s.close.execute(query); s.close.execute(query)
        assertEquals(1, s.events.count { it.type == "HikeStatusChanged" })
        val other = s.addHike(days = 2)
        other.cancel("admin", "Reason", s.now)
        s.now = other.details.registrationDeadline
        s.close.execute(CloseHikeQuery(other.id.value, Date.from(s.now)))
        assertEquals(HikeStatus.CANCELLED, other.status)
    }

    @Test
    fun `scheduled operations reject non-system callers and premature closure`() {
        val (s, hike) = prepared()
        assertThrows(IllegalArgumentException::class.java) {
            s.close.execute(CloseHikeQuery(hike.id.value, Date.from(s.now)))
        }
        assertThrows(IllegalArgumentException::class.java) {
            s.remind.execute(SendHikeReminderQuery(s.date(s.env.today().plusDays(1))))
        }
        s.system()
        assertThrows(IllegalArgumentException::class.java) {
            s.close.execute(CloseHikeQuery(hike.id.value, Date.from(s.now)))
        }
        assertEquals(HikeStatus.OPEN, s.hikes.getValue(hike.id).status)
    }

    @Test
    fun `reminders skip waitlisted and cancelled hikes and do not duplicate sent mail`() {
        val (s, hike) = prepared(capacity = 1)
        s.seedRegistration(hike, s.hikerEmail)
        s.seedRegistration(hike, s.waiterEmail)
        val cancelled = s.addHike()
        s.seedRegistration(cancelled, s.hikerEmail)
        cancelled.cancel("admin", "Reason", s.now)
        s.system()
        val query = SendHikeReminderQuery(s.date(s.env.today().plusDays(1)))
        s.remind.execute(query); s.remind.execute(query)
        assertEquals(1, s.delivered.size)
        assertEquals(s.hikerEmail, s.delivered.single().recipient)
        assertEquals(hike.id, s.delivered.single().hikeId)
    }

    @Test
    fun `failed reminder can be retried without a new notification`() {
        val (s, hike) = prepared()
        s.seedRegistration(hike, s.hikerEmail)
        s.system(); s.emailFails = true; s.weatherFails = true
        val query = SendHikeReminderQuery(s.date(s.env.today().plusDays(1)))
        s.remind.execute(query)
        assertEquals(DeliveryStatus.FAILED, s.notifications.values.single().status)
        s.emailFails = false
        s.remind.execute(query)
        assertEquals(1, s.notifications.size)
        assertEquals(DeliveryStatus.SENT, s.notifications.values.single().status)
        assertEquals(1, s.delivered.size)
    }

    @Test
    fun `unauthenticated or other hiker cannot impersonate actor`() {
        val (s, hike) = prepared()
        s.principal = null
        assertThrows(IllegalArgumentException::class.java) {
            s.signUp.execute(SignUpForHikeQuery(s.hikerEmail.value, hike.id.value))
        }
        s.login(s.waiterEmail)
        assertThrows(IllegalArgumentException::class.java) {
            s.update.execute(UpdateHikerQuery(s.hikerEmail.value, names = "Intruder"))
        }
        assertThrows(IllegalArgumentException::class.java) {
            s.cancel.execute(CancelHikeQuery(s.waiterEmail.value, hike.id.value, "Not allowed"))
        }
        assertEquals(HikeStatus.OPEN, s.hikes.getValue(hike.id).status)
    }

    @Test
    fun `admin cancellation is idempotent and notifies all active registrations`() {
        val (s, hike) = prepared(capacity = 1)
        s.seedRegistration(hike, s.hikerEmail); s.seedRegistration(hike, s.waiterEmail)
        s.login(s.adminEmail, admin = true)
        val query = CancelHikeQuery(s.adminEmail.value, hike.id.value, "Storm")
        s.cancel.execute(query); s.cancel.execute(query)
        assertEquals(2, s.delivered.size)
        assertTrue(s.registrations.values.all { it.status == RegistrationStatus.CANCELLED })
        assertEquals(s.adminEmail.value, s.hikes.getValue(hike.id).history.last().actor)
        assertEquals(1, s.events.count { it.type == "HikeStatusChanged" })
    }

    @Test
    fun `browse filters date and returns empty results without state changes`() {
        val s = ContractStubs()
        s.addHike(days = 1); s.addHike(days = 2)
        val result = s.browse.execute(BrowseHikesQuery(date = s.date(s.env.today().plusDays(2))))
        assertEquals(1, result.size)
        assertTrue(s.browse.execute(BrowseHikesQuery(trailName = "missing")).isEmpty())
        assertTrue(s.events.isEmpty())
    }

    @Test
    fun `browse enrichment highlights matching skill without changing existing DTO`() {
        val (s, hike) = prepared()
        val view = s.browse.view(BrowseHikesQuery())
        assertEquals(setOf(hike.id.value), view.recommendedHikeIds)
        assertEquals(view.hikes, s.browse.execute(BrowseHikesQuery()))
        s.principal = null
        assertTrue(s.browse.view(BrowseHikesQuery()).recommendedHikeIds.isEmpty())
    }

    @Test
    fun `finished is calculated on following local date and cancelled is never finished`() {
        val (s, hike) = prepared()
        assertFalse(hike.isFinished(hike.details.startsAt.plusSeconds(3600)))
        assertTrue(hike.isFinished(hike.date.plusDays(1).atStartOfDay(CLUB_ZONE).toInstant()))
        hike.cancel("admin", "Reason", s.now)
        assertFalse(hike.isFinished(hike.date.plusDays(1).atStartOfDay(CLUB_ZONE).toInstant()))
    }

    @Test
    fun `transaction failure rolls back aggregate changes and events`() {
        val (s, hike) = prepared()
        assertThrows(IllegalStateException::class.java) {
            s.transactions.run {
                s.env.registrations.signUp(hike.id, s.hikerEmail, 2, s.now)
                s.env.event("ShouldRollback", hike.id.value)
                throw IllegalStateException("Simulated persistence failure")
            }
        }
        assertTrue(s.registrations.isEmpty())
        assertTrue(s.events.isEmpty())
    }

    @Test
    fun `serialized concurrent signups never overbook last place`() {
        val (s, hike) = prepared(capacity = 1)
        s.addHiker(s.waiterEmail)
        s.login(s.adminEmail, admin = true)
        val executor = Executors.newFixedThreadPool(2)
        try {
            val results = executor.invokeAll(listOf(s.hikerEmail, s.waiterEmail).map { email ->
                Callable { s.signUp.execute(SignUpForHikeQuery(email.value, hike.id.value)) }
            }).map { it.get() }
            assertEquals(setOf("confirmed", "waitlisted"), results.map { it.status }.toSet())
            assertEquals(1, s.env.registrations.confirmed(hike.id).size)
        } finally {
            executor.shutdownNow()
        }
    }
}
