package com.uma.example.springuma.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uma.example.springuma.integration.base.AbstractIntegration;
import com.uma.example.springuma.model.Medico;

public class MedicoControllerMockMvcIT extends AbstractIntegration {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Medico medico;

    @BeforeEach
    void setUp() {
        medico = new Medico();
        medico.setId(1L);
        medico.setDni("835");
        medico.setNombre("Miguel");
        medico.setEspecialidad("Ginecologia");
    }

    private void crearMedico(Medico medico) throws Exception {
        this.mockMvc.perform(post("/medico")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(medico)))
                .andExpect(status().isCreated());
    }

    private void getMedicoById(Long id, Medico expected) throws Exception {
        mockMvc.perform(get("/medico/" + id))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.dni").value(expected.getDni()))
                .andExpect(jsonPath("$.nombre").value(expected.getNombre()))
                .andExpect(jsonPath("$.especialidad").value(expected.getEspecialidad()));
    }

    private void deleteMedicoById(Long id) throws Exception {
        mockMvc.perform(delete("/medico/" + id))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    @DisplayName("Crear médico y recuperarlo por ID pasado como parámetro")
    void crearMedico_y_RecuperarMedicoPorId() throws Exception {
        crearMedico(medico);
        getMedicoById(medico.getId(), medico);
    }

    @Test
    @DisplayName("Crear médico y eliminarlo correctamente")
    void crearMedico_y_EliminarMedico() throws Exception {
        crearMedico(medico);
        getMedicoById(medico.getId(), medico);
        deleteMedicoById(medico.getId());

        mockMvc.perform(get("/medico/" + medico.getId()))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @DisplayName("Crear médico y actualizarlo correctamente")
    void crearMedico_y_ActualizarMedico() throws Exception {
        crearMedico(medico);
        getMedicoById(medico.getId(), medico);

        Medico medicoActualizado = new Medico();
        medicoActualizado.setId(medico.getId());
        medicoActualizado.setDni(medico.getDni());
        medicoActualizado.setNombre("Ramón");
        medicoActualizado.setEspecialidad("Osteología");

        mockMvc.perform(put("/medico")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(medicoActualizado)))
                .andExpect(status().isNoContent());

        getMedicoById(medico.getId(), medicoActualizado);
    }

    @Test
    @DisplayName("Crear dos médicos con el mismo DNI debería fallar")
    void crearDosMedicosConMismoDni_DebefallarConConflicto() throws Exception {
        crearMedico(medico);

        Medico medico2 = new Medico();
        medico2.setId(2L);
        medico2.setDni("835");  // mismo DNI
        medico2.setNombre("Otro");
        medico2.setEspecialidad("Cardiologia");

        mockMvc.perform(post("/medico")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(medico2)))
                .andExpect(status().isInternalServerError());
    }
}
