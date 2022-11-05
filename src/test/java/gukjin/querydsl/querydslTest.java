package gukjin.querydsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import gukjin.querydsl.entity.Member;
import gukjin.querydsl.entity.QMember;
import gukjin.querydsl.entity.Team;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;


@SpringBootTest
@Transactional
class querydslTest {

    @PersistenceContext
    EntityManager em;

    JPAQueryFactory queryFactory;


    @BeforeEach
    public void beforeEach() throws Exception {

        queryFactory = new JPAQueryFactory(em);

        Team teamA = new Team("아스날");
        Team teamB = new Team("첼시");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 17, teamA);
        Member member3 = new Member("member3", 23, teamB);
        Member member4 = new Member("member4", 40, teamB);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
    }

    @Test
    public void jqplTest() throws Exception {
        Member result = em.createQuery("select m from Member m where m.username = :username", Member.class)
                .setParameter("username", "member1")
                .getSingleResult();
        Assertions.assertThat(result.getUsername()).isEqualTo("member1");
    }

    @Test
    public void querydslTest() throws Exception {
        QMember m = QMember.member;
        Member findMember = queryFactory.selectFrom(m)
                .where(m.username.eq("member1"))
                .fetchOne();
        Assertions.assertThat(findMember.getUsername()).isEqualTo("member1");
    }

}