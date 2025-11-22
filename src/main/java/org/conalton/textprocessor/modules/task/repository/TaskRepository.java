package org.conalton.textprocessor.modules.task.repository;

import org.conalton.textprocessor.modules.task.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<Task, String> {}
