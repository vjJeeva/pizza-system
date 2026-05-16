package com.pizza.delivery_service.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "drivers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Driver {

    @Id
    private String driverId;

    private String name;
    private String vehicleNumber;
    private boolean isAvailable;
}