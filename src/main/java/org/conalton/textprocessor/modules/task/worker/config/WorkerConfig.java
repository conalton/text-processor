package org.conalton.textprocessor.modules.task.worker.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(WorkerProperties.class)
public class WorkerConfig {}
