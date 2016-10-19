package mx.wen.pos.repository

import mx.wen.pos.model.Cliente
import mx.wen.pos.repository.custom.ClienteRepositoryCustom
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.querydsl.QueryDslPredicateExecutor

interface ClienteRepository extends JpaRepository<Cliente, Integer>, QueryDslPredicateExecutor<Cliente> {

}
