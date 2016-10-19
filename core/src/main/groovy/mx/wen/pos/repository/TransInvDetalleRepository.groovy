package mx.wen.pos.repository

import mx.wen.pos.model.TransInvDetalle
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.querydsl.QueryDslPredicateExecutor

interface TransInvDetalleRepository
extends JpaRepository<TransInvDetalle, Integer>,
    QueryDslPredicateExecutor<TransInvDetalle> {

  List<TransInvDetalle> findByIdTipoTransAndFolio( String pIdTipoTrans, Integer pFolio )

  List<TransInvDetalle> findBySku( Integer pSku )

  List<TransInvDetalle> findBySkuIn( Collection<Integer> pSkus )

}
