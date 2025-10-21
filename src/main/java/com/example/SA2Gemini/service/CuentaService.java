package com.example.SA2Gemini.service;

import com.example.SA2Gemini.entity.Cuenta;
import com.example.SA2Gemini.repository.CuentaRepository;
import com.example.SA2Gemini.repository.MovimientoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;

@Service
public class CuentaService {

    @Autowired
    private CuentaRepository cuentaRepository;

    @Autowired
    private MovimientoRepository movimientoRepository;

    public List<Cuenta> getAllCuentas() {
        List<Cuenta> cuentas = cuentaRepository.findAll();
        // Sort: active accounts first, then by name
        cuentas.sort(Comparator
                .comparing(Cuenta::isActivo, Comparator.reverseOrder()) // true (active) comes before false (inactive)
                .thenComparing(Cuenta::getNombre));
        return cuentas;
    }

    public List<Cuenta> getAllCuentasIncludingInactive() {
        return cuentaRepository.findAll();
    }

    public void saveCuenta(Cuenta cuenta) {
        if (cuenta.getId() != null) {
            if (cuentaRepository.existsByCodigoAndIdNot(cuenta.getCodigo(), cuenta.getId())) {
                throw new CodigoCuentaExistsException("Ya existe una cuenta con el código " + cuenta.getCodigo());
            }
        } else {
            if (cuentaRepository.existsByCodigo(cuenta.getCodigo())) {
                throw new CodigoCuentaExistsException("Ya existe una cuenta con el código " + cuenta.getCodigo());
            }
        }
        cuentaRepository.save(cuenta);
    }

    public Cuenta getCuentaById(Long id) {
        return cuentaRepository.findById(id).orElse(null);
    }

    public boolean isCuentaInUse(Long id) {
        Cuenta cuenta = getCuentaById(id);
        return cuenta != null && !movimientoRepository.findByCuenta(cuenta).isEmpty();
    }

    public boolean deleteCuenta(Long id) {
        if (isCuentaInUse(id)) {
            return false; // Deletion failed because the account is in use
        }
        cuentaRepository.deleteById(id);
        return true; // Deletion successful
    }

    // Regarding modification restrictions, if an account is in use, perhaps the 'codigo' should not be changed.
    // This can be handled in the update logic.

    public void toggleCuentaActivoStatus(Long id) {
        Cuenta cuenta = getCuentaById(id);
        if (cuenta != null) {
            cuenta.setActivo(!cuenta.isActivo());
            cuentaRepository.save(cuenta);
        }
    }



    @PreAuthorize("isAuthenticated()")
    public List<Cuenta> getActiveCuentas() {
        return cuentaRepository.findByActivoTrue();
    }
}
