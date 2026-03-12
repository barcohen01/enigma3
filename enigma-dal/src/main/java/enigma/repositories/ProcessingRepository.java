package enigma.repositories;

import enigma.entities.ProcessingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface ProcessingRepository extends JpaRepository<ProcessingEntity, UUID> {
}