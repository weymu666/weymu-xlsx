package com.weymu.xlsx.repository;

import com.weymu.xlsx.entity.WorkBookEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author weymu
 * @date 2020/10/29
 * @description
 */
@Repository
public interface WorkBookRepository extends MongoRepository<WorkBookEntity, String> {
    @Query(value = "{'option.title':{$regex:?0}}")
    List<WorkBookEntity> findAllByTitle(String title);
}
