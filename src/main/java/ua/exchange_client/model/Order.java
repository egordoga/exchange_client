package ua.exchange_client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Order implements Serializable {

    private Long idd;
    private Boolean sideOfSell;
    private BigDecimal price;
    private BigDecimal size;
    private Participant participant;
    private Product product;
}
