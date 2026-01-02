package com.opencode.alumxbackend.basics.suzzzal.house.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "suzzzal_member")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HouseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    private String mobileNumber;

    private String streetAddress;

    private String city;
}
