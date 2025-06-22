package br.com.itfree.heartmonitoring.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private Integer age;
    private String email;

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL)
    private List<Heartbeat> heartbeats;
}