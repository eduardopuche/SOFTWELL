package com.softwell.softwell.repository;

import com.softwell.softwell.model.Pedido;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PedidoRepository extends MongoRepository<Pedido, String> {
    List<Pedido> findByUsuarioIdOrderByFechaCreacionDesc(String usuarioId);
}
