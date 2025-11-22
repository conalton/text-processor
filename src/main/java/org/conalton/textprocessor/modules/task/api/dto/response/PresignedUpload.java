package org.conalton.textprocessor.modules.task.api.dto.response;

import org.conalton.textprocessor.web.response.ApiResponse;

public record PresignedUpload(String taskId, String uploadUrl) implements ApiResponse {}
