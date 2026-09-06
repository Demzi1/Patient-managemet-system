package com.pms.patientservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pms.patientservice.dto.PatientRequestDto;
import com.pms.patientservice.dto.PatientResponseDto;
import com.pms.patientservice.service.PatientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PatientController.class)
public class PatientControllerTest {

    private UUID patientId;
    private PatientRequestDto patientRequestDto;
    private PatientResponseDto patientResponseDto;

    @MockitoBean
    private PatientService patientService;

    @Autowired
    private MockMvc mockMvc;


    private final ObjectMapper objectMapper = new ObjectMapper();


    @BeforeEach
    void setUp(){
        patientId = UUID.randomUUID();
        this.patientResponseDto = new PatientResponseDto(
               String.valueOf(patientId),
                "test user",
                "2026-06-15",
                "test@gmail.com",
                "Banjul"
                );

        this.patientRequestDto = new  PatientRequestDto(
                "test user",
                "Banjul",
                "2026-06-15",
                "test@gmail.com",
                "2026-06-15");

    }

    @Nested
    class GetAllPatients {
        @Test
        void shouldReturnAllPatients() throws Exception {
            when(patientService.getPatients())
                    .thenReturn(List.of(patientResponseDto));

            mockMvc.perform(get("/patients"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.size()").value(1))
                    .andExpect(jsonPath("$[0].name").value("test user"))
                    .andExpect(jsonPath("$[0].email").value("test@gmail.com"));

            verify(patientService).getPatients();
        }
    }

    @Nested
    class GetPatient{
        @Test
        void shouldReturnPatientById() throws Exception{
            when(patientService.getPatient(patientId))
                    .thenReturn(patientResponseDto);

            mockMvc.perform(get("/patients/{patientId}", patientId))
                    .andExpect(status().isOk())
                    .andExpect( jsonPath("$.name").value("test user"))
                    .andExpect(jsonPath("$.email").value("test@gmail.com"));

            verify(patientService).getPatient(patientId);
        }

    }

    @Nested
    class CreatePatient{
        @Test
        void shouldCreatePatient() throws Exception{
            when(patientService.createPatient(any(PatientRequestDto.class)))
                    .thenReturn(patientResponseDto);

            mockMvc.perform(post("/patients")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(patientRequestDto)))
                    .andExpect(status().isCreated())
                    .andExpect( jsonPath("$.name").value("test user"))
                    .andExpect(jsonPath("$.email").value("test@gmail.com"));

            verify(patientService).createPatient(any(PatientRequestDto.class));
        }

    }

    @Nested
    class UpdatePatient{
        @Test
        void shouldUpdatePatientById() throws Exception{
            when(patientService.updatePatient(eq(patientId),any(PatientRequestDto.class
            )))
                    .thenReturn(patientResponseDto);

            mockMvc.perform(put("/patients/{patientId}", patientId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(patientRequestDto)))
                    .andExpect(status().isOk())
                    .andExpect( jsonPath("$.name").value("test user"))
                    .andExpect(jsonPath("$.email").value("test@gmail.com"));

            verify(patientService).updatePatient(eq(patientId), any(PatientRequestDto.class));
        }

    }

    @Nested
    class DeletePatient{
        @Test
        void shouldDeletePatientById() throws Exception{
            doNothing().when(patientService).deletePatient(patientId);

            mockMvc.perform(delete("/patients/{patientId}", patientId))
                    .andExpect(status().isNoContent());

            verify(patientService).deletePatient(patientId);
        }

    }




}
