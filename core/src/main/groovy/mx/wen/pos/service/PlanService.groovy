package mx.wen.pos.service

import mx.wen.pos.model.Plan

interface PlanService {

  //List<Plan> listarPlanesPorTerminal( String idTerminal )

  List<Plan> listarPlanesPorBancoDeposito( Integer idBancoDeposito )

}
