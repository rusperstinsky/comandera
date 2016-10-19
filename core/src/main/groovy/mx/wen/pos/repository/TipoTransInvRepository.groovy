package mx.wen.pos.repository

import mx.wen.pos.model.TipoTransInv
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.querydsl.QueryDslPredicateExecutor

interface TipoTransInvRepository
extends JpaRepository<TipoTransInv, String>,
    QueryDslPredicateExecutor<TipoTransInv> {

}
