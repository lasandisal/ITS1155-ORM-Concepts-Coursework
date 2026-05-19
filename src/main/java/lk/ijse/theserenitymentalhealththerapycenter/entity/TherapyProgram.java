package lk.ijse.theserenitymentalhealththerapycenter.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cache;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "therapy_programs")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TherapyProgram {

    @Id
    @Column(name = "program_id", length = 10)
    private String id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 50)
    private String duration;

    @Column(nullable = false)
    private double fee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.ACTIVE;

    @OneToMany(mappedBy = "therapyProgram", cascade = CascadeType.ALL)
    private List<TherapySession> sessions = new ArrayList<>();

    @OneToMany(mappedBy = "therapyProgram", cascade = CascadeType.ALL)
    private List<Payment> payments = new ArrayList<>();

    public enum Status {
        ACTIVE, INACTIVE
    }
}
