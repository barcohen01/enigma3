package enigma.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;

@Entity
@Table(name = "machines")
@Data
public class MachineEntity {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "name")
    private String name;

    @Column(name = "rotors_count")
    private Integer rotorsCount;

    @Column(name = "abc")
    private String abc;
}