package mx.wen.pos.repository.custom

import mx.wen.pos.model.NotaVenta

interface NotaVentaRepositoryCustom {

  List<NotaVenta> findByFacturaNotEmptyLimitingLatestResults( Integer results )

}
