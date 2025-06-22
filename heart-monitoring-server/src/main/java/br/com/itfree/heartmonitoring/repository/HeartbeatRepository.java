package br.com.itfree.heartmonitoring.repository;


import br.com.itfree.heartmonitoring.model.Heartbeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface HeartbeatRepository extends JpaRepository<Heartbeat, Long> {

    @Query(value = """
            SELECT hb.*
            FROM heartbeat hb
            JOIN patient p ON p.id = hb.patient_id
            WHERE UPPER(p.name) = UPPER(:name)
            ORDER BY hb.timestamp DESC
            LIMIT 1
            """, nativeQuery = true)
    Optional<Heartbeat> findLatestByPatientName(String name);

    List<Heartbeat> findByPatientId(Long patientId);

}
