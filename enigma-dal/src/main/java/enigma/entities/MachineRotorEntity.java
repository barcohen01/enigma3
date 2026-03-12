package enigma.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;

@Entity
@Table(name = "machines_rotors")
@Data
public class MachineRotorEntity {
    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "machine_id")
    private UUID machine_id;

    @Column(name = "rotor_id")
    private Integer rotor_id;

    @Column(name = "notch")
    private Integer notch;

    @Column(name = "wiring_right")
    private String wiring_right;

    @Column(name = "wiring_left")
    private String wiring_left;
}