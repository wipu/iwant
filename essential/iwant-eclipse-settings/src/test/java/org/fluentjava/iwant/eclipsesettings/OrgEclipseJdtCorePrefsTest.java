package org.fluentjava.iwant.eclipsesettings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.fluentjava.iwant.api.javamodules.CodeFormatterPolicy;
import org.fluentjava.iwant.api.javamodules.CodeFormatterPolicy.TabulationCharValue;
import org.fluentjava.iwant.api.javamodules.CodeStyle;
import org.fluentjava.iwant.api.javamodules.CodeStylePolicy;
import org.fluentjava.iwant.api.javamodules.CodeStylePolicy.CodeStylePolicySpex;
import org.fluentjava.iwant.api.javamodules.JavaCompliance;
import org.junit.jupiter.api.Test;

public class OrgEclipseJdtCorePrefsTest {

	@Test
	public void defaultCodeStyle() {
		CodeStylePolicySpex policy = CodeStylePolicy.defaultsExcept();
		CodeFormatterPolicy formatter = new CodeFormatterPolicy();

		OrgEclipseJdtCorePrefs prefs = new OrgEclipseJdtCorePrefs(policy.end(),
				formatter, JavaCompliance.JAVA_1_6, false);

		assertEquals("org.eclipse.jdt.core.compiler.problem.deadCode=warning\n",
				prefs.asPropertyLine(CodeStyle.DEAD_CODE));
		assertEquals(
				"org.eclipse.jdt.core.compiler.problem.deprecationInDeprecatedCode=disabled\n",
				prefs.asPropertyLine(CodeStyle.DEPRECATION_IN_DEPRECATED_CODE));
		assertEquals(
				"org.eclipse.jdt.core.compiler.problem.enumIdentifier=error\n",
				prefs.asPropertyLine(CodeStyle.ENUM_IDENTIFIER));
		assertEquals(
				"org.eclipse.jdt.core.compiler.problem.nonExternalizedStringLiteral=ignore\n",
				prefs.asPropertyLine(
						CodeStyle.NON_EXTERNALIZED_STRING_LITERAL));
	}

	@Test
	public void overriddenCodeStyle() {
		CodeStylePolicySpex policy = CodeStylePolicy.defaultsExcept();
		policy.ignore(CodeStyle.DEAD_CODE);
		policy.warn(CodeStyle.NON_EXTERNALIZED_STRING_LITERAL);
		CodeFormatterPolicy formatter = new CodeFormatterPolicy();

		OrgEclipseJdtCorePrefs prefs = new OrgEclipseJdtCorePrefs(policy.end(),
				formatter, JavaCompliance.JAVA_1_6, false);

		assertEquals("org.eclipse.jdt.core.compiler.problem.deadCode=ignore\n",
				prefs.asPropertyLine(CodeStyle.DEAD_CODE));
		assertEquals(
				"org.eclipse.jdt.core.compiler.problem.deprecationInDeprecatedCode=disabled\n",
				prefs.asPropertyLine(CodeStyle.DEPRECATION_IN_DEPRECATED_CODE));
		assertEquals(
				"org.eclipse.jdt.core.compiler.problem.enumIdentifier=error\n",
				prefs.asPropertyLine(CodeStyle.ENUM_IDENTIFIER));
		assertEquals(
				"org.eclipse.jdt.core.compiler.problem.nonExternalizedStringLiteral=warning\n",
				prefs.asPropertyLine(
						CodeStyle.NON_EXTERNALIZED_STRING_LITERAL));
	}

	@Test
	public void aStyleThatIsOnlyDisabledOrEnabled() {
		CodeStyle style = CodeStyle.MISSING_OVERRIDE_ANNOTATION_FOR_INTERFACE_METHOD_IMPLEMENTATION;
		CodeFormatterPolicy formatter = new CodeFormatterPolicy();
		assertEquals("org.eclipse.jdt.core.compiler.problem."
				+ "missingOverrideAnnotationForInterfaceMethodImplementation=disabled\n",
				new OrgEclipseJdtCorePrefs(
						CodeStylePolicy.defaultsExcept().ignore(style).end(),
						formatter, JavaCompliance.JAVA_1_6, false)
								.asPropertyLine(style));
		assertEquals("org.eclipse.jdt.core.compiler.problem."
				+ "missingOverrideAnnotationForInterfaceMethodImplementation=enabled\n",
				new OrgEclipseJdtCorePrefs(
						CodeStylePolicy.defaultsExcept().warn(style).end(),
						formatter, JavaCompliance.JAVA_1_6, false)
								.asPropertyLine(style));
		assertEquals("org.eclipse.jdt.core.compiler.problem."
				+ "missingOverrideAnnotationForInterfaceMethodImplementation=enabled\n",
				new OrgEclipseJdtCorePrefs(
						CodeStylePolicy.defaultsExcept().fail(style).end(),
						formatter, JavaCompliance.JAVA_1_6, false)
								.asPropertyLine(style));
	}

	@Test
	public void defaultsAsFileContent() {
		CodeStylePolicySpex policy = CodeStylePolicy.defaultsExcept();
		CodeFormatterPolicy formatter = new CodeFormatterPolicy();

		OrgEclipseJdtCorePrefs prefs = new OrgEclipseJdtCorePrefs(policy.end(),
				formatter, JavaCompliance.JAVA_1_6, false);

		StringBuilder b = new StringBuilder();
		b.append("#Fri Jan 13 10:19:42 EET 2012\n");
		b.append("eclipse.preferences.version=1\n");
		b.append(
				"org.eclipse.jdt.core.compiler.codegen.inlineJsrBytecode=enabled\n");
		b.append("org.eclipse.jdt.core.compiler.codegen.targetPlatform=1.6\n");
		b.append(
				"org.eclipse.jdt.core.compiler.codegen.unusedLocal=preserve\n");
		b.append("org.eclipse.jdt.core.compiler.compliance=1.6\n");
		b.append("org.eclipse.jdt.core.compiler.debug.lineNumber=generate\n");
		b.append(
				"org.eclipse.jdt.core.compiler.debug.localVariable=generate\n");
		b.append("org.eclipse.jdt.core.compiler.debug.sourceFile=generate\n");
		b.append(
				"org.eclipse.jdt.core.compiler.problem.annotationSuperInterface=warning\n");
		b.append(
				"org.eclipse.jdt.core.compiler.problem.assertIdentifier=error\n");
		b.append("org.eclipse.jdt.core.compiler.problem.autoboxing=ignore\n");
		b.append(
				"org.eclipse.jdt.core.compiler.problem.comparingIdentical=warning\n");
		b.append("org.eclipse.jdt.core.compiler.problem.deadCode=warning\n");
		b.append("org.eclipse.jdt.core.compiler.problem.deprecation=warning\n");
		b.append(
				"org.eclipse.jdt.core.compiler.problem.deprecationInDeprecatedCode=disabled\n");
		b.append(
				"org.eclipse.jdt.core.compiler.problem.deprecationWhenOverridingDeprecatedMethod=disabled\n");
		b.append(
				"org.eclipse.jdt.core.compiler.problem.discouragedReference=warning\n");
		b.append(
				"org.eclipse.jdt.core.compiler.problem.emptyStatement=warning\n");
		b.append(
				"org.eclipse.jdt.core.compiler.problem.enumIdentifier=error\n");
		b.append(
				"org.eclipse.jdt.core.compiler.problem.explicitlyClosedAutoCloseable=warning\n");
		b.append(
				"org.eclipse.jdt.core.compiler.problem.fallthroughCase=warning\n");
		b.append(
				"org.eclipse.jdt.core.compiler.problem.fatalOptionalError=disabled\n");
		b.append("org.eclipse.jdt.core.compiler.problem.fieldHiding=warning\n");
		b.append(
				"org.eclipse.jdt.core.compiler.problem.finalParameterBound=warning\n");
		b.append(
				"org.eclipse.jdt.core.compiler.problem.finallyBlockNotCompletingNormally=warning\n");
		b.append(
				"org.eclipse.jdt.core.compiler.problem.forbiddenReference=error\n");
		b.append(
				"org.eclipse.jdt.core.compiler.problem.hiddenCatchBlock=warning\n");
		b.append(
				"org.eclipse.jdt.core.compiler.problem.includeNullInfoFromAsserts=disabled\n");
		b.append(
				"org.eclipse.jdt.core.compiler.problem.incompatibleNonInheritedInterfaceMethod=warning\n");
		b.append(
				"org.eclipse.jdt.core.compiler.problem.incompleteEnumSwitch=warning\n");
		b.append(
				"org.eclipse.jdt.core.compiler.problem.indirectStaticAccess=warning\n");
		b.append(
				"org.eclipse.jdt.core.compiler.problem.localVariableHiding=ignore\n");
		b.append(
				"org.eclipse.jdt.core.compiler.problem.methodWithConstructorName=warning\n");
		b.append(
				"org.eclipse.jdt.core.compiler.problem.missingDefaultCase=warning\n");
		b.append(
				"org.eclipse.jdt.core.compiler.problem.missingDeprecatedAnnotation=warning\n");
		b.append(
				"org.eclipse.jdt.core.compiler.problem.missingHashCodeMethod=warning\n");
		b.append(
				"org.eclipse.jdt.core.compiler.problem.missingOverrideAnnotation=warning\n");
		b.append(
				"org.eclipse.jdt.core.compiler.problem.missingOverrideAnnotationForInterfaceMethodImplementation=enabled\n");
		b.append(
				"org.eclipse.jdt.core.compiler.problem.missingSerialVersion=ignore\n");
		b.append(
				"org.eclipse.jdt.core.compiler.problem.missingSynchronizedOnInheritedMethod=warning\n");
		b.append(
				"org.eclipse.jdt.core.compiler.problem.noEffectAssignment=warning\n");
		b.append(
				"org.eclipse.jdt.core.compiler.problem.noImplicitStringConversion=warning\n");
		b.append(
				"org.eclipse.jdt.core.compiler.problem.nonExternalizedStringLiteral=ignore\n");
		b.append(
				"org.eclipse.jdt.core.compiler.problem.nullReference=warning\n");
		b.append(
				"org.eclipse.jdt.core.compiler.problem.overridingPackageDefaultMethod=warning\n");
		b.append(
				"org.eclipse.jdt.core.compiler.problem.parameterAssignment=warning\n");
		b.append(
				"org.eclipse.jdt.core.compiler.problem.possibleAccidentalBooleanAssignment=warning\n");
		b.append(
				"org.eclipse.jdt.core.compiler.problem.potentiallyUnclosedCloseable=ignore\n");
		b.append(
				"org.eclipse.jdt.core.compiler.problem.potentialNullReference=warning\n");
		b.append(
				"org.eclipse.jdt.core.compiler.problem.rawTypeReference=warning\n");
		b.append(
				"org.eclipse.jdt.core.compiler.problem.redundantNullCheck=warning\n");
		b.append(
				"org.eclipse.jdt.core.compiler.problem.redundantSpecificationOfTypeArguments=warning\n");
		b.append(
				"org.eclipse.jdt.core.compiler.problem.redundantSuperinterface=ignore\n");
		b.append(
				"org.eclipse.jdt.core.compiler.problem.reportMethodCanBePotentiallyStatic=ignore\n");
		b.append(
				"org.eclipse.jdt.core.compiler.problem.reportMethodCanBeStatic=warning\n");
		b.append(
				"org.eclipse.jdt.core.compiler.problem.specialParameterHidingField=disabled\n");
		b.append(
				"org.eclipse.jdt.core.compiler.problem.staticAccessReceiver=warning\n");
		b.append(
				"org.eclipse.jdt.core.compiler.problem.suppressOptionalErrors=disabled\n");
		b.append(
				"org.eclipse.jdt.core.compiler.problem.suppressWarnings=enabled\n");
		b.append(
				"org.eclipse.jdt.core.compiler.problem.syntheticAccessEmulation=ignore\n");
		b.append(
				"org.eclipse.jdt.core.compiler.problem.typeParameterHiding=warning\n");
		b.append(
				"org.eclipse.jdt.core.compiler.problem.unavoidableGenericTypeProblems=enabled\n");
		b.append(
				"org.eclipse.jdt.core.compiler.problem.uncheckedTypeOperation=warning\n");
		b.append(
				"org.eclipse.jdt.core.compiler.problem.unclosedCloseable=warning\n");
		b.append(
				"org.eclipse.jdt.core.compiler.problem.undocumentedEmptyBlock=warning\n");
		b.append(
				"org.eclipse.jdt.core.compiler.problem.unhandledWarningToken=ignore\n");
		b.append(
				"org.eclipse.jdt.core.compiler.problem.unnecessaryElse=ignore\n");
		b.append(
				"org.eclipse.jdt.core.compiler.problem.unnecessaryTypeCheck=warning\n");
		b.append(
				"org.eclipse.jdt.core.compiler.problem.unqualifiedFieldAccess=ignore\n");
		b.append(
				"org.eclipse.jdt.core.compiler.problem.unusedDeclaredThrownException=warning\n");
		b.append(
				"org.eclipse.jdt.core.compiler.problem.unusedDeclaredThrownExceptionExemptExceptionAndThrowable=enabled\n");
		b.append(
				"org.eclipse.jdt.core.compiler.problem.unusedDeclaredThrownExceptionIncludeDocCommentReference=enabled\n");
		b.append(
				"org.eclipse.jdt.core.compiler.problem.unusedDeclaredThrownExceptionWhenOverriding=disabled\n");
		b.append(
				"org.eclipse.jdt.core.compiler.problem.unusedImport=warning\n");
		b.append("org.eclipse.jdt.core.compiler.problem.unusedLabel=warning\n");
		b.append("org.eclipse.jdt.core.compiler.problem.unusedLocal=warning\n");
		b.append(
				"org.eclipse.jdt.core.compiler.problem.unusedObjectAllocation=warning\n");
		b.append(
				"org.eclipse.jdt.core.compiler.problem.unusedParameter=warning\n");
		b.append(
				"org.eclipse.jdt.core.compiler.problem.unusedParameterIncludeDocCommentReference=enabled\n");
		b.append(
				"org.eclipse.jdt.core.compiler.problem.unusedParameterWhenImplementingAbstract=disabled\n");
		b.append(
				"org.eclipse.jdt.core.compiler.problem.unusedParameterWhenOverridingConcrete=disabled\n");
		b.append(
				"org.eclipse.jdt.core.compiler.problem.unusedPrivateMember=warning\n");
		b.append(
				"org.eclipse.jdt.core.compiler.problem.unusedTypeParameter=warning\n");
		b.append(
				"org.eclipse.jdt.core.compiler.problem.unusedWarningToken=warning\n");
		b.append(
				"org.eclipse.jdt.core.compiler.problem.varargsArgumentNeedCast=warning\n");
		b.append("org.eclipse.jdt.core.compiler.source=1.6\n");
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
		b.append(
				"org.eclipse.jdt.core.formatter.alignment_for_enum_constants=0\n");
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
		b.append("org.eclipse.jdt.core.formatter.lineSplit=80\n");
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
		b.append("org.eclipse.jdt.core.formatter.tabulation.char=tab\n");
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

		assertEquals(b.toString(), prefs.asFileContent());
	}

	@Test
	public void overriddenCodeStylesAffectFileContent() {
		CodeStylePolicySpex policy = CodeStylePolicy.defaultsExcept();
		policy.ignore(CodeStyle.DEAD_CODE);
		policy.warn(CodeStyle.NON_EXTERNALIZED_STRING_LITERAL);
		CodeFormatterPolicy formatter = new CodeFormatterPolicy();

		OrgEclipseJdtCorePrefs prefs = new OrgEclipseJdtCorePrefs(policy.end(),
				formatter, JavaCompliance.JAVA_1_6, false);

		String fileContent = prefs.asFileContent();

		String deadCodeFragment = "org.eclipse.jdt.core.compiler.problem.deadCode=ignore\n";
		String nonExtFragment = "org.eclipse.jdt.core.compiler.problem.nonExternalizedStringLiteral=warning\n";
		if (!fileContent.contains(deadCodeFragment)
				|| !fileContent.contains(nonExtFragment)) {
			assertEquals("Something that contains:\n" + deadCodeFragment
					+ "\n and\n" + nonExtFragment, fileContent);
		}
	}

	@Test
	public void overriddenCodeFormatterAffectsFileContent() {
		CodeStylePolicySpex policy = CodeStylePolicy.defaultsExcept();
		CodeFormatterPolicy formatter = new CodeFormatterPolicy();
		formatter.alignmentForEnumConstants = 48;
		formatter.tabulationChar = TabulationCharValue.SPACE;
		formatter.lineSplit = 120;

		OrgEclipseJdtCorePrefs prefs = new OrgEclipseJdtCorePrefs(policy.end(),
				formatter, JavaCompliance.JAVA_1_6, false);

		String fileContent = prefs.asFileContent();

		String alignmentKey = "org.eclipse.jdt.core.formatter.alignment_for_enum_constants";
		String tabKey = "org.eclipse.jdt.core.formatter.tabulation.char";
		String lineSplitKey = "org.eclipse.jdt.core.formatter.lineSplit";

		assertFalse(fileContent.contains(alignmentKey + "=0\n"));
		assertFalse(fileContent.contains(tabKey + "=tab\n"));
		assertFalse(fileContent.contains(lineSplitKey + "=80\n"));

		assertTrue(fileContent.contains(alignmentKey + "=48\n"));
		assertTrue(fileContent.contains(tabKey + "=space\n"));
		assertTrue(fileContent.contains(lineSplitKey + "=120\n"));
	}

	@Test
	public void javaCompliance17() {
		CodeStylePolicySpex policy = CodeStylePolicy.defaultsExcept();
		CodeFormatterPolicy formatter = new CodeFormatterPolicy();

		OrgEclipseJdtCorePrefs prefs = new OrgEclipseJdtCorePrefs(policy.end(),
				formatter, JavaCompliance.JAVA_1_7, false);

		String content = prefs.asFileContent();

		assertFalse(content.contains(
				"org.eclipse.jdt.core.compiler.codegen.targetPlatform=1.6\n"));
		assertFalse(content
				.contains("org.eclipse.jdt.core.compiler.compliance=1.6\n"));
		assertFalse(
				content.contains("org.eclipse.jdt.core.compiler.source=1.6\n"));

		assertTrue(content.contains(
				"org.eclipse.jdt.core.compiler.codegen.targetPlatform=1.7\n"));
		assertTrue(content
				.contains("org.eclipse.jdt.core.compiler.compliance=1.7\n"));
		assertTrue(
				content.contains("org.eclipse.jdt.core.compiler.source=1.7\n"));
	}

	@Test
	public void kotlinSupport() {
		CodeStylePolicySpex policy = CodeStylePolicy.defaultsExcept();
		CodeFormatterPolicy formatter = new CodeFormatterPolicy();

		OrgEclipseJdtCorePrefs withKotlin = new OrgEclipseJdtCorePrefs(
				policy.end(), formatter, JavaCompliance.JAVA_11, true);
		OrgEclipseJdtCorePrefs noKotlin = new OrgEclipseJdtCorePrefs(
				policy.end(), formatter, JavaCompliance.JAVA_11, false);

		String exclusionFilterLine = "org.eclipse.jdt.core.builder.resourceCopyExclusionFilter=*.kt\n";

		assertTrue(withKotlin.asFileContent().contains(exclusionFilterLine));
		assertFalse(noKotlin.asFileContent().contains(exclusionFilterLine));
	}

}
