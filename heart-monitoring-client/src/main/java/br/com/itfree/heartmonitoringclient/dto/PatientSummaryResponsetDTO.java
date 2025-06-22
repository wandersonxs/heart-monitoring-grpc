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
public class PatientSummaryResponsetDTO {
    private String name;
    private Float averageBpm;
    private Integer maxBpm;
    private Integer minBpm;
    private Integer lastBpm;
    private String timestamp;
}
