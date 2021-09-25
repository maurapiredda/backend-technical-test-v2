package com.tui.proof.model;

import java.time.ZonedDateTime;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.validation.Valid;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Contains the order data.")
@Entity(name = "pilotes_order")
public class Order
{
    @Hidden
    @JsonIgnore
    @Id
    @GeneratedValue(generator = "order_id_seq")
    @SequenceGenerator(name = "order_id_seq", sequenceName = "order_id_seq", initialValue = 100)
    private Long id;

    @Hidden
    @JsonIgnore
    @Column(nullable = false)
    private ZonedDateTime creationDate;

    @Hidden
    @JsonIgnore
    @Column(columnDefinition = "boolean not null default false")
    private Boolean notified;

    // --- JSON exposed fields ----------------------------------------------------------------------------------------

    @Schema(description = "The number that identifies the order. It is generated by the system, therefore it is ignored in import.",
            example = "0001021648")
    @Column(length = 250, unique = true, nullable = false)
    private String orderNumber;

    @Valid
    @NotNull(message = "the order's delivery address cannot be empty")
    @Schema(description = "The order's delivery address",
            example = "{\"street\": \"785 Cabell Avenue\", \"postcode\": \"23219\", \"city\": \"Raccoon\" \"country\": \"USA\"}",
            required = true)
    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "address_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "order_address_id", value = ConstraintMode.CONSTRAINT),
            nullable = false)
    private Address deliveryAddress;

    @NotNull(message = "the order's pilotes number cannot be empty")
    @Schema(description = "The order's pilotes number", example = "FIVE", required = true)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PilotesNumber pilotesNumber;

    @Digits(message = "The order's total price can have 18 digits of which 2 are decimals", integer = 18, fraction = 2)
    @Schema(description = "The order's total price", example = "125")
    @Column(precision = 18, scale = 2, nullable = false)
    private Double total;

    @NotNull(message = "the order's customer cannot be empty")
    @Schema(description = "The order's customer",
            example = "{\"firstName\": \"Clair\", \"lastName\": \"Redfield\", \"telephone\": \"+1 7577149738\" \"email\": \"clair.redfield@terrasave.com\"}",
            required = true)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "order_customer_id", value = ConstraintMode.CONSTRAINT),
            nullable = false)
    private Customer customer;

}
