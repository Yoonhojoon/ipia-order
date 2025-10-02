package com.ipia.order.member.service;

import com.ipia.order.member.domain.Member;
import com.ipia.order.member.repository.MemberRepository;
import com.ipia.order.common.exception.member.MemberHandler;
import com.ipia.order.common.exception.member.status.MemberErrorStatus;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;

    public MemberServiceImpl(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public Member signup(String name, String email) {
        // 이메일 중복 체크
        if (memberRepository.existsByEmail(email)) {
            throw new MemberHandler(MemberErrorStatus.MEMBER_ALREADY_EXISTS);
        }
        
        // 회원 생성 및 저장
        Member member = Member.builder()
                .name(name)
                .email(email)
                .build();
                
        return memberRepository.save(member);
    }

    @Override
    public Optional<Member> findById(Long id) {
        return memberRepository.findById(id);
    }

    @Override
    public Optional<Member> findByEmail(String email) {
        return memberRepository.findByEmail(email);
    }

    @Override
    public List<Member> findAll() {
        return memberRepository.findAll();
    }

    @Override
    public List<Member> findByName(String name) {
        return memberRepository.findByName(name);
    }
}
