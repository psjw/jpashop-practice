package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.ordersimpequery.OrderSimpleQueryDto;
import jpabook.jpashop.repository.ordersimpequery.OrderSimpleQueryRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * xToOne(ManyToOne, OneToOne)
 * Order
 * Order -> Member
 * Order -> Delivery
 */
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {
    private final OrderRepository orderRepository;
    private final OrderSimpleQueryRepository orderSimpleQueryRepository;

    @GetMapping("/api/v1/simple-orders")
    public List<Order> orderV1() {
        //Order에도 Member가 있고 Member에도 Order가 있음 -> 양방향쪽에 다 @JsonIgnore해줌
        // -> Type definition error: [simple type, class org.hibernate.proxy.pojo.bytebuddy.ByteBuddyInterceptor]
        //com.fasterxml.jackson.databind.exc.InvalidDefinitionException: No serializer found for class org.hibernate.proxy.pojo.bytebuddy.ByteBuddyInterceptor and no properties
        // discovered to create BeanSerializer (to avoid exception, disable SerializationFeature.FAIL_ON_EMPTY_BEANS) (through reference chain: java.util.ArrayList[0]->jpabook.jpashop.domain.Order["member"]->jpabook.jpashop.domain.Member$HibernateProxy$QTPGdvjX["hibernateLazyInitializer"])
        // Order의 Member가 Lazy로 설정 -> ByteBuddyInterceptor Proxy객체를 넣어 두고 조회시 SQL로 조회
        // Jackson 라이브러리가 변환시 Member가 아닌 다른 객체임 -> implementation 'com.fasterxml.jackson.datatype:jackson-datatype-hibernate5-jakarta' 사용
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for (Order order : all) {
            order.getMember().getName(); //Lazy 강제 초기화
            order.getDelivery().getAddress();
        }
        return all;
    }


    @GetMapping("/api/v2/simple-orders")
    public List<SimpleOrderDto> orderV2() {
        //ORDER 조회 -> SQL 1번 실행 -> 결과 주문수 2개
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        //N+1 -> 1(order) + 회원 N + 배송 N
        //EAGER로 바꾸면 양방향 연관관계시 예측이 안되는 쿼리들이 나감
        //JPQL에서 처음에 ORDER를 가져온 후 관련된 쿼리 다겨옴
        //ORDER가 2개 이므로 루프가 2번
        //2개 -> 루프가 2번
        return orders.stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(Collectors.toList());
    }


    //v3 vs v4 : 장단이 각각 있음
    //v3 : 재사용성이 높음 -> 데이터 변경이 가능
    //v4 : 재사용성이 안좋음 -> DTO로 조회는 데이터 변경이 안됨
    // 성능성 : v4 > v3
    @GetMapping("/api/v3/simple-orders")
    public List<SimpleOrderDto> orderV3() {
        //fetch join 사용
        List<Order> orders = orderRepository.findAllWithMemberDelivery();
        return orders.stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(Collectors.toList());
    }


    @GetMapping("/api/v4/simple-orders")
    public List<OrderSimpleQueryDto> orderV4() {
        return orderSimpleQueryRepository.findOrderDtos();
    }

    @Data
    static class SimpleOrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;

        public SimpleOrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName(); //LAZY 초기화(DB 쿼리 조회)
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress(); //LAZY 초기화(DB 쿼리 조회)
        }
    }

}
