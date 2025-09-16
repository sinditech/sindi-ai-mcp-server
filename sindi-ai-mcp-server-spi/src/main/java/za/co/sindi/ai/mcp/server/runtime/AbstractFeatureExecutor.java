/**
 * 
 */
package za.co.sindi.ai.mcp.server.runtime;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import za.co.sindi.ai.mcp.mapper.JSONObjectMapper;
import za.co.sindi.ai.mcp.mapper.ObjectMapper;
import za.co.sindi.ai.mcp.schema.Request;
import za.co.sindi.ai.mcp.server.runtime.exception.FeatureExecutionException;

/**
 * @author Buhake Sindi
 * @since 22 April 2025
 */
public abstract class AbstractFeatureExecutor<REQ extends Request, T, FD extends FeatureDefinition<T>> implements FeatureExecutor<REQ> {

	private static final ObjectMapper MAPPER = JSONObjectMapper.newInstance();
	protected final BeanDefinition bean;
	protected final FD feature;

	/**
	 * @param bean
	 * @param feature
	 */
	protected AbstractFeatureExecutor(BeanDefinition bean, FD feature) {
		super();
		this.bean = bean;
		this.feature = feature;
	}

	@Override
	public Object invoke(REQ request) throws FeatureExecutionException {
		// TODO Auto-generated method stub
		Method method = findMethod(request);
		Object[] arguments = prepareArguments(method, request);
		try {
			return method.invoke(bean.getInstance(), arguments);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			throw new FeatureExecutionException(e);
		}
	}
	
	protected Object[] prepareArguments(final Method method, final REQ request) {
		return null;
	}
	
	@SuppressWarnings("unchecked")
	protected Object coerceArgument(Object argument, String parameterName, Class<?> parameterClass, Type parameterType) {
        if (parameterClass == String.class) {
            return argument.toString();
        }

        // TODO handle enum and collection of enums (e.g. wrong case, etc)
        if (parameterClass.isEnum()) {
            try {
                @SuppressWarnings({"rawtypes"})
                Class<Enum> enumClass = (Class<Enum>) parameterClass;
                try {
                    return Enum.valueOf(
                            enumClass, Objects.requireNonNull(argument).toString());
                } catch (IllegalArgumentException e) {
                    // try to convert to uppercase as a last resort
                    return Enum.valueOf(
                            enumClass,
                            Objects.requireNonNull(argument).toString().toUpperCase());
                }
            } catch (Exception | Error e) {
                throw new IllegalArgumentException(
                        String.format(
                                "Argument \"%s\" is not a valid enum value for %s: <%s>",
                                parameterName, parameterClass.getName(), argument),
                        e);
            }
        }

        if (parameterClass == Boolean.class || parameterClass == boolean.class) {
            if (argument instanceof Boolean) {
                return argument;
            }
            if (argument instanceof String booleanString) {
            	return Boolean.valueOf(booleanString);
            }
            throw new IllegalArgumentException(String.format(
                    "Argument \"%s\" is not convertable to %s, got %s: <%s>",
                    parameterName, parameterClass.getName(), argument.getClass().getName(), argument));
        }

        if (parameterClass == Double.class || parameterClass == double.class) {
            return getDoubleValue(argument, parameterName, parameterClass);
        }

        if (parameterClass == Float.class || parameterClass == float.class) {
            double doubleValue = getDoubleValue(argument, parameterName, parameterClass);
            checkBounds(doubleValue, parameterName, parameterClass, -Float.MIN_VALUE, Float.MAX_VALUE);
            return (float) doubleValue;
        }

        if (parameterClass == BigDecimal.class) {
            return BigDecimal.valueOf(getDoubleValue(argument, parameterName, parameterClass));
        }

        if (parameterClass == Integer.class || parameterClass == int.class) {
            return (int)
                    getBoundedLongValue(argument, parameterName, parameterClass, Integer.MIN_VALUE, Integer.MAX_VALUE);
        }

        if (parameterClass == Long.class || parameterClass == long.class) {
            return getBoundedLongValue(argument, parameterName, parameterClass, Long.MIN_VALUE, Long.MAX_VALUE);
        }

        if (parameterClass == Short.class || parameterClass == short.class) {
            return (short)
                    getBoundedLongValue(argument, parameterName, parameterClass, Short.MIN_VALUE, Short.MAX_VALUE);
        }

        if (parameterClass == Byte.class || parameterClass == byte.class) {
            return (byte) getBoundedLongValue(argument, parameterName, parameterClass, Byte.MIN_VALUE, Byte.MAX_VALUE);
        }

        if (parameterClass == BigInteger.class) {
            return BigDecimal.valueOf(getNonFractionalDoubleValue(argument, parameterName, parameterClass))
                    .toBigInteger();
        }

        if (parameterClass.isArray() && argument instanceof Collection) {
            Class<?> type = parameterClass.getComponentType();
            if (type == String.class) {
                return ((Collection<String>) argument).toArray(new String[0]);
            }
            // TODO: Consider full type coverage.
        }

        if (Collection.class.isAssignableFrom(parameterClass) || Map.class.isAssignableFrom(parameterClass)) {
            // Conversion to JSON and back is required when parameterType is a POJO
        	return MAPPER.map(MAPPER.map(argument), parameterType.getClass());
        }

        if (parameterClass == UUID.class) {
            return UUID.fromString(argument.toString());
        }

        if (argument instanceof String) {
        	return MAPPER.map(MAPPER.map(argument.toString()), parameterClass);
        } else {
            // Conversion to JSON and back is required when parameterClass is a POJO
        	return MAPPER.map(MAPPER.map(argument), parameterClass);
        }
    }

	private double getDoubleValue(Object argument, String parameterName, Class<?> parameterType) {
        if (argument instanceof String) {
            try {
                return Double.parseDouble(argument.toString());
            } catch (Exception e) {
                // nothing, will be handled with bellow code
            }
        }
        if (!(argument instanceof Number)) {
            throw new IllegalArgumentException(String.format(
                    "Argument \"%s\" is not convertable to %s, got %s: <%s>",
                    parameterName, parameterType.getName(), argument.getClass().getName(), argument));
        }
        return ((Number) argument).doubleValue();
    }

	private double getNonFractionalDoubleValue(Object argument, String parameterName, Class<?> parameterType) {
        double doubleValue = getDoubleValue(argument, parameterName, parameterType);
        if (!hasNoFractionalPart(doubleValue)) {
            throw new IllegalArgumentException(String.format(
                    "Argument \"%s\" has non-integer value for %s: <%s>",
                    parameterName, parameterType.getName(), argument));
        }
        return doubleValue;
    }

    private void checkBounds(
            double doubleValue, String parameterName, Class<?> parameterType, double minValue, double maxValue) {
        if (doubleValue < minValue || doubleValue > maxValue) {
            throw new IllegalArgumentException(String.format(
                    "Argument \"%s\" is out of range for %s: <%s>",
                    parameterName, parameterType.getName(), doubleValue));
        }
    }

    public long getBoundedLongValue(
            Object argument, String parameterName, Class<?> parameterType, long minValue, long maxValue) {
        double doubleValue = getNonFractionalDoubleValue(argument, parameterName, parameterType);
        checkBounds(doubleValue, parameterName, parameterType, minValue, maxValue);
        return (long) doubleValue;
    }

    private boolean hasNoFractionalPart(Double doubleValue) {
        return doubleValue.equals(Math.floor(doubleValue));
    }
	
	protected abstract Method findMethod(final REQ request);
}
