package gukjin.querydsl;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import gukjin.querydsl.dto.MemberDto;
import gukjin.querydsl.dto.UserDto;
import gukjin.querydsl.entity.Member;
import gukjin.querydsl.entity.QMember;
import gukjin.querydsl.entity.QTeam;
import gukjin.querydsl.entity.Team;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;
import java.util.ArrayList;
import java.util.List;

import static gukjin.querydsl.entity.QMember.member;
import static gukjin.querydsl.entity.QTeam.team;
import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@Transactional
@Rollback(value = false)
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
        assertThat(result.getUsername()).isEqualTo("member1");
    }

    @Test
    public void querydslTest() throws Exception {
        QMember m = member;
        Member findMember = queryFactory.selectFrom(m)
                .where(m.username.eq("member1"))
                .fetchOne();
        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void qtypeTest() throws Exception {
        //given
        QMember m = new QMember("m");
        List<Member> member = queryFactory.select(m)
                .from(m)
                .fetch();

    }


    @Test
    public void queryTest() throws Exception {
        List<Member> members = queryFactory
                .selectFrom(member)
                .where(
                        member.username.eq("member1").and(member.age.eq(10)))
                /*.where(
                        member.username.eq("member1"),
                        member.age.eq(10) // where() 안에 Predicate...이므로 이런 식을 넘겨도 AND로 연산한다
                )*/
                .fetch();
    }

    @Test
    public void fetchsTest() throws Exception {
        //given
        List<Member> fetch = queryFactory.selectFrom(member).fetch();
        queryFactory.selectFrom(member).fetchOne();
        queryFactory.selectFrom(member).fetchAll();
        queryFactory.selectFrom(member).fetchFirst();
        queryFactory.selectFrom(member).fetchCount();
    }

    /**
     * 회원 정렬 순서
     * 1. 나이 내림차순(desc)
     * 2. 이름 올림차순(asc)
     * 단 2에서 이름이 없으면 마지막에 출력 (nulls last)
     */
    @Test
    public void sortTest() throws Exception {
        em.persist(new Member(null, 100));
        em.persist(new Member("member5", 100));
        em.persist(new Member("member6", 100));

        List<Member> members = queryFactory.selectFrom(member)
                .where(member.age.eq(100))
                .orderBy(member.age.desc(), member.username.asc().nullsLast())
                .fetch();

        assertThat(members.get(0).getUsername()).isEqualTo("member5");
        assertThat(members.get(1).getUsername()).isEqualTo("member6");
        assertThat(members.get(2).getUsername()).isNull();
    }

    @Test
    public void pagingTest() throws Exception {
        QueryResults<Member> result = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)
                .limit(2)
                .fetchResults();

        assertThat(result.getTotal()).isEqualTo(4); // select count(*) 쿼리
        assertThat(result.getResults().size()).isEqualTo(2);
        assertThat(result.getResults().get(0).getUsername()).isEqualTo("member3");
    }

    @Test
    public void aggregation() throws Exception {
        List<Tuple> tuples = queryFactory
                .select(
                        member.count(),
                        member.age.min(),
                        member.age.max(),
                        member.age.avg()
                )
                .from(member)
                .fetch();

        for (Tuple tuple : tuples) {
            System.out.println(tuple); // [4, 10, 40, 22.5]
        }
    }

    /**
     * 각 팀의 평균 연령이 20살이 넘는 팀과, 그 팀의 평균 연령 구하기
     * 아스날 13.5
     * 첼시	31.5 // 첼시만 출력되야 함
     */
    @Test
    public void groupByTest() throws Exception {
        List<Tuple> tuples = queryFactory.select(team.name, member.age.avg())
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
                .having(member.age.avg().gt(20))
                .fetch();

        assertThat(tuples.get(0).get(team.name)).isEqualTo("첼시");

    }

    /**
     * 아스날 팀 소속인 모든 회원 검색
     */
    @Test
    public void joinTest() throws Exception {
        List<Member> 아스날 = queryFactory.select(member)
                .from(member)
                .join(member.team, team)
                .where(team.name.eq("아스날"))
                .fetch();

        assertThat(아스날)
                .extracting("username")
                .containsExactly("member1", "member2");
    }

    /**
     * 회원의 이름이 팀 이름과 같은 회원 조회 (말도 안되는 예제)
     */
    @Test
    public void thetaJoinTest() throws Exception {
        em.persist(new Member("아스날"));
        em.persist(new Member("첼시"));

        List<Member> result = queryFactory.select(member)
                .from(member, team)
                .where(member.username.eq(team.name))
                .fetch();

        for (Member member1 : result) {
            System.out.println("member1 = " + member1);
        }

        assertThat(result)
                .extracting("username")
                .containsExactly("아스날", "첼시");
    }

    /**
     * 회원과 팀을 조회하면서, 팀 이름이 "아스날"인 팀만 조인, 회원은 모두 조회
     * jpql : select m, t from Member m left join m.team t on t.name = "아스날"
     */
    @Test
    public void joinOnFiltering() throws Exception {

        List<Tuple> 아스날 = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(member.team, team)
                .on(team.name.eq("아스날"))
                .fetch();

        for (Tuple tuple : 아스날) {
            System.out.println("tuple = " + tuple);
        }

    }

    @Test
    public void joinOnNoRelation() throws Exception {
        em.persist(new Member("아스날"));
        em.persist(new Member("첼시"));

        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .join(team).on(member.username.eq(team.name))
                // .join(member.team, team).on(member.username.eq(team.name)) 와의 차이 -> on절에서 id 조인 조건이 없음
                .fetch();

        for (Tuple member : result) {
            System.out.println("member1 = " + member);
        }
    }


    @PersistenceUnit EntityManagerFactory emf;
    @Test
    public void fetchJoinNo() throws Exception {
        em.flush();
        em.clear();

        Member findMember = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1"))
                .fetchOne();

        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam()); // 프록시 객체가 로딩됬는지 확인
        assertThat(loaded).as("페치 조인 미적용").isFalse();
    }

    @Test
    public void fetchJoinUse() throws Exception {
        em.flush();
        em.clear();

        Member findMember = queryFactory
                .selectFrom(member)
                .join(member.team, team).fetchJoin()
                .where(member.username.eq("member1"))
                .fetchOne();

        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam()); // 프록시 객체가 로딩됬는지 확인
        assertThat(loaded).as("페치 조인 적용").isTrue();

    }

    /**
     * 나이가 평균 이상인 멤버 조회
     */
    @Test
    public void subqueryTest() throws Exception {

        QMember subMember = new QMember("subMember"); // 서브쿼리 alias 충돌 방지하기 위해 새 QMember를 사용

        List<Member> result = queryFactory
                .select(member)
                .from(member)
                .where(member.age.gt(
                        JPAExpressions
                                .select(subMember.age.avg())
                                .from(subMember)
                ))
                .fetch();

        assertThat(result.get(0).getAge()).isGreaterThan(20);

    }

    @Test
    public void caseTest() throws Exception {
        List<Tuple> results = queryFactory.select(member, member.age
                        .when(10).then("열 살")
                        .when(20).then("스무 살")
                        .when(30).then("서른 살")
                        .when(40).then("마흔 살")
                        .otherwise("십의 단위로 안 끊어짐"))
                .from(member).fetch();

        for (Tuple result : results) {
            System.out.println("result = " + result);
        }
    }

    @Test
    public void complexCaseTest() throws Exception {
        List<Tuple> results = queryFactory
                .select(member, new CaseBuilder()
                        .when(member.age.between(10, 19)).then("10대")
                        .when(member.age.between(20, 29)).then("20대")
                        .otherwise("30대 이상"))
                .from(member)
                .fetch();

        for (Tuple result : results) {
            System.out.println("results = " + result);
        }

    }

    @Test
    public void constants() throws Exception {
        List<Tuple> 상수 = queryFactory
                .select(member, Expressions.constant("상수"))
                .from(member)
                .fetch();
        // 실제 sql에서는 상수 없이 가져옴. jqpl
        for (Tuple tuple : 상수) {
            System.out.println("tuple = " + tuple);
        }
    }

    @Test
    public void concat() throws Exception {
        // username_age처럼 concat 하고 싶다
        // queryFactory.select(member.username.concat("_").concat(member.age)) // 타입이 달라서 안됨
        List<String> fetch = queryFactory.select(member.username.concat("_").concat(member.age.stringValue()))
                .from(member)
                .where(member.username.like("mem%"))
                .fetch();

        for (String s : fetch) {
            System.out.println("s = " + s);
        }
    }

    @Test
    public void findDtoByJPQL() throws Exception {
        List<MemberDto> member = em.createQuery("select new gukjin.querydsl.dto.MemberDto(m.id, m.username) from Member m", MemberDto.class)
                .getResultList();

        for (MemberDto memberDto : member) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    @Test
    public void findDtoByQuerydsl() throws Exception {
        List<MemberDto> results = queryFactory
                .select(Projections.fields(MemberDto.class,
                        member.id,
                        member.username))
                .from(member)
                .fetch();

        for (MemberDto result : results) {
            System.out.println("result = " + result);
        }
    }

    @Test
    public void findDtoByQuerydslNoMatching() throws Exception {
QMember subMember = new QMember("subMember");
List<UserDto> results = queryFactory
    .select(Projections.fields(UserDto.class,
        member.id,
        member.username.as("name"),
        ExpressionUtils.as(JPAExpressions
            .select(subMember.age.max())
            .from(subMember), "age")
        ))
    .from(member)
    .fetch();

        for (UserDto result : results) {
            System.out.println("result = " + result);
        }
    }



}