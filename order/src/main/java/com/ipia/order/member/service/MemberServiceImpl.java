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
        if (id == null) {
            throw new MemberHandler(MemberErrorStatus.MEMBER_NOT_FOUND);
        }
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new MemberHandler(MemberErrorStatus.MEMBER_NOT_FOUND));
        return Optional.of(member);
    }

    @Override
    public Optional<Member> findByEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new MemberHandler(MemberErrorStatus.MEMBER_NOT_FOUND);
        }
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new MemberHandler(MemberErrorStatus.MEMBER_NOT_FOUND));
        return Optional.of(member);
    }

    @Override
    public List<Member> findAll() {
        return memberRepository.findAll();
    }

    @Override
    public List<Member> findByName(String name) {
        if (name == null || name.isBlank()) {
            throw new MemberHandler(MemberErrorStatus.MEMBER_NOT_FOUND);
        }
        return memberRepository.findByName(name);
    }

    @Override
    public Member updateNickname(Long id, String newNickname) {
        throw new UnsupportedOperationException("updateNickname is not implemented yet");
    }

    @Override
    public void updatePassword(Long id, String currentPassword, String newPassword) {
        throw new UnsupportedOperationException("updatePassword is not implemented yet");
    }
}
