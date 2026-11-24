package seg3x02.carhms.contracts

import seg3x02.carhms.application.dtos.queries.*
import seg3x02.carhms.application.services.*
import seg3x02.carhms.application.usecases.implementation.*
import seg3x02.carhms.domain.authentication.*
import seg3x02.carhms.domain.hikersorganizers.*
import seg3x02.carhms.domain.hikingevents.*
import seg3x02.carhms.domain.notifications.*
import seg3x02.carhms.domain.registrations.*
import seg3x02.carhms.domain.*
import seg3x02.carhms.domain.ClubTime.Companion.CLUB_ZONE
import seg3x02.carhms.domain.trails.*
import java.security.MessageDigest
import java.time.*
import java.util.Date

class ContractStubs {
    var now: Instant = Instant.parse("2026-11-23T15:00:00Z")
    val clock: Clock = object : Clock() {
        override fun getZone(): ZoneId = CLUB_ZONE
        override fun withZone(zone: ZoneId): Clock = Clock.fixed(now, zone)
        override fun instant(): Instant = now
    }
    var principal: Principal? = null
    val hikers = linkedMapOf<Email, Hiker>()
    val invitations = linkedMapOf<String, Invitation>()
    val hikes = linkedMapOf<HikeId, Hike>()
    val trails = linkedMapOf<String, Trail>()
    val registrations = linkedMapOf<String, Registration>()
    val notifications = linkedMapOf<String, Notification>()
    val events = mutableListOf<DomainEvent>()
    val delivered = mutableListOf<Notification>()
    var weatherResult: Forecast? = Forecast()
    var weatherFails = false
    var emailFails = false
    private var sequence = 0
    private fun id(prefix: String) = "$prefix-${++sequence}"

    val hikerRepository = object : HikerRepository {
        override fun find(email: Email) = hikers[email]
        override fun save(hiker: Hiker) { hikers[hiker.email] = hiker }
    }
    val invitationRepository = object : InvitationRepository {
        override fun find(code: String) = invitations[code]
        override fun save(invitation: Invitation) { invitations[invitation.code] = invitation }
    }
    val hikeRepository = object : HikeRepository {
        override fun find(id: HikeId) = hikes[id]
        override fun all() = hikes.values.toList()
        override fun save(hike: Hike) { hikes[hike.id] = hike }
    }
    val trailRepository = object : TrailRepository {
        override fun find(id: String) = trails[id]
    }
    val registrationRepository = object : RegistrationRepository {
        override fun forHike(hikeId: HikeId) = registrations.values.filter { it.hikeId == hikeId }
        override fun save(registration: Registration) { registrations[registration.id] = registration }
    }
    val notificationRepository = object : NotificationRepository {
        override fun save(notification: Notification) { notifications[notification.id] = notification }
        override fun find(hikeId: HikeId, recipient: Email, kind: String) =
            notifications.values.lastOrNull { it.hikeId == hikeId && it.recipient == recipient && it.kind == kind }
    }
    val hikerFactory = object : HikerFactory {
        override fun create(email: Email, passwordHash: String, profile: HikerProfile, waiver: Waiver) =
            Hiker(email, passwordHash, profile, waiver)
    }
    val hikeFactory = object : HikeFactory {
        override fun create(organizer: Email, details: HikeDetails, at: Instant) = Hike(HikeId(id("hike")), organizer, details, at)
    }
    val registrationFactory = object : RegistrationFactory {
        override fun create(hikeId: HikeId, hiker: Email, at: Instant, status: RegistrationStatus) =
            Registration(id("registration"), hikeId, hiker, at, status)
    }
    val notificationFactory = object : NotificationFactory {
        override fun create(hikeId: HikeId, recipient: Email, kind: String, subject: String, body: String, at: Instant) =
            Notification(id("notification"), hikeId, recipient, kind, subject, body, at)
    }

    val transactions = object : TransactionRunner {
        override fun <T> run(action: () -> T): T = synchronized(this@ContractStubs) {
            val savedHikers = hikers.mapValues { (_, h) ->
                Hiker(h.email, h.passwordHash, h.profile, h.waiver, h.membership, h.certification)
            }
            val savedInvitations = invitations.mapValues { (_, i) -> Invitation(i.code, i.used) }
            val savedHikes = hikes.mapValues { (_, h) -> copyHike(h) }
            val savedRegistrations = registrations.mapValues { (_, r) ->
                Registration(r.id, r.hikeId, r.hikerEmail, r.registeredAt, r.status)
            }
            val savedNotifications = notifications.mapValues { (_, n) ->
                Notification(n.id, n.hikeId, n.recipient, n.kind, n.subject, n.body, n.createdAt).also {
                    when (n.status) {
                        DeliveryStatus.SENT -> it.sent()
                        DeliveryStatus.FAILED -> it.failed(requireNotNull(n.failure))
                        else -> Unit
                    }
                }
            }
            val eventCount = events.size
            val savedSequence = sequence
            try {
                action()
            } catch (failure: Throwable) {
                hikers.clear(); hikers.putAll(savedHikers)
                invitations.clear(); invitations.putAll(savedInvitations)
                hikes.clear(); hikes.putAll(savedHikes)
                registrations.clear(); registrations.putAll(savedRegistrations)
                notifications.clear(); notifications.putAll(savedNotifications)
                while (events.size > eventCount) events.removeAt(events.lastIndex)
                sequence = savedSequence
                throw failure
            }
        }
    }

    private fun copyHike(hike: Hike): Hike {
        val copy = Hike(hike.id, hike.organizerEmail, hike.details, hike.history.first().at)
        hike.history.drop(1).forEach {
            when (it.status) {
                HikeStatus.OPEN -> copy.updateCapacity(0, it.actor, it.at)
                HikeStatus.COMPLETE -> copy.updateCapacity(copy.details.capacity, it.actor, it.at)
                HikeStatus.CLOSED -> copy.close(it.at)
                HikeStatus.CANCELLED -> copy.cancel(it.actor, requireNotNull(it.reason), it.at)
            }
        }
        return copy
    }

    val env = UseCaseHelpers(
        HikersFacade(hikerRepository, invitationRepository, hikerFactory),
        HikesFacade(hikeRepository, hikeFactory),
        RegistrationsFacade(registrationRepository, registrationFactory),
        TrailsFacade(trailRepository),
        NotificationsFacade(notificationRepository, notificationFactory),
        object : UserContext { override fun current() = principal },
        object : PasswordHasher {
            // Test-only deterministic hash; NOT a production password-storage algorithm.
            override fun hash(password: String) = "test-sha256:" + MessageDigest.getInstance("SHA-256")
                .digest(password.toByteArray()).joinToString("") { "%02x".format(it) }
        },
        object : EmailService {
            override fun send(notification: Notification) {
                if (emailFails) throw IllegalStateException("Simulated mail failure")
                synchronized(delivered) { delivered.add(notification) }
            }
        },
        object : WeatherService {
            override fun forecast(departurePoint: String, date: LocalDate): Forecast? {
                if (weatherFails) throw IllegalStateException("Simulated weather failure")
                return weatherResult
            }
        },
        object : EventEmitter { override fun emit(event: DomainEvent) { events.add(event) } },
        transactions, clock
    )

    val register = RegisterHikerImpl(env)
    val update = UpdateHikerImpl(env)
    val create = CreateHikeImpl(env)
    val browse = BrowseHikesImpl(env)
    val signUp = SignUpForHikeImpl(env)
    val cancelSignUp = CancelSignUpImpl(env)
    val close = CloseHikeImpl(env)
    val cancel = CancelHikeImpl(env)
    val remind = SendHikeReminderImpl(env)

    val hikerEmail = Email.of("hiker@example.com")
    val organizerEmail = Email.of("organizer@example.com")
    val waiterEmail = Email.of("waiter@example.com")
    val adminEmail = Email.of("admin@example.com")

    init {
        trails["trail-1"] = Trail("trail-1", "Pink Lake", Level.INTERMEDIATE, OpeningSeason(), true, "P2")
    }
    fun login(email: Email = hikerEmail, admin: Boolean = false) {
        principal = Principal(email, if (admin) setOf(Role.ADMINISTRATOR) else setOf(Role.HIKER))
    }
    fun system() { principal = Principal(null, setOf(Role.SYSTEM)) }
    fun addHiker(email: Email = hikerEmail, organizer: Boolean = false, skill: Level = Level.INTERMEDIATE): Hiker {
        val hiker = Hiker(email, "test-hash", HikerProfile(email.value.substringBefore('@'), "6135550100",
            "Ottawa", LocalDate.of(1995, 1, 1), EmergencyContact("Contact", "6135550199"), skill),
            Waiver(env.today(), null), certification = if (organizer)
                Certification("CERT-1", env.today().minusYears(1), env.today().plusYears(1), Level.EXPERT) else null)
        hikers[email] = hiker
        return hiker
    }
    fun addHike(
        capacity: Int = 2,
        days: Long = 1,
        trailId: String = "trail-1"
    ): Hike {
        if (organizerEmail !in hikers) addHiker(organizerEmail, organizer = true)
        val start = env.today().plusDays(days).atTime(10, 0).atZone(CLUB_ZONE).toInstant()
        val hike = hikeFactory.create(organizerEmail, HikeDetails("Club hike", "A scenic hike", trailId, start,
            120, start.minusSeconds(3600), capacity), now)
        hikes[hike.id] = hike
        return hike
    }
    fun seedRegistration(hike: Hike, email: Email, at: Instant = now): Registration {
        if (email !in hikers) addHiker(email)
        val r = env.registrations.signUp(hike.id, email, hike.details.capacity, at)
        hike.updateCapacity(env.registrations.confirmed(hike.id).size, email.value, at)
        return r
    }
    fun registrationQuery() = RegisterHikerQuery("VALID", hikerEmail.value, "password123", "Alex Hiker",
        "6135550100", "Ottawa", date(LocalDate.of(1995, 1, 1)), "Emergency", "6135550199",
        "intermediate", true)
    fun createQuery() = CreateHikeQuery(organizerEmail.value, "New hike", "Description", "trail-1",
        date(env.today().plusDays(7)), Date.from(env.today().atTime(10, 0).atZone(CLUB_ZONE).toInstant()), 120,
        Date.from(env.today().plusDays(6).atTime(10, 0).atZone(CLUB_ZONE).toInstant()), 2)
    fun date(value: LocalDate): Date = Date.from(value.atStartOfDay(CLUB_ZONE).toInstant())
}
