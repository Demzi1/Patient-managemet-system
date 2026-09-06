package com.pms.patientservice.service;

import com.pms.patientservice.dto.PatientRequestDto;
import com.pms.patientservice.dto.PatientResponseDto;
import com.pms.patientservice.exception.EmailAlreadyExistException;
import com.pms.patientservice.exception.PatientNotFoundException;
import com.pms.patientservice.grpc.BillingServiceGrpcClient;
import com.pms.patientservice.kafka.KafkaProducer;
import com.pms.patientservice.model.Patient;
import com.pms.patientservice.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PatientServiceTest {

    private PatientResponseDto patientResponseDto;
    private PatientRequestDto patientRequestDto;
    private Patient patient1;
    private Patient patient2;
    private Patient savedPatient;



    @Mock
    private PatientRepository patientRepository;

    @Mock
    private BillingServiceGrpcClient billingServiceGrpcClient;

    @Mock
    private KafkaProducer kafkaProducer;

    @InjectMocks
    private PatientService patientService;

    @BeforeEach
    void setup(){
        this.patientResponseDto = new  PatientResponseDto(
                "2893e7067-e89b-12d3-a456-422314174005",
                "test user",
                "2026-06-15",
                "test@gmail.com",
                "Banjul"
                );

        this.patientRequestDto = new PatientRequestDto(
                "test user",
                "Banjul",
                "2026-06-15",
                "test@gmail.com",
                "2026-06-15"
                );

        this.patient1 = new  Patient(
                LocalDate.parse("2026-06-09"),
                LocalDate.parse("2026-06-15"),
                "Banjul",
                "john@example.com",
                "John Doe",
                UUID.randomUUID()
        );



        this.savedPatient = new Patient(
                LocalDate.parse("2026-06-09"),
                LocalDate.parse("2026-06-15"),
                "Banjul",
                "john@example.com",
                "John Doe",
                UUID.randomUUID()
        );
    }

    @Nested
    class GetPatients {
        @DisplayName("should return all patients")
        @Test
        void shouldGetAllPatients(){
//        when
            when(patientRepository.findAll())
                    .thenReturn(List.of(patient1));

            List<PatientResponseDto> result = patientService.getPatients();

//        then
            assertNotNull(result);
            assertEquals(1,result.size());

            assertEquals("john@example.com", result.get(0).getEmail());
            assertEquals("John Doe", result.get(0).getName());

            verify(patientRepository).findAll();


        }

        @DisplayName("should an empty list if no patient exists")
        @Test
        void shouldReturnEmptyListIfNoPatientExists(){

            when(patientRepository.findAll())
                    .thenReturn(Collections.emptyList());

            List <PatientResponseDto> result = patientService.getPatients();

            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(patientRepository).findAll();
        }
    }

    @Nested
    class createPatient{
        @DisplayName("Should create a patient successfully")
        @Test
        void shouldCreatePatientSuccessfully(){
//            given

            when(patientRepository.existsByEmail(patientRequestDto.getEmail()))
                    .thenReturn(false);
            when(patientRepository.save(any(Patient.class)))
                    .thenReturn(savedPatient);

//            when
            PatientResponseDto results = patientService.createPatient(patientRequestDto);


//            then
            assertNotNull(results);
            assertEquals(savedPatient.getEmail(),results.getEmail());
            verify(patientRepository, times(1)).existsByEmail(patientResponseDto.getEmail());
            verify(patientRepository).save(argThat(patient -> patient.getEmail().equals("test@gmail.com")));
            verify(billingServiceGrpcClient).createBillingAccount(savedPatient.getId().toString(),savedPatient.getName(),savedPatient.getEmail());
            verify(kafkaProducer).sendEvent(savedPatient);
        }

        @DisplayName("should throw emailAlreadyExistsException when email exists")
        @Test
        void shouldThrowEmailAlreadyExistsExceptionWhenEmailExist(){
            when(patientRepository.existsByEmail(patientRequestDto.getEmail()))
                    .thenReturn(Boolean.TRUE);


            final EmailAlreadyExistException ex = assertThrows(EmailAlreadyExistException.class,
                    ()->patientService.createPatient(patientRequestDto));

            assertEquals("A patient with this email already exist " + patientRequestDto.getEmail(), ex.getMessage());
            verify(patientRepository, times(1)).existsByEmail(patientRequestDto.getEmail());

        }

    }

    @Nested
    class UpdatePatient{
        @Test
        @DisplayName("should update a patient successfully")
        void shouldUpdatePatientSuccessfully(){



            when(patientRepository.findById(savedPatient.getId()))
                    .thenReturn(Optional.of(savedPatient));
            when(patientRepository.existsByEmailAndIdNot(patientRequestDto.getEmail(), savedPatient.getId()))
                    .thenReturn(false);
            when(patientRepository.save(any(Patient.class)))
                    .thenReturn(savedPatient);

            PatientResponseDto result = patientService.updatePatient(savedPatient.getId(),patientRequestDto);

            assertEquals(savedPatient.getId(), UUID.fromString(result.getId()));
            verify(patientRepository, times(1)).save(savedPatient);


        }
        @Test
        @DisplayName("should Throw PatientNotFoundException when patient not found")
        void shouldThrowPatientNotFoundExceptionWhenPatientNotFound(){
            when(patientRepository.findById(savedPatient.getId()))
                    .thenReturn(Optional.empty());

            final PatientNotFoundException ex = assertThrows(PatientNotFoundException.class,
                    ()->patientService.updatePatient(savedPatient.getId(),patientRequestDto));

            verify(patientRepository,times(1)).findById(savedPatient.getId());
            assertEquals("Patient not found:" + savedPatient.getId(), ex.getMessage());
        }

        @Test
        @DisplayName("should Throw EmailAlreadyExistException if email belongs to another patient")
        void shouldThrowEmailAlreadyExistExceptionIfEmailBelongsToAnotherPatient(){
            when(patientRepository.findById(savedPatient.getId()))
                    .thenReturn(Optional.of(savedPatient));

            when(patientRepository.existsByEmailAndIdNot(patientRequestDto.getEmail(), savedPatient.getId()))
                    .thenReturn(true);

            final EmailAlreadyExistException ex = assertThrows(EmailAlreadyExistException.class, ()->
                    patientService.updatePatient( savedPatient.getId(), patientRequestDto));

            assertEquals("A patient with this email already exist " + patientRequestDto.getEmail(), ex.getMessage());
            verify(patientRepository,times(1)).findById(savedPatient.getId());
            verify(patientRepository,times(1)).existsByEmailAndIdNot(patientResponseDto.getEmail(),savedPatient.getId());
            verify(patientRepository, never()).save(savedPatient);
        }
    }
    @Nested
    class deletePatient{
        @Test
        @DisplayName("should delete a patient successfully")
        void shouldDeletePatientSuccessfully(){
            when(patientRepository.existsById(savedPatient.getId()))
                    .thenReturn(true);
            patientService.deletePatient(savedPatient.getId());

            verify(patientRepository, times(1)).deleteById(savedPatient.getId());
        }

        @Test
        @DisplayName("should delete a patient if patient no found")
        void shouldThrowPatientNotFoundExceptionIfPatientDoesNotExist(){
            when(patientRepository.existsById(savedPatient.getId()))
                    .thenReturn(false);
            PatientNotFoundException ex = assertThrows(PatientNotFoundException.class, ()->
                    patientService.deletePatient(savedPatient.getId()));

            assertEquals("Patient not found:" + savedPatient.getId(), ex.getMessage());


        }

    }

    @Nested
    class GetPatient{
        @Test
        @DisplayName("should fetch a patient successfully")
        void shouldFetchPatientSuccessfully(){
            when(patientRepository.findById(savedPatient.getId()))
                    .thenReturn(Optional.of(savedPatient));
        PatientResponseDto patient = patientService.getPatient(savedPatient.getId());

        assertEquals(savedPatient.getId(),UUID.fromString(patient.getId()));
        verify(patientRepository,times(1)).findById(savedPatient.getId());
        }

        @Test
        @DisplayName("should throw PatientNotFoundException if patient no found")
        void shouldThrowPatientNotFoundExceptionIfPatientDoesNotExist(){
            when(patientRepository.findById(savedPatient.getId()))
                    .thenReturn(Optional.empty());
            PatientNotFoundException ex = assertThrows(PatientNotFoundException.class, ()->
                    patientService.getPatient(savedPatient.getId()));

            assertEquals("Patient not found:" + savedPatient.getId(), ex.getMessage());
        }

    }


}
