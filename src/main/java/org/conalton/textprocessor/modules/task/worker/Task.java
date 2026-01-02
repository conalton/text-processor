package org.conalton.textprocessor.modules.task.worker;

public class Task implements Runnable {
    private final int jobId;

    public Task (int jobId) {
        this.jobId = jobId;
    }

  @Override
  public void run() {
        System.out.println(jobId);
  }
}
