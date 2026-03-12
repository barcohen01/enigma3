package enigma.repositories;

import enigma.entities.MachineReflectorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface ReflectorRepository extends JpaRepository<MachineReflectorEntity, UUID> {
}