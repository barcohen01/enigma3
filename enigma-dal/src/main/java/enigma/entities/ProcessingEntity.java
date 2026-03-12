package enigma.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;

@Entity
@Table(name = "processing")
@Data
public class ProcessingEntity {
    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "machine_id")
    private UUID machine_id;

    @Column(name = "session_id")
    private String session_id;

    @Column(name = "code")
    private String code;

    @Column(name = "input")
    private String input;

    @Column(name = "output")
    private String output;

    @Column(name = "time")
    private Long time;
}