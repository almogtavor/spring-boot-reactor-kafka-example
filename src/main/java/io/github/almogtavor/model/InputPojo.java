package io.github.almogtavor.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class InputPojo {
    private String id;
    private String name;
    private String age;
    private String height;
    private String job;
}