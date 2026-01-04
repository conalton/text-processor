package org.conalton.textprocessor.modules.task.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.conalton.textprocessor.modules.task.entity.Task;
import org.conalton.textprocessor.modules.task.entity.TaskStatus;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<Task, String> {
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

  @Query(
      value =
          """
    select *
      from tasks
     where status in (:statuses)
       and locked_by is null
       and IFNULL(next_attempt_at, utc_timestamp(6)) <= utc_timestamp(6)
     order by priority desc
     limit 1
     for update skip locked
    """,
      nativeQuery = true)
  Optional<Task> getNextAvailableForUpdate(@Param("statuses") Collection<String> statuses);
}
