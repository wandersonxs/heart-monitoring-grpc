package br.com.itfree.heartmonitoring.repository;


import br.com.itfree.heartmonitoring.model.Heartbeat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PatientRepository extends JpaRepository<Heartbeat, Long> {
    List<Heartbeat> findByPatientId(Long patientId);
}
