package org.conalton.textprocessor.repository.task;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.conalton.textprocessor.entity.Task;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class TaskRepositoryImpl implements TaskRepositoryCustom {
  @PersistenceContext private EntityManager entityManager;

  @Transactional
  public void insertTask(Task task) {
    entityManager.persist(task);
  }
}
