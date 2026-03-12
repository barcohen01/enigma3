package enigma.config;

import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseCleanupService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PreDestroy
    public void clearDatabase() {
        try {
            jdbcTemplate.execute("DELETE FROM processing");
            jdbcTemplate.execute("DELETE FROM machines_rotors");
            jdbcTemplate.execute("DELETE FROM machines_reflectors");

            jdbcTemplate.execute("DELETE FROM machines");
        } catch (Exception e) {
        }
    }
}