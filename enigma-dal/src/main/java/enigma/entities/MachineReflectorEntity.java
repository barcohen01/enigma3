package enigma.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;

@Entity
@Table(name = "machines_reflectors")
@Data
public class MachineReflectorEntity {
    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "machine_id")
    private UUID machine_id;

    @Enumerated(EnumType.STRING)
    @Column(name = "reflector_id", columnDefinition = "reflector_id_enum")
    private ReflectorId reflector_id;

    @Column(name = "input")
    private String input;

    @Column(name = "output")
    private String output;
}