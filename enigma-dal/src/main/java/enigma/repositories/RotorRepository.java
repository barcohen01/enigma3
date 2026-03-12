package enigma.repositories;

import enigma.entities.MachineRotorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface RotorRepository extends JpaRepository<MachineRotorEntity, UUID> {
}