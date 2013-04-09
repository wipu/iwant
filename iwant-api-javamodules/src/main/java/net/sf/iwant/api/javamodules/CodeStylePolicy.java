package net.sf.iwant.api.javamodules;

import static net.sf.iwant.api.javamodules.CodeStyle.ANNOTATION_SUPER_INTERFACE;
import static net.sf.iwant.api.javamodules.CodeStyle.ASSERT_IDENTIFIER;
import static net.sf.iwant.api.javamodules.CodeStyle.AUTOBOXING;
import static net.sf.iwant.api.javamodules.CodeStyle.COMPARING_IDENTICAL;
import static net.sf.iwant.api.javamodules.CodeStyle.DEAD_CODE;
import static net.sf.iwant.api.javamodules.CodeStyle.DEPRECATION;
import static net.sf.iwant.api.javamodules.CodeStyle.DEPRECATION_IN_DEPRECATED_CODE;
import static net.sf.iwant.api.javamodules.CodeStyle.DEPRECATION_WHEN_OVERRIDING_DEPRECATED_METHOD;
import static net.sf.iwant.api.javamodules.CodeStyle.DISCOURAGED_REFERENCE;
import static net.sf.iwant.api.javamodules.CodeStyle.EMPTY_STATEMENT;
import static net.sf.iwant.api.javamodules.CodeStyle.ENUM_IDENTIFIER;
import static net.sf.iwant.api.javamodules.CodeStyle.FALLTHROUGH_CASE;
import static net.sf.iwant.api.javamodules.CodeStyle.FATAL_OPTIONAL_ERROR;
import static net.sf.iwant.api.javamodules.CodeStyle.FIELD_HIDING;
import static net.sf.iwant.api.javamodules.CodeStyle.FINALLY_BLOCK_NOT_COMPLETING_NORMALLY;
import static net.sf.iwant.api.javamodules.CodeStyle.FINAL_PARAMETER_BOUND;
import static net.sf.iwant.api.javamodules.CodeStyle.FORBIDDEN_REFERENCE;
import static net.sf.iwant.api.javamodules.CodeStyle.HIDDEN_CATCH_BLOCK;
import static net.sf.iwant.api.javamodules.CodeStyle.INCLUDE_NULL_INFO_FROM_ASSERTS;
import static net.sf.iwant.api.javamodules.CodeStyle.INCOMPATIBLE_NON_INHERITED_INTERFACE_METHOD;
import static net.sf.iwant.api.javamodules.CodeStyle.INCOMPLETE_ENUM_SWITCH;
import static net.sf.iwant.api.javamodules.CodeStyle.INDIRECT_STATIC_ACCESS;
import static net.sf.iwant.api.javamodules.CodeStyle.LOCAL_VARIABLE_HIDING;
import static net.sf.iwant.api.javamodules.CodeStyle.METHOD_WITH_CONSTRUCTOR_NAME;
import static net.sf.iwant.api.javamodules.CodeStyle.MISSING_DEFAULT_CASE;
import static net.sf.iwant.api.javamodules.CodeStyle.MISSING_DEPRECATED_ANNOTATION;
import static net.sf.iwant.api.javamodules.CodeStyle.MISSING_HASHCODE_METHOD;
import static net.sf.iwant.api.javamodules.CodeStyle.MISSING_OVERRIDE_ANNOTATION;
import static net.sf.iwant.api.javamodules.CodeStyle.MISSING_OVERRIDE_ANNOTATION_FOR_INTERFACE_METHOD_IMPLEMENTATION;
import static net.sf.iwant.api.javamodules.CodeStyle.MISSING_SERIAL_VERSION;
import static net.sf.iwant.api.javamodules.CodeStyle.MISSING_SYNCHRONIZED_ON_INHERITED_METHOD;
import static net.sf.iwant.api.javamodules.CodeStyle.NON_EXTERNALIZED_STRING_LITERAL;
import static net.sf.iwant.api.javamodules.CodeStyle.NO_EFFECT_ASSIGNMENT;
import static net.sf.iwant.api.javamodules.CodeStyle.NO_IMPLICIT_STRING_CONVERSION;
import static net.sf.iwant.api.javamodules.CodeStyle.NULL_REFERENCE;
import static net.sf.iwant.api.javamodules.CodeStyle.OVERRIDING_PACKAGE_DEFAULT_METHOD;
import static net.sf.iwant.api.javamodules.CodeStyle.PARAMETER_ASSIGNMENT;
import static net.sf.iwant.api.javamodules.CodeStyle.POSSIBLE_ACCIDENTAL_BOOLEAN_ASSIGNMENT;
import static net.sf.iwant.api.javamodules.CodeStyle.POTENTIALLY_UNCLOSED_CLOSEABLE;
import static net.sf.iwant.api.javamodules.CodeStyle.POTENTIAL_NULL_REFERENCE;
import static net.sf.iwant.api.javamodules.CodeStyle.RAW_TYPE_REFERENCE;
import static net.sf.iwant.api.javamodules.CodeStyle.REDUNDANT_NULL_CHECK;
import static net.sf.iwant.api.javamodules.CodeStyle.REDUNDANT_SPECIFICATION_OF_TYPE_ARGUMENTS;
import static net.sf.iwant.api.javamodules.CodeStyle.REDUNDANT_SUPERINTERFACE;
import static net.sf.iwant.api.javamodules.CodeStyle.REPORT_METHOD_CAN_BE_POTENTIALLY_STATIC;
import static net.sf.iwant.api.javamodules.CodeStyle.REPORT_METHOD_CAN_BE_STATIC;
import static net.sf.iwant.api.javamodules.CodeStyle.SPECIAL_PARAMETER_HIDING_FIELD;
import static net.sf.iwant.api.javamodules.CodeStyle.STATIC_ACCESS_RECEIVER;
import static net.sf.iwant.api.javamodules.CodeStyle.SUPPRESS_OPTIONAL_ERRORS;
import static net.sf.iwant.api.javamodules.CodeStyle.SUPPRESS_WARNINGS;
import static net.sf.iwant.api.javamodules.CodeStyle.SYNTHETIC_ACCESS_EMULATION;
import static net.sf.iwant.api.javamodules.CodeStyle.TYPE_PARAMETER_HIDING;
import static net.sf.iwant.api.javamodules.CodeStyle.UNAVOIDABLE_GENERIC_TYPE_PROBLEMS;
import static net.sf.iwant.api.javamodules.CodeStyle.UNCHECKED_TYPE_OPERATION;
import static net.sf.iwant.api.javamodules.CodeStyle.UNCLOSED_CLOSEABLE;
import static net.sf.iwant.api.javamodules.CodeStyle.UNDOCUMENTED_EMPTY_BLOCK;
import static net.sf.iwant.api.javamodules.CodeStyle.UNHANDLED_WARNING_TOKEN;
import static net.sf.iwant.api.javamodules.CodeStyle.UNNECESSARY_ELSE;
import static net.sf.iwant.api.javamodules.CodeStyle.UNNECESSARY_TYPE_CHECK;
import static net.sf.iwant.api.javamodules.CodeStyle.UNQUALIFIED_FIELD_ACCESS;
import static net.sf.iwant.api.javamodules.CodeStyle.UNUSED_DECLARED_THROWN_EXCEPTION;
import static net.sf.iwant.api.javamodules.CodeStyle.UNUSED_DECLARED_THROWN_EXCEPTION_EXEMPT_EXCEPTION_AND_THROWABLE;
import static net.sf.iwant.api.javamodules.CodeStyle.UNUSED_DECLARED_THROWN_EXCEPTION_INCLUDE_DOC_COMMENT_REFERENCE;
import static net.sf.iwant.api.javamodules.CodeStyle.UNUSED_DECLARED_THROWN_EXCEPTION_WHEN_OVERRIDING;
import static net.sf.iwant.api.javamodules.CodeStyle.UNUSED_IMPORT;
import static net.sf.iwant.api.javamodules.CodeStyle.UNUSED_LABEL;
import static net.sf.iwant.api.javamodules.CodeStyle.UNUSED_LOCAL;
import static net.sf.iwant.api.javamodules.CodeStyle.UNUSED_OBJECT_ALLOCATION;
import static net.sf.iwant.api.javamodules.CodeStyle.UNUSED_PARAMETER;
import static net.sf.iwant.api.javamodules.CodeStyle.UNUSED_PARAMETER_INCLUDE_DOC_COMMENT_REFERENCE;
import static net.sf.iwant.api.javamodules.CodeStyle.UNUSED_PARAMETER_WHEN_IMPLEMENTING_ABSTRACT;
import static net.sf.iwant.api.javamodules.CodeStyle.UNUSED_PARAMETER_WHEN_OVERRIDING_CONCRETE;
import static net.sf.iwant.api.javamodules.CodeStyle.UNUSED_PRIVATE_MEMBER;
import static net.sf.iwant.api.javamodules.CodeStyle.UNUSED_WARNING_TOKEN;
import static net.sf.iwant.api.javamodules.CodeStyle.VARARGS_ARGUMENT_NEED_CAST;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CodeStylePolicy {

	private Map<CodeStyle, CodeStyleValue> settings;

	public CodeStylePolicy(Map<CodeStyle, CodeStyleValue> settings) {
		this.settings = Collections.unmodifiableMap(settings);
	}

	public static CodeStylePolicySpex defaultsExcept() {
		CodeStylePolicySpex d = new CodeStylePolicySpex();

		d.warn(ANNOTATION_SUPER_INTERFACE);
		d.fail(ASSERT_IDENTIFIER);
		d.ignore(AUTOBOXING);
		d.warn(COMPARING_IDENTICAL);
		d.warn(DEAD_CODE);
		d.warn(DEPRECATION);
		d.ignore(DEPRECATION_IN_DEPRECATED_CODE);
		d.ignore(DEPRECATION_WHEN_OVERRIDING_DEPRECATED_METHOD);
		d.warn(DISCOURAGED_REFERENCE);
		d.warn(EMPTY_STATEMENT);
		d.fail(ENUM_IDENTIFIER);
		d.warn(FALLTHROUGH_CASE);
		d.ignore(FATAL_OPTIONAL_ERROR);
		d.warn(FIELD_HIDING);
		d.warn(FINAL_PARAMETER_BOUND);
		d.warn(FINALLY_BLOCK_NOT_COMPLETING_NORMALLY);
		d.fail(FORBIDDEN_REFERENCE);
		d.warn(HIDDEN_CATCH_BLOCK);
		d.ignore(INCLUDE_NULL_INFO_FROM_ASSERTS);
		d.warn(INCOMPATIBLE_NON_INHERITED_INTERFACE_METHOD);
		d.warn(INCOMPLETE_ENUM_SWITCH);
		d.warn(INDIRECT_STATIC_ACCESS);
		d.ignore(LOCAL_VARIABLE_HIDING);
		d.warn(METHOD_WITH_CONSTRUCTOR_NAME);
		d.warn(MISSING_DEFAULT_CASE);
		d.warn(MISSING_DEPRECATED_ANNOTATION);
		d.warn(MISSING_HASHCODE_METHOD);
		d.warn(MISSING_OVERRIDE_ANNOTATION);
		d.warn(MISSING_OVERRIDE_ANNOTATION_FOR_INTERFACE_METHOD_IMPLEMENTATION);
		d.ignore(MISSING_SERIAL_VERSION);
		d.warn(MISSING_SYNCHRONIZED_ON_INHERITED_METHOD);
		d.warn(NO_EFFECT_ASSIGNMENT);
		d.warn(NO_IMPLICIT_STRING_CONVERSION);
		d.ignore(NON_EXTERNALIZED_STRING_LITERAL);
		d.warn(NULL_REFERENCE);
		d.warn(OVERRIDING_PACKAGE_DEFAULT_METHOD);
		d.warn(PARAMETER_ASSIGNMENT);
		d.warn(POSSIBLE_ACCIDENTAL_BOOLEAN_ASSIGNMENT);
		d.warn(POTENTIAL_NULL_REFERENCE);
		d.ignore(POTENTIALLY_UNCLOSED_CLOSEABLE);
		d.warn(RAW_TYPE_REFERENCE);
		d.warn(REDUNDANT_NULL_CHECK);
		d.warn(REDUNDANT_SPECIFICATION_OF_TYPE_ARGUMENTS);
		d.ignore(REDUNDANT_SUPERINTERFACE);
		d.ignore(REPORT_METHOD_CAN_BE_POTENTIALLY_STATIC);
		d.warn(REPORT_METHOD_CAN_BE_STATIC);
		d.ignore(SPECIAL_PARAMETER_HIDING_FIELD);
		d.warn(STATIC_ACCESS_RECEIVER);
		d.ignore(SUPPRESS_OPTIONAL_ERRORS);
		d.warn(SUPPRESS_WARNINGS);
		d.ignore(SYNTHETIC_ACCESS_EMULATION);
		d.warn(TYPE_PARAMETER_HIDING);
		d.warn(UNAVOIDABLE_GENERIC_TYPE_PROBLEMS);
		d.warn(UNCHECKED_TYPE_OPERATION);
		d.warn(UNCLOSED_CLOSEABLE);
		d.warn(UNDOCUMENTED_EMPTY_BLOCK);
		d.warn(UNHANDLED_WARNING_TOKEN);
		d.ignore(UNNECESSARY_ELSE);
		d.warn(UNNECESSARY_TYPE_CHECK);
		d.ignore(UNQUALIFIED_FIELD_ACCESS);
		d.warn(UNUSED_DECLARED_THROWN_EXCEPTION);
		d.warn(UNUSED_DECLARED_THROWN_EXCEPTION_EXEMPT_EXCEPTION_AND_THROWABLE);
		d.warn(UNUSED_DECLARED_THROWN_EXCEPTION_INCLUDE_DOC_COMMENT_REFERENCE);
		d.ignore(UNUSED_DECLARED_THROWN_EXCEPTION_WHEN_OVERRIDING);
		d.warn(UNUSED_IMPORT);
		d.warn(UNUSED_LABEL);
		d.warn(UNUSED_LOCAL);
		d.warn(UNUSED_OBJECT_ALLOCATION);
		d.warn(UNUSED_PARAMETER);
		d.warn(UNUSED_PARAMETER_INCLUDE_DOC_COMMENT_REFERENCE);
		d.ignore(UNUSED_PARAMETER_WHEN_IMPLEMENTING_ABSTRACT);
		d.ignore(UNUSED_PARAMETER_WHEN_OVERRIDING_CONCRETE);
		d.warn(UNUSED_PRIVATE_MEMBER);
		d.warn(UNUSED_WARNING_TOKEN);
		d.warn(VARARGS_ARGUMENT_NEED_CAST);

		return d;
	}

	public static class CodeStylePolicySpex {

		private final Map<CodeStyle, CodeStyleValue> settings = new HashMap<CodeStyle, CodeStyleValue>();

		public CodeStylePolicySpex ignore(CodeStyle... styles) {
			return mappedTo(CodeStyleValue.IGNORE, styles);
		}

		public CodeStylePolicySpex warn(CodeStyle... styles) {
			return mappedTo(CodeStyleValue.WARN, styles);
		}

		public CodeStylePolicySpex fail(CodeStyle... styles) {
			return mappedTo(CodeStyleValue.FAIL, styles);
		}

		private CodeStylePolicySpex mappedTo(CodeStyleValue value,
				CodeStyle... styles) {
			for (CodeStyle style : styles) {
				settings.put(style, value);
			}
			return this;
		}

		public CodeStylePolicy end() {
			return new CodeStylePolicy(settings);
		}

	}

	public CodeStyleValue valueOf(CodeStyle style) {
		return settings.get(style);
	}

}
