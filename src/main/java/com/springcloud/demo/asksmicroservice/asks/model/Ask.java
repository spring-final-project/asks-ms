package com.springcloud.demo.asksmicroservice.asks.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "asks")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Ask {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @Column(name = "created_at")
    @CreationTimestamp
    LocalDateTime createdAt;

    @Column(name = "responded_at")
    @UpdateTimestamp
    LocalDateTime respondedAt;

    String question;

    String answer;

    @Column(name = "room_id")
    String roomId;

    @Column(name = "user_id")
    String userId;
}
