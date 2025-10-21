package com.example.SA2Gemini.config;

import com.example.SA2Gemini.repository.AsientoRepository;
import com.example.SA2Gemini.repository.MovimientoRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("reset-db") // This runner will only execute when the 'reset-db' profile is active
public class ResetDatabaseRunner implements CommandLineRunner {

    private final AsientoRepository asientoRepository;
    private final MovimientoRepository movimientoRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public ResetDatabaseRunner(AsientoRepository asientoRepository, MovimientoRepository movimientoRepository) {
        this.asientoRepository = asientoRepository;
        this.movimientoRepository = movimientoRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        System.out.println("Resetting database...");

        // Delete all Movimiento records first due to foreign key constraint
        movimientoRepository.deleteAll();
        System.out.println("All Movimiento records deleted.");

        // Delete all Asiento records
        asientoRepository.deleteAll();
        System.out.println("All Asiento records deleted.");

        // Reset the sequence for the Asiento table (PostgreSQL specific)
        // This assumes the sequence name is 'asiento_id_seq'. Adjust if your sequence name is different.
        entityManager.createNativeQuery("ALTER SEQUENCE asiento_id_seq RESTART WITH 1").executeUpdate();
        System.out.println("Asiento ID sequence reset to 1.");

        System.out.println("Database reset complete.");
    }
}
