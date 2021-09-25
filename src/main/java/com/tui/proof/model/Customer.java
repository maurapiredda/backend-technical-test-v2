package com.tui.proof.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Contains the client details.")
@Entity
public class Customer
{
    @Hidden
    @JsonIgnore
    @Id
    @GeneratedValue(generator = "client_id_seq")
    @SequenceGenerator(name = "client_id_seq", sequenceName = "address_id_seq", initialValue = 100)
    private Long id;

    @NotNull(message = "the client's first name cannot be empty")
    @Size(message = "the client's first name max length is 250 characters")
    @Schema(description = "The client's first name.", example = "Leon", maxLength = 250, required = true)
    @Column(length = 250, nullable = false)
    private String firstName;

    @NotNull(message = "the client's last name cannot be empty")
    @Size(message = "the client's last name max length is 250 characters")
    @Schema(description = "The client's last name.", example = "Kennedy", maxLength = 250, required = true)
    @Column(length = 250, nullable = false)
    private String lastName;

    @NotNull(message = "the client's phone number cannot be empty")
    @Size(message = "the client's phone number max length is 250 characters")
    @Schema(description = "The client's phone number.", example = "+1 7035555015", maxLength = 250, required = true)
    @Column(length = 250, nullable = false)
    private String telephone;

    @Email
    @NotNull(message = "the client's email cannot be empty")
    @Size(message = "the client's email max length is 250 characters")
    @Schema(description = "The client's email.", example = "leon.kennedy@rpd.com", maxLength = 250, required = true)
    @Column(unique = true, length = 250, nullable = false)
    private String email;
}
