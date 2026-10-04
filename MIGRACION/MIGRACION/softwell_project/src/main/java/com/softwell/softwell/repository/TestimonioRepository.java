package com.softwell.softwell.repository;

import com.softwell.softwell.model.Testimonio;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TestimonioRepository extends MongoRepository<Testimonio, String> {
    List<Testimonio> findByVisible(boolean visible);
}
