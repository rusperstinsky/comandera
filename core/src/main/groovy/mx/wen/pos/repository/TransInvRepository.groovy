package mx.wen.pos.repository

import mx.wen.pos.model.TransInv
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.querydsl.QueryDslPredicateExecutor

interface TransInvRepository
extends JpaRepository<TransInv, Integer>,
    QueryDslPredicateExecutor<TransInv> {

  List<TransInv> findByIdTipoTransAndFolio( String pIdTipoTrans, Integer pFolio )

  List<TransInv> findByFechaBetween( Date pFromDate, Date pToDate )

  List<TransInv> findByIdTipoTrans( String pIdTipoTrans )

  List<TransInv> findByIdTipoTransAndReferencia( String pIdTipoTrans, String pReferencia )

  List<TransInv> findBySucursalDestino( Integer pSucursalDestino )

}
