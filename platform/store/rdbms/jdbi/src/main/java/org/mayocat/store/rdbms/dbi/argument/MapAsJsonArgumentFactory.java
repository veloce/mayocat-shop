package org.mayocat.store.rdbms.dbi.argument;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.Argument;
import org.skife.jdbi.v2.tweak.ArgumentFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @version $Id$
 */
public class MapAsJsonArgumentFactory implements ArgumentFactory<Map>
{
    @Override
    public boolean accepts(Class<?> expectedType, Object value, StatementContext ctx)
    {
        if (value == null) {
            return false;
        }
        return java.util.Map.class.isAssignableFrom(value.getClass());
    }

    @Override
    public Argument build(Class<?> expectedType, final Map value, StatementContext ctx)
    {
        try {
            return new Argument()
            {
                ObjectMapper mapper = new ObjectMapper();

                String json = mapper.writeValueAsString(value);

                @Override
                public void apply(int position, PreparedStatement statement, StatementContext ctx) throws SQLException
                {
                    statement.setString(position, json);
                }
            };
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert map argument to JSON", e);
        }
    }
}
