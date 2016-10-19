package mx.wen.pos.repository

import mx.wen.pos.model.NotaVenta
import mx.wen.pos.repository.custom.NotaVentaRepositoryCustom
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.querydsl.QueryDslPredicateExecutor
import org.springframework.data.jpa.repository.Modifying
import org.springframework.transaction.annotation.Transactional

@Transactional( readOnly = true )
interface NotaVentaRepository extends JpaRepository<NotaVenta, String>, QueryDslPredicateExecutor<NotaVenta> {

  List<NotaVenta> findByFechaHoraFacturaBetweenAndFacturaNotNull( Date fechaInicio, Date fechaFin )

  @Query( value = "SELECT next_folio('nota_venta_id_factura')", nativeQuery = true )
  String getNotaVentaSequence( )

  @Query( value = "SELECT NEXTVAL('factura_seq')", nativeQuery = true )
  BigInteger getFacturaSequence( )

  List<NotaVenta> findByObservacionesNv( String pObservaciones )

  List<NotaVenta> findByFechaHoraFacturaBetween( Date pDateFrom, Date pDateTo )

  @Modifying
  @Transactional
  @Query( value = "DELETE FROM nota_venta WHERE id_factura = ?1", nativeQuery = true )
  void deleteByIdFactura( String pIdFactura )

  //@Query( value = "SELECT nv FROM nota_venta nv WHERE factura = ''", nativeQuery = true )
  List<NotaVenta> findByFactura( String pFactura )

  List<NotaVenta> findByFacturaIsNull( )

  @Transactional
  @Query( value = "SELECT * FROM nota_venta WHERE factura is not null AND factura != '' AND id_factura NOT IN (SELECT referencia from trans_inv WHERE id_tipo_trans = 'VENTA') order by factura", nativeQuery = true )
  List<NotaVenta> findOrdersWithoutTrans(  )


  @Transactional
  @Query( value = "SELECT * FROM nota_venta WHERE factura is not null AND factura != '' AND s_factura = 'T' AND id_factura NOT IN (SELECT referencia from trans_inv WHERE id_tipo_trans = 'DEVOLUCION') order by factura", nativeQuery = true )
  List<NotaVenta> findOrdersCanWithoutTrans(  )

}
