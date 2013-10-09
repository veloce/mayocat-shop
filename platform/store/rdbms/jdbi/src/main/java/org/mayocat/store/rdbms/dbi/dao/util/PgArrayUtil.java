package org.mayocat.store.rdbms.dbi.dao.util;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.common.collect.Maps;

/**
 * Postgres array utils to work around limitations of the PG SQL driver.
 *
 * @version $Id$
 */
public class PgArrayUtil
{
    /**
     * Interface for classes de-serializing strings into parametrized type
     * @param <T> the type to deserialize into
     */
    public interface Deserializer<T> {
        T fromString(String string);
    }

    /**
     * Array list implementation specific for storing PG array elements.
     */
    private static class PgArrayList extends ArrayList
    {
        /**
         * How many dimensions.
         */
        int dimensionsCount = 1;
    }

    private static Map<Class<?>, Deserializer> knownDeserializers = Maps.newHashMap();

    static {
        // UUID Deserializer
        knownDeserializers.put(UUID.class, new Deserializer()
        {
            public Object fromString(String string)
            {
                return UUID.fromString(string);
            }
        });
    }

    public static <T> List<T> arrayFromString(String arrayAsString, Class<T> type)
    {
        if (knownDeserializers.containsKey(type)) {
            return arrayFromString(arrayAsString, knownDeserializers.get(type));
        }

        throw new RuntimeException(MessageFormat.format("Deserializer for type {0} is not known", type));
    }

    /**
     * This code is mostly copied from AbstractJdbc2Array#buildArrayList, the only difference is that it receives
     * a function that transforms a string to the target type T.
     *
     * This is an artifice to work around the fact that the Postgres JDBC driver does not know how to map arrays of non
     * standard types. See  https://github.com/pgjdbc/pgjdbc/issues/63
     *
     * Copyright (c) 2004-2011, PostgreSQL Global Development Group
     *
     * @param arrayAsString the array as a raw string
     * @param elementDeserializer the deserializer that transforms an element string to a target type T object
     * @param <T> the target parameter type of the array to build. Example: UUID from an array of UUID
     * @return the constructed array
     */
    public static <T> List<T> arrayFromString(String arrayAsString, Deserializer<T> elementDeserializer)
    {
        PgArrayList arrayList = new PgArrayList();
        char delim = ',';
        String fieldString = arrayAsString;
        if (fieldString != null)
        {

            char[] chars = fieldString.toCharArray();
            StringBuffer buffer = null;
            boolean insideString = false;
            boolean wasInsideString = false; // needed for checking if NULL
            // value occured
            List dims = new ArrayList(); // array dimension arrays
            PgArrayList curArray = arrayList; // currently processed array

            // Starting with 8.0 non-standard (beginning index
            // isn't 1) bounds the dimensions are returned in the
            // data formatted like so "[0:3]={0,1,2,3,4}".
            // Older versions simply do not return the bounds.
            //
            // Right now we ignore these bounds, but we could
            // consider allowing these index values to be used
            // even though the JDBC spec says 1 is the first
            // index. I'm not sure what a client would like
            // to see, so we just retain the old behavior.
            int startOffset = 0;
            {
                if (chars[0] == '[')
                {
                    while (chars[startOffset] != '=')
                    {
                        startOffset++;
                    }
                    startOffset++; // skip =
                }
            }

            for (int i = startOffset; i < chars.length; i++)
            {

                // escape character that we need to skip
                if (chars[i] == '\\')
                    i++;

                    // subarray start
                else if (!insideString && chars[i] == '{')
                {
                    if (dims.size() == 0)
                    {
                        dims.add(arrayList);
                    }
                    else
                    {
                        PgArrayList a = new PgArrayList();
                        PgArrayList p = ((PgArrayList) dims.get(dims.size() - 1));
                        p.add(a);
                        dims.add(a);
                    }
                    curArray = (PgArrayList) dims.get(dims.size() - 1);

                    // number of dimensions
                    {
                        for (int t = i + 1; t < chars.length; t++) {
                            if (Character.isWhitespace(chars[t])) continue;
                            else if (chars[t] == '{') curArray.dimensionsCount++;
                            else break;
                        }
                    }

                    buffer = new StringBuffer();
                    continue;
                }

                // quoted element
                else if (chars[i] == '"')
                {
                    insideString = !insideString;
                    wasInsideString = true;
                    continue;
                }

                // white space
                else if (!insideString && Character.isWhitespace(chars[i]))
                {
                    continue;
                }

                // array end or element end
                else if ((!insideString && (chars[i] == delim || chars[i] == '}')) || i == chars.length - 1)
                {

                    // when character that is a part of array element
                    if (chars[i] != '"' && chars[i] != '}' && chars[i] != delim && buffer != null)
                    {
                        buffer.append(chars[i]);
                    }

                    String b = buffer == null ? null : buffer.toString();
                    T fromString = elementDeserializer.fromString(b);

                    // add element to current array
                    if (b != null && (b.length() > 0 || wasInsideString))
                    {
                        curArray.add(!wasInsideString && b.equals("NULL") ? null : fromString);
                    }

                    wasInsideString = false;
                    buffer = new StringBuffer();

                    // when end of an array
                    if (chars[i] == '}')
                    {
                        dims.remove(dims.size() - 1);

                        // when multi-dimension
                        if (dims.size() > 0)
                        {
                            curArray = (PgArrayList) dims.get(dims.size() - 1);
                        }

                        buffer = null;
                    }

                    continue;
                }

                if (buffer != null)
                    buffer.append(chars[i]);
            }
        }

        return new ArrayList<T>(arrayList);
    }

}
