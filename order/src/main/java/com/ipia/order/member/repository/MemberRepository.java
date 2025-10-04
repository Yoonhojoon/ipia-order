package com.ipia.order.member.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ipia.order.member.domain.Member;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    boolean existsByEmail(String email);

    Optional<Member> findByEmail(String email);

    List<Member> findByName(String name);
    
    // 활성 회원만 조회하는 메서드들
    List<Member> findAllByIsActiveTrue();
    
    Optional<Member> findByIdAndIsActiveTrue(Long id);
    
    Optional<Member> findByEmailAndIsActiveTrue(String email);
    
    List<Member> findByNameAndIsActiveTrue(String name);
}
