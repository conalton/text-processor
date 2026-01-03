package org.conalton.textprocessor.modules.task.repository;

import java.util.Collection;
import java.util.List;
import org.conalton.textprocessor.modules.task.entity.Task;
import org.conalton.textprocessor.modules.task.entity.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<Task, String> {
  List<Task> findAllBySourcePathIn(Collection<String> sourcePaths);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query(
      """
            update Task t
            set t.status= :newStatus
            where t.status = :currentStatus
            AND t.sourcePath in (:paths)
            """)
  int updateTasksWithNewStatusBySourcePathAndCurrentStatus(
      @Param("paths") List<String> paths,
      @Param("currentStatus") TaskStatus currentStatus,
      @Param("newStatus") TaskStatus newStatus);
}
