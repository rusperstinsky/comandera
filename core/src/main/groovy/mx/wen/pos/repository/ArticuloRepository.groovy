package mx.wen.pos.repository

import mx.wen.pos.model.Articulo
import mx.wen.pos.model.NotaVenta
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.querydsl.QueryDslPredicateExecutor
import org.springframework.transaction.annotation.Transactional

interface ArticuloRepository extends JpaRepository<Articulo, Integer>, QueryDslPredicateExecutor<Articulo> {

  @Transactional
  @Query( value = "SELECT * FROM articulos WHERE tipo = ?1 order by id_articulo", nativeQuery = true )
  List<Articulo> findItemsByType( String tipo )

}
