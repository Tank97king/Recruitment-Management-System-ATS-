package com.example.ats.util;

import com.example.ats.dto.response.AuditLogResponse;
import com.example.ats.entity.AuditLog;
import org.mapstruct.Mapper;

/**
 * MapStruct mapper for {@link AuditLog} entity to {@link AuditLogResponse} DTO.
 */
@Mapper(componentModel = "spring")
public interface AuditLogMapper {

    AuditLogResponse toResponse(AuditLog auditLog);
}
