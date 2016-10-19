package mx.wen.pos.repository

import mx.wen.pos.model.Empleado
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.querydsl.QueryDslPredicateExecutor


interface EmpleadoRepository extends JpaRepository<Empleado, Integer>, QueryDslPredicateExecutor<Empleado> {

}
