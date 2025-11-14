package org.conalton.textprocessor.dto.response;

import org.conalton.textprocessor.dto.response.common.ApiResponse;

public record PresignedUploadResponse(String taskId, String uploadUrl) implements ApiResponse {}
