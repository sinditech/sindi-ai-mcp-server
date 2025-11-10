/**
 * 
 */
package za.co.sindi.ai.mcp.server.spi.cdi;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.inject.spi.PassivationCapable;

/**
 * @author Buhake Sindi
 * @since 12 May 2025
 */
abstract class CDIBean<T> implements Bean<T>, PassivationCapable, Serializable {

	private Set<Type> types;
	private Set<Annotation> qualifiers = Set.of(new Default.Literal(), new Any.Literal());
	private Class<? extends Annotation> scope = Dependent.class;
	private String name;
	private Set<Class<? extends Annotation>> stereotypes = Collections.emptySet();
	private boolean alternative = false;
	private String id = this.getClass().getName();
	private Class<T> beanClass;
	private Set<InjectionPoint> injectionPoints = Collections.emptySet();
	private Function<CreationalContext<T>, T> producer;
	
	@Override
	public T create(CreationalContext<T> creationalContext) {
		// TODO Auto-generated method stub
		if (producer != null)
			return producer.apply(creationalContext);
		
		return null;
	}

	@Override
	public void destroy(T instance, CreationalContext<T> creationalContext) {
		// TODO Auto-generated method stub

	}

	@Override
	public Set<Type> getTypes() {
		// TODO Auto-generated method stub
		return types;
	}

	@Override
	public Set<Annotation> getQualifiers() {
		// TODO Auto-generated method stub
		return qualifiers;
	}

	@Override
	public Class<? extends Annotation> getScope() {
		// TODO Auto-generated method stub
		return scope;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return name;
	}

	@Override
	public Set<Class<? extends Annotation>> getStereotypes() {
		// TODO Auto-generated method stub
		return stereotypes;
	}

	@Override
	public boolean isAlternative() {
		// TODO Auto-generated method stub
		return alternative;
	}

	@Override
	public String getId() {
		// TODO Auto-generated method stub
		return id;
	}

	@Override
	public Class<?> getBeanClass() {
		// TODO Auto-generated method stub
		return beanClass;
	}

	@Override
	public Set<InjectionPoint> getInjectionPoints() {
		// TODO Auto-generated method stub
		return injectionPoints;
	}
	
	@SuppressWarnings("unchecked")
	private static <V> Set<V> asSet(V... vs) {
		return Stream.of(vs).collect(Collectors.toSet());
	}
	
	protected CDIBean<T> types(Type... types) {
		this.types = asSet(types);
		return this;
	}
	
	protected CDIBean<T> qualifiers(Annotation... annotations) {
		this.qualifiers = asSet(annotations);
		return this;
	}
	
	protected CDIBean<T> scope(Class<? extends Annotation> scope) {
		this.scope = scope;
		return this;
	}
	
	protected CDIBean<T> name(String name) {
		this.name = name;
		return this;
	}
	
	protected CDIBean<T> stereotypes(@SuppressWarnings("unchecked") Class<? extends Annotation>... stereotypes) {
		this.stereotypes = asSet(stereotypes);
		return this;
	}
	
	protected CDIBean<T> alternative(boolean alternative) {
		this.alternative = alternative;
		return this;
	}
	
	protected CDIBean<T> id(String id) {
		this.id = id;
		return this;
	}
	
	protected CDIBean<T> beanClass(Class<T> beanClass) {
		this.beanClass = beanClass;
		return this;
	}
	
	protected CDIBean<T> injectionPoints(InjectionPoint... injectionPoints) {
		this.injectionPoints = asSet(injectionPoints);
		return this;
	}
	
	protected CDIBean<T> produce(Function<CreationalContext<T>, T> producer) {
		this.producer = producer;
		return this;
	}
}
