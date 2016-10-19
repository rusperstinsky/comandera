package mx.wen.pos.repository

import mx.wen.pos.model.Parametro
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.querydsl.QueryDslPredicateExecutor

interface ParametroRepository extends JpaRepository<Parametro, String>, QueryDslPredicateExecutor<Parametro> {
}
