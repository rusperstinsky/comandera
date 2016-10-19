package mx.wen.pos.repository

import mx.wen.pos.model.Plan
import mx.wen.pos.repository.custom.PlanRepositoryCustom
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.querydsl.QueryDslPredicateExecutor

interface PlanRepository extends JpaRepository<Plan, String>, QueryDslPredicateExecutor<Plan> {
}
