package com.vts.ProjectScheduler.entity.pms;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@Data
@Entity
@Table(name = "mail_configuration")
public class MailConfiguration {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long MailConfigurationId;
    private String TypeOfHost;
    private String Host;
    private String Port;
    private String Username;
    private String Password;
    private String CreatedBy;
    private LocalDateTime CreatedDate;
    private String ModifiedBy;
    private LocalDateTime ModifiedDate;
    private int IsActive;

}
