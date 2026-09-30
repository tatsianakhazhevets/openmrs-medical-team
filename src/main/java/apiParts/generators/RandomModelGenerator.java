package apiParts.generators;

import apiParts.config.Config;
import apiParts.models.vitals.Obs;
import apiParts.models.vitals.VitalsConcept;
import apiParts.steps.AdminSteps;
import apiParts.utils.DateTimeUtils;
import com.github.javafaker.Faker;
import com.mifmif.common.regex.Generex;
import common.storages.SessionStorage;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public final class RandomModelGenerator {
    private static final Faker FAKER = new Faker();
    private static final int MAX_DEPTH = 3;
    private static final int DEFAULT_INT_MIN = 0;
    private static final int DEFAULT_INT_MAX = 1000;
    private static final int DEFAULT_DURATION_MIN = 1;
    private static final int DEFAULT_DURATION_MAX = 10;
    private static final long DEFAULT_LONG_MIN = 0L;
    private static final long DEFAULT_LONG_MAX = 100_000L;
    private static final double DEFAULT_DOUBLE_MIN = 0.0;
    private static final double DEFAULT_DOUBLE_MAX = 1000.0;
    private static final int DEFAULT_COLLECTION_MIN_SIZE = 1;
    private static final int DEFAULT_COLLECTION_MAX_SIZE = 3;
    private static final int DATE_RANGE_START_MIN_DAYS_AGO = 1;
    private static final int DATE_RANGE_START_MAX_DAYS_AGO = 30;
    private static final int DATE_RANGE_MIN_MINUTES = 1;
    private static final int DATE_RANGE_MAX_MINUTES = 10 * 60;
    private static final String DATE_RANGE_START = "dateRangeStart";
    private static final String DATE_RANGE_END = "dateRangeEnd";
    private static final DateTimeFormatter OPENMRS_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    private RandomModelGenerator() {
    }

    public static <T> T generate(Class<T> clazz) {
        return generate(clazz, Collections.emptyMap());
    }

    public static <T> T generate(Class<T> clazz, Map<String, Object> overrides) {
        try {
            return generate(clazz, overrides == null ? Collections.emptyMap() : overrides, 0);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate model for: " + clazz.getName(), e);
        }
    }

    private static <T> T generate(Class<T> clazz, Map<String, Object> overrides, int depth) throws Exception {
        if (depth > MAX_DEPTH) {
            return null;
        }
        return clazz.isRecord()
                ? generateRecord(clazz, overrides, depth)
                : generatePojo(clazz, overrides, depth);
    }

    private static <T> T generateRecord(Class<T> clazz, Map<String, Object> overrides, int depth) throws Exception {
        RecordComponent[] components = clazz.getRecordComponents();
        Class<?>[] parameterTypes = Arrays.stream(components)
                .map(RecordComponent::getType)
                .toArray(Class<?>[]::new);
        Object[] values = new Object[components.length];
        Map<String, Object> generated = new LinkedHashMap<>();

        for (int i = 0; i < components.length; i++) {
            RecordComponent component = components[i];
            GeneratingRule rule = component.getAnnotation(GeneratingRule.class);
            if (rule == null) {
                rule = clazz.getDeclaredField(component.getName()).getAnnotation(GeneratingRule.class);
            }
            if (isDependent(rule) && !overrides.containsKey(component.getName())) {
                continue;
            }
            Object value = overrides.containsKey(component.getName())
                    ? overrides.get(component.getName())
                    : generateValue(component.getType(), component.getGenericType(), rule, component.getName(),
                    generated, depth);
            values[i] = value;
            generated.put(component.getName(), value);
        }

        for (int i = 0; i < components.length; i++) {
            RecordComponent component = components[i];
            if (overrides.containsKey(component.getName())) {
                continue;
            }
            GeneratingRule rule = component.getAnnotation(GeneratingRule.class);
            if (rule == null) {
                rule = clazz.getDeclaredField(component.getName()).getAnnotation(GeneratingRule.class);
            }
            if (!isDependent(rule)) {
                continue;
            }
            Object value = generateValue(component.getType(), component.getGenericType(), rule, component.getName(),
                    generated, depth);
            values[i] = value;
            generated.put(component.getName(), value);
        }

        Constructor<T> constructor = clazz.getDeclaredConstructor(parameterTypes);
        constructor.setAccessible(true);
        return constructor.newInstance(values);
    }

    private static <T> T generatePojo(Class<T> clazz, Map<String, Object> overrides, int depth) throws Exception {
        Constructor<T> constructor = clazz.getDeclaredConstructor();
        constructor.setAccessible(true);
        T instance = constructor.newInstance();
        List<Field> fields = getAllFields(clazz);
        Map<String, Object> generated = new LinkedHashMap<>();

        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            field.setAccessible(true);
            GeneratingRule rule = field.getAnnotation(GeneratingRule.class);
            if (overrides.containsKey(field.getName())) {
                Object value = overrides.get(field.getName());
                field.set(instance, value);
                generated.put(field.getName(), value);
                continue;
            }
            if (isDependent(rule)) {
                continue;
            }
            Object value = generateValue(
                    field.getType(), field.getGenericType(), rule, field.getName(), generated, depth);
            field.set(instance, value);
            generated.put(field.getName(), value);
        }

        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers()) || overrides.containsKey(field.getName())) {
                continue;
            }
            GeneratingRule rule = field.getAnnotation(GeneratingRule.class);
            if (!isDependent(rule)) {
                continue;
            }
            field.setAccessible(true);
            Object value = generateValue(field.getType(), field.getGenericType(), rule, field.getName(), generated, depth);
            field.set(instance, value);
            generated.put(field.getName(), value);
        }
        return instance;
    }

    private static boolean isDependent(GeneratingRule rule) {
        return rule != null && (rule.strategy() == GenerationStrategy.IDENTIFIER
                || rule.strategy() == GenerationStrategy.DEPENDS_ON_FIELD);
    }

    private static Object generateValue(
            Class<?> type,
            Type genericType,
            GeneratingRule rule,
            String fieldName,
            Map<String, Object> generated,
            int depth
    ) {
        if (rule != null) {
            if (rule.nullable()) {
                if (type.isPrimitive()) {
                    throw new IllegalArgumentException("Cannot set primitive field to null: " + fieldName);
                }
                return null;
            }
            if (!rule.property().isBlank()) {
                String value = Config.getProperty(rule.property());
                if (value == null) {
                    throw new IllegalArgumentException("Config property not found: " + rule.property());
                }
                return castGenerated(value, type);
            }
            if (!rule.fixedValue().isBlank()) {
                return castGenerated(rule.fixedValue(), type);
            }
            if (!rule.regex().isBlank()) {
                return castGenerated(new Generex(rule.regex()).random(), type);
            }
            if (isBoolean(type) && rule.booleanValue() != BooleanGeneration.RANDOM) {
                return rule.booleanValue() == BooleanGeneration.TRUE;
            }
            if (rule.enumClass() != GeneratingRule.EmptyEnum.class) {
                return generateEnum(type, rule);
            }

            return switch (rule.strategy()) {
                case PATIENT_UUID -> SessionStorage.getPatient(rule.patientNumber()).getUuid();
                case PROVIDER_UUID -> AdminSteps.getCurrentProviderUuid();
                case VITALS_OBSERVATIONS -> Arrays.stream(VitalsConcept.values()).map(Obs::random).toList();
                case DATE -> generateDate(rule);
                case DATE_TIME -> generateDateTime(rule, generated);
                case DATE_RANGE_START -> dateRange(generated).get(DATE_RANGE_START);
                case DATE_RANGE_END -> dateRange(generated).get(DATE_RANGE_END);
                case DURATION -> randomInt(DEFAULT_DURATION_MIN, DEFAULT_DURATION_MAX);
                case IDENTIFIER -> IdentifierGenerator.generate((String) requiredSourceValue(rule, generated, fieldName));
                case DEPENDS_ON_FIELD -> valueFromSource(rule, generated, fieldName);
                case RANDOM -> generateRandomValue(type, genericType, rule, depth);
            };
        }
        return generateRandomValue(type, genericType, null, depth);
    }

    private static Object generateEnum(Class<?> fieldType, GeneratingRule rule) {
        List<? extends Enum<?>> allowedValues = Arrays.stream(rule.enumClass().getEnumConstants())
                .filter(value -> !Arrays.asList(rule.excludedEnumValues()).contains(value.name()))
                .toList();
        if (allowedValues.isEmpty()) {
            throw new IllegalArgumentException(
                    "No enum values remain for field type: " + fieldType.getName());
        }

        Enum<?> value = rule.enumValue().isBlank()
                ? oneOf(allowedValues)
                : Enum.valueOf(rule.enumClass().asSubclass(Enum.class), rule.enumValue());
        if (fieldType.isEnum()) {
            return value;
        }
        try {
            return value.getClass().getMethod(rule.valueMethod()).invoke(value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate value from enum: " + rule.enumClass().getName(), e);
        }
    }

    private static Object valueFromSource(
            GeneratingRule rule,
            Map<String, Object> generated,
            String fieldName
    ) {
        Object source = requiredSourceValue(rule, generated, fieldName);
        try {
            return source.getClass().getMethod(rule.valueMethod()).invoke(source);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to generate " + fieldName + " from field " + rule.sourceField(), e);
        }
    }

    private static Object requiredSourceValue(
            GeneratingRule rule,
            Map<String, Object> generated,
            String fieldName
    ) {
        if (rule.sourceField().isBlank()) {
            throw new IllegalArgumentException("sourceField is required for: " + fieldName);
        }
        Object source = generated.get(rule.sourceField());
        if (source == null) {
            throw new IllegalStateException(
                    "Source field " + rule.sourceField() + " is null or was not generated for: " + fieldName);
        }
        return source;
    }

    private static Object generateRandomValue(
            Class<?> type,
            Type genericType,
            GeneratingRule rule,
            int depth
    ) {
        if (type.equals(String.class)) return UUID.randomUUID().toString().substring(0, 8);
        if (type.equals(int.class) || type.equals(Integer.class)) {
            return rule != null && rule.min() != rule.max()
                    ? randomInt((int) rule.min(), (int) rule.max())
                    : randomInt(DEFAULT_INT_MIN, DEFAULT_INT_MAX);
        }
        if (type.equals(long.class) || type.equals(Long.class)) {
            return ThreadLocalRandom.current().nextLong(DEFAULT_LONG_MIN, DEFAULT_LONG_MAX);
        }
        if (type.equals(double.class) || type.equals(Double.class)) {
            return rule != null && rule.min() != rule.max()
                    ? randomDouble(rule.min(), rule.max(), rule.scale())
                    : randomDouble(DEFAULT_DOUBLE_MIN, DEFAULT_DOUBLE_MAX, 2);
        }
        if (type.equals(float.class) || type.equals(Float.class)) return ThreadLocalRandom.current().nextFloat();
        if (type.equals(short.class) || type.equals(Short.class)) return (short) randomInt(0, Short.MAX_VALUE);
        if (type.equals(byte.class) || type.equals(Byte.class)) return (byte) randomInt(0, Byte.MAX_VALUE);
        if (type.equals(char.class) || type.equals(Character.class)) {
            return (char) (ThreadLocalRandom.current().nextInt(26) + 'a');
        }
        if (isBoolean(type)) return ThreadLocalRandom.current().nextBoolean();
        if (type.equals(Date.class)) {
            return new Date(System.currentTimeMillis() - ThreadLocalRandom.current().nextInt(1_000_000_000));
        }
        if (type.isEnum()) return oneOf(List.of(type.getEnumConstants()));
        if (Collection.class.isAssignableFrom(type)) {
            return generateCollection(genericType, rule, depth);
        }
        try {
            return generate(type, Collections.emptyMap(), depth + 1);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate nested model: " + type.getName(), e);
        }
    }

    private static List<?> generateCollection(Type genericType, GeneratingRule rule, int depth) {
        if (!(genericType instanceof ParameterizedType parameterizedType)) {
            return Collections.emptyList();
        }
        Type itemType = parameterizedType.getActualTypeArguments()[0];
        if (!(itemType instanceof Class<?> itemClass) || itemClass.equals(Object.class)) {
            return Collections.emptyList();
        }
        int minSize = rule == null ? DEFAULT_COLLECTION_MIN_SIZE : rule.minSize();
        int maxSize = rule == null ? DEFAULT_COLLECTION_MAX_SIZE : rule.maxSize();
        int size = randomInt(minSize, maxSize);
        List<Object> values = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            values.add(generateRandomValue(itemClass, itemClass, null, depth + 1));
        }
        return values;
    }

    private static Object castGenerated(String value, Class<?> type) {
        if (type.equals(String.class)) return value;
        if (type.isEnum()) return Enum.valueOf(type.asSubclass(Enum.class), value);
        if (type.equals(Integer.class) || type.equals(int.class)) return Integer.parseInt(value);
        if (type.equals(Long.class) || type.equals(long.class)) return Long.parseLong(value);
        if (type.equals(Double.class) || type.equals(double.class)) return Double.parseDouble(value);
        if (type.equals(Float.class) || type.equals(float.class)) return Float.parseFloat(value);
        if (type.equals(Short.class) || type.equals(short.class)) return Short.parseShort(value);
        if (type.equals(Byte.class) || type.equals(byte.class)) return Byte.parseByte(value);
        if (isBoolean(type)) return Boolean.parseBoolean(value);
        if (type.equals(Character.class) || type.equals(char.class)) return value.charAt(0);
        return value;
    }

    private static String generateDate(GeneratingRule rule) {
        LocalDate start = LocalDate.of(rule.minYear(), 1, 1);
        LocalDate end = LocalDate.of(rule.maxYear(), 12, 31);
        long daysBetween = ChronoUnit.DAYS.between(start, end);
        return start.plusDays(ThreadLocalRandom.current().nextLong(daysBetween + 1))
                .atStartOfDay()
                .atOffset(ZoneOffset.UTC)
                .format(OPENMRS_DATE_FORMAT);
    }

    private static String generateDateTime(GeneratingRule rule, Map<String, Object> generated) {
        OffsetDateTime dateTime;
        if (!rule.baseField().isBlank()) {
            Object baseValue = generated.get(rule.baseField());
            if (!(baseValue instanceof String value)) {
                throw new IllegalStateException("Base date-time field was not generated: " + rule.baseField());
            }
            dateTime = OffsetDateTime.parse(value, OPENMRS_DATE_FORMAT).plusMinutes(rule.minutesFromBase());
        } else {
            dateTime = OffsetDateTime.now(ZoneOffset.UTC).plusMinutes(rule.minutesFromNow());
        }
        return dateTime.withNano(0).format(OPENMRS_DATE_FORMAT);
    }

    private static Map<String, Object> dateRange(Map<String, Object> generated) {
        if (!generated.containsKey(DATE_RANGE_START)) {
            OffsetDateTime start = DateTimeUtils.now()
                    .minusDays(randomInt(DATE_RANGE_START_MIN_DAYS_AGO, DATE_RANGE_START_MAX_DAYS_AGO))
                    .minusMinutes(randomInt(0, 24 * 60 - 1));
            OffsetDateTime end = start.plusMinutes(randomInt(DATE_RANGE_MIN_MINUTES, DATE_RANGE_MAX_MINUTES));
            generated.put(DATE_RANGE_START, start.format(DateTimeUtils.OPENMRS_REQUEST_DATE_TIME));
            generated.put(DATE_RANGE_END, end.format(DateTimeUtils.OPENMRS_REQUEST_DATE_TIME));
        }
        return generated;
    }

    private static List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        for (Class<?> current = clazz; current != null && current != Object.class; current = current.getSuperclass()) {
            fields.addAll(Arrays.asList(current.getDeclaredFields()));
        }
        return fields;
    }

    private static boolean isBoolean(Class<?> type) {
        return type.equals(boolean.class) || type.equals(Boolean.class);
    }

    public static int randomInt(int minInclusive, int maxInclusive) {
        if (minInclusive > maxInclusive) {
            throw new IllegalArgumentException("min must be <= max, but was " + minInclusive + ".." + maxInclusive);
        }
        return minInclusive == maxInclusive
                ? minInclusive
                : ThreadLocalRandom.current().nextInt(minInclusive, maxInclusive + 1);
    }

    public static double randomDouble(double min, double max, int scale) {
        if (min > max) {
            throw new IllegalArgumentException("min must be <= max, but was " + min + ".." + max);
        }
        double value = min == max ? min : ThreadLocalRandom.current().nextDouble(min, max);
        double factor = Math.pow(10, scale);
        return Math.round(value * factor) / factor;
    }

    public static double randomNegativeDouble(double min, int scale) {
        return randomDouble(min, -(1 / Math.pow(10, scale)), scale);
    }

    public static int randomNegativeInt(int min) {
        return randomInt(min, -1);
    }

    public static String randomWord() {
        return FAKER.lorem().word();
    }

    public static String randomWord(String regex) {
        return new Generex(regex).random();
    }

    public static String randomSentence() {
        return FAKER.lorem().sentence();
    }

    public static boolean randomBoolean() {
        return ThreadLocalRandom.current().nextBoolean();
    }

    public static <E extends Enum<E>> E oneOf(Class<E> enumClass) {
        return oneOf(List.of(enumClass.getEnumConstants()));
    }

    @SafeVarargs
    public static <E extends Enum<E>> E oneOfExcept(Class<E> enumClass, E... excluded) {
        List<E> allowed = new ArrayList<>(List.of(enumClass.getEnumConstants()));
        allowed.removeAll(List.of(excluded));
        return oneOf(allowed);
    }

    public static <T> T oneOf(List<T> values) {
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException("Cannot pick a random value from an empty list");
        }
        return values.get(ThreadLocalRandom.current().nextInt(values.size()));
    }

    public static String futureDateTime() {
        return LocalDateTime.now().plusYears(1).withNano(0).atOffset(ZoneOffset.UTC).format(OPENMRS_DATE_FORMAT);
    }

    public static String pastDateTime() {
        return LocalDateTime.now().minusYears(1).withNano(0).atOffset(ZoneOffset.UTC).format(OPENMRS_DATE_FORMAT);
    }
}
