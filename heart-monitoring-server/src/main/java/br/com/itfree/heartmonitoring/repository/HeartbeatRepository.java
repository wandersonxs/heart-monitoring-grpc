package br.com.itfree.heartmonitoring.repository;


import br.com.itfree.heartmonitoring.model.Heartbeat;
import br.com.itfree.heartmonitoring.repository.projection.PatientHeartbeatSummaryProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface HeartbeatRepository extends JpaRepository<Heartbeat, Long> {

    @Query(value = """
    SELECT 
        p.name AS name,
        AVG(hb_subset.bpm) AS averageBpm,
        MIN(hb_subset.bpm) AS minBpm,
        MAX(hb_subset.bpm) AS maxBpm,
        CAST(MAX(hb_subset.timestamp) AS VARCHAR) AS timestamp,
        (
            SELECT hb2.bpm
            FROM heartbeat hb2
            WHERE hb2.patient_id = p.id
            ORDER BY hb2.timestamp DESC
            LIMIT 1
        ) AS lastBpm
    FROM (
        SELECT *
        FROM heartbeat
        WHERE patient_id = (
            SELECT id FROM patient WHERE UPPER(name) = UPPER(:name)
        )
        ORDER BY timestamp DESC
        LIMIT 10
    ) hb_subset
    JOIN patient p ON p.id = hb_subset.patient_id
    GROUP BY p.name, p.id
    """, nativeQuery = true)
    Optional<PatientHeartbeatSummaryProjection> findSummaryFromLast10RecordsByPatientName(String name);

    @Query(value = """
            SELECT hb.*
            FROM heartbeat hb
            JOIN patient p ON p.id = hb.patient_id
            WHERE UPPER(p.name) = UPPER(:name)
            ORDER BY hb.timestamp
            """, nativeQuery = true)
    List<Heartbeat> findAllByPatientName(String name);

}
