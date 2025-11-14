package org.conalton.textprocessor.repository.task;

import org.conalton.textprocessor.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<Task, String>, TaskRepositoryCustom {}
