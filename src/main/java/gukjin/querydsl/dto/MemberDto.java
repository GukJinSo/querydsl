package gukjin.querydsl.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//@Data
@NoArgsConstructor
@Setter
@Getter
public class MemberDto {

    private Long id;
    private String username;

    @QueryProjection
    public MemberDto(Long id, String username) {
        this.id = id;
        this.username = username;
    }

    @Override
    public String toString() {
        return "MemberDto{" +
                "id=" + id +
                ", username='" + username + '\'' +
                '}';
    }
}
