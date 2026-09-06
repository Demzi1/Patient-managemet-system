package com.pms.patientservice.dto;


import com.pms.patientservice.dto.validators.createPatientValidatorGroup;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;



public class PatientRequestDto {
    @NotBlank(message = "name is required")
    @Size(message = "name cannot exceed 100 characters")
    private String name;
    @NotBlank(message = "Address is required")
    private String address;
    @NotBlank(message = "Date of birth is required")
    private String dateOfBirth;
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank( groups = createPatientValidatorGroup.class, message = "Registered date is required")
    private String registeredDate;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRegisteredDate() {
        return registeredDate;
    }

    public PatientRequestDto(String name, String address, String dateOfBirth, String email, String registeredDate) {
        this.name = name;
        this.address = address;
        this.dateOfBirth = dateOfBirth;
        this.email = email;
        this.registeredDate = registeredDate;
    }

    public void setRegisteredDate(String registeredDate) {
        this.registeredDate = registeredDate;
    }
}
