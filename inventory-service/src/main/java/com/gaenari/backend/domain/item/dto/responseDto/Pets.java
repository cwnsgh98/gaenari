package com.gaenari.backend.domain.item.dto.responseDto;

import com.gaenari.backend.domain.item.entity.Category;
import com.gaenari.backend.domain.item.entity.Tier;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Pets {
    private Long id;
    private String name;
    private Long affection;
    private Tier tier;
    private Boolean isPartner;
    private int price;
}
