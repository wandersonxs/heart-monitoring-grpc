package br.com.itfree.heartmonitoring.repository;


import br.com.itfree.heartmonitoring.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    Optional<Patient> findByName(String name);
}
