package br.com.itfree.heartmonitoring.repository.projection;

public interface PatientHeartbeatSummaryProjection {
    String getName();
    Float getAverageBpm();
    Integer getMinBpm();
    Integer getMaxBpm();
    Integer getLastBpm();
    String getTimestamp();
}
