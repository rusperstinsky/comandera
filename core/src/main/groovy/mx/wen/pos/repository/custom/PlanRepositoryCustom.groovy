package mx.wen.pos.repository.custom

import mx.wen.pos.model.Plan

interface PlanRepositoryCustom {

  List<Plan> findBy_IdBanco( Integer idBanco )

  //List<Plan> findByIdTerminal( String idTerminal )

  //List<Plan> findByIdBancoDeposito( Integer idBancoDeposito )

}
