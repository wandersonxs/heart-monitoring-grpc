package br.com.itfree.heartmonitoringclient.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class PatientResponsetDTO {
    private Integer heartbeat;
    private LocalDateTime timestamp;
}
