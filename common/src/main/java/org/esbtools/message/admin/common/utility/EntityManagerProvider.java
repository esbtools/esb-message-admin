package org.esbtools.message.admin.common.utility;

import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceContext;

@Named
class EntityManagerProvider {
    @PersistenceContext(unitName = "EsbMessageAdminPU")
    private EntityManager entityMgr;

    private static EntityManagerFactory EMF;

    @Produces
    public EntityManager getEntityManager() {
        /* if running in container which doesn't support @PersistenceContext
         * (such as jetty), manually create the EntityManager. */
        if (entityMgr == null) {
            synchronized(EntityManagerProvider.class) {
                if (EMF == null) {
                    EMF = Persistence.createEntityManagerFactory("EsbMessageAdminPU");
                }
            }
            entityMgr = EMF.createEntityManager();
            entityMgr.getTransaction().begin();
        }
        return entityMgr;
    }

    public void closeEntityManager(@Disposes EntityManager em) {
        /* if running in a lightweight container (such as jetty), commit the
         * transaction and close the EntityManager */
        if (EMF != null) {
            em.getTransaction().commit();
            em.close();
        }
    }
}
