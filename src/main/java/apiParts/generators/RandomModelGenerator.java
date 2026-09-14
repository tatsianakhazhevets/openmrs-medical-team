package apiParts.generators;

import com.github.javafaker.Faker;
import com.mifmif.common.regex.Generex;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class RandomModelGenerator {
    private static final Faker faker = new Faker();
    private static final int MAX_DEPTH = 3;

    public static <T> T generate(Class<T> clazz) {
        return generate(clazz, new HashMap<>(), 0);
    }

    public static <T> T generate(Class<T> clazz, Map<String, Object> overrides) {
        return generate(clazz, overrides, 0);
    }

    private static <T> T generate(Class<T> clazz, Map<String, Object> overrides, int depth) {
        if (depth > MAX_DEPTH) return null;

        try {
            T instance = createInstance(clazz);

            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);

                if (overrides != null && overrides.containsKey(field.getName())) {
                    field.set(instance, overrides.get(field.getName()));
                    continue;
                }

                Object value = generateFieldValue(field, depth);
                field.set(instance, value);
            }

            return instance;

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate model for: " + clazz.getName(), e);
        }
    }

    private static Object generateFieldValue(Field field, int depth) {
        Class<?> type = field.getType();

        StringGeneratingRule stringRule = field.getAnnotation(StringGeneratingRule.class);
        if (stringRule != null && !stringRule.regex().isEmpty() && type.equals(String.class)) {
            return generateFromRegex(stringRule.regex());
        }

        DoubleGeneratingRule doubleRule = field.getAnnotation(DoubleGeneratingRule.class);
        if (doubleRule != null && (type.equals(double.class) || type.equals(Double.class))) {
            return generateDoubleByRule(doubleRule);
        }

        if (type.equals(String.class)) return faker.lorem().word();
        if (type.equals(int.class) || type.equals(Integer.class)) return randomInt();
        if (type.equals(long.class) || type.equals(Long.class)) return randomLong();
        if (type.equals(double.class) || type.equals(Double.class)) return randomDouble();
        if (type.equals(boolean.class) || type.equals(Boolean.class)) return randomBoolean();
        if (type.equals(float.class) || type.equals(Float.class)) return randomFloat();
        if (type.equals(short.class) || type.equals(Short.class)) return (short) randomInt();
        if (type.equals(byte.class) || type.equals(Byte.class)) return (byte) randomInt();
        if (type.equals(char.class) || type.equals(Character.class)) return randomChar();

        if (type.isEnum()) {
            Object[] constants = type.getEnumConstants();
            return constants[ThreadLocalRandom.current().nextInt(constants.length)];
        }

        if (Collection.class.isAssignableFrom(type)) {
            return generateCollection(field, depth);
        }

        return generate(type, null, depth + 1);
    }

    private static Object generateCollection(Field field, int depth) {
        try {
            ParameterizedType genericType = (ParameterizedType) field.getGenericType();
            Class<?> itemType = (Class<?>) genericType.getActualTypeArguments()[0];

            List<Object> list = new ArrayList<>();
            int size = ThreadLocalRandom.current().nextInt(1, 4);

            for (int i = 0; i < size; i++) {
                list.add(generate(itemType, null, depth + 1));
            }

            return list;

        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private static Object generateFromRegex(String regex) {
        Generex generex = new Generex(regex);
        return generex.random();
    }

    private static double generateDoubleByRule(DoubleGeneratingRule rule) {
        double value = ThreadLocalRandom.current().nextDouble(rule.min(), rule.max());
        double scale = Math.pow(10, rule.range());
        return Math.round(value * scale) / scale;
    }

    private static <T> T createInstance(Class<T> clazz) throws Exception {
        Constructor<T> constructor = clazz.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();
    }

    private static int randomInt() {
        return ThreadLocalRandom.current().nextInt(0, 1000);
    }

    private static long randomLong() {
        return ThreadLocalRandom.current().nextLong(0, 100000);
    }

    private static double randomDouble() {
        return ThreadLocalRandom.current().nextDouble(0, 1000);
    }

    private static float randomFloat() {
        return ThreadLocalRandom.current().nextFloat();
    }

    private static boolean randomBoolean() {
        return ThreadLocalRandom.current().nextBoolean();
    }

    private static char randomChar() {
        return (char) (ThreadLocalRandom.current().nextInt(26) + 'a');
    }
}