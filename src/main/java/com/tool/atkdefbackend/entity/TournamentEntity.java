package com.tool.atkdefbackend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "tournaments")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TournamentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_date")
    private LocalDateTime endTime;

//    //mapper = "tournament ben roundEntity
//    //get round trong 1 tournament
//    @OneToMany(mappedBy = "tournament", cascade = CascadeType.ALL, orphanRemoval = true)
//    @ToString.Exclude // Chan lombok toString tranh loop vo tan
//    @EqualsAndHashCode.Exclude
//    private List<RoundEntity> rounds;

}
