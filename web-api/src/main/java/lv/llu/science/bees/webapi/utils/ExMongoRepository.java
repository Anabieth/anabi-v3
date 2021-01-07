package lv.llu.science.bees.webapi.utils;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;
import java.util.Optional;

@NoRepositoryBean
public interface ExMongoRepository<T, ID extends Serializable> extends MongoRepository<T, ID>, CrudRepository<T, ID> {
    Optional<T> findById(ID id);
}