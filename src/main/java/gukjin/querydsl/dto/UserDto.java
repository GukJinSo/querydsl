package gukjin.querydsl.dto;


import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserDto {

    private Long id;
    private String name;

    private int age;

    public UserDto(Long id, String name) {
        this.id = id;
        this.name = name;
    }
}
