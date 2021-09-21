package com.tui.proof.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Contains the addresses details.")
@Entity
public class Address
{
    @Hidden
    @JsonIgnore
    @Id
    @GeneratedValue(generator = "address_id_seq")
    @SequenceGenerator(name = "address_id_seq", sequenceName = "address_id_seq", initialValue = 100)
    private Long id;

    @NotNull(message = "the address's street cannot be empty")
    @Size(message = "the address's street max length is 250 characters")
    @Schema(description = "The address's street.", example = "785 Cabell Avenue", maxLength = 250, required = true)
    @Column(length = 250, nullable = false)
    private String street;

    @NotNull(message = "the address's postal code cannot be empty")
    @Size(message = "the address's postal code max length is 250 characters")
    @Schema(description = "The address's postal code.", example = "23219", maxLength = 250, required = true)
    @Column(length = 250, nullable = false)
    private String postcode;

    @NotNull(message = "the address's city cannot be empty")
    @Size(message = "the address's city max length is 250 characters")
    @Schema(description = "The address's city.", example = "Raccoon", maxLength = 250, required = true)
    @Column(length = 250, nullable = false)
    private String city;

    @NotNull(message = "the address's country cannot be empty")
    @Size(message = "the address's country max length is 250 characters")
    @Schema(description = "The address's country.", example = "USA", maxLength = 250, required = true)
    @Column(length = 250, nullable = false)
    private String country;
}
