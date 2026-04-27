package com.uma.example.springuma.integration;

import static org.hamcrest.Matchers.hasSize;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uma.example.springuma.integration.base.AbstractIntegration;
import com.uma.example.springuma.model.Medico;
import com.uma.example.springuma.model.MedicoService;
import com.uma.example.springuma.model.Paciente;

public class PacienteControllerMockMvcIT extends AbstractIntegration {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MedicoService medicoService;

    Paciente paciente;
    Medico medico;

    @BeforeEach
    void setUp() {
        medico = new Medico();
        medico.setNombre("Miguel");
        medico.setId(1L);
        medico.setDni("835");
        medico.setEspecialidad("Ginecologo");

        paciente = new Paciente();
        paciente.setId(1L);
        paciente.setNombre("Maria");
        paciente.setDni("888");
        paciente.setEdad(20);
        paciente.setCita("Ginecologia");
        paciente.setMedico(this.medico);
    }

    private void crearMedico(Medico medico) throws Exception {
        this.mockMvc.perform(post("/medico")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(medico)))
                .andExpect(status().isCreated());
    }

    private void crearPaciente(Paciente paciente) throws Exception {
        mockMvc.perform(post("/paciente")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(paciente)))
                .andExpect(status().isCreated());
    }

    private void getPacienteById(Long id, Paciente expected) throws Exception {
        mockMvc.perform(get("/paciente/" + id))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$").value(expected));
    }

    @Test
    @DisplayName("Crear paciente y recuperarlo por ID pasado por parametro")
    void savePaciente_RecuperaPacientePorId() throws Exception {
        crearMedico(medico);
        crearPaciente(paciente);

        //Obtener paciente por ID
        getPacienteById(paciente.getId(), paciente);
    }

    @Test
    @DisplayName("Crear paciente y asociar paciente a médico")
    void crearPaciente_AsociarAMedico() throws Exception {
        crearMedico(medico);
        crearPaciente(paciente);

        mockMvc.perform(get("/paciente/medico/" + medico.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nombre").value(paciente.getNombre()))
                .andExpect(jsonPath("$[0].dni").value(paciente.getDni()))
                .andExpect(jsonPath("$[0].medico.id").value(medico.getId()));
    }

    @Test
    @DisplayName("Actualizar los datos de un paciente correctamente")
    void actualizarPaciente_VerificarCambios() throws Exception {
        crearMedico(medico);
        crearPaciente(paciente);

        Paciente pacienteModificado = new Paciente();
        pacienteModificado.setId(paciente.getId());
        pacienteModificado.setNombre("Maria Modificada");
        pacienteModificado.setDni(paciente.getDni());
        pacienteModificado.setEdad(25);
        pacienteModificado.setCita("Revision");
        pacienteModificado.setMedico(medico);

        mockMvc.perform(put("/paciente")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(pacienteModificado)))
                .andExpect(status().isNoContent());

        getPacienteById(paciente.getId(), pacienteModificado);
    }

}
