package com.example.multitenant.config;

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.hibernate.resource.jdbc.spi.StatementInspector;
import org.springframework.stereotype.Component;

//TODO this is not the best solution,
// I don't like to rewrite the schema directly as a string
// but since there was a problem as hibernate was overriding my schema name, did not have time to check it further and made this FAST solution

@Component
public class SchemaInterceptor implements StatementInspector {
    Log log = LogFactory.getLog(SchemaInterceptor.class);

    @Override
    public String inspect(String sql) {
        String schema = TenantContext.getCurrentTenant();
        if (schema == null) {
            schema = "public";
        }

        log.info("Rewriting SQL for schema: " + schema);
        return sql.replace("public.", schema + ".");
    }
}

