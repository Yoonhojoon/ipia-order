package com.ipia.order.member.service;

import com.ipia.order.member.domain.Member;
import com.ipia.order.member.repository.MemberRepostiory;
import org.springframework.stereotype.Service;

@Service
public class MemberServiceImpl implements MemberService {

    private MemberRepostiory memberRepostiory;

    public MemberServiceImpl(MemberRepostiory memberRepostiory) {
        this.memberRepostiory = memberRepostiory;
    }

    @Override
    public Member signup(String name, String email) {
        // TODO: 구현 예정
        throw new UnsupportedOperationException("아직 구현되지 않았습니다.");
    }
}
