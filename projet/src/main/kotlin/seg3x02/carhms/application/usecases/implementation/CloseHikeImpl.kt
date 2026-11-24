package seg3x02.carhms.application.usecases.implementation

import seg3x02.carhms.application.dtos.queries.*
import seg3x02.carhms.application.usecases.CloseHike
import seg3x02.carhms.domain.*

class CloseHikeImpl(private val env: UseCaseHelpers) : CloseHike {
    override fun execute(query: CloseHikeQuery) = env.transactions.run {
        env.authorization.requireSystem(env.users.current())
        require(query.evaluatedAt.toInstant() <= env.clock.instant()) { "Evaluation cannot be in the future" }
        val hike = env.hikes.get(HikeId(query.hikeId))
        if (hike.close(query.evaluatedAt.toInstant())) {
            env.hikes.save(hike)
            env.lifecycle(hike)
        }
        Unit
    }
}
