package jpabook.jpashop.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Setter
@Getter
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MEMBER_ID")
    private Long id;

    private String name;

    @Embedded
    private Address address;

    //JPQL을 사용시 select o from order o;  -> SQL select * from order를 우선 조회하고
    //각각 member에 대한 select쿼리가 나감
    //XToOne인경우 OneToOne, ManyToOne은 기본 fetch가 EAGER다

    @OneToMany(mappedBy = "member")
    private List<Order> orders = new ArrayList<>();



}
