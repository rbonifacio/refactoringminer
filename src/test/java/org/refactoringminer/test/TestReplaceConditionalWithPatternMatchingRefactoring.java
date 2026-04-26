package org.refactoringminer.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.nio.file.Files;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringType;

import gr.uom.java.xmi.UMLModel;
import gr.uom.java.xmi.UMLModelASTReader;
import gr.uom.java.xmi.diff.UMLModelDiff;

public class TestReplaceConditionalWithPatternMatchingRefactoring {

    private static final String BASE = System.getProperty("user.dir")
            + "/src/test/resources/python/replace-conditional-with-pattern-matching/";

    private List<Refactoring> detect(String scenarioDir, String relFilePath) throws Exception {
        File beforeFile = new File(BASE + scenarioDir + "/before/" + relFilePath);
        File afterFile  = new File(BASE + scenarioDir + "/after/"  + relFilePath);
        String beforeContent = new String(Files.readAllBytes(beforeFile.toPath()));
        String afterContent  = new String(Files.readAllBytes(afterFile.toPath()));

        Map<String, String> filesBefore = new LinkedHashMap<>();
        Map<String, String> filesAfter  = new LinkedHashMap<>();
        filesBefore.put(relFilePath, beforeContent);
        filesAfter.put(relFilePath, afterContent);

        UMLModel modelBefore = new UMLModelASTReader(filesBefore, Collections.emptySet(), false).getUmlModel();
        UMLModel modelAfter  = new UMLModelASTReader(filesAfter,  Collections.emptySet(), false).getUmlModel();

        UMLModelDiff diff = modelBefore.diff(modelAfter);
        return diff.getRefactorings();
    }

    /**
     * Scenario 1: if/elif/else dispatching on string -> match/case
     * Expected: exactly 1 REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING
     */
    @Test
    public void testSimpleIfElifElseReplacedByMatch() throws Exception {
        List<Refactoring> refactorings = detect("scenario1", "shape.py");

        List<Refactoring> matches = refactorings.stream()
                .filter(r -> r.getRefactoringType() == RefactoringType.REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING)
                .collect(Collectors.toList());

        assertEquals(1, matches.size(), "Expected exactly 1 REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING");
    }

    /**
     * Scenario 2: four elif branches dispatching on integer status code -> match/case
     * Expected: exactly 1 REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING
     */
    @Test
    public void testMultipleElifBranchesReplacedByMatch() throws Exception {
        List<Refactoring> refactorings = detect("scenario2", "http.py");

        List<Refactoring> matches = refactorings.stream()
                .filter(r -> r.getRefactoringType() == RefactoringType.REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING)
                .collect(Collectors.toList());

        assertEquals(1, matches.size(), "Expected exactly 1 REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING");
    }

    /**
     * Scenario 3: if/elif -> match/case with guard clauses (case x if x < 0:)
     * Expected: exactly 1 REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING (guards do not cause extra detections)
     */
    @Test
    public void testMatchWithGuardClauseNoFalsePositives() throws Exception {
        List<Refactoring> refactorings = detect("scenario3", "guard.py");

        List<Refactoring> matches = refactorings.stream()
                .filter(r -> r.getRefactoringType() == RefactoringType.REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING)
                .collect(Collectors.toList());

        assertEquals(1, matches.size(), "Expected exactly 1 REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING (guard clauses must not produce extra detections)");
    }

    /**
     * Scenario 4: if/elif/else where one branch contains a nested if -> match/case
     * The nested if inside a branch body must not prevent detection of the outer chain,
     * and the elif branches must be included in the refactoring (not left unmatched).
     * Expected: exactly 1 REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING
     */
    @Test
    public void testIfElifWithNestedIfReplacedByMatch() throws Exception {
        List<Refactoring> refactorings = detect("scenario4", "nested.py");

        List<Refactoring> matches = refactorings.stream()
                .filter(r -> r.getRefactoringType() == RefactoringType.REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING)
                .collect(Collectors.toList());

        assertEquals(1, matches.size(), "Expected exactly 1 REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING when if/elif contains a nested if");
    }

    /**
     * Scenario 5 (ansible-inspired): if/elif chain where nested ifs exist inside the elif
     * branches (not just the first branch). The outer if/elif dispatching on 'method' must be
     * detected; the nested ifs on 'sftp_action' inside each branch must not steal the match.
     * Expected: exactly 1 REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING
     */
    @Test
    public void testNestedIfsInsideElifBranchesDoNotBlockDetection() throws Exception {
        List<Refactoring> refactorings = detect("scenario5", "transport.py");

        List<Refactoring> matches = refactorings.stream()
                .filter(r -> r.getRefactoringType() == RefactoringType.REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING)
                .collect(Collectors.toList());

        assertEquals(1, matches.size(), "Expected exactly 1 REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING: nested ifs inside elif branches must not block outer detection");
    }

    /**
     * Scenario 6 (browser-use-inspired): if/elif chain on string 'provider' with nested ifs inside
     * each branch body → match/case with four string cases and a wildcard default.
     * Expected: exactly 1 REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING
     */
    @Test
    public void testIfElifOnStringProviderReplacedByMatch() throws Exception {
        List<Refactoring> refactorings = detect("scenario6", "provider.py");

        List<Refactoring> matches = refactorings.stream()
                .filter(r -> r.getRefactoringType() == RefactoringType.REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING)
                .collect(Collectors.toList());

        assertEquals(1, matches.size(), "Expected exactly 1 REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING for if/elif on provider string");
    }

    /**
     * Scenario 7 (mitmproxy-inspired): if/elif chain using 'in (...)' membership tests on
     * spec.scheme → match/case using '|' OR patterns.
     * Expected: exactly 1 REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING
     */
    @Test
    public void testIfElifWithSetMembershipReplacedByMatchWithOrPattern() throws Exception {
        List<Refactoring> refactorings = detect("scenario7", "scheme.py");

        List<Refactoring> matches = refactorings.stream()
                .filter(r -> r.getRefactoringType() == RefactoringType.REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING)
                .collect(Collectors.toList());

        assertEquals(1, matches.size(), "Expected exactly 1 REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING for if/elif with set-membership replaced by match '|' patterns");
    }

    /**
     * Scenario 8 (matplotlib-inspired): single if without elif (compound 'in' condition) followed
     * by a fallthrough return → match/case splitting each value into its own case.
     * Expected: exactly 1 REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING
     */
    @Test
    public void testSingleIfWithoutElifReplacedByMatch() throws Exception {
        List<Refactoring> refactorings = detect("scenario8", "encoding.py");

        List<Refactoring> matches = refactorings.stream()
                .filter(r -> r.getRefactoringType() == RefactoringType.REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING)
                .collect(Collectors.toList());

        assertEquals(1, matches.size(), "Expected exactly 1 REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING for single if without elif replaced by match");
    }

    /**
     * Scenario 9 (locust-inspired): if/elif chain on string 'key' with five named branches and
     * a final guard branch 'elif key in parsed_options_dict' → match/case with wildcard guard
     * 'case _ if key in parsed_options_dict'.
     * Expected: exactly 1 REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING
     */
    @Test
    public void testIfElifWithWildcardGuardReplacedByMatch() throws Exception {
        List<Refactoring> refactorings = detect("scenario9", "swarm.py");

        List<Refactoring> matches = refactorings.stream()
                .filter(r -> r.getRefactoringType() == RefactoringType.REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING)
                .collect(Collectors.toList());

        assertEquals(1, matches.size(), "Expected exactly 1 REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING for if/elif with final guard branch replaced by match");
    }

    /**
     * Scenario 10 (airbyte-inspired): if/elif chain on attribute access 'message.type' with two
     * branches → match/case on the same attribute subject.
     * Expected: exactly 1 REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING
     */
    @Test
    public void testIfElifOnAttributeSubjectReplacedByMatch() throws Exception {
        List<Refactoring> refactorings = detect("scenario10", "message.py");

        List<Refactoring> matches = refactorings.stream()
                .filter(r -> r.getRefactoringType() == RefactoringType.REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING)
                .collect(Collectors.toList());

        assertEquals(1, matches.size(), "Expected exactly 1 REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING for if/elif on attribute subject replaced by match");
    }

    /**
     * No refactoring when before and after are identical (scenario1/before used for both).
     * Expected: 0 REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING
     */
    @Test
    public void testNoRefactoringWhenConditionalUnchanged() throws Exception {
        File beforeFile = new File(BASE + "scenario1/before/shape.py");
        String content = new String(Files.readAllBytes(beforeFile.toPath()));

        Map<String, String> files = new LinkedHashMap<>();
        files.put("shape.py", content);

        UMLModel model = new UMLModelASTReader(files, Collections.emptySet(), false).getUmlModel();
        UMLModelDiff diff = model.diff(model);
        List<Refactoring> refactorings = diff.getRefactorings();

        long count = refactorings.stream()
                .filter(r -> r.getRefactoringType() == RefactoringType.REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING)
                .count();

        assertEquals(0, count, "Expected no REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING when files are identical");
    }

    /**
     * Scenario 11 (lm-evaluation-harness-inspired): if/elif chain using isinstance() type checks
     * → match/case with class patterns (case list():) and callable guard (case _ if callable():).
     * The condition uses isinstance(subject, Type) — class patterns are not yet supported.
     * Expected: 1 REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING
     */
    @Test
    public void testIsinstanceElifWithDefaultReplacedByClassPatterns() throws Exception {
        List<Refactoring> refactorings = detect("scenario11", "samples.py");

        List<Refactoring> matches = refactorings.stream()
                .filter(r -> r.getRefactoringType() == RefactoringType.REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING)
                .collect(Collectors.toList());

        assertEquals(1, matches.size(), "Expected exactly 1 REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING for isinstance/elif/default → class patterns");
    }

    /**
     * Scenario 12 (langflow-inspired): if/elif chain of isinstance() checks with no default
     * → match/case with class patterns (case HumanMessage():, etc.).
     * Expected: 1 REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING
     */
    @Test
    public void testIsinstanceElifNoDefaultReplacedByClassPatterns() throws Exception {
        List<Refactoring> refactorings = detect("scenario12", "messages.py");

        List<Refactoring> matches = refactorings.stream()
                .filter(r -> r.getRefactoringType() == RefactoringType.REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING)
                .collect(Collectors.toList());

        assertEquals(1, matches.size(), "Expected exactly 1 REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING for isinstance/elif/no-default → class patterns");
    }

    /**
     * Scenario 13 (lm-evaluation-harness-inspired): single if using 'in' dict-key membership test
     * (e.g. 'if "key" in config') → match/case with mapping patterns (case {"key": val}:).
     * The match subject is the dict itself; this uses structural mapping patterns not yet supported.
     * Expected: 1 REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING
     */
    @Test
    public void testInDictKeyMembershipReplacedByMappingPatterns() throws Exception {
        List<Refactoring> refactorings = detect("scenario13", "config.py");

        List<Refactoring> matches = refactorings.stream()
                .filter(r -> r.getRefactoringType() == RefactoringType.REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING)
                .collect(Collectors.toList());

        assertEquals(1, matches.size(), "Expected exactly 1 REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING for in-dict-key → mapping patterns");
    }

    /**
     * Scenario 14 (ansible-inspired): if/elif chain using 'in (tuple)' membership per branch
     * → match/case with OR patterns (case 'bool' | 'boolean':). No default/else.
     * Expected: 1 REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING
     */
    @Test
    public void testInTupleElifNoDefaultReplacedByOrPatterns() throws Exception {
        List<Refactoring> refactorings = detect("scenario14", "converter.py");

        List<Refactoring> matches = refactorings.stream()
                .filter(r -> r.getRefactoringType() == RefactoringType.REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING)
                .collect(Collectors.toList());

        assertEquals(1, matches.size(), "Expected exactly 1 REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING for in-tuple/elif/no-default → OR patterns");
    }

    /**
     * Scenario 15 (parlant-inspired): single if using 'in {set}' membership test
     * → match/case collapsing to a single OR-pattern case plus a wildcard default.
     * Expected: 1 REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING
     */
    @Test
    public void testInSetSingleIfReplacedByOrPatternCase() throws Exception {
        List<Refactoring> refactorings = detect("scenario15", "mapper.py");

        List<Refactoring> matches = refactorings.stream()
                .filter(r -> r.getRefactoringType() == RefactoringType.REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING)
                .collect(Collectors.toList());

        assertEquals(1, matches.size(), "Expected exactly 1 REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING for in-set/single-if → OR-pattern case");
    }

    /**
     * Scenario 16 (sentry-inspired): if/else where the if condition tests an unrelated predicate
     * ('if self.use_new_engine') while the match subject is a completely different variable ('key').
     * The match subject does not appear in the if condition — indirect refactoring not yet supported.
     * Expected: 1 REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING
     */
    @Disabled("indirect: match subject 'key' is absent from the if-condition 'self.use_new_engine'; cross-variable dispatch analysis not yet supported")
    @Test
    public void testIndirectSubjectSimpleElifWithDefaultNotDetected() throws Exception {
        List<Refactoring> refactorings = detect("scenario16", "notifier.py");

        List<Refactoring> matches = refactorings.stream()
                .filter(r -> r.getRefactoringType() == RefactoringType.REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING)
                .collect(Collectors.toList());

        assertEquals(1, matches.size(), "Expected exactly 1 REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING for indirect/simple/elif/default");
    }

    /**
     * Scenario 17 (sentry/certbot-inspired): if/elif dispatching on an attribute (artifact.artifact_type)
     * is replaced by match on a DIFFERENT attribute (artifact.state). The match subject does not appear
     * in the if condition — indirect refactoring where the dispatching variable itself changed.
     * Expected: 1 REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING
     */
    @Disabled("indirect: match subject 'artifact.state' is absent from the if-condition on 'artifact.artifact_type'; dispatching variable changed between before and after")
    @Test
    public void testIndirectAttrSubjectChangedNotDetected() throws Exception {
        List<Refactoring> refactorings = detect("scenario17", "audit.py");

        List<Refactoring> matches = refactorings.stream()
                .filter(r -> r.getRefactoringType() == RefactoringType.REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING)
                .collect(Collectors.toList());

        assertEquals(1, matches.size(), "Expected exactly 1 REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING for indirect/attr/subject-changed");
    }

    /**
     * Scenario 18 (airbyte-inspired): if/elif chain on attribute access 'message.type' WITH a
     * catch-all else branch → match/case with wildcard default.
     * Expected: 1 REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING
     */
    @Test
    public void testIfElifOnAttrSubjectWithDefaultReplacedByMatch() throws Exception {
        List<Refactoring> refactorings = detect("scenario18", "handler.py");

        List<Refactoring> matches = refactorings.stream()
                .filter(r -> r.getRefactoringType() == RefactoringType.REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING)
                .collect(Collectors.toList());

        assertEquals(1, matches.size(), "Expected exactly 1 REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING for if/elif on attr subject with default");
    }

    /**
     * Scenario 19 (ansible-inspired): single if (no elif, no else) → match/case where the after
     * adds an additional new case beyond the original branch. The original if branch is refactored
     * and a new case is introduced simultaneously.
     * Expected: 1 REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING
     */
    @Test
    public void testSingleIfExpandedToMultiCaseMatch() throws Exception {
        List<Refactoring> refactorings = detect("scenario19", "compat.py");

        List<Refactoring> matches = refactorings.stream()
                .filter(r -> r.getRefactoringType() == RefactoringType.REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING)
                .collect(Collectors.toList());

        assertEquals(1, matches.size(), "Expected exactly 1 REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING for single-if expanded to multi-case match");
    }

    /**
     * Scenario 20 (mitmproxy-inspired): if/else dispatching on subscript 'm["part"]'
     * → match/case on a DIFFERENT subscript 'm["type"]'. The match subject does not appear in
     * the original if condition — indirect refactoring where the subscript key itself changed.
     * Expected: 1 REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING
     */
    @Disabled("indirect: match subject m[\"type\"] is absent from if-condition on m[\"part\"]; subscript key changed between before and after")
    @Test
    public void testSubscriptSubjectChangedNotDetected() throws Exception {
        List<Refactoring> refactorings = detect("scenario20", "filter.py");

        List<Refactoring> matches = refactorings.stream()
                .filter(r -> r.getRefactoringType() == RefactoringType.REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING)
                .collect(Collectors.toList());

        assertEquals(1, matches.size(), "Expected exactly 1 REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING for subscript/subject-changed");
    }

    /**
     * Scenario 21 (sentry-inspired): single if (no elif, no else) on attribute access subject
     * 'artifact.artifact_type' → match/case on the same attribute, adding a new case.
     * Direct refactoring; attribute subject appears in the if condition.
     * Expected: 1 REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING
     */
    @Test
    public void testEqAttrSingleIfExpandedToMatch() throws Exception {
        List<Refactoring> refactorings = detect("scenario21", "artifact.py");
        List<Refactoring> matches = refactorings.stream()
                .filter(r -> r.getRefactoringType() == RefactoringType.REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING)
                .collect(Collectors.toList());
        assertEquals(1, matches.size(), "Expected exactly 1 REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING for eq/attr/single-if");
    }

    /**
     * Scenario 22 (ansible-inspired): single isinstance() check on a simple subject ('dirty')
     * with no elif and no default → match/case with a single class pattern (case MutableSequence():).
     * Expected: 1 REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING
     */
    @Test
    public void testIsinstanceSimpleSingleIfReplacedByClassPattern() throws Exception {
        List<Refactoring> refactorings = detect("scenario22", "cleaner.py");
        List<Refactoring> matches = refactorings.stream()
                .filter(r -> r.getRefactoringType() == RefactoringType.REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING)
                .collect(Collectors.toList());
        assertEquals(1, matches.size(), "Expected exactly 1 REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING for isinstance/simple/single-if");
    }

    /**
     * Scenario 23 (mindsdb-inspired): if/elif chain of isinstance() checks on an attribute
     * subject ('item.value') with no default → match/case with class patterns on the same attr.
     * Expected: 1 REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING
     */
    @Test
    public void testIsinstanceAttrElifNoDefaultReplacedByClassPatterns() throws Exception {
        List<Refactoring> refactorings = detect("scenario23", "parser.py");
        List<Refactoring> matches = refactorings.stream()
                .filter(r -> r.getRefactoringType() == RefactoringType.REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING)
                .collect(Collectors.toList());
        assertEquals(1, matches.size(), "Expected exactly 1 REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING for isinstance/attr/elif/no-default");
    }

    /**
     * Scenario 24 (certbot-inspired): single if checking 'problem.identifier is not None'
     * → match/case on a DIFFERENT attribute 'problem.identifier.typ'. The match subject does
     * not appear in the if condition — indirect/attr/no-default.
     * Expected: 1 REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING
     */
    @Disabled("indirect: match subject 'problem.identifier.typ' is absent from the if-condition 'problem.identifier is not None'")
    @Test
    public void testIndirectAttrSingleIfNoDefaultNotDetected() throws Exception {
        List<Refactoring> refactorings = detect("scenario24", "validator.py");
        List<Refactoring> matches = refactorings.stream()
                .filter(r -> r.getRefactoringType() == RefactoringType.REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING)
                .collect(Collectors.toList());
        assertEquals(1, matches.size(), "Expected exactly 1 REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING for indirect/attr/single-if/no-default");
    }

    /**
     * Scenario 25 (ansible-inspired): single if using a compound condition
     * ('self.apply_transforms and value_type in self.type_mapping') → match/case on a completely
     * different attribute ('self.encrypted_string_behavior'). Subject absent from condition.
     * Expected: 1 REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING
     */
    @Disabled("indirect: match subject 'self.encrypted_string_behavior' is absent from compound if-condition on 'self.apply_transforms and value_type in ...'")
    @Test
    public void testInAttrCompoundConditionSubjectChangedNotDetected() throws Exception {
        List<Refactoring> refactorings = detect("scenario25", "transformer.py");
        List<Refactoring> matches = refactorings.stream()
                .filter(r -> r.getRefactoringType() == RefactoringType.REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING)
                .collect(Collectors.toList());
        assertEquals(1, matches.size(), "Expected exactly 1 REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING for in/attr/compound-condition/subject-changed");
    }

    /**
     * Scenario 26 (sentry-inspired): multi-line if condition testing the same attribute
     * ('preprod_artifact.state == UPLOADING or ... == UPLOADED') with an else branch
     * → match/case with OR pattern on the same attribute subject plus a wildcard default.
     * Expected: 1 REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING
     */
    @Test
    public void testMultiLineIfConditionOnAttrWithDefaultReplacedByMatch() throws Exception {
        List<Refactoring> refactorings = detect("scenario26", "deployer.py");
        List<Refactoring> matches = refactorings.stream()
                .filter(r -> r.getRefactoringType() == RefactoringType.REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING)
                .collect(Collectors.toList());
        assertEquals(1, matches.size(), "Expected exactly 1 REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING for multi-line if/else on attr subject with default");
    }
}
