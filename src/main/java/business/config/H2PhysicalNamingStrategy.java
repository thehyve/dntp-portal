package business.config;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.springframework.boot.orm.jpa.hibernate.SpringPhysicalNamingStrategy;

/**
 * Workaround naming strategy to choose a valid column name
 * for fields named "values" in H2 databases.
 */
public class H2PhysicalNamingStrategy extends SpringPhysicalNamingStrategy {

    @Override
    protected Identifier getIdentifier(String name, boolean quoted, JdbcEnvironment jdbcEnvironment) {
        if ("values".equals(name)) {
            return super.getIdentifier("value", quoted, jdbcEnvironment);
        }
        return super.getIdentifier(name, quoted, jdbcEnvironment);
    }

}
