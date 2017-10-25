package org.fluentjava.iwant.eclipsesettings;

import org.fluentjava.iwant.api.javamodules.CodeFormatterPolicy;
import org.fluentjava.iwant.api.javamodules.CodeStyle;
import org.fluentjava.iwant.api.javamodules.CodeStylePolicy;
import org.fluentjava.iwant.api.javamodules.CodeStyleValue;
import org.fluentjava.iwant.api.javamodules.JavaCompliance;

public class OrgEclipseJdtCorePrefs {

	private final CodeStylePolicy codeStylePolicy;
	private final CodeFormatterPolicy formatterPolicy;
	private final JavaCompliance javaCompliance;

	public OrgEclipseJdtCorePrefs(CodeStylePolicy codeStylePolicy,
			CodeFormatterPolicy formatterPolicy,
			JavaCompliance javaCompliance) {
		this.codeStylePolicy = codeStylePolicy;
		this.formatterPolicy = formatterPolicy;
		this.javaCompliance = javaCompliance;
	}

	private static String asPropertyLine(CodeStyle style,
			CodeStyleValue value) {
		switch (style) {
		case ANNOTATION_SUPER_INTERFACE:
			return ignoreable("annotationSuperInterface", value);
		case ASSERT_IDENTIFIER:
			return ignoreable("assertIdentifier", value);
		case AUTOBOXING:
			return ignoreable("autoboxing", value);
		case COMPARING_IDENTICAL:
			return ignoreable("comparingIdentical", value);
		case DEAD_CODE:
			return ignoreable("deadCode", value);
		case DEPRECATION:
			return ignoreable("deprecation", value);
		case DEPRECATION_IN_DEPRECATED_CODE:
			return disableable("deprecationInDeprecatedCode", value);
		case DEPRECATION_WHEN_OVERRIDING_DEPRECATED_METHOD:
			return disableable("deprecationWhenOverridingDeprecatedMethod",
					value);
		case DISCOURAGED_REFERENCE:
			return ignoreable("discouragedReference", value);
		case EMPTY_STATEMENT:
			return ignoreable("emptyStatement", value);
		case ENUM_IDENTIFIER:
			return ignoreable("enumIdentifier", value);
		case FALLTHROUGH_CASE:
			return ignoreable("fallthroughCase", value);
		case FATAL_OPTIONAL_ERROR:
			return disableable("fatalOptionalError", value);
		case FIELD_HIDING:
			return ignoreable("fieldHiding", value);
		case FINAL_PARAMETER_BOUND:
			return ignoreable("finalParameterBound", value);
		case FINALLY_BLOCK_NOT_COMPLETING_NORMALLY:
			return ignoreable("finallyBlockNotCompletingNormally", value);
		case FORBIDDEN_REFERENCE:
			return ignoreable("forbiddenReference", value);
		case HIDDEN_CATCH_BLOCK:
			return ignoreable("hiddenCatchBlock", value);
		case INCLUDE_NULL_INFO_FROM_ASSERTS:
			return disableable("includeNullInfoFromAsserts", value);
		case INCOMPATIBLE_NON_INHERITED_INTERFACE_METHOD:
			return ignoreable("incompatibleNonInheritedInterfaceMethod", value);
		case INCOMPLETE_ENUM_SWITCH:
			return ignoreable("incompleteEnumSwitch", value);
		case INDIRECT_STATIC_ACCESS:
			return ignoreable("indirectStaticAccess", value);
		case LOCAL_VARIABLE_HIDING:
			return ignoreable("localVariableHiding", value);
		case METHOD_WITH_CONSTRUCTOR_NAME:
			return ignoreable("methodWithConstructorName", value);
		case MISSING_DEFAULT_CASE:
			return ignoreable("missingDefaultCase", value);
		case MISSING_DEPRECATED_ANNOTATION:
			return ignoreable("missingDeprecatedAnnotation", value);
		case MISSING_HASHCODE_METHOD:
			return ignoreable("missingHashCodeMethod", value);
		case MISSING_OVERRIDE_ANNOTATION:
			return ignoreable("missingOverrideAnnotation", value);
		case MISSING_OVERRIDE_ANNOTATION_FOR_INTERFACE_METHOD_IMPLEMENTATION:
			return disableable(
					"missingOverrideAnnotationForInterfaceMethodImplementation",
					value);
		case MISSING_SERIAL_VERSION:
			return ignoreable("missingSerialVersion", value);
		case MISSING_SYNCHRONIZED_ON_INHERITED_METHOD:
			return ignoreable("missingSynchronizedOnInheritedMethod", value);
		case NO_EFFECT_ASSIGNMENT:
			return ignoreable("noEffectAssignment", value);
		case NO_IMPLICIT_STRING_CONVERSION:
			return ignoreable("noImplicitStringConversion", value);
		case NON_EXTERNALIZED_STRING_LITERAL:
			return ignoreable("nonExternalizedStringLiteral", value);
		case NULL_REFERENCE:
			return ignoreable("nullReference", value);
		case OVERRIDING_PACKAGE_DEFAULT_METHOD:
			return ignoreable("overridingPackageDefaultMethod", value);
		case PARAMETER_ASSIGNMENT:
			return ignoreable("parameterAssignment", value);
		case POSSIBLE_ACCIDENTAL_BOOLEAN_ASSIGNMENT:
			return ignoreable("possibleAccidentalBooleanAssignment", value);
		case POTENTIAL_NULL_REFERENCE:
			return ignoreable("potentialNullReference", value);
		case POTENTIALLY_UNCLOSED_CLOSEABLE:
			return ignoreable("potentiallyUnclosedCloseable", value);
		case RAW_TYPE_REFERENCE:
			return ignoreable("rawTypeReference", value);
		case REDUNDANT_NULL_CHECK:
			return ignoreable("redundantNullCheck", value);
		case REDUNDANT_SPECIFICATION_OF_TYPE_ARGUMENTS:
			return ignoreable("redundantSpecificationOfTypeArguments", value);
		case REDUNDANT_SUPERINTERFACE:
			return ignoreable("redundantSuperinterface", value);
		case REPORT_METHOD_CAN_BE_POTENTIALLY_STATIC:
			return ignoreable("reportMethodCanBePotentiallyStatic", value);
		case REPORT_METHOD_CAN_BE_STATIC:
			return ignoreable("reportMethodCanBeStatic", value);
		case SPECIAL_PARAMETER_HIDING_FIELD:
			return disableable("specialParameterHidingField", value);
		case STATIC_ACCESS_RECEIVER:
			return ignoreable("staticAccessReceiver", value);
		case SUPPRESS_OPTIONAL_ERRORS:
			return disableable("suppressOptionalErrors", value);
		case SUPPRESS_WARNINGS:
			return disableable("suppressWarnings", value);
		case SYNTHETIC_ACCESS_EMULATION:
			return ignoreable("syntheticAccessEmulation", value);
		case TYPE_PARAMETER_HIDING:
			return ignoreable("typeParameterHiding", value);
		case UNAVOIDABLE_GENERIC_TYPE_PROBLEMS:
			return disableable("unavoidableGenericTypeProblems", value);
		case UNCHECKED_TYPE_OPERATION:
			return ignoreable("uncheckedTypeOperation", value);
		case UNCLOSED_CLOSEABLE:
			return ignoreable("unclosedCloseable", value);
		case UNDOCUMENTED_EMPTY_BLOCK:
			return ignoreable("undocumentedEmptyBlock", value);
		case UNHANDLED_WARNING_TOKEN:
			return ignoreable("unhandledWarningToken", value);
		case UNNECESSARY_ELSE:
			return ignoreable("unnecessaryElse", value);
		case UNNECESSARY_TYPE_CHECK:
			return ignoreable("unnecessaryTypeCheck", value);
		case UNQUALIFIED_FIELD_ACCESS:
			return ignoreable("unqualifiedFieldAccess", value);
		case UNUSED_DECLARED_THROWN_EXCEPTION:
			return ignoreable("unusedDeclaredThrownException", value);
		case UNUSED_DECLARED_THROWN_EXCEPTION_EXEMPT_EXCEPTION_AND_THROWABLE:
			return disableable(
					"unusedDeclaredThrownExceptionExemptExceptionAndThrowable",
					value);
		case UNUSED_DECLARED_THROWN_EXCEPTION_INCLUDE_DOC_COMMENT_REFERENCE:
			return disableable(
					"unusedDeclaredThrownExceptionIncludeDocCommentReference",
					value);
		case UNUSED_DECLARED_THROWN_EXCEPTION_WHEN_OVERRIDING:
			return disableable("unusedDeclaredThrownExceptionWhenOverriding",
					value);
		case UNUSED_IMPORT:
			return ignoreable("unusedImport", value);
		case UNUSED_LABEL:
			return ignoreable("unusedLabel", value);
		case UNUSED_LOCAL:
			return ignoreable("unusedLocal", value);
		case UNUSED_OBJECT_ALLOCATION:
			return ignoreable("unusedObjectAllocation", value);
		case UNUSED_PARAMETER:
			return ignoreable("unusedParameter", value);
		case UNUSED_PARAMETER_INCLUDE_DOC_COMMENT_REFERENCE:
			return disableable("unusedParameterIncludeDocCommentReference",
					value);
		case UNUSED_PARAMETER_WHEN_IMPLEMENTING_ABSTRACT:
			return disableable("unusedParameterWhenImplementingAbstract",
					value);
		case UNUSED_PARAMETER_WHEN_OVERRIDING_CONCRETE:
			return disableable("unusedParameterWhenOverridingConcrete", value);
		case UNUSED_PRIVATE_MEMBER:
			return ignoreable("unusedPrivateMember", value);
		case UNUSED_TYPE_PARAMETER:
			return ignoreable("unusedTypeParameter", value);
		case UNUSED_WARNING_TOKEN:
			return ignoreable("unusedWarningToken", value);
		case VARARGS_ARGUMENT_NEED_CAST:
			return ignoreable("varargsArgumentNeedCast", value);
		default:
			throw new UnsupportedOperationException(
					"Unsupported style: " + style);
		}
	}

	public String asPropertyLine(CodeStyle style) {
		return asPropertyLine(style, codeStylePolicy.valueOf(style));
	}

	private static String ignoreable(String key, CodeStyleValue value) {
		return propertyLine(key, value, false);
	}

	private static String propertyLine(String key, CodeStyleValue value,
			boolean disableable) {
		return "org.eclipse.jdt.core.compiler.problem." + key + "="
				+ valueToEclipseValue(disableable, value) + "\n";
	}

	private static String disableable(String key, CodeStyleValue value) {
		return propertyLine(key, value, true);
	}

	public static String valueToEclipseValue(boolean disableInsteadOfIgnore,
			CodeStyleValue value) {
		switch (value) {
		case FAIL:
			return disableInsteadOfIgnore ? "enabled" : "error";
		case IGNORE:
			return disableInsteadOfIgnore ? "disabled" : "ignore";
		case WARN:
			return disableInsteadOfIgnore ? "enabled" : "warning";
		default:
			throw new UnsupportedOperationException(
					"Unsupported value " + value);
		}
	}

	public String asFileContent() {
		StringBuilder b = new StringBuilder();
		b.append("#Fri Jan 13 10:19:42 EET 2012\n");
		b.append("eclipse.preferences.version=1\n");
		b.append(
				"org.eclipse.jdt.core.compiler.codegen.inlineJsrBytecode=enabled\n");
		b.append("org.eclipse.jdt.core.compiler.codegen.targetPlatform="
				+ javaCompliance.prettyName() + "\n");
		b.append(
				"org.eclipse.jdt.core.compiler.codegen.unusedLocal=preserve\n");
		b.append("org.eclipse.jdt.core.compiler.compliance="
				+ javaCompliance.prettyName() + "\n");
		b.append("org.eclipse.jdt.core.compiler.debug.lineNumber=generate\n");
		b.append(
				"org.eclipse.jdt.core.compiler.debug.localVariable=generate\n");
		b.append("org.eclipse.jdt.core.compiler.debug.sourceFile=generate\n");

		for (CodeStyle style : CodeStyle.values()) {
			b.append(asPropertyLine(style));
		}

		b.append("org.eclipse.jdt.core.compiler.source="
				+ javaCompliance.prettyName() + "\n");
		b.append(
				"org.eclipse.jdt.core.formatter.align_type_members_on_columns=false\n");
		b.append(
				"org.eclipse.jdt.core.formatter.alignment_for_arguments_in_allocation_expression=16\n");
		b.append(
				"org.eclipse.jdt.core.formatter.alignment_for_arguments_in_annotation=0\n");
		b.append(
				"org.eclipse.jdt.core.formatter.alignment_for_arguments_in_enum_constant=16\n");
		b.append(
				"org.eclipse.jdt.core.formatter.alignment_for_arguments_in_explicit_constructor_call=16\n");
		b.append(
				"org.eclipse.jdt.core.formatter.alignment_for_arguments_in_method_invocation=16\n");
		b.append(
				"org.eclipse.jdt.core.formatter.alignment_for_arguments_in_qualified_allocation_expression=16\n");
		b.append("org.eclipse.jdt.core.formatter.alignment_for_assignment=0\n");
		b.append(
				"org.eclipse.jdt.core.formatter.alignment_for_binary_expression=16\n");
		b.append(
				"org.eclipse.jdt.core.formatter.alignment_for_compact_if=16\n");
		b.append(
				"org.eclipse.jdt.core.formatter.alignment_for_conditional_expression=80\n");
		b.append("org.eclipse.jdt.core.formatter.alignment_for_enum_constants="
				+ formatterPolicy.alignmentForEnumConstants + "\n");
		b.append(
				"org.eclipse.jdt.core.formatter.alignment_for_expressions_in_array_initializer=16\n");
		b.append(
				"org.eclipse.jdt.core.formatter.alignment_for_method_declaration=0\n");
		b.append(
				"org.eclipse.jdt.core.formatter.alignment_for_multiple_fields=16\n");
		b.append(
				"org.eclipse.jdt.core.formatter.alignment_for_parameters_in_constructor_declaration=16\n");
		b.append(
				"org.eclipse.jdt.core.formatter.alignment_for_parameters_in_method_declaration=16\n");
		b.append(
				"org.eclipse.jdt.core.formatter.alignment_for_resources_in_try=80\n");
		b.append(
				"org.eclipse.jdt.core.formatter.alignment_for_selector_in_method_invocation=16\n");
		b.append(
				"org.eclipse.jdt.core.formatter.alignment_for_superclass_in_type_declaration=16\n");
		b.append(
				"org.eclipse.jdt.core.formatter.alignment_for_superinterfaces_in_enum_declaration=16\n");
		b.append(
				"org.eclipse.jdt.core.formatter.alignment_for_superinterfaces_in_type_declaration=16\n");
		b.append(
				"org.eclipse.jdt.core.formatter.alignment_for_throws_clause_in_constructor_declaration=16\n");
		b.append(
				"org.eclipse.jdt.core.formatter.alignment_for_throws_clause_in_method_declaration=16\n");
		b.append(
				"org.eclipse.jdt.core.formatter.alignment_for_union_type_in_multicatch=16\n");
		b.append(
				"org.eclipse.jdt.core.formatter.blank_lines_after_imports=1\n");
		b.append(
				"org.eclipse.jdt.core.formatter.blank_lines_after_package=1\n");
		b.append("org.eclipse.jdt.core.formatter.blank_lines_before_field=0\n");
		b.append(
				"org.eclipse.jdt.core.formatter.blank_lines_before_first_class_body_declaration=0\n");
		b.append(
				"org.eclipse.jdt.core.formatter.blank_lines_before_imports=1\n");
		b.append(
				"org.eclipse.jdt.core.formatter.blank_lines_before_member_type=1\n");
		b.append(
				"org.eclipse.jdt.core.formatter.blank_lines_before_method=1\n");
		b.append(
				"org.eclipse.jdt.core.formatter.blank_lines_before_new_chunk=1\n");
		b.append(
				"org.eclipse.jdt.core.formatter.blank_lines_before_package=0\n");
		b.append(
				"org.eclipse.jdt.core.formatter.blank_lines_between_import_groups=1\n");
		b.append(
				"org.eclipse.jdt.core.formatter.blank_lines_between_type_declarations=1\n");
		b.append(
				"org.eclipse.jdt.core.formatter.brace_position_for_annotation_type_declaration=end_of_line\n");
		b.append(
				"org.eclipse.jdt.core.formatter.brace_position_for_anonymous_type_declaration=end_of_line\n");
		b.append(
				"org.eclipse.jdt.core.formatter.brace_position_for_array_initializer=end_of_line\n");
		b.append(
				"org.eclipse.jdt.core.formatter.brace_position_for_block=end_of_line\n");
		b.append(
				"org.eclipse.jdt.core.formatter.brace_position_for_block_in_case=end_of_line\n");
		b.append(
				"org.eclipse.jdt.core.formatter.brace_position_for_constructor_declaration=end_of_line\n");
		b.append(
				"org.eclipse.jdt.core.formatter.brace_position_for_enum_constant=end_of_line\n");
		b.append(
				"org.eclipse.jdt.core.formatter.brace_position_for_enum_declaration=end_of_line\n");
		b.append(
				"org.eclipse.jdt.core.formatter.brace_position_for_method_declaration=end_of_line\n");
		b.append(
				"org.eclipse.jdt.core.formatter.brace_position_for_switch=end_of_line\n");
		b.append(
				"org.eclipse.jdt.core.formatter.brace_position_for_type_declaration=end_of_line\n");
		b.append(
				"org.eclipse.jdt.core.formatter.comment.clear_blank_lines_in_block_comment=false\n");
		b.append(
				"org.eclipse.jdt.core.formatter.comment.clear_blank_lines_in_javadoc_comment=false\n");
		b.append(
				"org.eclipse.jdt.core.formatter.comment.format_block_comments=true\n");
		b.append(
				"org.eclipse.jdt.core.formatter.comment.format_header=false\n");
		b.append("org.eclipse.jdt.core.formatter.comment.format_html=true\n");
		b.append(
				"org.eclipse.jdt.core.formatter.comment.format_javadoc_comments=true\n");
		b.append(
				"org.eclipse.jdt.core.formatter.comment.format_line_comments=true\n");
		b.append(
				"org.eclipse.jdt.core.formatter.comment.format_source_code=true\n");
		b.append(
				"org.eclipse.jdt.core.formatter.comment.indent_parameter_description=true\n");
		b.append(
				"org.eclipse.jdt.core.formatter.comment.indent_root_tags=true\n");
		b.append(
				"org.eclipse.jdt.core.formatter.comment.insert_new_line_before_root_tags=insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.comment.insert_new_line_for_parameter=insert\n");
		b.append("org.eclipse.jdt.core.formatter.comment.line_length=80\n");
		b.append(
				"org.eclipse.jdt.core.formatter.comment.new_lines_at_block_boundaries=true\n");
		b.append(
				"org.eclipse.jdt.core.formatter.comment.new_lines_at_javadoc_boundaries=true\n");
		b.append(
				"org.eclipse.jdt.core.formatter.comment.preserve_white_space_between_code_and_line_comments=false\n");
		b.append("org.eclipse.jdt.core.formatter.compact_else_if=true\n");
		b.append("org.eclipse.jdt.core.formatter.continuation_indentation=2\n");
		b.append(
				"org.eclipse.jdt.core.formatter.continuation_indentation_for_array_initializer=2\n");
		b.append(
				"org.eclipse.jdt.core.formatter.disabling_tag=@formatter\\:off\n");
		b.append(
				"org.eclipse.jdt.core.formatter.enabling_tag=@formatter\\:on\n");
		b.append(
				"org.eclipse.jdt.core.formatter.format_guardian_clause_on_one_line=false\n");
		b.append(
				"org.eclipse.jdt.core.formatter.format_line_comment_starting_on_first_column=true\n");
		b.append(
				"org.eclipse.jdt.core.formatter.indent_body_declarations_compare_to_annotation_declaration_header=true\n");
		b.append(
				"org.eclipse.jdt.core.formatter.indent_body_declarations_compare_to_enum_constant_header=true\n");
		b.append(
				"org.eclipse.jdt.core.formatter.indent_body_declarations_compare_to_enum_declaration_header=true\n");
		b.append(
				"org.eclipse.jdt.core.formatter.indent_body_declarations_compare_to_type_header=true\n");
		b.append(
				"org.eclipse.jdt.core.formatter.indent_breaks_compare_to_cases=true\n");
		b.append("org.eclipse.jdt.core.formatter.indent_empty_lines=false\n");
		b.append(
				"org.eclipse.jdt.core.formatter.indent_statements_compare_to_block=true\n");
		b.append(
				"org.eclipse.jdt.core.formatter.indent_statements_compare_to_body=true\n");
		b.append(
				"org.eclipse.jdt.core.formatter.indent_switchstatements_compare_to_cases=true\n");
		b.append(
				"org.eclipse.jdt.core.formatter.indent_switchstatements_compare_to_switch=false\n");
		b.append("org.eclipse.jdt.core.formatter.indentation.size=4\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_new_line_after_annotation_on_field=insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_new_line_after_annotation_on_local_variable=insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_new_line_after_annotation_on_method=insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_new_line_after_annotation_on_package=insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_new_line_after_annotation_on_parameter=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_new_line_after_annotation_on_type=insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_new_line_after_label=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_new_line_after_opening_brace_in_array_initializer=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_new_line_at_end_of_file_if_missing=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_new_line_before_catch_in_try_statement=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_new_line_before_closing_brace_in_array_initializer=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_new_line_before_else_in_if_statement=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_new_line_before_finally_in_try_statement=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_new_line_before_while_in_do_statement=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_new_line_in_empty_annotation_declaration=insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_new_line_in_empty_anonymous_type_declaration=insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_new_line_in_empty_block=insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_new_line_in_empty_enum_constant=insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_new_line_in_empty_enum_declaration=insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_new_line_in_empty_method_body=insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_new_line_in_empty_type_declaration=insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_after_and_in_type_parameter=insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_after_assignment_operator=insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_after_at_in_annotation=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_after_at_in_annotation_type_declaration=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_after_binary_operator=insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_after_closing_angle_bracket_in_type_arguments=insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_after_closing_angle_bracket_in_type_parameters=insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_after_closing_brace_in_block=insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_after_closing_paren_in_cast=insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_after_colon_in_assert=insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_after_colon_in_case=insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_after_colon_in_conditional=insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_after_colon_in_for=insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_after_colon_in_labeled_statement=insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_after_comma_in_allocation_expression=insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_after_comma_in_annotation=insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_after_comma_in_array_initializer=insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_after_comma_in_constructor_declaration_parameters=insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_after_comma_in_constructor_declaration_throws=insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_after_comma_in_enum_constant_arguments=insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_after_comma_in_enum_declarations=insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_after_comma_in_explicitconstructorcall_arguments=insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_after_comma_in_for_increments=insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_after_comma_in_for_inits=insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_after_comma_in_method_declaration_parameters=insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_after_comma_in_method_declaration_throws=insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_after_comma_in_method_invocation_arguments=insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_after_comma_in_multiple_field_declarations=insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_after_comma_in_multiple_local_declarations=insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_after_comma_in_parameterized_type_reference=insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_after_comma_in_superinterfaces=insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_after_comma_in_type_arguments=insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_after_comma_in_type_parameters=insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_after_ellipsis=insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_after_opening_angle_bracket_in_parameterized_type_reference=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_after_opening_angle_bracket_in_type_arguments=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_after_opening_angle_bracket_in_type_parameters=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_after_opening_brace_in_array_initializer=insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_after_opening_bracket_in_array_allocation_expression=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_after_opening_bracket_in_array_reference=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_after_opening_paren_in_annotation=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_after_opening_paren_in_cast=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_after_opening_paren_in_catch=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_after_opening_paren_in_constructor_declaration=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_after_opening_paren_in_enum_constant=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_after_opening_paren_in_for=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_after_opening_paren_in_if=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_after_opening_paren_in_method_declaration=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_after_opening_paren_in_method_invocation=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_after_opening_paren_in_parenthesized_expression=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_after_opening_paren_in_switch=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_after_opening_paren_in_synchronized=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_after_opening_paren_in_try=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_after_opening_paren_in_while=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_after_postfix_operator=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_after_prefix_operator=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_after_question_in_conditional=insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_after_question_in_wildcard=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_after_semicolon_in_for=insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_after_semicolon_in_try_resources=insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_after_unary_operator=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_before_and_in_type_parameter=insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_before_assignment_operator=insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_before_at_in_annotation_type_declaration=insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_before_binary_operator=insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_before_closing_angle_bracket_in_parameterized_type_reference=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_before_closing_angle_bracket_in_type_arguments=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_before_closing_angle_bracket_in_type_parameters=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_before_closing_brace_in_array_initializer=insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_before_closing_bracket_in_array_allocation_expression=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_before_closing_bracket_in_array_reference=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_before_closing_paren_in_annotation=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_before_closing_paren_in_cast=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_before_closing_paren_in_catch=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_before_closing_paren_in_constructor_declaration=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_before_closing_paren_in_enum_constant=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_before_closing_paren_in_for=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_before_closing_paren_in_if=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_before_closing_paren_in_method_declaration=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_before_closing_paren_in_method_invocation=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_before_closing_paren_in_parenthesized_expression=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_before_closing_paren_in_switch=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_before_closing_paren_in_synchronized=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_before_closing_paren_in_try=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_before_closing_paren_in_while=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_before_colon_in_assert=insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_before_colon_in_case=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_before_colon_in_conditional=insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_before_colon_in_default=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_before_colon_in_for=insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_before_colon_in_labeled_statement=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_before_comma_in_allocation_expression=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_before_comma_in_annotation=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_before_comma_in_array_initializer=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_before_comma_in_constructor_declaration_parameters=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_before_comma_in_constructor_declaration_throws=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_before_comma_in_enum_constant_arguments=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_before_comma_in_enum_declarations=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_before_comma_in_explicitconstructorcall_arguments=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_before_comma_in_for_increments=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_before_comma_in_for_inits=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_before_comma_in_method_declaration_parameters=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_before_comma_in_method_declaration_throws=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_before_comma_in_method_invocation_arguments=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_before_comma_in_multiple_field_declarations=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_before_comma_in_multiple_local_declarations=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_before_comma_in_parameterized_type_reference=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_before_comma_in_superinterfaces=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_before_comma_in_type_arguments=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_before_comma_in_type_parameters=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_before_ellipsis=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_before_opening_angle_bracket_in_parameterized_type_reference=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_before_opening_angle_bracket_in_type_arguments=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_before_opening_angle_bracket_in_type_parameters=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_before_opening_brace_in_annotation_type_declaration=insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_before_opening_brace_in_anonymous_type_declaration=insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_before_opening_brace_in_array_initializer=insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_before_opening_brace_in_block=insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_before_opening_brace_in_constructor_declaration=insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_before_opening_brace_in_enum_constant=insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_before_opening_brace_in_enum_declaration=insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_before_opening_brace_in_method_declaration=insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_before_opening_brace_in_switch=insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_before_opening_brace_in_type_declaration=insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_before_opening_bracket_in_array_allocation_expression=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_before_opening_bracket_in_array_reference=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_before_opening_bracket_in_array_type_reference=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_before_opening_paren_in_annotation=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_before_opening_paren_in_annotation_type_member_declaration=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_before_opening_paren_in_catch=insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_before_opening_paren_in_constructor_declaration=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_before_opening_paren_in_enum_constant=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_before_opening_paren_in_for=insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_before_opening_paren_in_if=insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_before_opening_paren_in_method_declaration=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_before_opening_paren_in_method_invocation=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_before_opening_paren_in_parenthesized_expression=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_before_opening_paren_in_switch=insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_before_opening_paren_in_synchronized=insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_before_opening_paren_in_try=insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_before_opening_paren_in_while=insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_before_parenthesized_expression_in_return=insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_before_parenthesized_expression_in_throw=insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_before_postfix_operator=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_before_prefix_operator=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_before_question_in_conditional=insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_before_question_in_wildcard=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_before_semicolon=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_before_semicolon_in_for=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_before_semicolon_in_try_resources=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_before_unary_operator=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_between_brackets_in_array_type_reference=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_between_empty_braces_in_array_initializer=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_between_empty_brackets_in_array_allocation_expression=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_between_empty_parens_in_annotation_type_member_declaration=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_between_empty_parens_in_constructor_declaration=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_between_empty_parens_in_enum_constant=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_between_empty_parens_in_method_declaration=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.insert_space_between_empty_parens_in_method_invocation=do not insert\n");
		b.append(
				"org.eclipse.jdt.core.formatter.join_lines_in_comments=true\n");
		b.append("org.eclipse.jdt.core.formatter.join_wrapped_lines=true\n");
		b.append(
				"org.eclipse.jdt.core.formatter.keep_else_statement_on_same_line=false\n");
		b.append(
				"org.eclipse.jdt.core.formatter.keep_empty_array_initializer_on_one_line=false\n");
		b.append(
				"org.eclipse.jdt.core.formatter.keep_imple_if_on_one_line=false\n");
		b.append(
				"org.eclipse.jdt.core.formatter.keep_then_statement_on_same_line=false\n");
		b.append("org.eclipse.jdt.core.formatter.lineSplit="
				+ formatterPolicy.lineSplit + "\n");
		b.append(
				"org.eclipse.jdt.core.formatter.never_indent_block_comments_on_first_column=false\n");
		b.append(
				"org.eclipse.jdt.core.formatter.never_indent_line_comments_on_first_column=false\n");
		b.append(
				"org.eclipse.jdt.core.formatter.number_of_blank_lines_at_beginning_of_method_body=0\n");
		b.append(
				"org.eclipse.jdt.core.formatter.number_of_empty_lines_to_preserve=1\n");
		b.append(
				"org.eclipse.jdt.core.formatter.put_empty_statement_on_new_line=true\n");
		b.append("org.eclipse.jdt.core.formatter.tabulation.char="
				+ formatterPolicy.tabulationChar.name().toLowerCase() + "\n");
		b.append("org.eclipse.jdt.core.formatter.tabulation.size=4\n");
		b.append("org.eclipse.jdt.core.formatter.use_on_off_tags=false\n");
		b.append(
				"org.eclipse.jdt.core.formatter.use_tabs_only_for_leading_indentations=false\n");
		b.append(
				"org.eclipse.jdt.core.formatter.wrap_before_binary_operator=true\n");
		b.append(
				"org.eclipse.jdt.core.formatter.wrap_before_or_operator_multicatch=true\n");
		b.append(
				"org.eclipse.jdt.core.formatter.wrap_outer_expressions_when_nested=true\n");
		return b.toString();
	}

	public CodeFormatterPolicy codeFormatterPolicy() {
		return formatterPolicy;
	}

}
