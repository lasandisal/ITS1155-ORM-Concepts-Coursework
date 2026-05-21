package lk.ijse.theserenitymentalhealththerapycenter.entity;
//
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
//
//@Entity
//@Table(name = "therapy_sessions")
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//public class TherapySession {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @ManyToOne(fetch = FetchType.EAGER)
//    @JoinColumn(name = "patient_id", nullable = false)
//    private Patient patient;
//
//    @ManyToOne(fetch = FetchType.EAGER)
//    @JoinColumn(name = "therapist_id", nullable = false)
//    private Therapist therapist;
//
//    @ManyToOne(fetch = FetchType.EAGER)
//    @JoinColumn(name = "program_id", nullable = false)
//    private TherapyProgram therapyProgram;
//
//    @Column(name = "session_date_time", nullable = false)
//    private LocalDateTime sessionDateTime;
//
//    @Enumerated(EnumType.STRING)
//    @Column(nullable = false, length = 20)
//    private Status status = Status.SCHEDULED;
//
//    public enum Status {
//        SCHEDULED, COMPLETED, CANCELLED
//    }
//}

@Entity
@Table(name = "therapy_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TherapySession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "therapist_id", nullable = false)
    private Therapist therapist;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "program_id", nullable = false)
    private TherapyProgram therapyProgram;

    @Column(name = "session_date_time", nullable = false)
    private LocalDateTime sessionDateTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.SCHEDULED;

    // New Bidirectional link to the bridge table
    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SessionAttendance> attendances = new ArrayList<>();

    public enum Status { SCHEDULED, COMPLETED, CANCELLED }
}
