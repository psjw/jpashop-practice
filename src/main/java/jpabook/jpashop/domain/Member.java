package jpabook.jpashop.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Setter
@Getter
public class Member {

    @Id
    @GeneratedValue
    @Column(name = "MEMBER_ID")
    private Long id;

    @NotEmpty
    private String name;

    @Embedded
    private Address address;

    //JPQL을 사용시 select o from order o;  -> SQL select * from order를 우선 조회하고
    //각각 member에 대한 select쿼리가 나감
    //XToOne인경우 OneToOne, ManyToOne은 기본 fetch가 EAGER다

    @JsonIgnore
    @OneToMany(mappedBy = "member")
    private List<Order> orders = new ArrayList<>();



}
