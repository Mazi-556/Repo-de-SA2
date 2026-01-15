package com.example.SA2Gemini.repository;

import com.example.SA2Gemini.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByEntityNameAndEntityIdOrderByTimestampDesc(String entityName, String entityId);

    List<AuditLog> findByUserNameOrderByTimestampDesc(String userName);

    List<AuditLog> findByEntityNameOrderByTimestampDesc(String entityName);

    List<AuditLog> findByOperationTypeOrderByTimestampDesc(AuditLog.OperationType operationType);

    @Query("SELECT a FROM AuditLog a ORDER BY a.timestamp DESC LIMIT :limit")
    List<AuditLog> findTopByOrderByTimestampDesc(@Param("limit") int limit);

    @Query("SELECT a FROM AuditLog a WHERE a.timestamp BETWEEN :startDate AND :endDate ORDER BY a.timestamp DESC")
    List<AuditLog> findByTimestampBetweenOrderByTimestampDesc(
            @Param("startDate") LocalDateTime startDate, 
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT a FROM AuditLog a WHERE a.entityName = :entityName AND a.timestamp BETWEEN :startDate AND :endDate ORDER BY a.timestamp DESC")
    List<AuditLog> findByEntityNameAndTimestampBetweenOrderByTimestampDesc(
            @Param("entityName") String entityName,
            @Param("startDate") LocalDateTime startDate, 
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT a FROM AuditLog a WHERE a.userName = :userName AND a.timestamp BETWEEN :startDate AND :endDate ORDER BY a.timestamp DESC")
    List<AuditLog> findByUserNameAndTimestampBetweenOrderByTimestampDesc(
            @Param("userName") String userName,
            @Param("startDate") LocalDateTime startDate, 
            @Param("endDate") LocalDateTime endDate);

    // Estadísticas útiles
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.operationType = :operationType")
    Long countByOperationType(@Param("operationType") AuditLog.OperationType operationType);

    @Query("SELECT a.userName, COUNT(a) FROM AuditLog a GROUP BY a.userName ORDER BY COUNT(a) DESC")
    List<Object[]> getUserActivityStats();

    @Query("SELECT a.entityName, COUNT(a) FROM AuditLog a GROUP BY a.entityName ORDER BY COUNT(a) DESC")
    List<Object[]> getEntityActivityStats();
}