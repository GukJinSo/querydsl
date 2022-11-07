package gukjin.querydsl.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import gukjin.querydsl.dto.MemberSearchCondition;
import gukjin.querydsl.dto.MemberTeamDto;
import gukjin.querydsl.entity.Member;
import gukjin.querydsl.entity.Team;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@Transactional
@Rollback(value = false)
class MemberRepositoryTest {

    @PersistenceContext
    private EntityManager em;

    @Autowired private MemberJpaRepository memberRepository;

    @Test
    public void basicTest() throws Exception {

        Member member = new Member("GJ S");
        memberRepository.save(member);

        Member findMember = memberRepository.findById(member.getId()).get();
        assertThat(findMember.getId()).isEqualTo(member.getId());

        List<Member> all = memberRepository.findAllQuerydsl();
        assertThat(all.size()).isEqualTo(1);

        List<Member> gj_s = memberRepository.findByUsernameQuerydsl("GJ S");
        assertThat(gj_s).containsExactly(member);

    }

    @Test
    public void querydslUsingBuilder() throws Exception {
        Team teamA = new Team("아스날");
        Team teamB = new Team("첼시");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);

        MemberSearchCondition condition = new MemberSearchCondition();
        condition.setTeamName("첼시");
        condition.setAgeGoe(20);
        condition.setAgeLoe(30);

        List<MemberTeamDto> memberTeamDtos = memberRepository.searchByBuilder(condition);

        for (MemberTeamDto memberTeamDto : memberTeamDtos) {
            System.out.println("memberTeamDto = " + memberTeamDto);
        }

        assertThat(memberTeamDtos.size()).isEqualTo(1);
        assertThat(memberTeamDtos).extracting("teamName").containsExactly("첼시");
    }

    @Test
    public void querydslUsingWhereMultiParams() throws Exception {
        Team teamA = new Team("아스날");
        Team teamB = new Team("첼시");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);

        MemberSearchCondition condition = new MemberSearchCondition();
        condition.setTeamName("첼시");
        condition.setAgeGoe(20);
        condition.setAgeLoe(30);

        List<MemberTeamDto> memberTeamDtos = memberRepository.searchByWhereMultiParam(condition);

        for (MemberTeamDto memberTeamDto : memberTeamDtos) {
            System.out.println("memberTeamDto = " + memberTeamDto);
        }

        assertThat(memberTeamDtos.size()).isEqualTo(1);
        assertThat(memberTeamDtos).extracting("teamName").containsExactly("첼시");
    }

}