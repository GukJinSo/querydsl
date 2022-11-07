package gukjin.querydsl.config;


import gukjin.querydsl.entity.Member;
import gukjin.querydsl.entity.Team;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Profile("local")
@Component
@RequiredArgsConstructor
public class InitMember {

    private final MemberInitService memberInitService;

    @PostConstruct
    void init(){
        memberInitService.init();
    }

    @Component
    static class MemberInitService {

        @PersistenceContext
        EntityManager em;

        @Transactional
        public void init(){
            Team arsenal = new Team("아스날");
            Team chelsea = new Team("첼시");
            em.persist(arsenal);
            em.persist(chelsea);

            for (int i = 0; i < 100; i++) {
                Team selectedTeam = i % 2 == 0 ? arsenal : chelsea;
                em.persist(new Member("member" + i, i, selectedTeam));
            }

        }
    }
}
