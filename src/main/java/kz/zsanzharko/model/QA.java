/*
 * Copyright (c) 2023.
 */

package kz.zsanzharko.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class QA {
    private Integer questionId;
    private Double answer;
}
