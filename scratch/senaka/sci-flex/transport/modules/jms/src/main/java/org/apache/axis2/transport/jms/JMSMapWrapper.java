/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.axis2.transport.jms;

import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.MessageFormatException;
import javax.jms.MessageNotWriteableException;

/*
 * Most logic was borrowed from Apache Harmony or extended based
 * on Apache Harmony source code - Senaka.
 */

/**
 * Wrapper to make it possible to represent a {@link MapMessage} as
 * a {@link Map}. This class implements {@link Map}. This supports
 * on-demand creation of Map, and also the type of {@link Map} can
 * be decided as well.
 */

public class JMSMapWrapper implements Map {
    private final MapMessage mapMessage;

    private int entries = -1;

    private Collection<Object> valuesCollection = null;

    private AbstractSet<String> keySet = null;

    /** 
     * Construct a new JMSMapWrapper object utilizing the passed down
     * {@link MapMessage}.
     * 
     * @param mapMessage the Map Message passed as a parameter to the
     * constructor.
     */
    public JMSMapWrapper(MapMessage mapMessage) {
        this.mapMessage = mapMessage;
    }

    /**
     * Returns object wrapped by this wrapper.
     *
     * @return wrapped {@link MapMessage}.
     */
    public MapMessage getWrappedObject() {
        return mapMessage;
    }

    /**
     * Removes all elements from this Map, leaving it empty.
     * 
     * @throws UnsupportedOperationException
     *                when removing from this Map is not supported
     * 
     * @see #isEmpty
     * @see #size
     */
    public void clear() {
        try {
            mapMessage.clearBody();
            entries = -1;
        } catch (JMSException e) {
            // A JMSException is thrown only if the operation is not
            // supported. Therefore, logically it is correct to
            // capture the JMSException and throw a UnsupportedOperationException
            throw new UnsupportedOperationException(e);
        }
    }

    /**
     * Searches this Map for the specified key.
     * 
     * @param key the object to search for
     * @return true if <code>key</code> is a key of this Map, false otherwise
     * @throws ClassCastException   if the key is of an inappropriate type
     * @throws NullPointerException if the specified key is null
     */
    public boolean containsKey(Object key) {
        if (key == null) {
            throw new NullPointerException();
        }
        boolean itemExists = false;
        try {
            if (key instanceof String) {
                itemExists = mapMessage.itemExists((String)key);
            } else {
                throw new ClassCastException("This Map can only handle key's of type String");
            }
        } catch (JMSException e) { }
        return itemExists;
    }

    /**
     * Searches this Map for the specified value.
     * 
     * @param value the object to search for
     * @return true if <code>value</code> is a value of this Map, false
     *         otherwise
     * @throws NullPointerException if the specified value is null
     */
    public boolean containsValue(Object value) {
        if (value == null) {
            throw new NullPointerException();
        }
        try {
            Enumeration enumeration = mapMessage.getMapNames();
            while (enumeration.hasMoreElements()) {
                Object obj = enumeration.nextElement();
                if (obj != null && obj.equals(value)) {
                    return true;
                }
            }
        } catch (JMSException e) { }
        return false;
    }

    /**
     * Compares the argument to the receiver, and answers true if they represent
     * the <em>same</em> object using a class specific comparison.
     * 
     * @param object Object the object to compare with this object.
     * @return boolean <code>true</code> if the object is the same as this
     *         object <code>false</code> if it is different from this object.
     * @throws NullPointerException if the specified object is null
     * @see #hashCode
     */
    public boolean equals(Object object) {
        if (object == null) {
            throw new NullPointerException();
        } else if (object instanceof JMSMapWrapper && (getWrappedObject() != null)) {
            return getWrappedObject().equals(((JMSMapWrapper)object).getWrappedObject());
        }
        return false;
    }

    /**
     * Answers the value of the mapping with the specified key.
     * 
     * @param key the key
     * @return the value of the mapping with the specified key
     * @throws ClassCastException   if the key is of an inappropriate type
     * @throws NullPointerException if the specified key is null
     */
    public Object get(Object key) {
        if (containsKey(key)) {
            try {
                return mapMessage.getObject((String)key);
            } catch (JMSException e) { }
        }
        return null;
    }

    /**
     * Answers an integer hash code for the receiver. Objects which are equal
     * answer the same value for this method.
     * 
     * @return the receiver's hash
     * 
     * @see #equals
     */
    public int hashCode() {
        int result = 0;
        Iterator<Map.Entry<String, Object>> it = entrySet().iterator();
        while (it.hasNext()) {
            result += it.next().hashCode();
        }
        return result;
    }

    /**
     * Answers if this Map has no elements, a size of zero.
     * 
     * @return true if this Map has no elements, false otherwise
     * 
     * @see #size
     */
    public boolean isEmpty() {
        if (entries == -1) {
            size();
        }
        return (entries > 0);
    }

    /**
     * Answers a Set of the keys contained in this Map. The set is backed by
     * this Map so changes to one are reflected by the other. The set does not
     * support adding.
     * 
     * @return a Set of the keys
     */
    public Set<String> keySet() {
        if (keySet == null) {
            keySet = new AbstractSet<String>() {
                @Override
                public boolean contains(Object object) {
                    return containsKey(object);
                }

                @Override
                public int size() {
                    return JMSMapWrapper.this.size();
                }

                @Override
                public void clear() {
                    JMSMapWrapper.this.clear();
                }

                @Override
                public boolean remove(Object object) {
                    // We don't allow removing of objects
                    return false;
                }

                @Override
                public boolean retainAll(Collection<?> collection) {
                    // We don't allow removing of objects
                    return false;
                }

                @Override
                public Iterator<String> iterator() {
                    return new KeyIterator<String,Object> (JMSMapWrapper.this);
                }
            };
        }
        return keySet;
    }

    /**
     * Maps the specified key to the specified value.
     * 
     * @param key the key
     * @param value the value
     * @return the value of any previous mapping with the specified key or null
     *         if there was no mapping
     * 
     * @exception UnsupportedOperationException
     *                when adding to this Map is not supported
     * @exception ClassCastException
     *                when the class of the key or value is inappropriate for
     *                this Map
     * @exception IllegalArgumentException
     *                when the key or value cannot be added to this Map
     * @exception NullPointerException
     *                when the key or value is null and this Map does not
     *                support null keys or values
     */
    public Object put(Object key, Object value) {
        if (!(key instanceof String)) {
            throw new ClassCastException("This Map can only handle key's of type String");
        }
        Object ret = null;
        try {
            ret = get(key);
            if (ret != null) {
                throw new IllegalArgumentException();
            }
            mapMessage.setObject((String)key, value);
        } catch (MessageNotWriteableException e) {
            throw new UnsupportedOperationException(e);
        } catch (MessageFormatException e) {
            throw new IllegalArgumentException(e);
        } catch (JMSException e) {
            throw new UnsupportedOperationException(e);
        }
        if (entries == -1) {
            size();
        }
        entries += 1;
        return ret;
    }

    /**
     * Copies every mapping in the specified Map to this Map.
     * 
     * @param map the Map to copy mappings from
     * 
     * @exception UnsupportedOperationException
     *                when adding to this Map is not supported
     * @exception ClassCastException
     *                when the class of a key or value is inappropriate for this
     * @exception IllegalArgumentException
     *                when a key or value cannot be added to this Map
     * @exception NullPointerException
     *                when a key or value is null and this Map does not support
     *                null keys or values
     */
    public void putAll(Map map) {
        if (!map.isEmpty()) {
            for (Object object : map.entrySet()) {
               Map.Entry<String, Object> entry = (Map.Entry<String, Object>) object;
               put(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Removes a mapping with the specified key from this Map.
     * 
     * @param key the key of the mapping to remove
     * @return the value of the removed mapping or null if key is not a key in
     *         this Map
     * 
     * @exception UnsupportedOperationException
     *                when removing from this Map is not supported
     */
    public Object remove(Object key) {
        throw new UnsupportedOperationException();
    }

    /**
     * Answers the number of elements in this Map.
     * 
     * @return the number of elements in this Map
     */
    public int size() {
        if (entries != -1) {
            return entries;
        } else {
            try {
                // If you ran size once, you never should come here
                // again unless someone cleared something on the Map
                // Message.
                entries = 0;
                Enumeration enumeration = mapMessage.getMapNames();
                while (enumeration.hasMoreElements()) {
                    entries += 1;
                    enumeration.nextElement();
                }
            } catch (JMSException e) { }
        }
        return entries;
    }

    private static class MapEntry<K, V> implements Map.Entry<K, V>, Cloneable {

        private K key;
        private V value;

        public MapEntry(K theKey, V theValue) {
            key = theKey;
            value = theValue;
        }

        @Override
        public Object clone() {
            try {
                return super.clone();
            } catch (CloneNotSupportedException e) {
                return null;
            }
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object instanceof Map.Entry) {
                Map.Entry<?, ?> entry = (Map.Entry<?, ?>) object;
                return (key == null ? entry.getKey() == null : key.equals(entry
                    .getKey()))
                    && (value == null ? entry.getValue() == null : value
                    .equals(entry.getValue()));
            }
            return false;
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }

        @Override
        public int hashCode() {
            return (key == null ? 0 : key.hashCode())
                ^ (value == null ? 0 : value.hashCode());
        }

        public V setValue(V object) {
            V result = value;
            value = object;
            return result;
        }

        @Override
        public String toString() {
            return key + "=" + value;
        }
    }


    private static class BaseMapIterator<K, V> {

        private final JMSMapWrapper map;
        private final Enumeration enumeration;

        public BaseMapIterator(JMSMapWrapper map) {
            this.map = map;
            Enumeration en = null;
            try {
                if (map != null && map.getWrappedObject() != null) {
                    en = map.getWrappedObject().getMapNames();
                }
            } catch (JMSException e) { }
            enumeration = en;
        }

        public final Map.Entry<K, V> getNext() {
            if (hasNext()) {
                K key = (K) enumeration.nextElement();
                V value = (V) map.get((String)key);
                return new MapEntry<K, V>(key, value); 
            }
            throw new NoSuchElementException();
        }

        public final void remove() {
            // We don't allow removing of objects
        }

        public boolean hasNext() {
            return (enumeration != null && enumeration.hasMoreElements());
        }
    }

    private static class EntryIterator <K, V> extends BaseMapIterator<K, V> implements Iterator<Map.Entry<K, V>> {

        EntryIterator (JMSMapWrapper map) {
            super(map);
        }

        public Map.Entry<K, V> next() {
            return getNext();
        }
    }

    private static class KeyIterator <K, V> extends BaseMapIterator<K, V> implements Iterator<K> {

        KeyIterator (JMSMapWrapper map) {
            super(map);
        }

        public K next() {
            return getNext().getKey();
        }
    }

    private static class ValueIterator <K, V> extends BaseMapIterator<K, V> implements Iterator<V> {

        ValueIterator (JMSMapWrapper map) {
            super(map);
        }

        public V next() {
            return getNext().getValue();
        }
    }

    /**
     * Returns all of the current <code>Map</code> values in a
     * <code>Collection</code>. As the returned <code>Collection</code> is
     * backed by this <code>Map</code>, users should be aware that changes in
     * one will be immediately visible in the other.
     * 
     * @return a Collection of the values
     */
    public Collection<Object> values() {
        if (valuesCollection == null) {
            valuesCollection = new AbstractCollection<Object>() {
                @Override
                public boolean contains(Object object) {
                    return containsValue(object);
                }

                @Override
                public int size() {
                    return JMSMapWrapper.this.size();
                }

                @Override
                public void clear() {
                    JMSMapWrapper.this.clear();
                }

                @Override
                public Iterator<Object> iterator() {
                    return new ValueIterator<String,Object> (JMSMapWrapper.this);
                }
            };
        }
        return valuesCollection;
    }

    private static class EntrySet <KT, VT> extends AbstractSet<Map.Entry<KT, VT>> {
        private final JMSMapWrapper map;

        public EntrySet(JMSMapWrapper map) {
            this.map = map;
        }

        @Override
        public int size() {
            return map.size();
        }

        @Override
        public void clear() {
            map.clear();
        }

        @Override
        public boolean remove(Object object) {
            // We don't allow removing of objects
            return false;
        }

        @Override
        public boolean retainAll(Collection<?> collection) {
            // We don't allow removing of objects
            return false;
        }

        @Override
        public boolean contains(Object object) {
            if (object instanceof Map.Entry) {
                Map.Entry<?, ?> entry = (Map.Entry<?, ?>) object;
                Object value = map.get(entry.getKey());
                return (value == null) ? (entry.getValue() == null) :
                    value.equals(entry.getValue());
            }
            return false;
        }

        @Override
        public Iterator<Map.Entry<KT, VT>> iterator() {
            return new EntryIterator<KT,VT> (map);
        }
    }

    /**
     * Returns a <code>Set</code> whose elements comprise all of the mappings
     * that are to be found in this <code>Map</code>. Information on each of
     * the mappings is encapsulated in a separate {@link Map.Entry} instance. As
     * the <code>Set</code> is backed by this <code>Map</code>, users
     * should be aware that changes in one will be immediately visible in the
     * other.
     * 
     * @return a <code>Set</code> of the mappings
     */
    public Set<Map.Entry<String, Object>> entrySet() {
        return new EntrySet(this);
    }
}
