package gukjin.querydsl.controller;

import gukjin.querydsl.dto.MemberDto;
import gukjin.querydsl.dto.MemberSearchCondition;
import gukjin.querydsl.dto.MemberTeamDto;
import gukjin.querydsl.entity.Member;
import gukjin.querydsl.repository.MemberJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class MemberController {

    @Autowired
    MemberJpaRepository memberJpaRepository;

    @GetMapping("/members")
    public List<MemberTeamDto> members(MemberSearchCondition condition){
        return memberJpaRepository.searchByWhereMultiParam(condition);
    }
}
