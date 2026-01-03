/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.orm.test.collection.map;

import java.util.HashMap;
import java.util.Map;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.testing.orm.junit.DomainModel;
import org.hibernate.testing.orm.junit.Jira;
import org.hibernate.testing.orm.junit.SessionFactory;
import org.hibernate.testing.orm.junit.SessionFactoryScope;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Gail Badner
 */
@DomainModel(
		annotatedClasses = MapElementNullBasicTest.AnEntity.class
)
@SessionFactory
public class MapElementNullBasicTest {
	@Test
	public void testUpdateAddNullValue(SessionFactoryScope scope) {
		scope.inTransaction( s -> {
			AnEntity e = new AnEntity();
			persistFlushAndClear(s, e);
			e = s.get( AnEntity.class, e.id );
			e.aCollection.put( "abc", null );
			persistFlushAndClear(s, e);
			e = s.get( AnEntity.class, e.id );
			assertEquals( 1, e.aCollection.size() );
			assertNull( e.aCollection.get( "abc" ) );
		} );
	}

	@Test
	public void testUpdateNonNullValueToNull(SessionFactoryScope scope) {
		scope.inTransaction( s -> {
			AnEntity e = new AnEntity();
			e.aCollection.put( "abc", "def" );
			persistFlushAndClear(s, e);
			e = s.get( AnEntity.class, e.id );
			e.aCollection.put( "abc", null );
			persistFlushAndClear(s, e);
			e = s.get( AnEntity.class, e.id );
			assertEquals( 1, e.aCollection.size() );
			assertTrue( e.aCollection.containsKey( "abc" ) );
			assertNull( e.aCollection.get( "abc" ) );
		} );
	}

	@Test
	public void testUpdateNonNullValueToNullToNonNull(SessionFactoryScope scope) {
		scope.inTransaction( s -> {
			AnEntity e = new AnEntity();
			e.aCollection.put( "abc", "def" );
			persistFlushAndClear(s, e);
			e = s.get( AnEntity.class, e.id );
			e.aCollection.put( "abc", null );
			persistFlushAndClear(s, e);
			e = s.get( AnEntity.class, e.id );
			assertTrue( e.aCollection.containsKey( "abc" ) );
			e.aCollection.put( "abc", "not null" );
			persistFlushAndClear(s, e);
			e = s.get( AnEntity.class, e.id );
			assertEquals( "not null", e.aCollection.get( "abc" ) );
		} );
	}

	@Test
	@Jira( "https://hibernate.atlassian.net/browse/HHH-9456" )
	public void testInsertNullValue(SessionFactoryScope scope) {
		scope.inTransaction( s -> {
			AnEntity e = new AnEntity();
			e.aCollection.put( "a", null );
			persistFlushAndClear(s, e);
			e = s.get( AnEntity.class, e.id );
			assertEquals( 1, e.aCollection.size() );
		} );
	}

	@Test
	public void testRemoveNullValue(SessionFactoryScope scope) {
		scope.inTransaction( s -> {
			AnEntity e = new AnEntity();
			e.aCollection.put( "a", null );
			e.aCollection.put( "b", "not null" );
			persistFlushAndClear(s, e);
			e = s.get( AnEntity.class, e.id );
			e.aCollection.remove( "a" );
			persistFlushAndClear(s, e);
			e = s.get( AnEntity.class, e.id );
			assertEquals( 1, e.aCollection.size() );
			assertEquals( "not null", e.aCollection.get( "b" ) );
		} );
	}

	private void persistFlushAndClear(SessionImplementor session, Object entity) {
		session.persist( entity );
		session.flush();
		session.clear();
	}

	@Entity(name = "AnEntity")
	public static class AnEntity {
		@Id
		@GeneratedValue
		private int id;
		@ElementCollection
		private Map<String, String> aCollection = new HashMap<>();
	}
}
