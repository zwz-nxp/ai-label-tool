package com.nxp.iemdm.generator;

import java.io.Serializable;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

public class ExistingIdGenerator implements IdentifierGenerator {
  @Override
  public Serializable generate(SharedSessionContractImplementor session, Object object)
      throws HibernateException {
    Object id =
        session.getEntityPersister(null, object).getClassMetadata().getIdentifier(object, session);
    return (Serializable) id;
  }
}
