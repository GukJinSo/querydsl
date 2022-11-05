package gukjin.querydsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import gukjin.querydsl.entity.Member;
import gukjin.querydsl.entity.QMember;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Rollback(value = false)
class QuerydslApplicationTests {

	@PersistenceContext
	EntityManager em;

	@Test
	void contextLoads() {

		Member member = new Member();
		em.persist(member);
		JPAQueryFactory query = new JPAQueryFactory(em);
		QMember qMember = new QMember("h");

		Member findMember = query.selectFrom(qMember)
				.fetchOne();

		assertThat(findMember).isEqualTo(member);
		assertThat(findMember.getId()).isEqualTo(member.getId());
	}

}
