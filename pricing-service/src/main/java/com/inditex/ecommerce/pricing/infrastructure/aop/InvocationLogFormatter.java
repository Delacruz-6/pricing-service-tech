package com.inditex.ecommerce.pricing.infrastructure.aop;

import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.math.BigDecimal;
import java.time.temporal.TemporalAccessor;
import java.util.Collection;
import java.util.Currency;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.UUID;

final class InvocationLogFormatter {

    private static final int MAX_SUMMARY_FIELDS = 4;
    private static final int MAX_SCALAR_LENGTH = 60;

    private static final Set<String> SUMMARY_FIELD_NAMES = Set.of(
            "id", "uuid", "code", "name", "status", "type",
            "price", "amount", "total", "quantity", "currency"
    );

    private InvocationLogFormatter() {}

    static String formatInvocation(MethodSignature signature, Object[] args, long elapsedMs, Object result) {
        String target = "%s.%s(%s)".formatted(
                shortClassName(signature.getDeclaringTypeName()),
                signature.getName(),
                formatArgs(signature, args));
        return "%s %dms -> %s".formatted(target, elapsedMs, formatResult(result));
    }

    static String formatError(MethodSignature signature, Object[] args, long elapsedMs, String cause) {
        String target = "%s.%s(%s)".formatted(
                shortClassName(signature.getDeclaringTypeName()),
                signature.getName(),
                formatArgs(signature, args));
        return "%s %dms !! %s".formatted(target, elapsedMs, cause);
    }

    private static String shortClassName(String className) {
        return className.substring(className.lastIndexOf('.') + 1);
    }

    private static String formatArgs(MethodSignature signature, Object[] args) {
        if (args == null || args.length == 0) {
            return "";
        }
        String[] names = signature.getParameterNames();
        StringJoiner joiner = new StringJoiner(", ");
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof Pageable) {
                continue;
            }
            String name = names != null && i < names.length ? names[i] : "arg" + i;
            joiner.add(name + "=" + formatArg(args[i]));
        }
        return joiner.toString();
    }

    static String formatResult(Object result) {
        if (result == null) {
            return "null";
        }
        if (result instanceof ResponseEntity<?> response) {
            String body = response.hasBody() ? summarize(response.getBody()) : "empty";
            return "HTTP %d, %s".formatted(response.getStatusCode().value(), body);
        }
        return summarize(result);
    }

    private static String formatArg(Object value) {
        if (value == null) {
            return "null";
        }
        if (isScalar(value)) {
            return formatScalar(value);
        }
        return summarize(value);
    }

    private static String summarize(Object value) {
        if (value == null) {
            return "null";
        }
        if (isScalar(value)) {
            return formatScalar(value);
        }
        return switch (value) {
            case Optional<?> optional -> optional.map(InvocationLogFormatter::summarize).orElse("none");
            case Collection<?> collection -> collection.isEmpty()
                    ? "empty list"
                    : "list(%d)".formatted(collection.size());
            case Map<?, ?> map -> map.isEmpty()
                    ? "empty map"
                    : "map(%d)".formatted(map.size());
            case Pageable pageable -> "page %d, size %d".formatted(
                    pageable.getPageNumber(), pageable.getPageSize());
            default -> {
                if (value.getClass().isArray()) {
                    yield "array(%d)".formatted(Array.getLength(value));
                }
                yield summarizeObject(value);
            }
        };
    }

    private static String summarizeObject(Object value) {
        Map<String, Object> fields = extractFields(value);
        if (fields.isEmpty()) {
            return value.getClass().getSimpleName();
        }

        if (fields.containsKey("price") && fields.containsKey("currency")) {
            String amount = formatScalar(fields.remove("price"));
            String currency = formatCurrency(fields.remove("currency"));
            String priceLabel = currency.isBlank() ? amount : amount + " " + currency;
            return joinSummary(priceLabel, fields);
        }

        return joinSummary(null, fields);
    }

    private static String joinSummary(String leading, Map<String, Object> fields) {
        StringJoiner joiner = new StringJoiner(", ");
        if (leading != null && !leading.isBlank()) {
            joiner.add(leading);
        }
        int count = 0;
        for (Map.Entry<String, Object> entry : fields.entrySet()) {
            if (count >= MAX_SUMMARY_FIELDS) {
                break;
            }
            Object fieldValue = entry.getValue();
            if (fieldValue == null || !isSummaryField(entry.getKey())) {
                continue;
            }
            joiner.add(entry.getKey() + "=" + formatScalar(fieldValue));
            count++;
        }
        String summary = joiner.toString();
        return summary.isEmpty() ? "object" : summary;
    }

    private static Map<String, Object> extractFields(Object value) {
        Map<String, Object> fields = new LinkedHashMap<>();
        Class<?> type = value.getClass();

        if (type.isRecord()) {
            RecordComponent[] components = type.getRecordComponents();
            if (components != null) {
                for (RecordComponent component : components) {
                    putField(fields, component.getName(), readRecordField(value, component));
                }
            }
            return fields;
        }

        for (Field field : type.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            putField(fields, field.getName(), readBeanField(value, field));
        }
        return fields;
    }

    private static void putField(Map<String, Object> fields, String name, Object value) {
        if (value != null && isSummaryField(name)) {
            fields.put(name, value);
        }
    }

    private static Object readRecordField(Object value, RecordComponent component) {
        try {
            Method accessor = component.getAccessor();
            return accessor.invoke(value);
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private static Object readBeanField(Object value, Field field) {
        try {
            field.setAccessible(true);
            return field.get(value);
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private static boolean isSummaryField(String name) {
        String normalized = name.toLowerCase();
        return SUMMARY_FIELD_NAMES.contains(normalized) || normalized.endsWith("id");
    }

    private static boolean isScalar(Object value) {
        return value instanceof CharSequence
                || value instanceof Number
                || value instanceof Boolean
                || value instanceof Enum<?>
                || value instanceof TemporalAccessor
                || value instanceof UUID
                || value instanceof Currency;
    }

    private static String formatScalar(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof Currency currency) {
            return currency.getCurrencyCode();
        }
        if (value instanceof BigDecimal amount) {
            return amount.toPlainString();
        }
        String text = String.valueOf(value);
        return text.length() <= MAX_SCALAR_LENGTH ? text : text.substring(0, MAX_SCALAR_LENGTH) + "...";
    }

    private static String formatCurrency(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof Currency currency) {
            return currency.getCurrencyCode();
        }
        return formatScalar(value);
    }
}
